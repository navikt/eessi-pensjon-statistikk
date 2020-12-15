package no.nav.eessi.pensjon.eux

import no.nav.eessi.pensjon.json.mapJsonToAny
import no.nav.eessi.pensjon.json.typeRefs
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*


@Service
class EuxService(private val euxKlient: EuxKlient){

    private val offsetTimeDatePattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"

    private fun getBucMetadata(rinaSakId: String) : BucMetadata {
        val response =  euxKlient.getBucMetadata(rinaSakId = rinaSakId)
        return  mapJsonToAny(response, typeRefs())
    }

    fun getTimeStampFromSedMetaDataInBuc(rinaSakId: String, dokumentId : String) : OffsetDateTime? {
        val bucMetadata : BucMetadata = getBucMetadata(rinaSakId)
        val dokument : Optional<Document> = bucMetadata.documents.stream().filter { it.id == dokumentId }.findFirst()

        val creationDate = dokument.get().creationDate

        return OffsetDateTime.parse(creationDate, DateTimeFormatter.ofPattern(offsetTimeDatePattern))
    }
}