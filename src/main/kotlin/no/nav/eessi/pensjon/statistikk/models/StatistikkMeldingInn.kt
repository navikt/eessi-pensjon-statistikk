package no.nav.eessi.pensjon.statistikk.models

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

data class StatistikkMeldingInn(
    val hendelseType: HendelseType,
    val rinaid: String,
    val dokumentId: String?
){
    companion object {
        private val sedMapper: ObjectMapper = jacksonObjectMapper().configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)

        fun fromJson(json: String): StatistikkMeldingInn = sedMapper.readValue(json, StatistikkMeldingInn::class.java)
    }
}

