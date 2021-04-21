package no.nav.eessi.pensjon.statistikk.services

import no.nav.eessi.pensjon.eux.*
import no.nav.eessi.pensjon.json.mapAnyToJson
import no.nav.eessi.pensjon.json.mapJsonToAny
import no.nav.eessi.pensjon.json.toJson
import no.nav.eessi.pensjon.json.typeRefs
import no.nav.eessi.pensjon.s3.S3StorageService
import no.nav.eessi.pensjon.statistikk.models.BucOpprettetMeldingUt
import no.nav.eessi.pensjon.statistikk.models.HendelseType
import no.nav.eessi.pensjon.statistikk.models.SedMeldingP6000Ut
import no.nav.eessi.pensjon.statistikk.models.SedMeldingUt
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Component
class HendelsesAggregeringsService(private val euxService: EuxService,
                                   private val s3StorageService: S3StorageService
) {
    private val offsetTimeDatePattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
    private val logger = LoggerFactory.getLogger(HendelsesAggregeringsService::class.java)

    fun aggregateSedOpprettetData(rinaId: String,
                                  dokumentId: String,
                                  vedtaksId: String?): SedMeldingUt? {
        val sedHendelse = populerSedMeldingUt(rinaId,
            dokumentId,
            vedtaksId,
            HendelseType.SED_OPPRETTET)

        lagreSedHendelse(sedHendelse)

        return sedHendelse
    }

    fun populerSedMeldingUt(
        rinaid: String,
        dokumentId: String,
        vedtaksId: String?,
        hendelseType: HendelseType,
        avsenderLand: String? = null
    ): SedMeldingUt {

        val sed = euxService.getSed(rinaid, dokumentId)
        val bucMetadata = euxService.getBucMetadata(rinaid)
        val mottakerLand = populerMottakerland(bucMetadata!!)
        val beregning  = hentBeregning(sed?.pensjon?.vedtak)

        return when(hendelseType){
            HendelseType.SED_SENDT, HendelseType.SED_MOTTATT ->  SedMeldingP6000Ut(
                rinaid = rinaid,
                dokumentId = dokumentId,
                hendelseType = hendelseType,
                bucType = bucMetadata.processDefinitionName,
                sedType = sed!!.sed,
                pesysSakId = sed.nav.eessisak?.firstOrNull()?.saksnummer,
                pid = sed.nav.bruker?.person?.pin?.firstOrNull { it.land == "NO" }?.identifikator,
                opprettetTidspunkt = getTimeStampFromSedMetaDataInBuc(bucMetadata, dokumentId),
                vedtaksId = vedtaksId,
                mottakerLand = mottakerLand,
                avsenderLand = avsenderLand!!,
                rinaDokumentVersjon = bucMetadata.documents.filter { it.id == dokumentId }[0].versions.size.toString(),
                bostedsland =  sed.nav.bruker?.adresse?.land,
                bruttoBelop = beregning?.beloepBrutto?.beloep,
                nettoBelop = beregning?.beloepNetto?.beloep,
                valuta = beregning?.valuta,
                anmodningOmRevurdering = sed.pensjon?.tilleggsinformasjon?.artikkel48,
                pensjonsType = sed.pensjon?.vedtak?.firstOrNull()?.type,
                vedtakStatus = sed.pensjon?.vedtak?.firstOrNull()?.resultat
            )
            else -> SedMeldingUt(
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
    }

    private fun hentBeregning(vedtak: List<Vedtak>?): Beregning? {
        return vedtak?.firstOrNull()?.beregning?.firstOrNull()
    }

    private fun populerMottakerland(bucMetadata: BucMetadata): List<String> {
        val list : List<Participant> = bucMetadata.documents
            .flatMap { it.conversations }
            .flatMap { it.participants.orEmpty() }
            .filter { it.role == "Receiver" }
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

    fun aggregateBucData(rinaId: String): BucOpprettetMeldingUt {

        logger.info("Aggregering for BUC $rinaId")
        val timeStamp : String?

        val bucMetadata = euxService.getBucMetadata(rinaId)!!
        val bucType = bucMetadata.processDefinitionName
        timeStamp = BucMetadata.offsetTimeStamp(bucMetadata.startDate)
        return BucOpprettetMeldingUt(bucType, HendelseType.BUC_OPPRETTET, rinaId, timeStamp)

    }

    fun getTimeStampFromSedMetaDataInBuc(bucMetadata: BucMetadata, dokumentId : String ) : String {
        val dokument : Document? = bucMetadata.documents.firstOrNull { it.id == dokumentId }

        logger.debug("Dokument: ${dokument?.toJson()}")

        return OffsetDateTime.parse(dokument?.creationDate, DateTimeFormatter.ofPattern(offsetTimeDatePattern)).toString()
    }
}
