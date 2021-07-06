package no.nav.eessi.pensjon.statistikk.integrationtest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import no.nav.eessi.pensjon.ResourceHelper
import no.nav.eessi.pensjon.eux.BucMetadata
import no.nav.eessi.pensjon.eux.EuxService
import no.nav.eessi.pensjon.eux.model.buc.BucType
import no.nav.eessi.pensjon.json.mapJsonToAny
import no.nav.eessi.pensjon.json.toJson
import no.nav.eessi.pensjon.json.typeRefs
import no.nav.eessi.pensjon.s3.S3StorageService
import no.nav.eessi.pensjon.security.sts.STSService
import no.nav.eessi.pensjon.statistikk.S3StorageHelper
import no.nav.eessi.pensjon.statistikk.listener.SedHendelseRina
import no.nav.eessi.pensjon.statistikk.listener.StatistikkListener
import no.nav.eessi.pensjon.statistikk.models.SedMeldingP6000Ut
import no.nav.eessi.pensjon.statistikk.services.StatistikkPublisher
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.Header
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import org.mockserver.model.HttpStatusCode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
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
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.kafka.test.utils.ContainerTestUtils
import org.springframework.kafka.test.utils.KafkaTestUtils
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.client.RestTemplate
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.TimeUnit

private const val STATISTIKK_TOPIC = "eessi-pensjon-statistikk-sed-mottatt"
private lateinit var mockServer: ClientAndServer

@SpringBootTest
@ActiveProfiles("integrationtest")
@DirtiesContext
@EmbeddedKafka(
    topics = [STATISTIKK_TOPIC]
)

@Disabled
class SedMottattIntegrasjonsTest {

    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    lateinit var embeddedKafka: EmbeddedKafkaBroker

    @MockkBean(name = "pensjonsinformasjonOidcRestTemplate")
    lateinit var restEuxTemplate: RestTemplate

    @MockkBean
    lateinit var stsService: STSService

    var euxService: EuxService = mockk()

    @Autowired
    lateinit var statistikkListener: StatistikkListener

    @Autowired
    lateinit var statistikkPublisher: StatistikkPublisher

    lateinit var container: KafkaMessageListenerContainer<String, String>
    lateinit var sedMottattProducerTemplate: KafkaTemplate<Int, String>

    @BeforeEach
    fun setup() {
        every { stsService.getSystemOidcToken() } returns "a nice little token?"

        container = settOppUtitlityConsumer(STATISTIKK_TOPIC)
        container.start()
        ContainerTestUtils.waitForAssignment(container, embeddedKafka.partitionsPerTopic)

        sedMottattProducerTemplate = settOppProducerTemplate()
    }

    @AfterEach
    fun after() {
        container.stop()
        embeddedKafka.kafkaServers.forEach { it.shutdown() }
    }

    @Test
    fun `En sed hendelse skal sendes videre til riktig kanal  `() {
        val bucMetadata  = BucMetadata (listOf(), BucType.P_BUC_01, "2020-12-08T09:52:55.345+0000")

        every{ euxService.getBucMetadata(any()) } returns bucMetadata

        val sedHendelse = ResourceHelper.getResourceSedHendelseRina("eux/P_BUC_01_P2000.json").toJson()
        val model = mapJsonToAny(sedHendelse, typeRefs<SedHendelseRina>())

        sendMelding(model).let {
            statistikkListener.getLatch().await(40000, TimeUnit.MILLISECONDS)
        }
        verify(exactly = 1) { statistikkPublisher.publiserSedHendelse(eq(sedMeldingP6000Ut())) }
    }

    private fun sedMeldingP6000Ut(): SedMeldingP6000Ut {
        val meldingUtJson = """
            {
              "dokumentId" : "ae000ec3d718416a934e94e22c844ba6",
              "bucType" : "P_BUC_06",
              "rinaid" : "147729",
              "mottakerLand" : [ "NO" ],
              "avsenderLand" : "NO",
              "rinaDokumentVersjon" : "1",
              "sedType" : "P6000",
              "pid" : "09028020144",
              "hendelseType" : "SED_MOTTATT",
              "pesysSakId" : "22919968",
              "opprettetTidspunkt" : "2021-02-11T13:08:03Z",
              "vedtaksId" : null,
              "bostedsland" : "HR",
              "pensjonsType" : "GJENLEV",
              "vedtakStatus" : "FORELOPIG_UTBETALING",
              "bruttoBelop" : "12482",
              "valuta" : "NOK"
            }
        """.trimIndent()
        return mapJsonToAny(meldingUtJson, typeRefs())
    }

    private fun sendMelding(melding: SedHendelseRina) {
        sedMottattProducerTemplate.sendDefault(melding.toJson())
    }

    private fun settOppProducerTemplate(): KafkaTemplate<Int, String> {
        val senderProps = KafkaTestUtils.producerProps(embeddedKafka.brokersAsString)
        val pf = DefaultKafkaProducerFactory<Int, String>(senderProps)
        val template = KafkaTemplate(pf)
        template.defaultTopic = STATISTIKK_TOPIC
        return template
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

    companion object {

        init {
            // Start Mockserver in memory
            val port = randomFrom()
            mockServer = ClientAndServer.startClientAndServer(port)
            System.setProperty("mockServerport", port.toString())
            // Mocker STS
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

            mockServer.`when`(
                HttpRequest.request()
                    .withMethod(HttpMethod.GET.name)
                    .withPath("/buc/147729")
            )
                .respond(
                    HttpResponse.response()
                        .withHeader(Header("Content-Type", "application/json; charset=utf-8"))
                        .withStatusCode(HttpStatusCode.OK_200.code())
                        .withBody(String(Files.readAllBytes(Paths.get("src/test/resources/buc/bucMedP6000.json"))))
                )

            mockServer.`when`(
                HttpRequest.request()
                    .withMethod(HttpMethod.GET.name)
                    .withPath("/buc/147729/sed/ae000ec3d718416a934e94e22c844ba6")
            )
                .respond(
                    HttpResponse.response()
                        .withHeader(Header("Content-Type", "application/json; charset=utf-8"))
                        .withStatusCode(HttpStatusCode.OK_200.code())
                        .withBody(String(Files.readAllBytes(Paths.get("src/test/resources/sed/P6000-komplett.json"))))
                )
        }

        private fun randomFrom(from: Int = 1024, to: Int = 65535): Int {
            return Random().nextInt(to - from) + from
        }
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
}
