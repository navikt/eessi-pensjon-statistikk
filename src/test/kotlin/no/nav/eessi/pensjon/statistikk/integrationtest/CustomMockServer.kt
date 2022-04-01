package no.nav.eessi.pensjon.statistikk.integrationtest

import org.mockserver.client.MockServerClient
import org.mockserver.model.Header
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import org.mockserver.model.HttpStatusCode
import org.springframework.http.HttpMethod
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.CompletableFuture

class CustomMockServer() {
    private val serverPort = System.getProperty("mockserverport").toInt()

    fun medBuc(bucPath: String, bucLocation: String) = apply {
        MockServerClient("localhost", serverPort).`when`(
            HttpRequest.request()
                .withMethod(HttpMethod.GET.name)
                .withPath(bucPath)
        )
            .respond(
                HttpResponse.response()
                    .withHeader(Header("Content-Type", "application/json; charset=utf-8"))
                    .withStatusCode(HttpStatusCode.OK_200.code())
                    .withBody(String(Files.readAllBytes(Paths.get(bucLocation))))
            )
    }
}