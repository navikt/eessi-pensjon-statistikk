package no.nav.eessi.pensjon.statistikk.listener

import no.nav.eessi.pensjon.json.mapJsonToAny
import no.nav.eessi.pensjon.json.toJson
import no.nav.eessi.pensjon.json.typeRefs
import no.nav.eessi.pensjon.statistikk.models.HendelseType
import no.nav.eessi.pensjon.statistikk.models.OpprettelseType
import no.nav.eessi.pensjon.statistikk.services.HendelsesAggregeringsService
import no.nav.eessi.pensjon.statistikk.services.StatistikkPublisher
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.CountDownLatch

@Component("statistikkListener")
class StatistikkListener(
    private val sedInfoService: HendelsesAggregeringsService,
    private val statistikkPublisher: StatistikkPublisher
) {

    private val logger = LoggerFactory.getLogger(StatistikkListener::class.java)

    private val latch = CountDownLatch(1)

    fun getLatch() = latch

    @KafkaListener(
        id = "statistikkListener",
        idIsGroup = false,
        topics = ["\${kafka.statistikk-inn.topic}"],
        groupId = "\${kafka.statistikk-inn.groupid}",
        autoStartup = "false"
    )
    fun consumeOpprettelseMelding(
        hendelse: String,
        cr: ConsumerRecord<String, String>,
        acknowledgment: Acknowledgment
    ) {
        MDC.putCloseable("x_request_id", UUID.randomUUID().toString()).use {
            logger.info("Innkommet statistikk hendelse i partisjon: ${cr.partition()}, med offset: ${cr.offset()}")

            try {
                logger.debug("Hendelse : ${hendelse.toJson()}")
                val melding = mapJsonToAny(hendelse, typeRefs<OpprettelseMelding>())

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
                acknowledgment.acknowledge()
                logger.info("Acket statistikk melding med offset: ${cr.offset()} i partisjon ${cr.partition()}")
            } catch (ex: Exception) {
                logger.error("Noe gikk galt under behandling av statistikk-hendelse:\n $hendelse \n", ex)
                throw RuntimeException(ex.message)
            }
            latch.countDown()
        }

        @KafkaListener(
            id = "sedMottattListener",
            idIsGroup = false,
            topics = ["\${kafka.statistikk-sed-mottatt.topic}"],
            groupId = "\${kafka.statistikk-sed-mottatt.groupid}",
            autoStartup = "false"
        )
        fun consumeSedMottatt(hendelse: String, cr: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
            MDC.putCloseable("x_request_id", UUID.randomUUID().toString()).use {}

            try {
                val sedHendelseRina = mapJsonToAny(hendelse, typeRefs<SedHendelseRina>())
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
            } catch (ex: Exception) {
                logger.error("Noe gikk galt under behandling av statistikk-sed-hendelse:\n $hendelse \n", ex)
                throw RuntimeException(ex.message)
            }
            latch.countDown()
        }

        @KafkaListener(
            id = "sedSendtListener",
            idIsGroup = false,
            topics = ["\${kafka.statistikk-sed-sendt.topic}"],
            groupId = "\${kafka.statistikk-sed-sendt.groupid}",
            autoStartup = "false"
        )
        fun consumeSedSendt(hendelse: String, cr: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
            MDC.putCloseable("x_request_id", UUID.randomUUID().toString()).use {
            }

            try {
                val sedHendelseRina = mapJsonToAny(hendelse, typeRefs<SedHendelseRina>())
                if (GyldigeHendelser.sendt(sedHendelseRina)) {
                    logger.debug(sedHendelseRina.toJson())
                    val vedtaksId =
                        sedInfoService.hentVedtaksId(sedHendelseRina.rinaSakId, sedHendelseRina.rinaDokumentId)
                    val sedMeldingUt = sedInfoService.populerSedMeldingUt(
                        sedHendelseRina.rinaSakId,
                        sedHendelseRina.rinaDokumentId,
                        vedtaksId,
                        HendelseType.SED_SENDT,
                        sedHendelseRina.avsenderLand
                    )
                    statistikkPublisher.publiserSedHendelse(sedMeldingUt)
                }
            } catch (ex: Exception) {
                logger.error("Noe gikk galt under behandling av statistikk-sed-hendelse:\n $hendelse \n", ex)
                throw RuntimeException(ex.message)
            }
            latch.countDown()
        }
    }
}
