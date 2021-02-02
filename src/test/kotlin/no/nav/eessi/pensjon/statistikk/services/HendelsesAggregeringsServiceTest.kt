package no.nav.eessi.pensjon.statistikk.services

import io.mockk.every
import io.mockk.mockk
import no.nav.eessi.pensjon.eux.EuxService
import no.nav.eessi.pensjon.eux.Nav
import no.nav.eessi.pensjon.eux.Sak
import no.nav.eessi.pensjon.eux.Sed
import no.nav.eessi.pensjon.services.storage.amazons3.S3StorageService
import no.nav.eessi.pensjon.statistikk.models.HendelseType
import no.nav.eessi.pensjon.statistikk.models.OpprettelseMelding
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class HendelsesAggregeringsServiceTest {

    var euxService = mockk<EuxService>(relaxed = true)
    var s3Service = mockk<S3StorageService>(relaxed = true)
    var infoService = HendelsesAggregeringsService(euxService,  s3Service)

    @Test
    fun aggregateSedData() {
        val pesysSaksID = "123456"
        val opprettetDato = "2020-01-01"
        val dokumentId = "222"
        val rinaid = "111"
        val vedtaksId = "333"

        every { euxService.getTimeStampFromSedMetaDataInBuc(any(), any()) } returns opprettetDato
        every { euxService.getSed(any(), any()) } returns Sed(Nav(null, listOf(Sak("", pesysSaksID))))

        val melding = OpprettelseMelding(rinaid = rinaid, dokumentId = dokumentId, hendelseType = HendelseType.BUC_OPPRETTET, vedtaksId = vedtaksId)
        val sedHendelse = infoService.aggregateSedOpprettetData(melding);

        assertEquals(sedHendelse?.rinaSakId, rinaid)
        assertEquals(sedHendelse?.rinaDokumentId, dokumentId)
        assertEquals(sedHendelse?.pesysSakId, pesysSaksID)
        assertEquals(sedHendelse?.opprettetDato, opprettetDato)
        assertEquals(sedHendelse?.vedtaksId, vedtaksId)

    }

    private fun hentSaksNummer(sed: Sed?) =
        sed?.nav?.eessisak?.firstOrNull { sak -> sak?.land == "NO" }?.saksnummer
}