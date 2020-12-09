package no.nav.eessi.pensjon.statistikk.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class BucMetadata(
    val sedGVer: String? = "4",
    val sedVer: String? = "1",
    val documents: List<Document>)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Document (
    val id: String,
    val creationDate: String)
