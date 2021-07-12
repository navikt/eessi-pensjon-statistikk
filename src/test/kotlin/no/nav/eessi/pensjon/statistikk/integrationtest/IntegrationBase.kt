package no.nav.eessi.pensjon.statistikk.integrationtest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import no.nav.eessi.pensjon.s3.S3StorageService
import no.nav.eessi.pensjon.security.sts.STSService
import no.nav.eessi.pensjon.statistikk.S3StorageHelper
import no.nav.eessi.pensjon.statistikk.services.StatistikkPublisher
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.Header
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import org.mockserver.model.HttpStatusCode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpMethod
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.listener.KafkaMessageListenerContainer
import org.springframework.kafka.listener.MessageListener
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.utils.ContainerTestUtils
import org.springframework.kafka.test.utils.KafkaTestUtils
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

private lateinit var mockServer: ClientAndServer

abstract class IntegrationBase(val topicName : String) {

    @Autowired
    lateinit var embeddedKafka: EmbeddedKafkaBroker

    @MockkBean
    lateinit var stsService: STSService

    lateinit var sedMottattProducerTemplate: KafkaTemplate<Int, String>
    lateinit var container: KafkaMessageListenerContainer<String, String>


    @BeforeEach
    fun setup() {
        every { stsService.getSystemOidcToken() } returns "a nice little token?"

        container = settOppUtitlityConsumer(topicName)
        container.start()
        ContainerTestUtils.waitForAssignment(container, embeddedKafka.partitionsPerTopic)

        sedMottattProducerTemplate = settOppProducerTemplate(topicName)
    }

    @AfterEach
    fun after() {
        embeddedKafka.kafkaServers.forEach { it.shutdown() }
        clearAllMocks()
    }

    private fun settOppUtitlityConsumer(topicNavn: String): KafkaMessageListenerContainer<String, String> {
        val consumerProperties = KafkaTestUtils.consumerProps(
            "eessi-pensjon-group2",
            "false",
            embeddedKafka
        )
        consumerProperties["auto.offset.reset"] = "earliest"

        val consumerFactory = DefaultKafkaConsumerFactory<String, String>(consumerProperties)
        val containerProperties = ContainerProperties(topicNavn)
        val container = KafkaMessageListenerContainer(consumerFactory, containerProperties)
        val messageListener = MessageListener<String, String> { record -> println("Konsumerer melding:  $record") }
        container.setupMessageListener(messageListener)

        return container
    }


    private fun settOppProducerTemplate(producer : String): KafkaTemplate<Int, String> {
        val senderProps = KafkaTestUtils.producerProps(embeddedKafka.brokersAsString)
        val pf = DefaultKafkaProducerFactory<Int, String>(senderProps)
        val template = KafkaTemplate(pf)
        template.defaultTopic = producer
        return template
    }

    @TestConfiguration
    class TestConfig {

        @Bean
        fun statistikkPublisher(): StatistikkPublisher {
            return spyk(StatistikkPublisher(mockk(relaxed = true), "bogusTopic"))
        }

        @Bean
        fun s3StorageService(): S3StorageService {
            println("InintMock S3")
            return S3StorageHelper.createStoreService().also { it.init() }
        }
    }

    init {
        val port = randomFrom()
        System.setProperty("mockserverport", port.toString())
        mockServer = ClientAndServer.startClientAndServer(port)

    }
    private fun randomFrom(from: Int = 1024, to: Int = 65535): Int {
        return Random().nextInt(to - from) + from
    }


    class CustomMockServer() {

        fun mockSTSToken() = apply {

            mockServer.`when`(
                HttpRequest.request()
                    .withMethod(HttpMethod.GET.name)
                    .withQueryStringParameter("grant_type", "client_credentials")
            )
                .respond(
                    HttpResponse.response()
                        .withHeader(Header("Content-Type", "application/json; charset=utf-8"))
                        .withStatusCode(HttpStatusCode.OK_200.code())
                        .withBody(String(Files.readAllBytes(Paths.get("src/test/resources/STStoken.json"))))
                )
        }

        fun medBuc(bucPath: String, bucLocation: String) = apply {

            mockServer.`when`(
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

}