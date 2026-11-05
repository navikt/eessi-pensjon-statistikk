package no.nav.eessi.pensjon.statistikk.listener

import no.nav.eessi.pensjon.gcp.SedCreatedRecord
import no.nav.eessi.pensjon.gcp.SedCreatedStorage
import no.nav.eessi.pensjon.metrics.MetricsHelper
import no.nav.eessi.pensjon.statistikk.models.OpprettelseType
import no.nav.eessi.pensjon.utils.JsonException
import no.nav.eessi.pensjon.utils.mapJsonToAny
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
                if (melding.buctype == null) {
                    // Events published before tracking was introduced cannot supply all five fields.
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
            acknowledgment.acknowledge()
        }
    }
}
