package no.nav.eessi.pensjon.eux

import no.nav.eessi.pensjon.eux.klient.EuxKlientLib
import no.nav.eessi.pensjon.utils.mapJsonToAny
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service


@Service
class EuxService(private val euxKlient: EuxKlientLib){


    private val logger = LoggerFactory.getLogger(EuxService::class.java)

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
    fun getSed(rinaSakId: String, dokumentId : String) : Sed? {
        val sedAsJson = euxKlient.hentSedJson(rinaSakId, dokumentId)

        logger.debug("Dokument: ${sedAsJson}")
        return sedAsJson?.let { mapJsonToAny(sedAsJson)}
    }
}