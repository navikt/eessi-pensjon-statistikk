package no.nav.eessi.pensjon.statistikk.services

import no.nav.eessi.pensjon.eux.EuxService
import no.nav.eessi.pensjon.json.mapAnyToJson
import no.nav.eessi.pensjon.json.mapJsonToAny
import no.nav.eessi.pensjon.json.typeRefs
import no.nav.eessi.pensjon.pesys.PensjonsinformasjonClient
import no.nav.eessi.pensjon.services.storage.amazons3.S3StorageService
import no.nav.eessi.pensjon.statistikk.models.BucOpprettetHendelseUt
import no.nav.eessi.pensjon.statistikk.models.SedHendelse
import no.nav.eessi.pensjon.statistikk.models.StatistikkMeldingInn
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class HendelsesAggregeringsService(private val euxService: EuxService,
                                   private val penService: PensjonsinformasjonClient,
                                   private val s3StorageService: S3StorageService) {

    private val logger = LoggerFactory.getLogger(HendelsesAggregeringsService::class.java)

    fun aggregateSedOpprettetData(meldingInn: StatistikkMeldingInn): SedHendelse? {

        val dokumentOpprettetDato = meldingInn.dokumentId?.let { euxService.getTimeStampFromSedMetaDataInBuc(meldingInn.rinaid, it) }
        val saksId = meldingInn.dokumentId?.let { euxService.getSakIdFraSed(meldingInn.rinaid, it) }

        val sedHendelse = meldingInn.dokumentId?.let { SedHendelse(rinaSakId = meldingInn.rinaid, rinaDokumentId = it) }

        sedHendelse?.pesysSakId = saksId
        sedHendelse?.opprettetDato = dokumentOpprettetDato

        if (sedHendelse != null) {
            lagreSedHendelse(sedHendelse)
        }

        return sedHendelse
    }

    private fun lagreSedHendelse(sedhendelse: SedHendelse) {
        val path = "${sedhendelse.rinaSakId}/${sedhendelse.rinaDokumentId}"
        logger.info("Storing sedhendelse to S3: $path")

        s3StorageService.put(path, mapAnyToJson(sedhendelse))
    }


    fun hentLagretSedhendelse(rinaSakId: String, rinaDokumentId: String): SedHendelse? {
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