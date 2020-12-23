package no.nav.eessi.pensjon.eux

class Sed(val nav: Nav)

class Nav(val eessisak: List<Sak?>?)

class Sak(val land: String, val saksnummer: String)
