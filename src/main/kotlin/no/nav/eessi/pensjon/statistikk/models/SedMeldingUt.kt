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
    open val bucType: BucType,
    open val rinaid: String,
    open val mottakerLand: List<String>? = null,
    open val rinaDokumentVersjon: String,
    open val sedType: SedType,
    open val pid: String? = null,
    open val hendelseType: HendelseType,
    open val pesysSakId: String? = null,
    open val opprettetTidspunkt: String,
    open val vedtaksId: String? = null){

    companion object {
        private val sedMapper: ObjectMapper =
            jacksonObjectMapper().configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)
        fun fromJson(json: String): SedMeldingUt = sedMapper.readValue(json, SedMeldingUt::class.java)
    }

    override fun toString(): String {
        return "SedMeldingUt(dokumentId='$dokumentId', bucType=$bucType, rinaid='$rinaid', mottakerLand=$mottakerLand, rinaDokumentVersjon='$rinaDokumentVersjon', sedType=$sedType, pid=$pid, hendelseType=$hendelseType, pesysSakId=$pesysSakId, opprettetTidspunkt='$opprettetTidspunkt', vedtaksId=$vedtaksId)"
    }


}

