package no.nav.eessi.pensjon.statistikk.listener

import no.nav.eessi.pensjon.statistikk.json.toJson
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
                          @Value("\${kafka.statistikk-inn.topic}") private val statistikkTopic: String) {

    private val logger = LoggerFactory.getLogger(StatistikkListener::class.java)

     private val latch = CountDownLatch(1)

    fun getLatch() = latch

    @KafkaListener(id="statistikkListener",
            idIsGroup = false,
            topics = ["\${kafka.statistikk-inn.topic}"],
            groupId = "\${kafka.statistikk-inn.groupid}",
            autoStartup = "false")
    fun consume(hendelse: String, cr: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
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

    private fun produserKafkaMelding(melding: StatistikkMelding) {
        logger.info("Produserer melding på kafka: $statistikkTopic  melding: $melding")
        kafkaTemplate.send(statistikkTopic, melding.toJson()).get()
    }
}
