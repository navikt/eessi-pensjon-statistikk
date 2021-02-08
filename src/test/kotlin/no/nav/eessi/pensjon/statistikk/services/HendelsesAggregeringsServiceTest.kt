package no.nav.eessi.pensjon.statistikk.services

import io.mockk.every
import io.mockk.mockk
import no.nav.eessi.pensjon.eux.BucMetadata
import no.nav.eessi.pensjon.eux.BucType
import no.nav.eessi.pensjon.eux.Conversation
import no.nav.eessi.pensjon.eux.Document
import no.nav.eessi.pensjon.eux.EuxService
import no.nav.eessi.pensjon.eux.Nav
import no.nav.eessi.pensjon.eux.Organisation
import no.nav.eessi.pensjon.eux.Participant
import no.nav.eessi.pensjon.eux.Sak
import no.nav.eessi.pensjon.eux.Sed
import no.nav.eessi.pensjon.eux.SedType
import no.nav.eessi.pensjon.services.storage.amazons3.S3StorageService
import no.nav.eessi.pensjon.statistikk.models.OpprettelseMelding
import no.nav.eessi.pensjon.statistikk.models.OpprettelseType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class HendelsesAggregeringsServiceTest {

    var euxService = mockk<EuxService>(relaxed = true)
    var s3Service = mockk<S3StorageService>(relaxed = true)
    var infoService = HendelsesAggregeringsService(euxService,  s3Service)

    @Test
    fun aggregateSedData() {
        val pesysSaksID = "123456"
        val dokumentId = "222"
        val rinaid = "111"
        val vedtaksId = "333"
        val mottakerland = listOf("NO")

        every { euxService.getBucMetadata(any())} returns BucMetadata ("", "",
            listOf(Document(dokumentId,
                "2020-12-08T09:52:55.345+0000",
                listOf(Conversation(listOf(
                    Participant(Organisation("NO")),
                    Participant(Organisation("NO"))
                ))))),
            BucType.P_BUC_01, "2020-12-08T09:52:55.345+0000")
        every { euxService.getSed(any(), any()) } returns Sed(Nav(null, listOf(Sak("", pesysSaksID))), sed = SedType.P2100)

        val melding = OpprettelseMelding(rinaid = rinaid, dokumentId = dokumentId, opprettelseType = OpprettelseType.SED, vedtaksId = vedtaksId)
        val sedOpprettetMeldingUt = infoService.aggregateSedOpprettetData(melding);

        assertEquals(sedOpprettetMeldingUt?.rinaid, rinaid)
        assertEquals(sedOpprettetMeldingUt?.dokumentId, dokumentId)
        assertEquals(sedOpprettetMeldingUt?.pesysSakId, pesysSaksID)
        assertEquals(sedOpprettetMeldingUt?.opprettetTidspunkt, "2020-12-08T09:52:55.345Z")
        assertEquals(sedOpprettetMeldingUt?.vedtaksId, vedtaksId)
        assertEquals(sedOpprettetMeldingUt?.mottakerLand, mottakerland)
    }
}