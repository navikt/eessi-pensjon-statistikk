package no.nav.eessi.pensjon.statistikk.services

import io.mockk.every
import io.mockk.mockk
import no.nav.eessi.pensjon.ResourceHelper
import no.nav.eessi.pensjon.eux.BucMetadata
import no.nav.eessi.pensjon.eux.EuxService
import no.nav.eessi.pensjon.eux.Sed
import no.nav.eessi.pensjon.eux.model.SedType
import no.nav.eessi.pensjon.eux.model.buc.BucType
import no.nav.eessi.pensjon.gcp.GcpStorageService
import no.nav.eessi.pensjon.json.toJson
import no.nav.eessi.pensjon.statistikk.models.HendelseType
import no.nav.eessi.pensjon.statistikk.models.PensjonsType
import no.nav.eessi.pensjon.statistikk.models.SedMeldingP6000Ut
import no.nav.eessi.pensjon.statistikk.models.SedMeldingUt
import no.nav.eessi.pensjon.statistikk.models.VedtakStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class HendelsesAggregeringsServiceTest {

    private var euxService = mockk<EuxService>(relaxed = true)
    private var s3Service = mockk<GcpStorageService>(relaxed = true)
    private var infoService = HendelsesAggregeringsService(euxService,  s3Service)

    @Test
    fun `gitt en sedOpprettet melding så populer sedOpprettetMeldingUt`() {
        val pesysSaksID = "22929783"
        val dokumentId = "d740047e730f475aa34ae59f62e3bb99"
        val rinaSakId = "111"
        val vedtaksId = "333"
        val mottakerland = listOf("NO")

        //Mocker BUC
        val bucJson = ResourceHelper.getResourceBucMetadata("buc/bucMedP2000.json").toJson()
        val buc = BucMetadata.fromJson(bucJson)
        every { euxService.getBucMetadata(any())} returns buc

        // Mocker SED
        val jsonSed = ResourceHelper.getResourceSed("sed/P2000-preutfylt-fnr-og-sakid.json").toJson()
        val sed = Sed.fromJson(jsonSed)
        every { euxService.getSed(any(), any()) } returns sed

        val sedOpprettetMeldingUt = infoService.aggregateSedOpprettetData(rinaSakId, dokumentId, vedtaksId)

        assertEquals(SedMeldingUt(
            dokumentId = dokumentId,
            bucType = BucType.P_BUC_01,
            rinaId = rinaSakId,
            mottakerLand = mottakerland,
            avsenderLand = null, // finnes ikke for opprettetmelding (er vel alltid Norge)
            rinaDokumentVersjon = "2",
            sedType = SedType.P2000,
            pid = "08035325493",
            hendelseType = HendelseType.SED_OPPRETTET,
            pesysSakId = pesysSaksID,
            opprettetTidspunkt = "2020-12-08T10:53:36.241",
            vedtaksId = vedtaksId), sedOpprettetMeldingUt)
    }

    @Test
    fun `gitt en sedOpprettet for P6000 melding så populer SedMeldingUt`() {
        //Mocker BUC
        val bucJson = ResourceHelper.getResourceBucMetadata("buc/bucMedP6000.json").toJson()
        val buc = BucMetadata.fromJson(bucJson)
        every { euxService.getBucMetadata(any())} returns buc

        // Mocker SED
        val p6000Json = ResourceHelper.getResourceSed("sed/P6000-komplett.json").toJson()
        val p6000 = Sed.fromJson(p6000Json)
        every { euxService.getSed(any(), any()) } returns p6000

        val dokumentId = "08e5310500a94640abfb309e481ca319"
        val rinaSakId = "1271728"
        val mottakerland = listOf("NO")

        val sedOpprettetMeldingUt = infoService.aggregateSedOpprettetData(rinaSakId, dokumentId, null)

        assertEquals(SedMeldingUt(
            dokumentId = dokumentId,
            bucType = BucType.P_BUC_06,
            rinaId = rinaSakId,
            mottakerLand = mottakerland,
            avsenderLand = null, // finnes ikke for opprettetmelding (er vel alltid Norge)
            rinaDokumentVersjon = "5",
            sedType = SedType.P6000,
            pid = "09028020144",
            hendelseType = HendelseType.SED_OPPRETTET,
            pesysSakId = "22919968",
            opprettetTidspunkt = "2021-02-11T14:08:29.914",
            vedtaksId = null
        ), sedOpprettetMeldingUt)
    }

    @Test
    fun `gitt en sed sendt for P6000 melding så populer SedMeldingP6000Ut `() {
        //Mocker BUC
        val bucJson = ResourceHelper.getResourceBucMetadata("buc/bucMedP6000.json").toJson()
        val buc = BucMetadata.fromJson(bucJson)
        every { euxService.getBucMetadata(any())} returns buc

        // Mocker SED
        val p6000Json = ResourceHelper.getResourceSed("sed/P6000-komplett.json").toJson()
        val p6000 = Sed.fromJson(p6000Json)
        every { euxService.getSed(any(), any()) } returns p6000

        val dokumentId = "08e5310500a94640abfb309e481ca319"
        val rinaSakId = "1271728"
        val mottakerland = listOf("NO")

        val sedOpprettetMeldingUt = infoService.populerSedMeldingUt(rinaSakId, dokumentId, null, HendelseType.SED_SENDT, "SE") as SedMeldingP6000Ut

        assertEquals(SedMeldingP6000Ut(
            dokumentId = dokumentId,
            bucType = BucType.P_BUC_06,
            rinaId = rinaSakId,
            mottakerLand = mottakerland,
            avsenderLand = "SE",
            rinaDokumentVersjon = "5",
            sedType = SedType.P6000,
            pid = "09028020144",
            hendelseType = HendelseType.SED_SENDT,
            pesysSakId = "22919968",
            opprettetTidspunkt = "2021-02-11T14:08:29.914",
            vedtaksId = null,
            bostedsland = "HR",
            pensjonsType = PensjonsType.GJENLEV,
            vedtakStatus = VedtakStatus.FORELOPIG_UTBETALING,
            bruttoBelop = "12482",
            valuta = "NOK",
            anmodningOmRevurdering = "1"
        ), sedOpprettetMeldingUt)
    }
}