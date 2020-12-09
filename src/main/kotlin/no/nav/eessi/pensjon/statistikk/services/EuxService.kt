package no.nav.eessi.pensjon.statistikk.services

import no.nav.eessi.pensjon.statistikk.clients.EuxKlient
import no.nav.eessi.pensjon.statistikk.json.mapJsonToAny
import no.nav.eessi.pensjon.statistikk.json.typeRefs
import no.nav.eessi.pensjon.statistikk.models.BucMetadata
import no.nav.eessi.pensjon.statistikk.models.Document
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class EuxService(private val euxKlient: EuxKlient){

    fun getBucMetadata(rinaSakId: String) : BucMetadata{
        val response =  euxKlient.getBucMetadata(rinaSakId = rinaSakId)
        return  mapJsonToAny(response, typeRefs())
    }

    fun getTimeStampFromSedMetaDataInBuc(rinaSakId: String, dokumentId : String) : LocalDateTime {
        val bucMetadata : BucMetadata = getBucMetadata(rinaSakId)
        val dokument : Optional<Document> = bucMetadata.documents.stream().filter { it.id == dokumentId }.findFirst()

        val pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
        //return SimpleDateFormat(pattern).parse(dokument.get().creationDate).toString()
        //String str = "2016-03-04 11:30";
        //"yyyy-MM-dd HH:mm"
        return LocalDateTime.parse(dokument.get().creationDate, DateTimeFormatter.ofPattern(pattern))
    }
}