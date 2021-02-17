package no.nav.eessi.pensjon.statistikk.models

import no.nav.eessi.pensjon.eux.BucType

data class BucOpprettetMeldingUt(
    val bucType: BucType,
    val hendelseType: HendelseType,
    val rinaId: String,
    val opprettetTidspunkt: String){
}
