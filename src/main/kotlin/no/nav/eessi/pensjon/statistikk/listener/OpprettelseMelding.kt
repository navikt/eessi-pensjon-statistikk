package no.nav.eessi.pensjon.statistikk.listener

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.eessi.pensjon.statistikk.models.OpprettelseType

@JsonIgnoreProperties(ignoreUnknown = true)
data class OpprettelseMelding(
    val opprettelseType: OpprettelseType,
    val rinaId: String,
    val dokumentId: String?,
    val vedtaksId: String?
)

