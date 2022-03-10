package no.nav.eessi.pensjon.eux

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import no.nav.eessi.pensjon.metrics.MetricsHelper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestTemplate
import javax.annotation.PostConstruct

/**
 *   https://eux-app.nais.preprod.local/swagger-ui.html#/eux-cpi-service-controller/
 */
@Component
class EuxKlient(
    private val euxClientCredentialsResourceRestTemplate: RestTemplate,
    @Autowired(required = false) private val metricsHelper: MetricsHelper = MetricsHelper(SimpleMeterRegistry())) {

    private val logger = LoggerFactory.getLogger(EuxKlient::class.java)

    private lateinit var hentBucMetadata: MetricsHelper.Metric
    private lateinit var hentSedMetadata: MetricsHelper.Metric

    @PostConstruct
    fun initMetrics() {
        hentBucMetadata = metricsHelper.init("hentBucMetadata")
        hentSedMetadata = metricsHelper.init("hentSedMetadata")
    }

    fun getBucMetadata(rinaSakId: String): BucMetadata? {
        logger.info("Henter BUC metadata for rinasakId: $rinaSakId")
        return hentBucMetadata.measure {
            try {
                retryHelper({ euxClientCredentialsResourceRestTemplate.getForObject("/buc/$rinaSakId", BucMetadata::class.java)} )
            } catch (ex: HttpClientErrorException) {
                logger.error("Feil ved henting av Buc metadata for rinasakId: $rinaSakId")
                throw ex
            } catch (sx: HttpServerErrorException) {
                logger.error("Serverfeil ved henting av Buc metadata for rinasakId: $rinaSakId")
                throw sx
            }
        }
    }

    fun getSed(rinaSakId: String, rinaDokumentId: String): Sed {
        logger.info("Henter SED for rinasakId: $rinaSakId ,  rinaDokumentId: $rinaDokumentId")

        return hentSedMetadata.measure {
            try {
                retryHelper({ euxClientCredentialsResourceRestTemplate.getForObject("/buc/$rinaSakId/sed/$rinaDokumentId", Sed::class.java)!! })
            } catch (ex: HttpClientErrorException) {
                if (ex.statusCode == HttpStatus.NOT_FOUND) {
                    logger.warn("Fant ikke SED for rinasakId: $rinaSakId rinaDokumentId $rinaDokumentId")
                    throw RuntimeException("Fant ikke SED for rinasakId: $rinaSakId rinaDokumentId $rinaDokumentId")
                }
                logger.error("Feil ved henting av SED metadata for rinasakId: $rinaSakId rinaDokumentId $rinaDokumentId")
                throw ex
            }
        }
    }

    @Throws(Throwable::class)
    fun <T> retryHelper(func: () -> T, maxAttempts: Int = 3, waitTimes: Long = 30000L): T {
        var failException: Throwable? = null
        var count = 0
        while (count < maxAttempts) {
            try {
                return func.invoke()
            } catch (ex: Throwable) {
                count++
                logger.warn("feilet å kontakte eux prøver på nytt. nr.: $count, feilmelding: ${ex.message}")
                failException = ex
                Thread.sleep(waitTimes * count)
            }
        }
        logger.error("Feilet å kontakte eux melding: ${failException?.message}", failException)
        throw failException!!
    }
}

