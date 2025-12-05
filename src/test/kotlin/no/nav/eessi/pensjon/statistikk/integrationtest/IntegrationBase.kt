package no.nav.eessi.pensjon.statistikk.integrationtest

import io.mockk.mockk
import io.mockk.spyk
import no.nav.eessi.pensjon.gcp.GcpStorageService
import no.nav.eessi.pensjon.statistikk.listener.StatistikkListener
import no.nav.eessi.pensjon.statistikk.services.StatistikkPublisher
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy
import org.apache.hc.client5.http.ssl.HostnameVerificationPolicy
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier
import org.apache.hc.core5.ssl.SSLContextBuilder
import org.junit.jupiter.api.AfterEach
import org.mockserver.integration.ClientAndServer
import org.mockserver.socket.PortFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.restclient.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.listener.KafkaMessageListenerContainer
import org.springframework.kafka.listener.MessageListener
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.utils.KafkaTestUtils
import org.springframework.web.client.RestTemplate
import java.util.concurrent.TimeUnit


const val STATISTIKK_TOPIC = "eessi-pensjon-statistikk-inn"
const val STATISTIKK_TOPIC_MOTATT = "eessi-pensjon-statistikk-sed-mottatt"

private var mockServerPort = PortFactory.findFreePort()
private lateinit var mockServer: ClientAndServer

abstract class IntegrationBase {

    @Autowired
    lateinit var statistikkListener: StatistikkListener

    @Autowired
    lateinit var statistikkPublisher: StatistikkPublisher

    @Autowired
    lateinit var embeddedKafka: EmbeddedKafkaBroker

    @Autowired
    lateinit var producerFactory: ProducerFactory<String, String>

    lateinit var container: KafkaMessageListenerContainer<String, String>

    companion object {
        init {
            System.setProperty("mockserverport", "" + mockServerPort)
            mockServer = ClientAndServer.startClientAndServer(mockServerPort)
        }
    }

    @AfterEach
    fun afterEach() {
        println("************************* CLEANING UP AFTER CLASS*****************************")
        container.stop()
        mockServer.reset()
    }

    fun initAndRunContainer(topic: String): TestResult {
        container = initConsumer(topic)
        container.start()
        Thread.sleep(5000) // wait a bit for the container to start
        //ContainerTestUtils.waitForAssignment(container, embeddedKafka.partitionsPerTopic)
        val template = KafkaTemplate(producerFactory).apply { setDefaultTopic(topic) }
        return TestResult(template, container).also {
            println("*************************  INIT DONE *****************************")
        }
    }

    data class TestResult(
        val kafkaTemplate: KafkaTemplate<String, String>,
        val topic: KafkaMessageListenerContainer<String, String>
    ) {

        fun sendMsgOnDefaultTopic(kafkaMsgFromPath : String){
            kafkaTemplate.sendDefault(kafkaMsgFromPath)
        }

        fun waitForlatch(sendtListner: StatistikkListener) = sendtListner.getLatch().await(20, TimeUnit.SECONDS)
        fun waitForlatchMottatt(sendtListner: StatistikkListener) = sendtListner.getLatchMottatt().await(20, TimeUnit.SECONDS)
    }

    private fun initConsumer(topicName: String): KafkaMessageListenerContainer<String, String> {
        val consumerProperties = KafkaTestUtils.consumerProps(
            embeddedKafka,
            "eessi-pensjon-group2",
            false
        )
        consumerProperties["auto.offset.reset"] = "earliest"
        val consumerFactory = DefaultKafkaConsumerFactory<String, String>(consumerProperties)
        val container = KafkaMessageListenerContainer(consumerFactory, ContainerProperties(topicName))

        container.setupMessageListener(
            MessageListener<String, String> { record -> println("Konsumerer melding:  $record") }
        )
        return container
    }

    @TestConfiguration
    class TestConfig {

        @Bean
        fun statistikkPublisher(): StatistikkPublisher {
            return spyk(StatistikkPublisher(mockk(relaxed = true), "bogusTopic"))
        }

        @Bean
        fun gcpStorageService(): GcpStorageService {
            return mockk()
        }

        @Bean
        fun euxClientCredentialsResourceRestTemplate(): RestTemplate {
            val sslContext = SSLContextBuilder.create()
                .loadTrustMaterial(null) { _, _ -> true } // Trust all certificates
                .build()

            val connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setTlsSocketStrategy(
                    DefaultClientTlsStrategy(
                        sslContext,
                        HostnameVerificationPolicy.CLIENT,
                        NoopHostnameVerifier.INSTANCE
                    )
                ).build()

            val httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .build()

            val customRequestFactory = HttpComponentsClientHttpRequestFactory(httpClient)

            return RestTemplateBuilder()
                .rootUri("https://localhost:${mockServerPort}")
                .build().apply {
                    requestFactory = customRequestFactory
                }
        }
    }
}