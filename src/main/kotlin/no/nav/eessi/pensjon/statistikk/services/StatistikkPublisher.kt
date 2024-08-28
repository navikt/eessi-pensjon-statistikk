package no.nav.eessi.pensjon.statistikk.services

import no.nav.eessi.pensjon.statistikk.models.BucOpprettetMeldingUt
import no.nav.eessi.pensjon.statistikk.models.SedMeldingUt
import no.nav.eessi.pensjon.utils.toJson
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class StatistikkPublisher(private val kafkaTemplate: KafkaTemplate<String, String>,
                          @Value("\${kafka.statistikk-ut.topic}") private val statistikkUtTopic: String
) {

    private val logger = LoggerFactory.getLogger(StatistikkPublisher::class.java)
    private val secureLogger = LoggerFactory.getLogger("secureLog")

    fun publiserBucOpprettetStatistikk(bucOpprettet: BucOpprettetMeldingUt) {
        logger.info("Produserer melding på kafka: $statistikkUtTopic  melding: $bucOpprettet")

        kafkaTemplate.send(statistikkUtTopic, bucOpprettet.toJson()).get()
    }

    fun publiserSedHendelse(sedMeldingUt: SedMeldingUt) {
        secureLogger.info("Produserer sed hendelse melding på kafka: $statistikkUtTopic melding: ${sedMeldingUt.toJson()}")

        val future = kafkaTemplate.send(statistikkUtTopic, sedMeldingUt.toJson())

        try {
            val result = future.get()
            logger.info("Melding produsert til kafka topic ${result?.recordMetadata?.topic()} med offset ${result?.recordMetadata?.offset()}")
        } catch (ex: Exception) {
            secureLogger.error("Feil ved sending av melding til kafka topic $statistikkUtTopic", ex)
        }
    }
}