package no.nav.eessi.pensjon.eux

import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter


@Service
class EuxService(private val euxKlient: EuxKlient){

    private val offsetTimeDatePattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"

    fun getTimeStampFromSedMetaDataInBuc(rinaSakId: String, dokumentId : String) : String? {
        val bucMetadataFraEux : BucMetadata? = euxKlient.getBucMetadata(rinaSakId = rinaSakId)

        if(bucMetadataFraEux == null) {
            return bucMetadataFraEux
        }
        val dokument : Document? = bucMetadataFraEux.documents.firstOrNull { it.id == dokumentId }

        val creationDate = dokument?.creationDate

        val offsetDateTime = OffsetDateTime.parse(creationDate, DateTimeFormatter.ofPattern(offsetTimeDatePattern))
        return offsetDateTime?.toString().orEmpty()
    }
}