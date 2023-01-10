package no.nav.eessi.pensjon.statistikk.listener

import no.nav.eessi.pensjon.eux.model.BucType
import no.nav.eessi.pensjon.eux.model.SedType

data class SedHendelseRina(
    val id: Long? = 0,
    val sedId: String? = null,
    val sektorKode: String,
    val bucType: BucType? = null,
    val rinaSakId: String,
    val avsenderId: String? = null,
    val avsenderNavn: String? = null,
    val avsenderLand: String? = null,
    val mottakerId: String? = null,
    val mottakerNavn: String? = null,
    val mottakerLand: String? = null,
    val rinaDokumentId: String,
    val rinaDokumentVersjon: String? = null,
    val sedType: SedType? = null,
    val navBruker: String? = null
)




