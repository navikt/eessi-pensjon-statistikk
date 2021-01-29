package no.nav.eessi.pensjon.statistikk.listener

import no.nav.eessi.pensjon.statistikk.models.SedHendelseRina
import no.nav.eessi.pensjon.statistikk.models.StatistikkMeldingInn
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

@Component
class StatistikkListener(
    private val sedInfoService: HendelsesAggregeringsService,
    private val statistikkPublisher: StatistikkPublisher) {

    private val logger = LoggerFactory.getLogger(StatistikkListener::class.java)

     private val latch = CountDownLatch(1)

    fun getLatch() = latch

    @KafkaListener(id="statistikkListener",
            idIsGroup = false,
            topics = ["\${kafka.statistikk-inn.topic}"],
            groupId = "\${kafka.statistikk-inn.groupid}",
            autoStartup = "false")
    fun consumeBuc(hendelse: String, cr: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        MDC.putCloseable("x_request_id", UUID.randomUUID().toString()).use {
            logger.info("Innkommet statistikk hendelse i partisjon: ${cr.partition()}, med offset: ${cr.offset()}")

            try {
                logger.debug("Hendelse : $hendelse")
                val melding = StatistikkMeldingInn.fromJson(hendelse)
                val bucHendelse = sedInfoService.aggregateBucData(melding)
                statistikkPublisher.publiserBucOpprettetStatistikk(bucHendelse)
                acknowledgment.acknowledge()
                logger.info("Acket statistikk melding med offset: ${cr.offset()} i partisjon ${cr.partition()}")
            } catch (ex: Exception) {
                logger.error("Noe gikk galt under behandling av statistikk-hendelse:\n $hendelse \n", ex)
                throw RuntimeException(ex.message)
            }
            latch.countDown()
        }
    }

    @KafkaListener(id = "sedSendtListener",
            idIsGroup = false,
            topics = ["\${kafka.statistikk-inn.topic}"],
            groupId = "\${kafka.statistikk-inn.groupid}",
            autoStartup = "false")
    fun consumeSedOpprettet(hendelse: String, cr: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        MDC.putCloseable("x_request_id", UUID.randomUUID().toString()).use {
            try {
                val melding = StatistikkMeldingInn.fromJson(hendelse)
                logger.info("*** Starter behandling av SED ${melding.hendelseType}  bucid: ${melding.rinaid} ***")

                sedInfoService.aggregateSedOpprettetData(melding)?.let {
                    statistikkPublisher.publiserSedHendelse(it)
                    logger.info("Acket statistikk (sed sendt) med offset: ${cr.offset()} i partisjon ${cr.partition()}")
                }

            } catch (ex: Exception) {
                logger.error("Noe gikk galt under behandling av statistikk-sed-hendelse:\n $hendelse \n", ex)
                throw RuntimeException(ex.message)
            }
            latch.countDown()
        }
    }

/*   @KafkaListener(id = "sedMottattListener",
            idIsGroup = false,
            topics = ["\${kafka.statistikk-sed-mottatt.topic}"],
            groupId = "\${kafka.statistikk-sed-mottatt.groupid}",
            autoStartup = "false")
    fun consumeSedMottatt(hendelse: String, cr: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        MDC.putCloseable("x_request_id", UUID.randomUUID().toString()).use {
            try {
                val sedHendelse = SedHendelse.fromJson(hendelse)
                logger.info("*** Starter behandling av SED ${sedHendelse.sedType} BUCtype: ${sedHendelse.bucType} bucid: ${sedHendelse.rinaSakId} ***")

                val sedHendelseAgg = sedInfoService.aggregateSedSendtData(sedHendelse)
                statistikkPublisher.publiserSedHendelse(sedHendelseAgg)

                logger.info("Acket statistikk (sed mottatt) med offset: ${cr.offset()} i partisjon ${cr.partition()}")

            } catch (ex: Exception) {
                logger.error("Noe gikk galt under behandling av statistikk-sed-hendelse:\n $hendelse \n", ex)
                throw RuntimeException(ex.message)
            }
            latch.countDown()
        }
    }*/

    @KafkaListener(id = "sedMottattListener",
        idIsGroup = false,
        topics = ["\${kafka.statistikk-sed-sendt.topic}"],
        groupId = "\${kafka.statistikk-sed-sendt.groupid}",
        autoStartup = "false")
    fun consumeSedSendt(hendelse: String, cr: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        MDC.putCloseable("x_request_id", UUID.randomUUID().toString()).use {
            try {
                val sedHendelseRina = SedHendelseRina.fromJson(hendelse)

                sedInfoService.hentLagretSedhendelse(sedHendelseRina.rinaSakId, sedHendelseRina.rinaDokumentId).let {
                    if (it != null) {
                        statistikkPublisher.publiserSedHendelse(it)
                    }
                    else{
                        logger.error("Noe gikk galt under behandling av statistikk-sed-hendelse:\n $hendelse \n", sedHendelseRina)
                    }
                }

            } catch (ex: Exception) {
                logger.error("Noe gikk galt under behandling av statistikk-sed-hendelse:\n $hendelse \n", ex)
                throw RuntimeException(ex.message)
            }
            latch.countDown()
        }
    }

}
