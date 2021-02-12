package no.nav.eessi.pensjon.statistikk.models

import no.nav.eessi.pensjon.eux.BucType
import no.nav.eessi.pensjon.eux.SedType

class SedMeldingP6000Ut(dokumentId: String,
                        bucType: BucType,
                        rinaid: String,
                        mottakerLand: List<String>? = null,
                        rinaDokumentVersjon: String,
                        sedType: SedType,
                        pid: String? = null,
                        hendelseType: HendelseType,
                        pesysSakId: String? = null,
                        opprettetTidspunkt: String,
                        vedtaksId: String? = null,
                        bostedsland: String? = null,
                        pensjonsType: String? = null,
                        vedtakStatus: String? = null,
                        bruttoBelop: String? = null,
                        nettoBelop: String? = null,
                        valuta: String? = null,
                        anmodningOmRevurdering: String?
                        ):
    SedMeldingUt(dokumentId,
        bucType,
        rinaid,
        mottakerLand,
        rinaDokumentVersjon,
        sedType,
        pid,
        hendelseType,
        pesysSakId,
        opprettetTidspunkt,
        vedtaksId)
