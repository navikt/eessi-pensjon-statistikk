package no.nav.eux.rina.app.kafka

class KafkaMessage (
    val id : Long?,
    val sedId : String?,
    val sektorKode : String?,
    val bucType : String?,
    val rinaSakId : String?,
    val avsenderId : String?,
    val avsenderNavn : String?,
    val avsenderLand : String?,
    val mottakerId : String?,
    val mottakerNavn : String?,
    val mottakerLand : String?,
    val rinaDokumentId : String?,
    val rinaDokumentVersjon : String?,
    val sedType : String?,
    val navBruker : String?,
)