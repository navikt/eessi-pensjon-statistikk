package no.nav.eessi.pensjon.statistikk.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.eessi.pensjon.eux.model.SedType
import no.nav.eessi.pensjon.eux.model.buc.BucType

@JsonIgnoreProperties(ignoreUnknown = true)
open class SedMeldingUt (
    /* Om felt endres - husk å regenerer equals/hashcode/toString */
    open val dokumentId: String,
    open val bucType: BucType,
    open val rinaid: String, // rinaSakId
    open val mottakerLand: List<String>,
    open val avsenderLand: String? = null,
    open val rinaDokumentVersjon: String,
    open val sedType: SedType,
    open val pid: String? = null, // (første) norske pin funnet på bruker
    open val hendelseType: HendelseType,
    open val pesysSakId: String? = null,
    open val opprettetTidspunkt: String,
    open val vedtaksId: String? = null){

    /* Genereres f eks med IDEA. */
    override fun toString(): String {
        return "SedMeldingUt(dokumentId='$dokumentId', bucType=$bucType, rinaid='$rinaid', mottakerLand=$mottakerLand, rinaDokumentVersjon='$rinaDokumentVersjon', sedType=$sedType, pid=$pid, hendelseType=$hendelseType, pesysSakId=$pesysSakId, opprettetTidspunkt='$opprettetTidspunkt', vedtaksId=$vedtaksId)"
    }

    /* Genereres f eks med IDEA. */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SedMeldingUt

        if (dokumentId != other.dokumentId) return false
        if (bucType != other.bucType) return false
        if (rinaid != other.rinaid) return false
        if (mottakerLand != other.mottakerLand) return false
        if (avsenderLand != other.avsenderLand) return false
        if (rinaDokumentVersjon != other.rinaDokumentVersjon) return false
        if (sedType != other.sedType) return false
        if (pid != other.pid) return false
        if (hendelseType != other.hendelseType) return false
        if (pesysSakId != other.pesysSakId) return false
        if (opprettetTidspunkt != other.opprettetTidspunkt) return false
        if (vedtaksId != other.vedtaksId) return false

        return true
    }

    /* Genereres f eks med IDEA. */
    override fun hashCode(): Int {
        var result = dokumentId.hashCode()
        result = 31 * result + bucType.hashCode()
        result = 31 * result + rinaid.hashCode()
        result = 31 * result + mottakerLand.hashCode()
        result = 31 * result + (avsenderLand?.hashCode() ?: 0)
        result = 31 * result + rinaDokumentVersjon.hashCode()
        result = 31 * result + sedType.hashCode()
        result = 31 * result + (pid?.hashCode() ?: 0)
        result = 31 * result + hendelseType.hashCode()
        result = 31 * result + (pesysSakId?.hashCode() ?: 0)
        result = 31 * result + opprettetTidspunkt.hashCode()
        result = 31 * result + (vedtaksId?.hashCode() ?: 0)
        return result
    }
}

