package no.nav.eessi.pensjon.statistikk.listener

import no.nav.eessi.pensjon.eux.model.BucType
import no.nav.eessi.pensjon.eux.model.SedHendelse
import no.nav.eessi.pensjon.eux.model.SedType
import no.nav.eessi.pensjon.eux.model.SedType.*

class GyldigeHendelser {
    companion object {
        private const val gyldigSektorKode = "P"

        private val gyldigeInnkommendeBucTyper = listOf(BucType.H_BUC_07, BucType.R_BUC_02)
        private val gyldigUtgaaendeBucType = BucType.R_BUC_02

        /**
         * SED-typer vi IKKE behandler i Journalføring.
         */
        val ugyldigeTyper: Set<SedType> = setOf(
            SEDTYPE_P13000, SEDTYPE_X001, SEDTYPE_X002, SEDTYPE_X003, SEDTYPE_X004, SEDTYPE_X006, SEDTYPE_X007, SEDTYPE_X009,
            SEDTYPE_X011, SEDTYPE_X012, SEDTYPE_X013, SEDTYPE_X050, SEDTYPE_X100, SEDTYPE_H001, SEDTYPE_H002, SEDTYPE_H020, SEDTYPE_H021, SEDTYPE_H120, SEDTYPE_H121, SEDTYPE_R004, SEDTYPE_R006
        )

        fun mottatt(hendelse: SedHendelse) =
                when {
                    (hendelse.bucType in gyldigeInnkommendeBucTyper || gyldigSektorKode == hendelse.sektorKode)
                            && hendelse.sedType !in ugyldigeTyper -> true
                    else -> false
                }

        fun sendt(hendelse: SedHendelse) =
                when {
                    (gyldigUtgaaendeBucType == hendelse.bucType || gyldigSektorKode == hendelse.sektorKode)
                            && hendelse.sedType !in ugyldigeTyper -> true
                    else -> false
                }
    }
}
