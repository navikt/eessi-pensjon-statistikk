package no.nav.eessi.pensjon.eux

import no.nav.eessi.pensjon.json.toJson
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter


@Service
class EuxService(private val euxKlient: EuxKlient){

    private val offsetTimeDatePattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
    private val logger = LoggerFactory.getLogger(EuxService::class.java)

    fun getTimeStampFromSedMetaDataInBuc(rinaSakId: String, dokumentId : String) : String? {
        val bucMetadataFraEux : BucMetadata? = euxKlient.getBucMetadata(rinaSakId = rinaSakId)

        if(bucMetadataFraEux == null) {
            return bucMetadataFraEux
        }
        val dokument : Document? = bucMetadataFraEux.documents.firstOrNull { it.id == dokumentId }

        logger.debug("Dokument: ${dokument?.toJson()}")

        //TODO Dette er bare midlertidig fordi man får parentDocumentID istedet for ny sed documentID ( bug i eux-rina-api )
        val creationDate = dokument?.creationDate ?: return null

        val offsetDateTime = OffsetDateTime.parse(creationDate, DateTimeFormatter.ofPattern(offsetTimeDatePattern))
        return offsetDateTime?.toString().orEmpty()
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
        val sed : Sed? = euxKlient.getSed(rinaSakId, dokumentId)

        logger.debug("Dokument: ${sed?.toJson()}")
        return sed
    }
}