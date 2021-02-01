package no.nav.eessi.pensjon.eux

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.whenever
import no.nav.eessi.pensjon.logging.RequestResponseLoggerInterceptor
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.DefaultResponseErrorHandler
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestTemplate
import java.nio.charset.Charset

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EuxKlientTest {

    private lateinit var klient: EuxKlient

    @Mock
    private lateinit var mockEuxrestTemplate: RestTemplate


    @BeforeEach
    fun setup() {
        mockEuxrestTemplate.errorHandler = DefaultResponseErrorHandler()
        mockEuxrestTemplate.interceptors = listOf(RequestResponseLoggerInterceptor())
        klient = EuxKlient(mockEuxrestTemplate)
        klient.initMetrics()
    }

    @AfterEach
    fun takedown() {
        Mockito.reset(mockEuxrestTemplate)
    }


    @Test
    fun `Calling EuxKlient  feiler med kontakt fra eux med kall til getSedOnBucByDocumentId`() {
        doThrow(createDummyServerRestExecption(HttpStatus.BAD_GATEWAY, "Dummybody"))
            .whenever(mockEuxrestTemplate).exchange(any<String>(), eq(HttpMethod.GET), eq(null), eq(String::class.java))

        org.junit.jupiter.api.assertThrows<HttpClientErrorException> {
            klient.getSed("12345678900", "P_BUC_99")
        }
    }

    @Test
    fun `Calling EuxKlient  feiler med motta navsed fra eux med kall til getSedOnBucByDocumentId`() {
        val errorresponse = ResponseEntity<String?>(HttpStatus.UNAUTHORIZED)
        whenever(
            mockEuxrestTemplate.exchange(
                any<String>(),
                eq(HttpMethod.GET),
                eq(null),
                eq(String::class.java)
            )
        ).thenReturn(errorresponse)
        org.junit.jupiter.api.assertThrows<HttpClientErrorException> {
            klient.getSed("12345678900", "P_BUC_99")
        }
    }

    private fun createDummyServerRestExecption(httpstatus: HttpStatus, dummyBody: String)
            = HttpServerErrorException.create (httpstatus, httpstatus.name, HttpHeaders(), dummyBody.toByteArray(), Charset.defaultCharset())

    private fun createDummyClientRestExecption(httpstatus: HttpStatus, dummyBody: String)
            = HttpClientErrorException.create (httpstatus, httpstatus.name, HttpHeaders(), dummyBody.toByteArray(), Charset.defaultCharset())

}
