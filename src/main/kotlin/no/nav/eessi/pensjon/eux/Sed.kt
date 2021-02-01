package no.nav.eessi.pensjon.eux

class Sed(val nav: Nav)

class Nav(val eessisak: List<Sak?>?)

class Sak(val land: String, val saksnummer: String)

class Bruker(val person: Person)

class Person(val pin: Pin)

class Pin(val land: String, val identifikator: String)
