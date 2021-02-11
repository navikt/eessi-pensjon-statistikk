package no.nav.eessi.pensjon.statistikk.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.eessi.pensjon.eux.BucType
import no.nav.eessi.pensjon.eux.SedType

@JsonIgnoreProperties(ignoreUnknown = true)
open class SedMeldingUt (
    val dokumentId: String,
    val bucType: BucType,
    val rinaid: String,
    val mottakerLand: List<String>? = null,
    val rinaDokumentVersjon: String,
    val sedType: SedType,
    val pid: String? = null,
    val hendelseType: HendelseType,
    val pesysSakId: String? = null,
    val opprettetTidspunkt: String,
    val vedtaksId: String? = null){

    companion object {
        private val sedMapper: ObjectMapper =
            jacksonObjectMapper().configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)
        fun fromJson(json: String): SedMeldingUt = sedMapper.readValue(json, SedMeldingUt::class.java)
    }
}

