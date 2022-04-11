package no.nav.eessi.pensjon.statistikk.listener

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import no.nav.eessi.pensjon.eux.model.buc.MissingBuc
import no.nav.eessi.pensjon.json.mapJsonToAny
import no.nav.eessi.pensjon.json.toJson
import no.nav.eessi.pensjon.json.typeRefs
import no.nav.eessi.pensjon.metrics.MetricsHelper
import no.nav.eessi.pensjon.statistikk.models.HendelseType
import no.nav.eessi.pensjon.statistikk.models.OpprettelseType
import no.nav.eessi.pensjon.statistikk.services.HendelsesAggregeringsService
import no.nav.eessi.pensjon.statistikk.services.StatistikkPublisher
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.CountDownLatch
import javax.annotation.PostConstruct


@Component("statistikkListener")
class StatistikkListener(
    private val sedInfoService: HendelsesAggregeringsService,
    private val statistikkPublisher: StatistikkPublisher,
    @Autowired(required = false) private val metricsHelper: MetricsHelper = MetricsHelper(SimpleMeterRegistry())
) {

    private val logger = LoggerFactory.getLogger(StatistikkListener::class.java)

    private val latch = CountDownLatch(1)
    private val latchMottatt = CountDownLatch(1)

    private lateinit var opprettMeldingMetric: MetricsHelper.Metric
    private lateinit var sedMottattMeldingMetric: MetricsHelper.Metric
    private lateinit var sedSedSendMeldingtMetric: MetricsHelper.Metric

    fun getLatch() = latch
    fun getLatchMottatt() = latchMottatt


    @PostConstruct
    fun initMetrics() {
        opprettMeldingMetric = metricsHelper.init("consumeOpprettMelding")
        sedMottattMeldingMetric = metricsHelper.init("consumeSedMottatt")
        sedSedSendMeldingtMetric = metricsHelper.init("consumeSedSendtMelding")
    }

    @KafkaListener(
       containerFactory = "aivenKafkaListenerContainerFactory",
        topics = ["\${kafka.statistikk-inn.topic}"],
        groupId = "\${kafka.statistikk-inn.groupid}",
    )
    fun consumeOpprettelseMelding(
        hendelse: String,
        cr: ConsumerRecord<String, String>,
        acknowledgment: Acknowledgment
    ) {
        MDC.putCloseable("x_request_id", MDC.get("x_request_id") ?: UUID.randomUUID().toString()).use {
            val timestamp = timeWithFormat(cr.timestamp())
            logger.info("Innkommet statistikk hendelse i partisjon: ${cr.partition()}, med offset: ${cr.offset()}, tid:$timestamp")

            opprettMeldingMetric.measure {
                val offsetToSkip = listOf<Long>(14574, 14504, 14606, 14544, 14544, 14639, 14731)
                val offset = cr.offset()
                try {
                    if (offsetToSkip.contains(offset)) {
                        logger.warn("Hopper over offset: $offset")
                    } else {
                        logger.info("Oppretter melding av type: $hendelse")
                        val melding = meldingsMapping(hendelse)
                        when (melding.opprettelseType) {
                            OpprettelseType.BUC -> {
                                val bucHendelse = sedInfoService.aggregateBucData(melding.rinaId)
                                statistikkPublisher.publiserBucOpprettetStatistikk(bucHendelse)
                            }
                            OpprettelseType.SED -> {
                                val sedHendelse = sedInfoService.aggregateSedOpprettetData(
                                    melding.rinaId,
                                    melding.dokumentId!!,
                                    melding.vedtaksId
                                )
                                if (sedHendelse != null) {
                                    statistikkPublisher.publiserSedHendelse(sedHendelse)
                                }
                            }
                        }
                    }
                    acknowledgment.acknowledge()
                    logger.info("Acket opprettelse melding med offset: $offset i partisjon ${cr.partition()}")
                } catch (ex: Exception) {
                    logger.error("Noe gikk galt med offset:$offset, tid:$timestamp, ved behandling av statistikk-hendelse:\n $hendelse \n", ex)
                    throw RuntimeException(ex.message)
                }
                latch.countDown()
            }
        }
    }


    @KafkaListener(
        containerFactory = "onpremKafkaListenerContainerFactory",
        topics = ["\${kafka.statistikk-sed-mottatt.topic}"],
        groupId = "\${kafka.statistikk-sed-mottatt.groupid}",
    )
    fun consumeSedMottatt(hendelse: String, cr: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        MDC.putCloseable("x_request_id", MDC.get("x_request_id") ?: UUID.randomUUID().toString()).use {
            sedMottattMeldingMetric.measure {
                val sedHendelseRina = mapJsonToAny(hendelse, typeRefs<SedHendelseRina>())
                val offsetToSkip = listOf(299742L, 299743L, 299746L)
                val offset = cr.offset()
                try {
                    if (offsetToSkip.contains(offset) || MissingBuc.checkForMissingBuc(sedHendelseRina.rinaSakId)) {
                        logger.warn("Hopper over offset: $offset")
                    } else {
                        if (GyldigeHendelser.mottatt(sedHendelseRina)) {
                            val sedMeldingUt = sedInfoService.populerSedMeldingUt(
                                sedHendelseRina.rinaSakId,
                                sedHendelseRina.rinaDokumentId,
                                null,
                                HendelseType.SED_MOTTATT,
                                sedHendelseRina.avsenderLand
                            )
                            statistikkPublisher.publiserSedHendelse(sedMeldingUt)
                        }
                    }
                    acknowledgment.acknowledge()
                    logger.info("Acket sedMottatt melding med offset: $offset i partisjon ${cr.partition()}")
                } catch (ex: Exception) {
                    logger.error("Noe gikk galt med offset : $offset, under behandling av statistikk-sed-hendelse:\n $hendelse \n", ex)
                    throw RuntimeException(ex.message)
                }
                latchMottatt.countDown()
            }
        }
    }

    @KafkaListener(
        containerFactory = "onpremKafkaListenerContainerFactory",
        topics = ["\${kafka.statistikk-sed-sendt.topic}"],
        groupId = "\${kafka.statistikk-sed-sendt.groupid}",
    )
    fun consumeSedSendt(hendelse: String, cr: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        MDC.putCloseable("x_request_id", MDC.get("x_request_id") ?: UUID.randomUUID().toString()).use {
            sedSedSendMeldingtMetric.measure {
                val offset = cr.offset()
                try {
                    val sedHendelseRina = mapJsonToAny(hendelse, typeRefs<SedHendelseRina>())
                    if (MissingBuc.checkForMissingBuc(sedHendelseRina.rinaSakId)) {
                        logger.warn("Hopper over offset: $offset")
                    } else {
                        if (GyldigeHendelser.sendt(sedHendelseRina)) {
                            logger.debug(sedHendelseRina.toJson())
                            val vedtaksId = sedInfoService.hentVedtaksId(sedHendelseRina.rinaSakId, sedHendelseRina.rinaDokumentId)
                            val sedMeldingUt = sedInfoService.populerSedMeldingUt(
                                sedHendelseRina.rinaSakId,
                                sedHendelseRina.rinaDokumentId,
                                vedtaksId,
                                HendelseType.SED_SENDT,
                                sedHendelseRina.avsenderLand
                            )
                            logger.info("sedmeldingUt: $sedMeldingUt")
                            statistikkPublisher.publiserSedHendelse(sedMeldingUt)
                        }
                    }
                    acknowledgment.acknowledge()
                    logger.info("Acket sedSendt melding med offset: ${cr.offset()} i partisjon ${cr.partition()}")
                } catch (ex: Exception) {
                    logger.error("Noe gikk galt med offset : $offset, under behandling av statistikk-sed-hendelse:\n $hendelse \n", ex)
                    throw RuntimeException(ex.message)
                }
                latch.countDown()
            }
        }
    }

    fun meldingsMapping(hendelse: String): OpprettelseMelding {
        return try {
            logger.debug("Opprinnelig mapping av hendelse")
            mapJsonToAny(hendelse, typeRefs<OpprettelseMelding>())
        } catch (ex: Exception) {
            try {
            logger.debug("Trimming og mapping av hendelse")
                val json = hendelse.replace("\\n", "").replace("\\", "")
                logger.debug("Trimmet json: $json")
                mapJsonToAny(json, typeRefs<OpprettelseMelding>())
            } catch (ex2: Exception) {
                logger.error("Could not compute")
                throw RuntimeException()
            }

        }
    }

    fun timeWithFormat(time: Long): String? {
        try {
            return SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(time)
        }
        catch (ex : Exception){
            logger.warn("Noe gikk galt med formatering av tid", ex)
        }
        return ""
    }
}
