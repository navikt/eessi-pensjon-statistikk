package no.nav.eessi.pensjon.eux

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import no.nav.eessi.pensjon.metrics.MetricsHelper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import javax.annotation.PostConstruct

/**
 *   https://eux-app.nais.preprod.local/swagger-ui.html#/eux-cpi-service-controller/
 */
@Component
class EuxKlient(
    private val euxOidcRestTemplate: RestTemplate,
    @Autowired(required = false) private val metricsHelper: MetricsHelper = MetricsHelper(SimpleMeterRegistry())) {

    private val logger = LoggerFactory.getLogger(EuxKlient::class.java)

    private lateinit var hentBucMetadata: MetricsHelper.Metric

    @PostConstruct
    fun initMetrics() {
        hentBucMetadata = metricsHelper.init("hentBucMetadata")
    }

    fun getBucMetadata(rinaSakId: String): BucMetadata? {
        logger.info("Henter BUC metadata for rinasakId: $rinaSakId")

        return try {
            euxOidcRestTemplate.getForObject(
            "/buc/$rinaSakId",
            BucMetadata::class.java)
        }
        catch (ex: HttpClientErrorException) {
            logger.error("Feil ved henting av Buc metadata for rinasakId: $rinaSakId")
            throw ex
        }
    }

    fun getSed(rinaSakId: String, rinaDokumentId: String): Sed {
        logger.info("Henter BUC metadata for rinasakId: $rinaSakId")

        return try {
            euxOidcRestTemplate.getForObject(
                "/buc/$rinaSakId/sed/$rinaDokumentId",
                Sed::class.java)!!
        }
        catch (ex: HttpClientErrorException) {
            if(ex.statusCode == HttpStatus.NOT_FOUND){
                logger.warn("Fant ikke SED for rinasakId: $rinaSakId rinaDokumentId $rinaDokumentId")
                throw RuntimeException("Fant ikke SED for rinasakId: $rinaSakId rinaDokumentId $rinaDokumentId")
            }
            logger.error("Feil ved henting av SED metadata for rinasakId: $rinaSakId rinaDokumentId $rinaDokumentId")
            throw ex
        }
    }

}

