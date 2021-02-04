package no.nav.eessi.pensjon.eux

enum class SedType{

    // Pensjon
    P1000 {
        fun beskrivelse() = "P1000 - Anmodning om perioder med omsorg for barn"
    },
    P1100 {
         fun beskrivelse() = "P1100 - Svar på anmodning om perioder med omsorg for barn"
    },
    P2000 {
         fun beskrivelse() = "P2000 - Krav om alderspensjon"
    },
    P2100 {
         fun beskrivelse() = "P2100 - Krav om gjenlevendepensjon"
    },
    P2200  {
         fun beskrivelse() = "P2200 - Krav om uførepensjon"
    },
    P3000_AT  {
         fun beskrivelse() = "P3000_AT - Landsspesifikk informasjon - Østerrike"
    },
    P3000_BE  {
         fun beskrivelse() = "P3000_BE - Landsspesifikk informasjon - Belgia"
    },
    P3000_BG  {
         fun beskrivelse() = "P3000_BG - Landsspesifikk informasjon - Bulgaria"
    },
    P3000_CH  {
         fun beskrivelse() = "P3000_CH - Landsspesifikk informasjon - Sveits"
    },
    P3000_CY  {
         fun beskrivelse() = "P3000_CY - Landsspesifikk informasjon - Kypros"
    },
    P3000_CZ  {
         fun beskrivelse() = "P3000_CZ - Landsspesifikk informasjon - Republikken Tsjekkia"
    },
    P3000_DE  {
         fun beskrivelse() = "P3000_DE - Landsspesifikk informasjon - Tyskland"
    },
    P3000_DK  {
         fun beskrivelse() = "P3000_DK - Landsspesifikk informasjon - Danmark"
    },
    P3000_EE  {
         fun beskrivelse() = "P3000_EE - Landsspesifikk informasjon - Estland"
    },
    P3000_EL  {
         fun beskrivelse() = "P3000_EL - Landsspesifikk informasjon - Hellas"
    },
    P3000_ES  {
         fun beskrivelse() = "P3000_ES - Landsspesifikk informasjon - Spania"
    },
    P3000_FI  {
         fun beskrivelse() = "P3000_FI - Landsspesifikk informasjon - Finland"
    },
    P3000_FR  {
         fun beskrivelse() = "P3000_FR - Landsspesifikk informasjon - Frankrike"
    },
    P3000_HR  {
         fun beskrivelse() = "P3000_HR - Landsspesifikk informasjon - Kroatia"
    },
    P3000_HU  {
         fun beskrivelse() = "P3000_HU - Landsspesifikk informasjon - Ungarn"
    },
    P3000_IE  {
         fun beskrivelse() = "P3000_IE - Landsspesifikk informasjon - Irland"
    },
    P3000_IS  {
         fun beskrivelse() = "P3000_IS - Landsspesifikk informasjon - Island "
    },
    P3000_IT  {
         fun beskrivelse() = "P3000_IT - Landsspesifikk informasjon - Italia"
    },
    P3000_LI  {
        fun beskrivelse() = "P3000_LI - Landsspesifikk informasjon - Liechtenstein"
    },
    P3000_LT  {
        fun beskrivelse() = "P3000_LT - Landsspesifikk informasjon - Litauen"
    },
    P3000_LU  {
        fun beskrivelse() = "P3000_LU - Landsspesifikk informasjon - Luxembourg"
    },
    P3000_LV  {
        fun beskrivelse() = "P3000_LV - Landsspesifikk informasjon - Latvia"
    },
    P3000_MT  {
        fun beskrivelse() = "P3000_MT - Landsspesifikk informasjon - Malta"
    },
    P3000_NL  {
        fun beskrivelse() = "P3000_NL - Landsspesifikk informasjon - Nederland"
    },
    P3000_NO  {
        fun beskrivelse() = "P3000_NO - Landsspesifikk informasjon - Norge"
    },
    P3000_PL  {
        fun beskrivelse() = "P3000_PL - Landsspesifikk informasjon - Polen"
    },
    P3000_PT  { 
        fun beskrivelse() = "P3000_PT - Landsspesifikk informasjon - Portugal"
    },
    P3000_RO  {
        fun beskrivelse() = "P3000_RO - Landsspesifikk informasjon - Romania"
    },
    P3000_SE  {
        fun beskrivelse() = "P3000_SE - Landsspesifikk informasjon - Sverige"
    },
    P3000_SI  {
        fun beskrivelse() = "P3000_SI - Landsspesifikk informasjon - Slovenia"
    },
    P3000_SK  {
        fun beskrivelse() = "P3000_SK - Landsspesifikk informasjon - Slovakia"
    },
    P3000_UK  {
        fun beskrivelse() = "P3000_UK - Landsspesifikk informasjon - Storbritannia"
    },
    P4000 {
        fun beskrivelse() = "P4000 - Brukers oversikt botid og arbeid"
    },
    P5000 {
        fun beskrivelse() = "P5000 - Oversikt TT"
    },
    P6000 {
        fun beskrivelse() = "P6000 - Melding om vedtak"
    },
    P7000 {
        fun beskrivelse() = "P7000 - Samlet melding om vedtak"
    },
    P8000 {
        fun beskrivelse() = "P8000 - Forespørsel om informasjon"
    },
    P9000 {
        fun beskrivelse() = "P9000 - Svar på forespørsel om informasjon"
    },
    P10000 {
        fun beskrivelse() = "P10000 - Oversendelse av informasjon"
    },
    P11000 {
        fun beskrivelse() = "P11000 - Anmodning om pensjonsbeløp"
    },
    P12000 {
        fun beskrivelse() = "P12000 - Informasjon om pensjonsbeløp"
    },
    P13000 {
        fun beskrivelse() = "P13000 - Informasjon om pensjonstillegg"
    },
    P14000 {
        fun beskrivelse() = "P14000 - Endring i personlige forhold"
    },
    P15000 {
        fun beskrivelse() = "P15000 - Overføring av pensjonssaker til EESSI"
    },
    // Administrative
    X001 {
        fun beskrivelse() = "X001 - Anmodning om avslutning"
    },
    X002 {
        fun beskrivelse() = "X002 - Anmodning om gjenåpning av avsluttet sak"
    },
    X003 {
        fun beskrivelse() = "X003 - Svar på anmodning om gjenåpning av avsluttet sak"
    },
    X004 {
        fun beskrivelse() = "X004 - Gjenåpne saken"
    },
    X005 {
        fun beskrivelse() = "X005 - Legg til ny institusjon"
    },
    X006 {
        fun beskrivelse() = "X006 - Fjern institusjon"
    },
    X007 {
        fun beskrivelse() = "X007 - Videresend sak"
    },
    X008 {
        fun beskrivelse() = "X008 - Ugyldiggjøre SED"
    },
    X009 {
        fun beskrivelse() = "X009 - Påminnelse"
    },
    X010 {
        fun beskrivelse() = "X010 - Svar på påminnelse"
    },
    X011 {
        fun beskrivelse() = "X011 - Avvis SED"
    },
    X012 {
        fun beskrivelse() = "X012 - Klargjør innhold"
    },
    X013 {
        fun beskrivelse() = "X013 - Svar på anmodning om klargjøring"
    },
    X050 {
        fun beskrivelse() = "X050 - Unntaksfeil"
    },

    // Horisontale
    H001 {
        fun beskrivelse() = "H001 - Melding/anmodning om informasjon"
    },
    H002 {
        fun beskrivelse() = "H002 - Svar på anmodning om informasjon"
    },
    H020 {
        fun beskrivelse() = "H020 - Krav om - refusjon - administrativ kontroll / medisinsk informasjon"
    },
    H021 {
        fun beskrivelse() = "H021 - Svar på krav om refusjon - administrativ kontroll / legeundersøkelse / medisinsk informasjon"
    },
    H070 {
        fun beskrivelse() = "H070 - Melding om dødsfall"
    },
    H120 {
        fun beskrivelse() = "H120 - Anmodning om medisinsk informasjon"
    },
    H121 { 
        fun beskrivelse() = "H121 - Melding om medisinsk informasjon / Svar på forespørsel om medisinsk informasjon"
    },

    // Seder i R_BUC_02 Motregning av overskytende utbetaling i etterbetalinger er
    R004 { 
        fun beskrivelse() = "R004 - Melding om utbetaling"
    },
    R005 { 
        fun beskrivelse() = "R005 - Anmodning om motregning i etterbetalinger (foreløpig eller endelig)"
    },
    R006 { 
        fun beskrivelse() = "R006 - Svar på anmodning om informasjon"
    }
}


