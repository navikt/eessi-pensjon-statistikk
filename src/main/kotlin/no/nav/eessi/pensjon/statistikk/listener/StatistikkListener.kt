package no.nav.eessi.pensjon.statistikk.listener

import no.nav.eessi.pensjon.json.toJson
import no.nav.eessi.pensjon.statistikk.models.HendelseType
import no.nav.eessi.pensjon.statistikk.models.OpprettelseMelding
import no.nav.eessi.pensjon.statistikk.models.OpprettelseType
import no.nav.eessi.pensjon.statistikk.models.SedHendelseRina
import no.nav.eessi.pensjon.statistikk.services.HendelsesAggregeringsService
import no.nav.eessi.pensjon.statistikk.services.StatistikkPublisher
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.PartitionOffset
import org.springframework.kafka.annotation.TopicPartition
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

/*    @KafkaListener(id="statistikkListener",
            idIsGroup = false,
            topics = ["\${kafka.statistikk-inn.topic}"],
            groupId = "\${kafka.statistikk-inn.groupid}",
            autoStartup = "false")*/
    @KafkaListener(groupId = "\${kafka.statistikk-inn.groupid}",
        topicPartitions = [TopicPartition(topic = "\${kafka.statistikk-inn.topic}",
            partitionOffsets = [PartitionOffset(partition = "0", initialOffset = "1905")])])
    fun consumeOpprettelseMelding(hendelse: String, cr: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        MDC.putCloseable("x_request_id", UUID.randomUUID().toString()).use {
            logger.info("Innkommet statistikk hendelse i partisjon: ${cr.partition()}, med offset: ${cr.offset()}")

            try {
                logger.debug("Hendelse : ${hendelse.toJson()}")
                val melding = OpprettelseMelding.fromJson(hendelse)

                when(melding.opprettelseType){
                    OpprettelseType.BUC -> {
                        val bucHendelse = sedInfoService.aggregateBucData(melding)
                      //  val bucOpprettetMeldingUt = BucOpprettetMeldingUt(melding.)
                        statistikkPublisher.publiserBucOpprettetStatistikk(bucHendelse)
                    }
                    OpprettelseType.SED -> {
                        val sedHendelse = sedInfoService.aggregateSedOpprettetData(melding)
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

    @KafkaListener(id = "sedSendtListener",
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
                        logger.debug(it.toJson())
                        it.hendelseType = HendelseType.SED_SENDT
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
