package no.nav.eessi.pensjon.statistikk.services

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.whenever
import no.nav.eessi.pensjon.statistikk.clients.EuxKlient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.ResponseEntity
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
    fun `Se at json fra eux mappes riktig mot BucMetadata`() {
        val gyldigBuc = getResource("buc/BucMedP2000.json")
        val responseEntity = ResponseEntity.ok(gyldigBuc!!)
        val mockEuxRinaid = "123456"
        val mockEuxDocumentId = "sdfjkdf9834tjiw"

        doReturn(responseEntity)
            .whenever(euxOidcRestTemplate).getForEntity(
                eq("/buc/$mockEuxRinaid"),
                eq(String::class.java))

        val actual = euxService.getBucMetadata(mockEuxRinaid)

        assertNotNull(actual)
        assertEquals(actual.documents[0].id, "d740047e730f475aa34ae59f62e3bb99")
        assertEquals(actual.documents[0].creationDate, "2020-12-08T09:53:36.241+0000")
    }

    @Test
    fun `Se timestamp konverters fra zone til datetime`() {
        val gyldigBuc = getResource("buc/BucMedP2000.json")
        val responseEntity = ResponseEntity.ok(gyldigBuc!!)
        val mockEuxRinaid = "123456"
        val mockEuxDocumentId = "d740047e730f475aa34ae59f62e3bb99"

        doReturn(responseEntity)
            .whenever(euxOidcRestTemplate).getForEntity(
                eq("/buc/$mockEuxRinaid"),
                eq(String::class.java))

        val offsetDateTime = euxService.getTimeStampFromSedMetaDataInBuc(mockEuxRinaid, mockEuxDocumentId)

        assertEquals("2020-12-08T09:53:36.241Z", offsetDateTime.toString())
     }

    private fun getResource(resourcePath: String): String? =
        javaClass.classLoader.getResource(resourcePath)!!.readText()
}


