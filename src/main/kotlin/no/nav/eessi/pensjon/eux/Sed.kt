package no.nav.eessi.pensjon.eux

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.eessi.pensjon.statistikk.models.SedType

class Sed(val nav: Nav, val sedType: SedType)
{
    companion object {
        private val sedMapper: ObjectMapper = jacksonObjectMapper().configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)
        fun fromJson(json: String): Sed = sedMapper.readValue(json, Sed::class.java)
    }
}

class Nav(val bruker: Bruker?, val eessisak: List<Sak?>?)

class Sak(val land: String, val saksnummer: String)

class Bruker(val person: Person?)

class Person(val pin: List<Pin>?)

class Pin(val land: String, val identifikator: String)
