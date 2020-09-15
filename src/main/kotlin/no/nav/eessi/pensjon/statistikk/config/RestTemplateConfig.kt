package no.nav.eessi.pensjon.statistikk.config

import io.micrometer.core.instrument.MeterRegistry
import no.nav.eessi.pensjon.security.sts.STSService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpRequest
import org.springframework.http.MediaType
import org.springframework.http.client.*
import java.util.*

@Configuration
class RestTemplateConfig(private val meterRegistry: MeterRegistry, private val stsService: STSService) {

    @Value("\${srvusername}")
    lateinit var username: String

    @Value("\${srvpassword}")
    lateinit var password: String



    class RequestInterceptor : ClientHttpRequestInterceptor {
        override fun intercept(request: HttpRequest, body: ByteArray, execution: ClientHttpRequestExecution): ClientHttpResponse {
            request.headers["X-Correlation-ID"] = UUID.randomUUID().toString()
            request.headers["Content-Type"] = MediaType.APPLICATION_JSON.toString()
            return execution.execute(request, body)
        }
    }
}
