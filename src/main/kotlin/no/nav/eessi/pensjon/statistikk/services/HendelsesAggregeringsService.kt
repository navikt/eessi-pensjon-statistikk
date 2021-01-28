package no.nav.eessi.pensjon.statistikk.services

import no.nav.eessi.pensjon.eux.EuxService
import no.nav.eessi.pensjon.json.mapAnyToJson
import no.nav.eessi.pensjon.json.mapJsonToAny
import no.nav.eessi.pensjon.json.toJson
import no.nav.eessi.pensjon.json.typeRefs
import no.nav.eessi.pensjon.pesys.PensjonsinformasjonClient
import no.nav.eessi.pensjon.services.storage.amazons3.S3StorageService
import no.nav.eessi.pensjon.statistikk.models.BucOpprettetHendelseUt
import no.nav.eessi.pensjon.statistikk.models.SedHendelse
import no.nav.eessi.pensjon.statistikk.models.SedHendelseRina
import no.nav.eessi.pensjon.statistikk.models.StatistikkMeldingInn
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class HendelsesAggregeringsService(private val euxService: EuxService,
                                   private val penService: PensjonsinformasjonClient,
                                   private val s3StorageService: S3StorageService) {

    private val logger = LoggerFactory.getLogger(HendelsesAggregeringsService::class.java)

    fun aggregateSedOpprettetData(sedHendelseRina: SedHendelseRina): SedHendelse {

        val sedhendelse = SedHendelse.fromJson(sedHendelseRina.toJson())

        val dokumentOpprettetDato = euxService.getTimeStampFromSedMetaDataInBuc(sedHendelseRina.rinaSakId, sedHendelseRina.rinaDokumentId)
        val saksId = euxService.getSakIdFraSed(sedHendelseRina.rinaSakId, sedHendelseRina.rinaDokumentId)

        sedhendelse.pesysSakId = saksId
        sedhendelse.opprettetDato = dokumentOpprettetDato

        lagreSedHendelse(sedhendelse)

        return sedhendelse
    }

    fun aggregateSedSendtData(sedHendelse: SedHendelseRina) {
        logger.info("Henter vedtakId")
      //  val lagretSedHendelse = hentLagretSedhendelse(sedHendelse.rinaSakId, sedHendelse.rinaDokumentId)

     //   return sedHendelse
    }

    private fun lagreSedHendelse(sedhendelse: SedHendelse) {
        val path = "${sedhendelse.rinaSakId}/${sedhendelse.rinaDokumentId}"
        logger.info("Storing sedhendelse to S3: $path")

        s3StorageService.put(path, mapAnyToJson(sedhendelse))
    }


    private fun hentLagretSedhendelse(rinaSakId: String, rinaDokumentId: String): SedHendelse? {
        val path = "$rinaSakId/$rinaDokumentId"
        logger.info("Getting SedhendelseID: ${rinaSakId} from $path")

        val sedHendelseAsJson = s3StorageService.get(path)
        return sedHendelseAsJson?.let { mapJsonToAny(it, typeRefs()) }
    }


    fun aggregateBucData(statistikkMeldingInn: StatistikkMeldingInn): BucOpprettetHendelseUt {
        //TODO: se paa dato
        logger.info("Aggregering for BUC ${statistikkMeldingInn.rinaid}")

        return BucOpprettetHendelseUt(statistikkMeldingInn, null)
    }
}