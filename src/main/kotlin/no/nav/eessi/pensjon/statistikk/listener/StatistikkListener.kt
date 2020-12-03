package no.nav.eessi.pensjon.statistikk.listener

import no.nav.eessi.pensjon.statistikk.json.toJson
import no.nav.eessi.pensjon.statistikk.models.SedHendelseModel
import no.nav.eessi.pensjon.statistikk.models.StatistikkMelding
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.CountDownLatch

@Component
class StatistikkListener (private val kafkaTemplate: KafkaTemplate<String, String>,
                          @Value("\${kafka.statistikk-ut.topic}") private val statistikkUtTopic: String) {

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
            logger.debug(hendelse)

            try {
                logger.debug("Hendelse : $hendelse")
                val melding = StatistikkMelding.fromJson(hendelse)

                produserKafkaMelding(melding)
                acknowledgment.acknowledge()
                logger.info("Acket statistikk melding med offset: ${cr.offset()} i partisjon ${cr.partition()}")
            } catch (ex: Exception) {
                logger.error("Noe gikk galt under behandling av statistikk-hendelse:\n $hendelse \n", ex)
                throw RuntimeException(ex.message)
            }
            latch.countDown()
        }
    }

    /*@KafkaListener(id = "sedSendtListener",
            idIsGroup = false,
            topics = ["\${kafka.statistikk-sed-sendt.topic}"],
            groupId = "\${kafka.statistikk-sed-sendt.groupid}",
            autoStartup = "false")*/
    fun consumeSedSendt(hendelse: String, cr: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        MDC.putCloseable("x_request_id", UUID.randomUUID().toString()).use {
            receiveSed(cr, hendelse)
        }
    }

   /* @KafkaListener(id = "sedMottattListener",
            idIsGroup = false,
            topics = ["\${kafka.statistikk-sed-mottatt.topic}"],
            groupId = "\${kafka.statistikk-sed-mottatt.groupid}",
            autoStartup = "false")*/
    fun consumeSedMottatt(hendelse: String, cr: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        MDC.putCloseable("x_request_id", UUID.randomUUID().toString()).use {
            receiveSed(cr, hendelse)
        }
    }

    private fun receiveSed(cr: ConsumerRecord<String, String>, hendelse: String) {
        logger.info("Innkommet statistikk sed-motatt i partisjon: ${cr.partition()}, med offset: ${cr.offset()}")

        try {
            val offset = cr.offset()
            val sedHendelse = SedHendelseModel.fromJson(hendelse)
            logger.info("*** Starter behandling av SED ${sedHendelse.sedType} BUCtype: ${sedHendelse.bucType} bucid: ${sedHendelse.rinaSakId} ***")
            kafkaTemplate.send(statistikkUtTopic, sedHendelse.toJson()).get()

        } catch (ex: Exception) {
            logger.error("Noe gikk galt under behandling av statistikk-sed-hendelse:\n $hendelse \n", ex)
            throw RuntimeException(ex.message)
        }
        latch.countDown()
    }

    private fun produserKafkaMelding(melding: StatistikkMelding) {
        logger.info("Produserer melding på kafka: $statistikkUtTopic  melding: $melding")
        kafkaTemplate.send(statistikkUtTopic, melding.toJson()).get()
    }
}
