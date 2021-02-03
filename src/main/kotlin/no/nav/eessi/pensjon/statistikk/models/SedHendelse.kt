package no.nav.eessi.pensjon.statistikk.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

@JsonIgnoreProperties(ignoreUnknown = true)
data class SedHendelse (
    val id: Long? = 0,
    val sedId: String? = null,
    val bucType: String? = null,
    val rinaSakId: String,
    val avsenderLand: String? = null,
    val mottakerLand: String? = null,
    val rinaDokumentId: String,
    val rinaDokumentVersjon: String? = null,
    val sedType: SedType? = null,
    var navBruker: String? = null,
    var hendelseType: HendelseType
) {
    var pesysSakId: String? = null
    var opprettetDato: String? = null
    var vedtaksId: String? = null

    companion object {
        private val sedMapper: ObjectMapper = jacksonObjectMapper().configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)

        fun fromJson(json: String): SedHendelse = sedMapper.readValue(json, SedHendelse::class.java)
    }
}

