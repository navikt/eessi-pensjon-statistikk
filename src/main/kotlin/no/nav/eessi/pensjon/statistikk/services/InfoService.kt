package no.nav.eessi.pensjon.statistikk.services

import no.nav.eessi.pensjon.eux.EuxService
import no.nav.eessi.pensjon.json.toJson
import no.nav.eessi.pensjon.pesys.PensjonsinformasjonClient
import no.nav.eessi.pensjon.statistikk.models.BucOpprettetHendelseUt
import no.nav.eessi.pensjon.statistikk.models.SedHendelse
import no.nav.eessi.pensjon.statistikk.models.SedHendelseRina
import no.nav.eessi.pensjon.statistikk.models.StatistikkMeldingInn
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class InfoService(private val euxService: EuxService,
                  private val penService: PensjonsinformasjonClient) {

    private val logger = LoggerFactory.getLogger(InfoService::class.java)

    fun aggregateSedData(sedHendelseRina: SedHendelseRina): SedHendelse {

        val sedhendelse = SedHendelse.fromJson(sedHendelseRina.toJson())

        val dokumentOpprettetDato = euxService.getTimeStampFromSedMetaDataInBuc(sedHendelseRina.rinaSakId, sedHendelseRina.rinaDokumentId)
        val saksId = euxService.getSakIdFraSed(sedHendelseRina.rinaSakId, sedHendelseRina.rinaDokumentId)

        //sedhendelse.vedtaksId = finnVedtaksId(sedHendelseRina, saksId)
        sedhendelse.pesysSakId = saksId
        sedhendelse.opprettetDato = dokumentOpprettetDato

        return sedhendelse
    }

    fun aggregateBucData(statistikkMeldingInn: StatistikkMeldingInn): BucOpprettetHendelseUt {
        val dokumentOpprettetDato = euxService.getTimeStampFromSedMetaDataInBuc(statistikkMeldingInn.rinaid, statistikkMeldingInn.dokumentId!!)

        //mangler gyldig opprettetdato, avslutter
        if(dokumentOpprettetDato == null){
            logger.error("Finner ikke opprettetdato for RinaId: ${statistikkMeldingInn.dokumentId}")
            throw RuntimeException("Klarte ikke å hente opprettetDato for BUC")
        }
        //sed opprettet
        return BucOpprettetHendelseUt(statistikkMeldingInn, dokumentOpprettetDato)
    }
}