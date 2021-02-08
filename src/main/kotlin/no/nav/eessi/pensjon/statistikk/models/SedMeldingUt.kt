package no.nav.eessi.pensjon.statistikk.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.eessi.pensjon.eux.BucType
import no.nav.eessi.pensjon.eux.SedType

@JsonIgnoreProperties(ignoreUnknown = true)
data class SedMeldingUt (
    val dokumentId: String,
    val bucType: BucType,
    val rinaid: String,
    val mottakerLand: List<String>? = null,
    var rinaDokumentVersjon: String,
    val sedType: SedType,
    var pid: String? = null,
    var hendelseType: HendelseType,
    var pesysSakId: String? = null,
    var opprettetTidspunkt: String,
    var vedtaksId: String? = null){

    companion object {
        private val sedMapper: ObjectMapper =
            jacksonObjectMapper().configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)
        fun fromJson(json: String): SedMeldingUt = sedMapper.readValue(json, SedMeldingUt::class.java)
    }
}

