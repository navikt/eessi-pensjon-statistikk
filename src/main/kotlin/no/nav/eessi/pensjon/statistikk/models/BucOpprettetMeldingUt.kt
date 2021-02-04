package no.nav.eessi.pensjon.statistikk.models

import no.nav.eessi.pensjon.eux.BucType

data class BucOpprettetMeldingUt(
    val bucType: BucType,
    val hendelseType: HendelseType,
    val rinaid: String,
    val opprettetDato: String){
}