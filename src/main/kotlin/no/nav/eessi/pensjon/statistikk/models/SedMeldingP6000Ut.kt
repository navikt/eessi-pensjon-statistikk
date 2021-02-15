package no.nav.eessi.pensjon.statistikk.models

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.eessi.pensjon.eux.BucType
import no.nav.eessi.pensjon.eux.SedType

class SedMeldingP6000Ut(dokumentId: String,
                        bucType: BucType,
                        rinaid: String,
                        mottakerLand: List<String>? = null,
                        rinaDokumentVersjon: String,
                        sedType: SedType,
                        pid: String? = null,
                        hendelseType: HendelseType,
                        pesysSakId: String? = null,
                        opprettetTidspunkt: String,
                        vedtaksId: String? = null,
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
}
