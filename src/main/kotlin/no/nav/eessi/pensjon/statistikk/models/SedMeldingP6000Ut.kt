package no.nav.eessi.pensjon.statistikk.models

import no.nav.eessi.pensjon.eux.BucType
import no.nav.eessi.pensjon.eux.SedType

data class SedMeldingP6000Ut(
    override val dokumentId: String,
    override val bucType: BucType,
    override val rinaid: String,
    override val mottakerLand: List<String>,
    override val avsenderLand: String,
    override val rinaDokumentVersjon: String,
    override val sedType: SedType,
    override val pid: String? = null,
    override val hendelseType: HendelseType,
    override val pesysSakId: String? = null,
    override val opprettetTidspunkt: String,
    override val vedtaksId: String? = null,
    val bostedsland: String? = null,
    val pensjonsType: PensjonsType?,
    val vedtakStatus: VedtakStatus?,
    val bruttoBelop: String? = null,
    val valuta: String? = null
):
    SedMeldingUt(dokumentId,
        bucType,
        rinaid,
        mottakerLand,
        avsenderLand,
        rinaDokumentVersjon,
        sedType,
        pid,
        hendelseType,
        pesysSakId,
        opprettetTidspunkt,
        vedtaksId)

enum class VedtakStatus(val value: String) {
    INNVILGENSE("01"),
    AVSLAG("02"),
    NY_BEREGNING_OMREGNING("03"),
    FORELOPIG_UTBETALING("04");

    companion object {
        @JvmStatic
        fun fra(value: String?): VedtakStatus? {
            return if (value == null) null
            else values().firstOrNull { it.value == value }
        }
    }
}

enum class PensjonsType(val value: String) {
    ALDER("01"),
    UFORE("02"),
    GJENLEV("03"),
    DELVIS_UFORE("04"),
    HELT_UFORE("05"),
    FORTID_PENSJON("06");

    companion object {
        @JvmStatic
        fun fra(value: String?): PensjonsType? {
            return if (value == null) null
            else values().firstOrNull { it.value == value }
        }
    }
}