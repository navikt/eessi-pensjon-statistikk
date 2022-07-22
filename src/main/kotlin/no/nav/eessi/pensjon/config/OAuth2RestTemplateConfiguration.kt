package no.nav.eessi.pensjon.config

import no.nav.security.token.support.client.core.ClientProperties
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.web.client.RestTemplate
import java.util.*


@Profile("prod", "test")
@Configuration
class OAuth2RestTemplateConfiguration {

    @Value("\${eux_rina_api_v1_url}")
    private lateinit var euxUrl: String

    /**
     * Create one RestTemplate per OAuth2 client entry to separate between different scopes per API
     */
    @Bean
    fun euxClientCredentialsResourceRestTemplate(
        restTemplateBuilder: RestTemplateBuilder,
        clientConfigurationProperties: ClientConfigurationProperties,
        oAuth2AccessTokenService: OAuth2AccessTokenService?
    ): RestTemplate? {
        val clientProperties =
            Optional.ofNullable(clientConfigurationProperties.registration["eux-credentials"])
                .orElseThrow { RuntimeException("could not find oauth2 client config for example-onbehalfof") }
        return restTemplateBuilder
            .rootUri(euxUrl)
            .additionalInterceptors(bearerTokenInterceptor(clientProperties, oAuth2AccessTokenService!!))
            .build()
    }

    private fun bearerTokenInterceptor(
        clientProperties: ClientProperties,
        oAuth2AccessTokenService: OAuth2AccessTokenService): ClientHttpRequestInterceptor? {
        return ClientHttpRequestInterceptor { request: HttpRequest, body: ByteArray?, execution: ClientHttpRequestExecution ->
            val response = oAuth2AccessTokenService.getAccessToken(clientProperties)
            request.headers.setBearerAuth(response.accessToken)
            execution.execute(request, body!!)
        }
    }
}