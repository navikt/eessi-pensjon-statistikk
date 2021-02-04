package no.nav.eessi.pensjon.statistikk.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.eessi.pensjon.eux.BucType
import no.nav.eessi.pensjon.eux.SedType

@JsonIgnoreProperties(ignoreUnknown = true)
data class SedOpprettetMeldingUt (
    val dokumentId: String,
    val bucType: BucType,
    val rinaid: String,
    val mottakerLand: String? = null,
    val rinaDokumentVersjon: String? = null,
    val sedType: SedType,
    var navBruker: String? = null,
    var hendelseType: HendelseType,
    var pesysSakId: String? = null,
    var opprettetDato: String,
    var vedtaksId: String? = null)

