package no.nav.eessi.pensjon.statistikk.listener

import no.nav.eessi.pensjon.eux.model.SedType
import no.nav.eessi.pensjon.eux.model.SedType.*
import no.nav.eessi.pensjon.eux.model.buc.BucType

class GyldigeHendelser {
    companion object {
        private const val gyldigSektorKode = "P"

        private val gyldigeInnkommendeBucTyper = listOf(BucType.H_BUC_07, BucType.R_BUC_02)
        private val gyldigUtgaaendeBucType = BucType.R_BUC_02

        /**
         * SED-typer vi IKKE behandler i Journalføring.
         */
        val ugyldigeTyper: Set<SedType> = setOf(
            P13000, X001, X002, X003, X004, X006, X007, X009,
            X011, X012, X013, X050, X100, H001, H002, H020, H021, H120, H121, R004, R006
        )

        fun mottatt(hendelse: SedHendelseRina) =
                when {
                    (hendelse.bucType in gyldigeInnkommendeBucTyper || gyldigSektorKode == hendelse.sektorKode)
                            && hendelse.sedType !in ugyldigeTyper -> true
                    else -> false
                }

        fun sendt(hendelse: SedHendelseRina) =
                when {
                    (gyldigUtgaaendeBucType == hendelse.bucType || gyldigSektorKode == hendelse.sektorKode)
                            && hendelse.sedType !in ugyldigeTyper -> true
                    else -> false
                }
    }
}
