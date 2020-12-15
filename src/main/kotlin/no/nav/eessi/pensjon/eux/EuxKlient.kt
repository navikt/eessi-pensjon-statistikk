package no.nav.eessi.pensjon.eux

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import no.nav.eessi.pensjon.metrics.MetricsHelper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.rmi.ServerException
import javax.annotation.PostConstruct

/**
 *   https://eux-app.nais.preprod.local/swagger-ui.html#/eux-cpi-service-controller/
 */
@Component
class EuxKlient(
    private val euxOidcRestTemplate: RestTemplate,
    @Autowired(required = false) private val metricsHelper: MetricsHelper = MetricsHelper(SimpleMeterRegistry())) {

    // Vi trenger denne no arg konstruktøren for å kunne bruke @Spy med mockito
    constructor() : this(RestTemplate(), MetricsHelper(SimpleMeterRegistry()))

    private val logger = LoggerFactory.getLogger(EuxKlient::class.java)

    private lateinit var hentBucMetadata: MetricsHelper.Metric


    @PostConstruct
    fun initMetrics() {
        hentBucMetadata = metricsHelper.init("hentBucMetadata")
    }

    fun getBucMetadata(rinaSakId: String): String {
        logger.info("Henter BUC metadata for rinasakId: $rinaSakId")

        val response = euxOidcRestTemplate.getForEntity(
            "/buc/$rinaSakId",
            String::class.java)

        return response.body ?: throw ServerException("Feil ved henting av Buc metadata for rinasakId: $rinaSakId")
    }
}

