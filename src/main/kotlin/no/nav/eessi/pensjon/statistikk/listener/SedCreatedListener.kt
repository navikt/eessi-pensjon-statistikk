package no.nav.eessi.pensjon.statistikk.listener

import no.nav.eessi.pensjon.eux.EuxService
import no.nav.eessi.pensjon.gcp.SedCreatedRecord
import no.nav.eessi.pensjon.gcp.SedCreatedStorage
import no.nav.eessi.pensjon.metrics.MetricsHelper
import no.nav.eessi.pensjon.statistikk.models.OpprettelseType
import no.nav.eessi.pensjon.utils.JsonException
import no.nav.eessi.pensjon.utils.mapJsonToAny
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(name = ["sed-created.enabled"], havingValue = "true")
class SedCreatedListener(
    private val storage: SedCreatedStorage,
    @Autowired(required = false) private val metricsHelper: MetricsHelper = MetricsHelper.ForTest()
) {
    private val storeMetric = metricsHelper.init("StoreSedCreated")
    private val logger = LoggerFactory.getLogger(EuxService::class.java)

    @KafkaListener(
        containerFactory = "sedCreatedKafkaListenerContainerFactory",
        topics = ["\${kafka.statistikk-inn.topic}"],
        groupId = "\${kafka.sed-created.groupid}"
    )
    fun consume(hendelse: String, acknowledgment: Acknowledgment) {
        storeMetric.measure {
            val json = hendelse.trim()
            val decodedJson = if (json.startsWith('"')) mapJsonToAny<String>(json) else json
            val melding = try {
                mapJsonToAny<OpprettelseMelding>(decodedJson)
            } catch (e: JsonException) {
                metricsHelper.registry.counter("sed.created.tracking", "result", "malformed").increment()
                throw e

            }
            if (melding.opprettelseType == OpprettelseType.SED) {
                logger.info("SED opprettet: $melding")
                if (melding.buctype == null) {
                    metricsHelper.registry.counter("sed.created.tracking", "result", "legacy_skipped").increment()
                } else {
                    storage.store(
                        SedCreatedRecord(
                            buctype = melding.buctype,
                            sedtype = requireNotNull(melding.sedtype) { "Missing sedtype in SED creation event" },
                            euxCaseId = melding.rinaId,
                            documentId = requireNotNull(melding.dokumentId) { "Missing documentId in SED creation event" },
                            sakId = melding.sakId
                        )
                    )
                    metricsHelper.registry.counter("sed.created.tracking", "result", "stored").increment()
                }
            }
            else if(melding.opprettelseType == OpprettelseType.BUC) {
                logger.info("BUC opprettet, men ikke lagret: $melding")
            }
            acknowledgment.acknowledge()
        }
    }
}
