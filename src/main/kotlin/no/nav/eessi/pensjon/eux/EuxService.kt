package no.nav.eessi.pensjon.eux

import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*


@Service
class EuxService(private val euxKlient: EuxKlient){

    private val offsetTimeDatePattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"

    fun getTimeStampFromSedMetaDataInBuc(rinaSakId: String, dokumentId : String) : String {
        val bucMetadataFraEux : BucMetadata? = euxKlient.getBucMetadata(rinaSakId = rinaSakId)

        val dokument : Optional<Document> = bucMetadataFraEux?.documents!!.stream().filter { it.id == dokumentId }.findFirst()

        val creationDate = dokument.get().creationDate

        val offsetDateTime = OffsetDateTime.parse(creationDate, DateTimeFormatter.ofPattern(offsetTimeDatePattern))
        return offsetDateTime?.toString().orEmpty()
    }
}