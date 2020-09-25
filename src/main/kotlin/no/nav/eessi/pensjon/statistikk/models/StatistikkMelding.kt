package no.nav.eessi.pensjon.statistikk.models

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

class StatistikkMelding(
        val hendelseType: HendelseType,
        val rinaid: String,
        val bucType: String,
        val timeStamp: Long,
        val saksNummer: String? = null,
        val vetaksId: String? = null,
        val hendelseVersjon: Int? = null
) {
    companion object {
        private val sedMapper: ObjectMapper = jacksonObjectMapper().configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)

        fun fromJson(json: String): StatistikkMelding = sedMapper.readValue(json, StatistikkMelding::class.java)
    }
}


enum class HendelseType {
    OPPRETTBUC,
    OPPRETTSED
}