package no.nav.eessi.pensjon.statistikk.listener

import no.nav.eessi.pensjon.eux.model.SedHendelse
import no.nav.eessi.pensjon.eux.model.buc.MissingBuc
import no.nav.eessi.pensjon.metrics.MetricsHelper
import no.nav.eessi.pensjon.statistikk.models.HendelseType
import no.nav.eessi.pensjon.statistikk.models.OpprettelseType
import no.nav.eessi.pensjon.statistikk.services.HendelsesAggregeringsService
import no.nav.eessi.pensjon.statistikk.services.StatistikkPublisher
import no.nav.eessi.pensjon.utils.mapJsonToAny
import no.nav.eessi.pensjon.utils.toJson
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
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
    @Value("\${SPRING_PROFILES_ACTIVE:}") private val profile: String,
    @Autowired(required = false) private val metricsHelper: MetricsHelper = MetricsHelper.ForTest()
) {

    private val logger = LoggerFactory.getLogger(StatistikkListener::class.java)
    private val secureLogger = LoggerFactory.getLogger("secureLog")

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
       containerFactory = "kafkaListenerContainerFactory",
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
                try {
                    val melding = meldingsMapping(hendelse)
                    logger.info("Oppretter melding av type: $hendelse")
                    val hopperOverOffset = listOf(43313L, 43318L, 43461L, 43631L, 43499L, 55362L, 55031L, 55036L, 176378L, 176379L)
                    if (MissingBuc.checkForMissingBuc(melding.rinaId) || cr.offset() in hopperOverOffset) {
                        logger.warn("Hopper over offset: ${cr.offset()}")
                    } else {
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
                    logger.info("Acket opprettelse melding med offset: ${cr.offset()} i partisjon ${cr.partition()}")
                } catch (ex: Exception) {
                    logger.error("Noe gikk galt med offset:${cr.offset()}, tid:$timestamp, ved behandling av statistikk-hendelse", ex)
                    secureLogger.info("Noe gikk galt med offset:\n$hendelse")
                    throw RuntimeException(ex.message)
                }
                latch.countDown()
            }
        }
    }


    @KafkaListener(
        containerFactory = "kafkaListenerContainerFactory",
        topics = ["\${kafka.statistikk-sed-mottatt.topic}"],
        groupId = "\${kafka.statistikk-sed-mottatt.groupid}",
    )
    fun consumeSedMottatt(hendelse: String, cr: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        MDC.putCloseable("x_request_id", MDC.get("x_request_id") ?: UUID.randomUUID().toString()).use {
            sedMottattMeldingMetric.measure {
                val sedHendelseRina = mapJsonToAny<SedHendelse>(hendelse)

                if (testMeldingIProdLogError(sedHendelseRina, acknowledgment)) return@measure

                try {
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
                    acknowledgment.acknowledge()
                    logger.info("Acket sedMottatt melding med offset: ${cr.offset()} i partisjon ${cr.partition()}")
                } catch (ex: Exception) {
                    logger.error("Noe gikk galt med offset : ${cr.offset()}, under behandling av statistikk-sed-hendelse", ex)

                    throw RuntimeException(ex.message)
                }
                latchMottatt.countDown()
            }
        }
    }

    private fun testMeldingIProdLogError(
        sedHendelseRina: SedHendelse,
        acknowledgment: Acknowledgment
    ): Boolean {
        if (profile == "prod" && sedHendelseRina.avsenderId in listOf("NO:NAVAT05", "NO:NAVAT07")) {
            logger.error("Avsender id er ${sedHendelseRina.avsenderId}. Dette er testdata i produksjon!!!\n$sedHendelseRina")
            acknowledgment.acknowledge()
            return true
        }
        return false
    }

    @KafkaListener(
        containerFactory = "kafkaListenerContainerFactory",
        topics = ["\${kafka.statistikk-sed-sendt.topic}"],
        groupId = "\${kafka.statistikk-sed-sendt.groupid}",
    )
    fun consumeSedSendt(hendelse: String, cr: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        MDC.putCloseable("x_request_id", MDC.get("x_request_id") ?: UUID.randomUUID().toString()).use {
            sedSedSendMeldingtMetric.measure {
                val offset = cr.offset()
                try {
                    val sedHendelseRina = mapJsonToAny<SedHendelse>(hendelse)
                    val offsetToSkip = listOf(70196L, 70197L)

                    if (MissingBuc.checkForMissingBuc(sedHendelseRina.rinaSakId) || offset in offsetToSkip) {
                        logger.warn("Hopper over offset: $offset")
                    } else if (GyldigeHendelser.sendt(sedHendelseRina)) {
                        logger.debug(sedHendelseRina.toJson())
                        val vedtaksId = sedInfoService.hentVedtaksId(sedHendelseRina.rinaSakId, sedHendelseRina.rinaDokumentId)
                        val sedMeldingUt = sedInfoService.populerSedMeldingUt(
                            sedHendelseRina.rinaSakId,
                            sedHendelseRina.rinaDokumentId,
                            vedtaksId,
                            HendelseType.SED_SENDT,
                            sedHendelseRina.avsenderLand
                        )
                        logger.info("sedmeldingUt: ${sedMeldingUt.toStringUtenPid()}")
                        statistikkPublisher.publiserSedHendelse(sedMeldingUt)
                    } else {
                        logger.warn("SED er ikke gyldig eller en av missingBuc: $offset, fra SED: ${sedHendelseRina.sedId}")
                    }
                    acknowledgment.acknowledge()
                    logger.info("Acket sedSendt melding med offset: $offset i partisjon ${cr.partition()}")
                } catch (ex: Exception) {
                    logger.error("Noe gikk galt med offset : $offset, under behandling av statistikk-sed-hendelse", ex)
                    secureLogger.info("Noe gikk galt med offset:\n$hendelse")

                    throw RuntimeException(ex.message)
                }
                latch.countDown()
            }
        }
    }

    fun meldingsMapping(hendelse: String): OpprettelseMelding {
        return try {
            logger.debug("Opprinnelig mapping av hendelse")
            mapJsonToAny(hendelse)
        } catch (ex: Exception) {
            try {
            logger.debug("Trimming og mapping av hendelse")
                val json = hendelse.replace("\\n", "").replace("\\", "")
                logger.debug("Trimmet json: $json")
                mapJsonToAny(json)
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
