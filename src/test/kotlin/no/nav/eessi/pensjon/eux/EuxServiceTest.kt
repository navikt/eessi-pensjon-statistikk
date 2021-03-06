package no.nav.eessi.pensjon.eux
import io.mockk.every
import io.mockk.mockk
import no.nav.eessi.pensjon.ResourceHelper.Companion.getResourceBucMetadata
import no.nav.eessi.pensjon.ResourceHelper.Companion.getResourceSed
import no.nav.eessi.pensjon.s3.S3StorageService
import no.nav.eessi.pensjon.statistikk.services.HendelsesAggregeringsService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.client.RestTemplate

internal class EuxServiceTest {

    protected var euxOidcRestTemplate: RestTemplate = mockk()

    protected var s3StorageService: S3StorageService  = mockk()

    lateinit var euxService: EuxService

    @BeforeEach
    fun before() {
        euxService = EuxService(EuxKlient(euxOidcRestTemplate))
    }

    @Test
    fun `Se timestamp konverters fra zone til offsettDateTime`() {
        val gyldigBuc : BucMetadata = getResourceBucMetadata("buc/bucMedP2000.json")
        val mockEuxRinaid = "123456"
        val mockEuxDocumentId = "d740047e730f475aa34ae59f62e3bb99"

        every { euxOidcRestTemplate.getForObject("/buc/$mockEuxRinaid", BucMetadata::class.java)} returns  gyldigBuc

        val metaData = euxService.getBucMetadata(mockEuxRinaid)
        val offsetDateTime =
            metaData?.let { HendelsesAggregeringsService(euxService, s3StorageService).getTimeStampFromSedMetaDataInBuc(it, mockEuxDocumentId) }

        assertEquals("2020-12-08T09:53:36.241Z", offsetDateTime)
     }

    @Test
    fun  `Gitt en SED når norskSakId er utfylt så returner norsk saksId`() {
        val gyldigBuc : Sed = getResourceSed("sed/P2000-minimal-med-en-norsk-sakId.json")
        val mockEuxRinaid = "123456"
        val mockEuxDocumentId = "d740047e730f475aa34ae59f62e3bb99"

        every { euxOidcRestTemplate.getForObject(eq("/buc/$mockEuxRinaid/sed/$mockEuxDocumentId"), eq(Sed::class.java))} returns  gyldigBuc

        val sed = euxService.getSed(mockEuxRinaid, mockEuxDocumentId)
        val saksNummer = hentSaksNummer(sed)

        assertEquals("123456", saksNummer)
    }

    @Test
    fun `Gitt en SED når norskSakId ikke er utfylt så returner null`() {
        val gyldigBuc = getResourceSed("sed/P2000-minimal-med-kun-utenlandsk-sakId.json")
        val mockEuxRinaid = "123456"
        val mockEuxDocumentId = "d740047e730f475aa34ae59f62e3bb99"

        every { euxOidcRestTemplate.getForObject("/buc/$mockEuxRinaid/sed/$mockEuxDocumentId", Sed::class.java)} returns  gyldigBuc

        val sed = euxService.getSed(mockEuxRinaid, mockEuxDocumentId)
        val sakId = hentSaksNummer(sed)

        assertEquals(null, sakId)
    }

    private fun hentSaksNummer(sed: Sed?) =
        sed?.nav?.eessisak?.firstOrNull { sak -> sak?.land == "NO" }?.saksnummer

    @Test
    fun `Gitt en SED når ingen sakId er utfylt så returner null`() {
        val gyldigBuc : Sed = getResourceSed("sed/P2000-minimal-uten-sakId.json")
        val mockEuxRinaid = "123456"
        val mockEuxDocumentId = "d740047e730f475aa34ae59f62e3bb99"

        every { euxOidcRestTemplate.getForObject(eq("/buc/$mockEuxRinaid/sed/$mockEuxDocumentId"), eq(Sed::class.java))} returns  gyldigBuc

        val sed = euxService.getSed(mockEuxRinaid, mockEuxDocumentId)
        val sakId = hentSaksNummer(sed)

        assertEquals(null, sakId)
    }
}


