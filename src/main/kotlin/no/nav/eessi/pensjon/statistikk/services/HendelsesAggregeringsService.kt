package no.nav.eessi.pensjon.statistikk.services

import no.nav.eessi.pensjon.eux.BucMetadata
import no.nav.eessi.pensjon.eux.Document
import no.nav.eessi.pensjon.eux.EuxService
import no.nav.eessi.pensjon.json.mapAnyToJson
import no.nav.eessi.pensjon.json.mapJsonToAny
import no.nav.eessi.pensjon.json.toJson
import no.nav.eessi.pensjon.json.typeRefs
import no.nav.eessi.pensjon.services.storage.amazons3.S3StorageService
import no.nav.eessi.pensjon.statistikk.models.BucOpprettetMeldingUt
import no.nav.eessi.pensjon.statistikk.models.HendelseType
import no.nav.eessi.pensjon.statistikk.models.OpprettelseMelding
import no.nav.eessi.pensjon.statistikk.models.SedHendelse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Component
class HendelsesAggregeringsService(private val euxService: EuxService,
                                   private val s3StorageService: S3StorageService) {
    private val offsetTimeDatePattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
    private val logger = LoggerFactory.getLogger(HendelsesAggregeringsService::class.java)

    fun aggregateSedOpprettetData(melding: OpprettelseMelding): SedHendelse? {

        val sedHendelse = melding.dokumentId?.let {
            SedHendelse(rinaSakId = melding.rinaid, rinaDokumentId = it, hendelseType = HendelseType.SED_SENDT)
        }

        val sed = sedHendelse?.let { euxService.getSed(it.rinaSakId, sedHendelse.rinaDokumentId) }

        sedHendelse?.apply {
            this.pesysSakId = sed?.nav?.eessisak?.firstOrNull()?.saksnummer
            this.navBruker = sed?.nav?.bruker.toString()

            val bucMetadata = euxService.getBucMetadata(rinaSakId)
            this.opprettetDato = bucMetadata?.let { getTimeStampFromSedMetaDataInBuc(it, rinaDokumentId) }

            this.vedtaksId = melding.vedtaksId
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


    fun aggregateBucData(opprettelseMelding: OpprettelseMelding): BucOpprettetMeldingUt {

        logger.info("Aggregering for BUC ${opprettelseMelding.rinaid}")
        val timeStamp : String?

        val bucMetadata = euxService.getBucMetadata(opprettelseMelding.rinaid)!!
        val bucType = bucMetadata.processDefinitionName
        timeStamp = BucMetadata.offsetTimeStamp(bucMetadata.startDate)
        return BucOpprettetMeldingUt(bucType, HendelseType.BUC_OPPRETTET, opprettelseMelding.rinaid, opprettelseMelding.dokumentId, timeStamp)

    }

    fun getTimeStampFromSedMetaDataInBuc(bucMetadata: BucMetadata, dokumentId : String ) : String {
        val dokument : Document? = bucMetadata.documents.firstOrNull { it.id == dokumentId }

        logger.debug("Dokument: ${dokument?.toJson()}")

        return OffsetDateTime.parse(dokument?.creationDate, DateTimeFormatter.ofPattern(offsetTimeDatePattern)).toString()
    }
}