package no.nav.eessi.pensjon.statistikk.services

import io.mockk.every
import io.mockk.mockk
import no.nav.eessi.pensjon.eux.EuxService
import no.nav.eessi.pensjon.pesys.PensjonsinformasjonClient
import no.nav.eessi.pensjon.services.storage.amazons3.S3StorageService
import no.nav.eessi.pensjon.statistikk.models.HendelseType
import no.nav.eessi.pensjon.statistikk.models.SedHendelseRina
import no.nav.eessi.pensjon.statistikk.models.SedType
import no.nav.eessi.pensjon.statistikk.models.StatistikkMeldingInn
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class HendelsesAggregeringsServiceTest {

    var euxService = mockk<EuxService>(relaxed = true)
    var penService = mockk<PensjonsinformasjonClient>(relaxed = true)
    var s3Service = mockk<S3StorageService>(relaxed = true)
    var infoService = HendelsesAggregeringsService(euxService, penService, s3Service)

    @Test
    fun aggregateSedData() {
        val pesysSaksID = "123456"
        val opprettetDato = "2020-01-01"

        every { euxService.getTimeStampFromSedMetaDataInBuc(any(), any()) } returns opprettetDato
        every { euxService.getSakIdFraSed(any(), any()) } returns pesysSaksID

        val sedHendelserina = SedHendelseRina( rinaSakId = "111", rinaDokumentId = "222", sedType = SedType.P2100, navBruker = "010101", sektorKode = "333")
        val sedHendelse = infoService.aggregateSedOpprettetData(sedHendelserina);

        assertEquals(sedHendelse.pesysSakId, pesysSaksID)
        assertEquals(sedHendelse.opprettetDato, opprettetDato)
    }

    @Test
    fun aggregateBucData() {
        val rinaid = "12345"
        val dokumentId = "111111"
        val opprettetDato = "2020-01-01"

        every { euxService.getTimeStampFromSedMetaDataInBuc(any(), any()) } returns opprettetDato

        val statMelding =  StatistikkMeldingInn(hendelseType = HendelseType.OPPRETTBUC,  rinaid = rinaid, dokumentId = dokumentId, vedtaksId = null)
        assertEquals(infoService.aggregateBucData(statMelding).dokumentOpprettetDato, opprettetDato)

    }
}