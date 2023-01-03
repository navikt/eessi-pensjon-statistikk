package no.nav.eessi.pensjon.eux

import no.nav.eessi.pensjon.eux.klient.EuxKlientLib
import no.nav.eessi.pensjon.utils.mapJsonToAny
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.retry.RetryCallback
import org.springframework.retry.RetryContext
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.retry.listener.RetryListenerSupport
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service


@Service
class EuxService(private val euxKlient: EuxKlientLib){


    private val logger = LoggerFactory.getLogger(EuxService::class.java)

    @Retryable(
        backoff = Backoff(delayExpression = "@euxKlientRetryConfig.initialRetryMillis", maxDelay = 200000L, multiplier = 3.0),
        listeners  = ["euxKlientRetryLogger"]
    )
    fun getBucMetadata(rinaSakId: String) : BucMetadata? {
        val metaData = euxKlient.hentBucJson(rinaSakId = rinaSakId)
        logger.debug("bucmetadata: ${metaData}")

        return metaData?.let { mapJsonToAny(it) }
    }

    /**
     * Henter norsk sakID fra SED
     *
     * 1.1.[1].2. Case number
     *
     * Tjenesten går ut ifra at kun en norsk sakID er oppgitt i listen av sakIder
     * Returnerer null dersom det ikke finnes noen norske sakIder i listen
     *
     */
    @Retryable(
        backoff = Backoff(delayExpression = "@euxKlientRetryConfig.initialRetryMillis", maxDelay = 200000L, multiplier = 3.0),
        listeners  = ["euxKlientRetryLogger"]
    )
    fun getSed(rinaSakId: String, dokumentId : String) : Sed? {
        val sedAsJson = euxKlient.hentSedJson(rinaSakId, dokumentId)

        logger.debug("Dokument: ${sedAsJson}")
        return sedAsJson?.let { mapJsonToAny(sedAsJson)}
    }
}

@Profile("!retryConfigOverride")
@Component
data class EuxKlientRetryConfig(val initialRetryMillis: Long = 20000L)

@Component
class EuxKlientRetryLogger : RetryListenerSupport() {
    private val logger = LoggerFactory.getLogger(EuxKlientRetryLogger::class.java)
    override fun <T : Any?, E : Throwable?> onError(context: RetryContext?, callback: RetryCallback<T, E>?, throwable: Throwable?) {
        logger.warn("Feil under henting fra EUX - try #${context?.retryCount } - ${throwable?.toString()}", throwable)
    }
}

