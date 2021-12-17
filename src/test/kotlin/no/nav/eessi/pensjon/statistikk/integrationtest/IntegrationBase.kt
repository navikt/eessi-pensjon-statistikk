package no.nav.eessi.pensjon.statistikk.integrationtest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import no.nav.eessi.pensjon.s3.S3StorageService
import no.nav.eessi.pensjon.security.sts.STSService
import no.nav.eessi.pensjon.statistikk.S3StorageHelper
import no.nav.eessi.pensjon.statistikk.listener.StatistikkListener
import no.nav.eessi.pensjon.statistikk.services.StatistikkPublisher
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.mockserver.integration.ClientAndServer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.listener.KafkaMessageListenerContainer
import org.springframework.kafka.listener.MessageListener
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.utils.ContainerTestUtils
import org.springframework.kafka.test.utils.KafkaTestUtils
import java.util.*
import java.util.concurrent.TimeUnit

const val STATISTIKK_TOPIC = "eessi-pensjon-statistikk-inn"
const val STATISTIKK_TOPIC_MOTATT = "eessi-pensjon-statistikk-sed-mottatt"

abstract class IntegrationBase() {

    @Autowired
    lateinit var statistikkListener: StatistikkListener

    @Autowired
    lateinit var statistikkPublisher: StatistikkPublisher

    @Autowired
    lateinit var embeddedKafka: EmbeddedKafkaBroker

    @MockkBean
    lateinit var stsService: STSService

    @Autowired
    lateinit var producerFactory: ProducerFactory<String, String>

    var mockServer: ClientAndServer

    init {
        randomFrom().apply {
            System.setProperty("mockserverport", "" + this)
            mockServer = ClientAndServer.startClientAndServer(this)
        }
    }

    @BeforeEach
    fun setup() {
        every { stsService.getSystemOidcToken() } returns "a nice little token?"
    }

    @AfterEach
    fun after() {
        println("************************* CLEANING UP AFTER CLASS*****************************")
        clearAllMocks()
        embeddedKafka.kafkaServers.forEach { it.shutdown() }
        mockServer.stopAsync()
        mockServer.stop().also { print("mockServer -> HasStopped: ${mockServer.hasStopped()}") }
    }

    fun initAndRunContainer(topic: String): TestResult {
        val container = initConsumer(topic)
        container.start()
        ContainerTestUtils.waitForAssignment(container, embeddedKafka.partitionsPerTopic)
        var template = KafkaTemplate(producerFactory).apply { defaultTopic = topic }
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

        fun waitForlatch(sendtListner: StatistikkListener) = sendtListner.getLatch().await(10, TimeUnit.SECONDS)
        fun waitForlatchMottatt(sendtListner: StatistikkListener) = sendtListner.getLatchMottatt().await(10, TimeUnit.SECONDS)
    }

    private fun initConsumer(topicName: String): KafkaMessageListenerContainer<String, String> {
        val consumerProperties = KafkaTestUtils.consumerProps(
            "eessi-pensjon-group2",
            "false",
            embeddedKafka
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
        fun s3StorageService(): S3StorageService {
            return S3StorageHelper.createStoreService().also { it.init() }
        }
    }

    private fun randomFrom(from: Int = 1024, to: Int = 65535): Int {
        return Random().nextInt(to - from) + from
    }
}