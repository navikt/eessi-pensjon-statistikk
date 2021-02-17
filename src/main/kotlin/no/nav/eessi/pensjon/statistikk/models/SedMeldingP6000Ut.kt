package no.nav.eessi.pensjon.statistikk.models

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.eessi.pensjon.eux.BucType
import no.nav.eessi.pensjon.eux.SedType

class SedMeldingP6000Ut(dokumentId: String,
                        override val bucType: BucType,
                        override val rinaid: String,
                        override val mottakerLand: List<String>,
                        override val rinaDokumentVersjon: String,
                        override val sedType: SedType,
                        override val pid: String? = null,
                        override val hendelseType: HendelseType,
                        override val pesysSakId: String? = null,
                        override val opprettetTidspunkt: String,
                        override val vedtaksId: String? = null,
                        val bostedsland: String? = null,
                        val pensjonsType: String? = null,
                        val vedtakStatus: String? = null,
                        val bruttoBelop: String? = null,
                        val nettoBelop: String? = null,
                        val valuta: String? = null,
                        val anmodningOmRevurdering: String?
                        ):
    SedMeldingUt(dokumentId,
        bucType,
        rinaid,
        mottakerLand,
        rinaDokumentVersjon,
        sedType,
        pid,
        hendelseType,
        pesysSakId,
        opprettetTidspunkt,
        vedtaksId)
{

    companion object {
        private val sedMapper: ObjectMapper = jacksonObjectMapper().configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)
        fun fromJson(json: String): SedMeldingP6000Ut = sedMapper.readValue(json, SedMeldingP6000Ut::class.java)
    }

    override fun toString(): String {
        return "SedMeldingP6000Ut(bucType=$bucType, rinaid='$rinaid', mottakerLand=$mottakerLand, rinaDokumentVersjon='$rinaDokumentVersjon', sedType=$sedType, pid=$pid, hendelseType=$hendelseType, pesysSakId=$pesysSakId, opprettetTidspunkt='$opprettetTidspunkt', vedtaksId=$vedtaksId, bostedsland=$bostedsland, pensjonsType=$pensjonsType, vedtakStatus=$vedtakStatus, bruttoBelop=$bruttoBelop, nettoBelop=$nettoBelop, valuta=$valuta, anmodningOmRevurdering=$anmodningOmRevurdering)"
    }
}
