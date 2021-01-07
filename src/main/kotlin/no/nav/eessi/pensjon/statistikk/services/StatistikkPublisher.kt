package no.nav.eessi.pensjon.statistikk.services

import no.nav.eessi.pensjon.json.toJson
import no.nav.eessi.pensjon.statistikk.models.BucOpprettetHendelseUt
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class StatistikkPublisher(private val kafkaTemplate: KafkaTemplate<String, String>,
                          @Value("\${kafka.statistikk-ut.topic}") private val statistikkUtTopic: String
) {

    private val logger = LoggerFactory.getLogger(StatistikkPublisher::class.java)

    fun publiserBucOpprettetStatistikk(bucOpprettet: BucOpprettetHendelseUt) {
        logger.info("Produserer melding på kafka: $statistikkUtTopic  melding: $bucOpprettet")

        kafkaTemplate.send(statistikkUtTopic, bucOpprettet.toJson()).get()
    }
}