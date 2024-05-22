package no.nav.eessi.pensjon.statistikk.services

import no.nav.eessi.pensjon.eux.Beregning
import no.nav.eessi.pensjon.eux.BucMetadata
import no.nav.eessi.pensjon.eux.Document
import no.nav.eessi.pensjon.eux.EuxService
import no.nav.eessi.pensjon.eux.Participant
import no.nav.eessi.pensjon.eux.Vedtak
import no.nav.eessi.pensjon.eux.model.SedType
import no.nav.eessi.pensjon.gcp.GcpStorageService
import no.nav.eessi.pensjon.statistikk.models.BucOpprettetMeldingUt
import no.nav.eessi.pensjon.statistikk.models.HendelseType
import no.nav.eessi.pensjon.statistikk.models.PensjonsType
import no.nav.eessi.pensjon.statistikk.models.SedMeldingP6000Ut
import no.nav.eessi.pensjon.statistikk.models.SedMeldingUt
import no.nav.eessi.pensjon.statistikk.models.VedtakStatus
import no.nav.eessi.pensjon.utils.mapAnyToJson
import no.nav.eessi.pensjon.utils.mapJsonToAny
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class HendelsesAggregeringsService(private val euxService: EuxService,
                                   private val gcpStorageService: GcpStorageService
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
        rinaId: String,
        dokumentId: String,
        vedtaksId: String?,
        hendelseType: HendelseType,
        avsenderLand: String? = null
    ): SedMeldingUt {

        val sed = euxService.getSed(rinaId, dokumentId)
        val bucMetadata = euxService.getBucMetadata(rinaId)
        val mottakerLand = populerMottakerland(bucMetadata!!)
        val beregning  = hentBeregning(sed?.pensjon?.vedtak)

        logger.info("Oppretter melding med bucmetadata med size: ${bucMetadata.documents.size}")

        return when(hendelseType){
            HendelseType.SED_SENDT, HendelseType.SED_MOTTATT ->  SedMeldingP6000Ut(
                dokumentId = dokumentId,
                bucType = bucMetadata.processDefinitionName,
                rinaId = rinaId,
                mottakerLand = mottakerLand,
                avsenderLand = avsenderLand!!,
                rinaDokumentVersjon = getDocumentVersion(bucMetadata.documents, dokumentId),
                sedType = sed!!.sed,
                // pin sendes kun for P6000
                pid = sed.nav.bruker?.person?.pin?.firstOrNull { it.land == "NO" }?.identifikator.takeIf { sed.sed == SedType.P6000 },
                hendelseType = hendelseType,
                pesysSakId = sed.nav.eessisak?.firstOrNull { it?.land == "NO" }?.saksnummer,
                opprettetTidspunkt = getCreationDateFromSedMetaData(bucMetadata, dokumentId),
                vedtaksId = vedtaksId,
                bostedsland = sed.nav.bruker?.adresse?.land,
                pensjonsType = PensjonsType.fra(sed.pensjon?.vedtak?.firstOrNull().let { it?.type }),
                vedtakStatus = VedtakStatus.fra(sed.pensjon?.vedtak?.firstOrNull().let { it?.resultat }),
                // brutto sendes kun for utgående
                bruttoBelop = beregning?.beloepBrutto?.beloep.takeIf { hendelseType == HendelseType.SED_SENDT },
                // valuta sendes kun for utgående
                valuta = beregning?.valuta.takeIf { hendelseType == HendelseType.SED_SENDT },
                anmodningOmRevurdering = sed.pensjon?.tilleggsinformasjon?.artikkel48,

                )
            else -> SedMeldingUt(
                rinaId = rinaId,
                dokumentId = dokumentId,
                hendelseType = hendelseType,
                bucType = bucMetadata.processDefinitionName,
                sedType = sed?.sed!!,
                pesysSakId = sed.nav.eessisak?.firstOrNull { it?.land == "NO" }?.saksnummer,
                pid = null, // ikke obligatorisk for andre enn P6000
                opprettetTidspunkt = getCreationDateFromSedMetaData(bucMetadata, dokumentId),
                vedtaksId = vedtaksId,
                avsenderLand = avsenderLand,
                mottakerLand = mottakerLand,
                rinaDokumentVersjon = getDocumentVersion(bucMetadata.documents, dokumentId)
            )
        }
    }

    private fun getDocumentVersion(documents: List<Document>?, dokumentId: String): String {
        if(documents.isNullOrEmpty()){
            return ""
        }
        return documents.filter { it.id == dokumentId }[0].versions.size.toString()
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
        val path = "${sedhendelse.rinaId}/${sedhendelse.dokumentId}"
        logger.info("Storing sedhendelse to S3: $path")

        gcpStorageService.lagre(path, mapAnyToJson(sedhendelse))
    }


    fun hentVedtaksId(rinaSakId: String, rinaDokumentId: String): String? {
        val path = "$rinaSakId/$rinaDokumentId"
        logger.info("Henter SedhendelseID: $rinaSakId fra $path")

        // Hack: vi hadde liten 'i' i rinaid - i noen tilfeller - og når konsistent valgte stor I hadde vi fortsatt noen som var lagret med liten i ...
        val sedHendelseAsJson = gcpStorageService.hent(path)?.replace("\"rinaid\"", "\"rinaId\"")

        val hendelse = sedHendelseAsJson?.let { mapJsonToAny<SedMeldingUt>(it) }
        logger.info("sedHendelse etter mapping: dokumentId='${hendelse?.dokumentId}', bucType=${hendelse?.bucType}, rinaId='${hendelse?.rinaId}'}")

        return hendelse?.vedtaksId ?: ""
    }

    fun aggregateBucData(rinaId: String): BucOpprettetMeldingUt {

        logger.info("Aggregering for BUC $rinaId")

        val bucMetadata = euxService.getBucMetadata(rinaId)!!
        val bucType = bucMetadata.processDefinitionName
        return BucOpprettetMeldingUt(bucType, HendelseType.BUC_OPPRETTET, rinaId, bucMetadata.startDate)

    }

    fun getCreationDateFromSedMetaData(bucMetadata: BucMetadata, dokumentId : String ) : String {
        val dokument : Document? = bucMetadata.documents.firstOrNull { it.id == dokumentId }
        return dokument?.creationDate ?: ""
    }
}
