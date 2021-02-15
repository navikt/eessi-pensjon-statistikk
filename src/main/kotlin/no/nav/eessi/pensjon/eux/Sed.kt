package no.nav.eessi.pensjon.eux

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

class Sed(val nav: Nav, val sed: SedType, val pensjon: Pensjon?)
{
    companion object {
        private val sedMapper: ObjectMapper = jacksonObjectMapper().configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)
        fun fromJson(json: String): Sed = sedMapper.readValue(json, Sed::class.java)
    }
}

class Nav(val bruker: Bruker?, val eessisak: List<Sak?>?)

class Sak(val land: String, val saksnummer: String)

class Bruker(val adresse: Adresse?, val person: Person?)

class Person(val pin: List<Pin>?)

class Pin(val land: String, val identifikator: String)

class Adresse(val land: String?)

class Pensjon(val vedtak: List<Vedtak>?, val tilleggsinformasjon: Tilleggsinformasjon?)

//rinamapping:  resultat == vedtakstype
class Vedtak(val type: String?, val resultat: String?, val beregning: List<Beregning>?)

class Beregning(val belopBrutto: BeloepBrutto?, val valuta: String?,  val belopNetto: BeloepNetto?)

class BeloepBrutto(val beloep: String?)

class BeloepNetto(val beloep: String?)

class Tilleggsinformasjon(val artikkel48: String?)

