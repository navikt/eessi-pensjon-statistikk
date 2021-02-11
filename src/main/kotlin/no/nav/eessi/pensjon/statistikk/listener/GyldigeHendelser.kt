package no.nav.eessi.pensjon.statistikk.listener

import no.nav.eessi.pensjon.eux.BucType
import no.nav.eessi.pensjon.statistikk.models.SedHendelseRina

class GyldigeHendelser {
    companion object {
        private const val gyldigSektorKode = "P"

        private val gyldigeInnkommendeBucTyper = listOf(BucType.H_BUC_07, BucType.R_BUC_02)
        private val gyldigUtgaaendeBucType = BucType.R_BUC_02

        fun mottatt(hendelse: SedHendelseRina) =
                when {
                    hendelse.bucType in gyldigeInnkommendeBucTyper || gyldigSektorKode == hendelse.sektorKode -> true
                    else -> false
                }

        fun sendt(hendelse: SedHendelseRina) =
                when {
                    gyldigUtgaaendeBucType == hendelse.bucType || gyldigSektorKode == hendelse.sektorKode -> true
                    else -> false
                }
    }
}
