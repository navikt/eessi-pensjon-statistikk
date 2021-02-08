package no.nav.eessi.pensjon.statistikk.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.eessi.pensjon.eux.BucType
import no.nav.eessi.pensjon.eux.SedType

@JsonIgnoreProperties(ignoreUnknown = true)
data class SedOpprettetMeldingUt (
    val dokumentId: String,
    val bucType: BucType,
    val rinaid: String,
    val mottakerLand: List<String>? = null,
    val rinaDokumentVersjon: String = "1",
    val sedType: SedType,
    var navBruker: String? = null,
    var hendelseType: HendelseType,
    var pesysSakId: String? = null,
    var opprettetDato: String,
    var vedtaksId: String? = null){

    companion object {
        private val sedMapper: ObjectMapper =
            jacksonObjectMapper().configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)
        fun fromJson(json: String): SedOpprettetMeldingUt = sedMapper.readValue(json, SedOpprettetMeldingUt::class.java)
    }
}

