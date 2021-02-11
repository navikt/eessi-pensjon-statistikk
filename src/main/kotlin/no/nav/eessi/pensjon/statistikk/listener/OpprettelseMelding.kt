package no.nav.eessi.pensjon.statistikk.listener

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.eessi.pensjon.statistikk.models.OpprettelseType

@JsonIgnoreProperties(ignoreUnknown = true)
data class OpprettelseMelding(
    val opprettelseType: OpprettelseType,
    val rinaId: String,
    val dokumentId: String?,
    val vedtaksId: String?
) {
    companion object {
        private val sedMapper: ObjectMapper =
            jacksonObjectMapper().configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)

        fun fromJson(json: String): OpprettelseMelding = sedMapper.readValue(json, OpprettelseMelding::class.java)
    }
}

