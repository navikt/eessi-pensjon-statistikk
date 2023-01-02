package no.nav.eessi.pensjon.eux
import io.mockk.every
import io.mockk.mockk
import no.nav.eessi.pensjon.ResourceHelper.Companion.getResourceBucMetadata
import no.nav.eessi.pensjon.ResourceHelper.Companion.getResourceSed
import no.nav.eessi.pensjon.eux.klient.EuxKlientLib
import no.nav.eessi.pensjon.gcp.GcpStorageService
import no.nav.eessi.pensjon.statistikk.services.HendelsesAggregeringsService
import no.nav.eessi.pensjon.utils.toJson
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.client.RestTemplate

internal class EuxServiceTest {

    protected var euxRestTemplate: RestTemplate = mockk()

    protected var gcpStorageService: GcpStorageService  = mockk()

    lateinit var euxService: EuxService

    @BeforeEach
    fun before() {
        val euxKlient  = EuxKlientLib(euxRestTemplate)
        euxService = EuxService(euxKlient)
    }

    @Test
    fun `Se timestamp konverters fra zone til offsettDateTime`() {
        val bucJson = getResourceBucMetadata("buc/bucMedP2000.json").toJson()
        val mockEuxRinaid = "123456"
        val mockEuxDocumentId = "d740047e730f475aa34ae59f62e3bb99"

        every { euxRestTemplate.getForObject("/buc/$mockEuxRinaid", String::class.java)} returns bucJson

        val metaData = euxService.getBucMetadata(mockEuxRinaid)
        val offsetDateTime =
            metaData?.let { HendelsesAggregeringsService(euxService, gcpStorageService).getCreationDateFromSedMetaData(it, mockEuxDocumentId) }

        assertEquals("2020-12-08T09:53:36.241+0000", offsetDateTime)
     }

    @Test
    fun  `Gitt en SED når norskSakId er utfylt så returner norsk saksId`() {
        val sedJson = getResourceSed("sed/P2000-minimal-med-en-norsk-sakId.json").toJson()
        val mockEuxRinaid = "123456"
        val mockEuxDocumentId = "d740047e730f475aa34ae59f62e3bb99"

        every { euxRestTemplate.getForObject(eq("/buc/$mockEuxRinaid/sed/$mockEuxDocumentId"), eq(String::class.java))} returns sedJson

        val sed = euxService.getSed(mockEuxRinaid, mockEuxDocumentId)
        val saksNummer = hentSaksNummer(sed)

        assertEquals("123456", saksNummer)
    }

    @Test
    fun `Gitt en SED når norskSakId ikke er utfylt så returner null`() {
        val sedJson = getResourceSed("sed/P2000-minimal-med-kun-utenlandsk-sakId.json").toJson()
        val mockEuxRinaid = "123456"
        val mockEuxDocumentId = "d740047e730f475aa34ae59f62e3bb99"

        every { euxRestTemplate.getForObject("/buc/$mockEuxRinaid/sed/$mockEuxDocumentId", String::class.java)} returns sedJson

        val sed = euxService.getSed(mockEuxRinaid, mockEuxDocumentId)
        val sakId = hentSaksNummer(sed)

        assertEquals(null, sakId)
    }

    private fun hentSaksNummer(sed: Sed?) =
        sed?.nav?.eessisak?.firstOrNull { sak -> sak?.land == "NO" }?.saksnummer

    @Test
    fun `Gitt en SED når ingen sakId er utfylt så returner null`() {
        val sedJson = getResourceSed("sed/P2000-minimal-uten-sakId.json").toJson()
        val mockEuxRinaid = "123456"
        val mockEuxDocumentId = "d740047e730f475aa34ae59f62e3bb99"

        every { euxRestTemplate.getForObject(eq("/buc/$mockEuxRinaid/sed/$mockEuxDocumentId"), eq(String::class.java))} returns  sedJson

        val sed = euxService.getSed(mockEuxRinaid, mockEuxDocumentId)
        val sakId = hentSaksNummer(sed)

        assertEquals(null, sakId)
    }
}


