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
        avsenderLand,
        rinaDokumentVersjon,
        sedType,
        pid,
        hendelseType,
        pesysSakId,
        opprettetTidspunkt,
        vedtaksId)
{
    override fun toString(): String {
        return "SedMeldingP6000Ut(bucType=$bucType, rinaid='$rinaid', mottakerLand=$mottakerLand, rinaDokumentVersjon='$rinaDokumentVersjon', sedType=$sedType, pid=$pid, hendelseType=$hendelseType, pesysSakId=$pesysSakId, opprettetTidspunkt='$opprettetTidspunkt', vedtaksId=$vedtaksId, bostedsland=$bostedsland, pensjonsType=$pensjonsType, vedtakStatus=$vedtakStatus, bruttoBelop=$bruttoBelop, nettoBelop=$nettoBelop, valuta=$valuta, anmodningOmRevurdering=$anmodningOmRevurdering)"
    }
}
