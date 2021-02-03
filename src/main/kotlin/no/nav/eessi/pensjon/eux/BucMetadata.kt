package no.nav.eessi.pensjon.eux

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.eessi.pensjon.statistikk.models.BucType
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@JsonIgnoreProperties(ignoreUnknown = true)
data class BucMetadata(
    val sedGVer: String? = "4",
    val sedVer: String? = "1",
    val documents: List<Document>,
    val processDefinitionName: BucType,
    val startDate: String){

    companion object {
        fun offsetTimeStamp(json: String): String = OffsetDateTime.parse(json, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")).toString()
    }
}


@JsonIgnoreProperties(ignoreUnknown = true)
data class Document (
    val id: String,
    val creationDate: String)

