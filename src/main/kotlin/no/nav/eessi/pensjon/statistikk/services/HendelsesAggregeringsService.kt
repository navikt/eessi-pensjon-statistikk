package no.nav.eessi.pensjon.statistikk.services

import no.nav.eessi.pensjon.eux.BucMetadata
import no.nav.eessi.pensjon.eux.Document
import no.nav.eessi.pensjon.eux.EuxService
import no.nav.eessi.pensjon.eux.Participant
import no.nav.eessi.pensjon.json.mapAnyToJson
import no.nav.eessi.pensjon.json.mapJsonToAny
import no.nav.eessi.pensjon.json.toJson
import no.nav.eessi.pensjon.json.typeRefs
import no.nav.eessi.pensjon.services.storage.amazons3.S3StorageService
import no.nav.eessi.pensjon.statistikk.models.BucOpprettetMeldingUt
import no.nav.eessi.pensjon.statistikk.models.HendelseType
import no.nav.eessi.pensjon.statistikk.models.OpprettelseMelding
import no.nav.eessi.pensjon.statistikk.models.SedMeldingUt
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Component
class HendelsesAggregeringsService(private val euxService: EuxService,
                                   private val s3StorageService: S3StorageService) {
    private val offsetTimeDatePattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
    private val logger = LoggerFactory.getLogger(HendelsesAggregeringsService::class.java)

    fun aggregateSedOpprettetData(melding: OpprettelseMelding): SedMeldingUt? {
        val sedHendelse = populerSedMeldingUt(melding.rinaid,
            melding.dokumentId!!,
            melding.vedtaksId,
            HendelseType.SED_OPPRETTET)
        lagreSedHendelse(sedHendelse)

        return sedHendelse
    }

    fun populerSedMeldingUt(rinaid: String, dokumentId: String, vedtaksId: String?, hendelseType: HendelseType): SedMeldingUt {

        val sed = euxService.getSed(rinaid, dokumentId)
        val bucMetadata = euxService.getBucMetadata(rinaid)
        val mottakerLand = populerMottakerland(bucMetadata!!)

        return SedMeldingUt(
            rinaid = rinaid,
            dokumentId = dokumentId,
            hendelseType = hendelseType,
            bucType = bucMetadata.processDefinitionName,
            sedType = sed?.sed!!,
            pesysSakId = sed.nav.eessisak?.firstOrNull()?.saksnummer,
            pid = sed.nav.bruker?.person?.pin?.firstOrNull { it.land == "NO" }?.identifikator,
            opprettetTidspunkt = getTimeStampFromSedMetaDataInBuc(bucMetadata, dokumentId),
            vedtaksId = vedtaksId,
            mottakerLand = mottakerLand,
            rinaDokumentVersjon = bucMetadata.documents.filter { it.id == dokumentId }[0].versions.size.toString()
        )
    }

    private fun populerMottakerland(bucMetadata: BucMetadata): List<String> {
        val list : List<Participant> = bucMetadata.documents
            .flatMap { it.conversations }
            .flatMap { it.participants.orEmpty() }
            .toList()

        return list.map { it.organisation.countryCode }.distinct()
    }

    private fun lagreSedHendelse(sedhendelse: SedMeldingUt) {
        val path = "${sedhendelse.rinaid}/${sedhendelse.dokumentId}"
        logger.info("Storing sedhendelse to S3: $path")

        s3StorageService.put(path, mapAnyToJson(sedhendelse))
    }


    fun hentVedtaksId(rinaSakId: String, rinaDokumentId: String): String? {
        val path = "$rinaSakId/$rinaDokumentId"
        logger.info("Getting SedhendelseID: $rinaSakId from $path")

        val sedHendelseAsJson = s3StorageService.get(path)

        return sedHendelseAsJson?.let { mapJsonToAny(it, typeRefs<SedMeldingUt>()) }?.vedtaksId
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