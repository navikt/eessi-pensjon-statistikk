package no.nav.eessi.pensjon.eux

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@JsonIgnoreProperties(ignoreUnknown = true)
data class BucMetadata(
    val documents: List<Document>,
    val processDefinitionName: BucType,
    val startDate: String){

    companion object {
        private val metadataMapper: ObjectMapper = jacksonObjectMapper().configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)

        fun fromJson(json: String): BucMetadata = metadataMapper.readValue(json, BucMetadata::class.java)

        fun offsetTimeStamp(json: String): String = OffsetDateTime.parse(json, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")).toString()
    }
}

data class Document (
    val id: String,
    val creationDate: String,
    var conversations: List<Conversation> = emptyList(),
    var versions: List<Version>
)

data class Version(var id: String)

data class Conversation (var participants: List<Participant>?)

data class Participant (val role: String, val organisation: Organisation)

data class Organisation (val countryCode: String)
