package no.nav.eessi.pensjon.statistikk.listener

import no.nav.eessi.pensjon.eux.EuxService
import no.nav.eessi.pensjon.json.toJson
import no.nav.eessi.pensjon.statistikk.models.SedHendelseModel
import no.nav.eessi.pensjon.statistikk.models.StatistikkMeldingInn
import no.nav.eessi.pensjon.statistikk.models.StatistikkMeldingUt
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.PartitionOffset
import org.springframework.kafka.annotation.TopicPartition
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.CountDownLatch

@Component
class StatistikkListener (private val kafkaTemplate: KafkaTemplate<String, String>,
                          @Value("\${kafka.statistikk-ut.topic}") private val statistikkUtTopic: String,
                          private val euxService: EuxService) {

    private val logger = LoggerFactory.getLogger(StatistikkListener::class.java)

     private val latch = CountDownLatch(1)

    fun getLatch() = latch

/*    @KafkaListener(id="statistikkListener",
            idIsGroup = false,
            topics = ["\${kafka.statistikk-inn.topic}"],
            groupId = "\${kafka.statistikk-inn.groupid}",
            autoStartup = "false")*/

    @KafkaListener(   id="statistikkListener",
        idIsGroup = false,
        autoStartup = "false",
        groupId = "\${kafka.statistikk-inn.groupid}",
        topicPartitions = [TopicPartition(topic = "\${kafka.statistikk-inn.topic}",
            partitionOffsets = [PartitionOffset(partition = "0", initialOffset = "1091")])])
    fun consumeBuc(hendelse: String, cr: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        MDC.putCloseable("x_request_id", UUID.randomUUID().toString()).use {
            logger.info("Innkommet statistikk hendelse i partisjon: ${cr.partition()}, med offset: ${cr.offset()}")
            logger.debug(hendelse)

            try {
                logger.debug("Hendelse : $hendelse")
                val melding = StatistikkMeldingInn.fromJson(hendelse)

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

    private fun produserKafkaMelding(meldingInn: StatistikkMeldingInn) {
        logger.info("Produserer melding på kafka: $statistikkUtTopic  melding: $meldingInn")

        val dokumentId = meldingInn.dokumentId

        //buc opprettet
        if(dokumentId == null){
            kafkaTemplate.send(statistikkUtTopic, meldingInn.toJson()).get()
            return
        }

        val dokumentOpprettetDato = euxService.getTimeStampFromSedMetaDataInBuc(meldingInn.rinaid, dokumentId)
        //mangler gyldig opprettetdato, avslutter
        if(dokumentOpprettetDato == null){
            logger.warn("Finner ikke opprettetdato for RinaId: $dokumentId")
        }
        //sed opprettet
        else {
            val meldingUt = StatistikkMeldingUt(meldingInn, dokumentOpprettetDato)
            kafkaTemplate.send(statistikkUtTopic, meldingUt.toJson()).get()
        }
    }
}
