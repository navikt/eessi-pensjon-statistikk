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
import no.nav.eessi.pensjon.statistikk.models.SedOpprettetMeldingUt
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Component
class HendelsesAggregeringsService(private val euxService: EuxService,
                                   private val s3StorageService: S3StorageService) {
    private val offsetTimeDatePattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
    private val logger = LoggerFactory.getLogger(HendelsesAggregeringsService::class.java)

    fun aggregateSedOpprettetData(melding: OpprettelseMelding): SedOpprettetMeldingUt? {

        val sed = euxService.getSed(melding.rinaid, melding.dokumentId!!)
        val bucMetadata = euxService.getBucMetadata(melding.rinaid)

        val mottakerLand = populerMottakerland(bucMetadata!!)

        val sedHendelse = SedOpprettetMeldingUt(
            rinaid = melding.rinaid,
            dokumentId = melding.dokumentId,
            hendelseType = HendelseType.SED_SENDT,
            bucType = bucMetadata!!.processDefinitionName,
            sedType = sed?.sed!!,
            pesysSakId = sed.nav.eessisak?.firstOrNull()?.saksnummer,
            pid = sed.nav.bruker?.person?.pin?.firstOrNull { it.land == "NO" }?.identifikator,
            opprettetDato = getTimeStampFromSedMetaDataInBuc(bucMetadata, melding.dokumentId),
            vedtaksId = melding.vedtaksId,
            mottakerLand = mottakerLand
        )

        lagreSedHendelse(sedHendelse)

        return sedHendelse
    }

    private fun populerMottakerland(bucMetadata: BucMetadata): List<String> {
        return bucMetadata.documents
            .flatMap { it.conversations }
            .flatMap { it.participants }
            .map { it.organisation.countryCode }
            .distinct()
    }

    private fun lagreSedHendelse(sedhendelse: SedOpprettetMeldingUt) {
        val path = "${sedhendelse.rinaid}/${sedhendelse.dokumentId}"
        logger.info("Storing sedhendelse to S3: $path")

        s3StorageService.put(path, mapAnyToJson(sedhendelse))
    }


    fun hentLagretSedhendelse(rinaSakId: String, rinaDokumentId: String): SedOpprettetMeldingUt? {
        val path = "$rinaSakId/$rinaDokumentId"
        logger.info("Getting SedhendelseID: $rinaSakId from $path")

        val sedHendelseAsJson = s3StorageService.get(path)
        return sedHendelseAsJson?.let { mapJsonToAny(it, typeRefs()) }
    }


    fun aggregateBucData(opprettelseMelding: OpprettelseMelding): BucOpprettetMeldingUt {

        logger.info("Aggregering for BUC ${opprettelseMelding.rinaid}")
        val timeStamp : String?

        val bucMetadata = euxService.getBucMetadata(opprettelseMelding.rinaid)!!
        val bucType = bucMetadata.processDefinitionName
        timeStamp = BucMetadata.offsetTimeStamp(bucMetadata.startDate)
        return BucOpprettetMeldingUt(bucType, HendelseType.BUC_OPPRETTET, opprettelseMelding.rinaid, timeStamp)

    }

    fun getTimeStampFromSedMetaDataInBuc(bucMetadata: BucMetadata, dokumentId : String ) : String {
        val dokument : Document? = bucMetadata.documents.firstOrNull { it.id == dokumentId }

        logger.debug("Dokument: ${dokument?.toJson()}")

        return OffsetDateTime.parse(dokument?.creationDate, DateTimeFormatter.ofPattern(offsetTimeDatePattern)).toString()
    }
}