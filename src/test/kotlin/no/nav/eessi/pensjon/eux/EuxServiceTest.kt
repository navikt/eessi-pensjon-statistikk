package no.nav.eessi.pensjon.eux

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.whenever
import no.nav.eessi.pensjon.json.mapJsonToAny
import no.nav.eessi.pensjon.json.typeRefs
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.web.client.RestTemplate

@ExtendWith(MockitoExtension::class)
internal class EuxServiceTest {

    @Mock
    lateinit var euxOidcRestTemplate: RestTemplate

    lateinit var euxService: EuxService

    @BeforeEach
    fun before() {
        euxService = EuxService(EuxKlient(euxOidcRestTemplate))
    }

    @Test
    fun `Se timestamp konverters fra zone til offsettDateTime`() {
        val gyldigBuc : BucMetadata = getResourceBucMetadata("buc/BucMedP2000.json")
        val mockEuxRinaid = "123456"
        val mockEuxDocumentId = "d740047e730f475aa34ae59f62e3bb99"

        doReturn(gyldigBuc)
            .whenever(euxOidcRestTemplate).getForObject(
                eq("/buc/$mockEuxRinaid"),
                eq(BucMetadata::class.java))

        val offsetDateTime = euxService.getTimeStampFromSedMetaDataInBuc(mockEuxRinaid, mockEuxDocumentId)

        assertEquals("2020-12-08T09:53:36.241Z", offsetDateTime)
     }

    @Test
    fun `Gitt en SED når norskSakId er utfylt så returner norsk saksId`() {
        val gyldigBuc : Sed = getResourceSed("sed/P2000-minimal-med-en-norsk-sakId.json")
        val mockEuxRinaid = "123456"
        val mockEuxDocumentId = "d740047e730f475aa34ae59f62e3bb99"

        doReturn(gyldigBuc)
            .whenever(euxOidcRestTemplate).getForObject(
                eq("/buc/$mockEuxRinaid/sed/$mockEuxDocumentId"),
                eq(Sed::class.java))

        val sakId = euxService.getSakIdFraSed(mockEuxRinaid, mockEuxDocumentId)

        assertEquals("123456", sakId)
    }

    @Test
    fun `Gitt en SED når norskSakId ikke er utfylt så returner null`() {
        val gyldigBuc : Sed = getResourceSed("sed/P2000-minimal-med-kun-utenlandsk-sakId.json")
        val mockEuxRinaid = "123456"
        val mockEuxDocumentId = "d740047e730f475aa34ae59f62e3bb99"

        doReturn(gyldigBuc)
            .whenever(euxOidcRestTemplate).getForObject(
                eq("/buc/$mockEuxRinaid/sed/$mockEuxDocumentId"),
                eq(Sed::class.java))

        val sakId = euxService.getSakIdFraSed(mockEuxRinaid, mockEuxDocumentId)

        assertEquals(null, sakId)
    }

    @Test
    fun `Gitt en SED når ingen sakId er utfylt så returner null`() {
        val gyldigBuc : Sed = getResourceSed("sed/P2000-minimal-uten-sakId.json")
        val mockEuxRinaid = "123456"
        val mockEuxDocumentId = "d740047e730f475aa34ae59f62e3bb99"

        doReturn(gyldigBuc)
            .whenever(euxOidcRestTemplate).getForObject(
                eq("/buc/$mockEuxRinaid/sed/$mockEuxDocumentId"),
                eq(Sed::class.java))

        val sakId = euxService.getSakIdFraSed(mockEuxRinaid, mockEuxDocumentId)

        assertEquals(null, sakId)
    }

    private fun getResourceBucMetadata(resourcePath: String): BucMetadata {
        val json = javaClass.classLoader.getResource(resourcePath)!!.readText()
        return mapJsonToAny(json, typeRefs())
    }

    private fun getResourceSed(resourcePath: String): Sed {
        val json = javaClass.classLoader.getResource(resourcePath)!!.readText()
        return mapJsonToAny(json, typeRefs())
    }
}


