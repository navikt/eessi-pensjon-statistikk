package no.nav.eessi.pensjon.eux

import no.nav.eessi.pensjon.utils.toJson
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service


@Service
class EuxService(private val euxKlient: EuxKlient){


    private val logger = LoggerFactory.getLogger(EuxService::class.java)

    fun getBucMetadata(rinaSakId: String) : BucMetadata? {
         val metaData = euxKlient.getBucMetadata(rinaSakId = rinaSakId)
        logger.debug("BucMetaData: ${metaData?.toJson()}")
        return metaData
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
        val sed : Sed = euxKlient.getSed(rinaSakId, dokumentId)

        logger.debug("Dokument: ${sed.toJson()}")
        return sed
    }
}