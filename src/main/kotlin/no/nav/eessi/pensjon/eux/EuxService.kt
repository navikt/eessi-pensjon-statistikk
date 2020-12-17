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

        val creationDate = dokument?.creationDate

        val offsetDateTime = OffsetDateTime.parse(creationDate, DateTimeFormatter.ofPattern(offsetTimeDatePattern))
        return offsetDateTime?.toString().orEmpty()
    }
}