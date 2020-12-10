package no.nav.eessi.pensjon.statistikk.models

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

data class StatistikkMeldingUt(val statistikkMeldingInn: StatistikkMeldingInn, val dokumentOpprettetDato: String){

    val hendelseType: HendelseType = statistikkMeldingInn.hendelseType
    val rinaid: String = statistikkMeldingInn.rinaid
    val dokumentId: String? = statistikkMeldingInn.dokumentId
    val opprettetDato: String? = dokumentOpprettetDato

    companion object {
        private val sedMapper: ObjectMapper = jacksonObjectMapper().configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)
        fun fromJson(json: String): StatistikkMeldingUt = sedMapper.readValue(json, StatistikkMeldingUt::class.java)
    }
}