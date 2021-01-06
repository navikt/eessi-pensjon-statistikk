/*
package no.nav.eessi.pensjon.statistikk.integrationtest

import no.nav.eessi.pensjon.security.sts.STSService
import no.nav.eessi.pensjon.statistikk.listener.StatistikkListener
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
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
import java.util.*
import java.util.concurrent.TimeUnit

private const val STATISTIKK_TOPIC = "eessi-pensjon-statistikk-inn"

@SpringBootTest
@ActiveProfiles("integrationtest")
@DirtiesContext
@EmbeddedKafka(controlledShutdown = true, partitions = 1, topics = [STATISTIKK_TOPIC], brokerProperties= ["log.dir=out/embedded-kafkamottatt"])
class StatistikkListenerIntegrasjonsTest {

    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    lateinit var embeddedKafka: EmbeddedKafkaBroker

    @Autowired
    lateinit var statistikkListener: StatistikkListener

    @MockBean
    lateinit var stsService: STSService

    @Test
    @Disabled
    fun `Når en sedMottatt hendelse blir konsumert skal det opprettes journalføringsoppgave for pensjon SEDer`() {

        // Vent til kafka er klar
        val container = settOppUtitlityConsumer(STATISTIKK_TOPIC)
        container.start()
        ContainerTestUtils.waitForAssignment(container, embeddedKafka.partitionsPerTopic)

        // oppgave lytter kafka
        // val oppgaveContainer = settOppUtitlityConsumer(STATISTIKK_TOPIC)
        // oppgaveContainer.start()
        // ContainerTestUtils.waitForAssignment(oppgaveContainer, embeddedKafka.partitionsPerTopic)

        // Sett opp producer
        val sedMottattProducerTemplate = settOppProducerTemplate()

        // produserer sedSendt meldinger på kafka
        produserSedHendelser(sedMottattProducerTemplate)

        // Venter på at sedListener skal consumeSedSendt meldingene
        statistikkListener.getLatch().await(15000, TimeUnit.MILLISECONDS)
        // Shutdown
        shutdown(container)
    }

    private fun produserSedHendelser(statistikkMottattProducerTemplate: KafkaTemplate<Int, String>) {
        // Sender 1 Foreldre SED til Kafka
        statistikkMottattProducerTemplate.sendDefault("hallasldasdasd sdfkshdjkhasdkjha sd")

    }

    private fun shutdown(container: KafkaMessageListenerContainer<String, String>) {
        container.stop()
        embeddedKafka.kafkaServers.forEach { it.shutdown() }
    }

    private fun settOppProducerTemplate(): KafkaTemplate<Int, String> {
        val senderProps = KafkaTestUtils.producerProps(embeddedKafka.brokersAsString)
        val pf = DefaultKafkaProducerFactory<Int, String>(senderProps)
        val template = KafkaTemplate(pf)
        template.defaultTopic = STATISTIKK_TOPIC
        return template
    }

    private fun settOppUtitlityConsumer(topicNavn: String): KafkaMessageListenerContainer<String, String> {
        val consumerProperties = KafkaTestUtils.consumerProps("eessi-pensjon-group2",
                "false",
                embeddedKafka)
        consumerProperties["auto.offset.reset"] = "earliest"

        val consumerFactory = DefaultKafkaConsumerFactory<String, String>(consumerProperties)
        val containerProperties = ContainerProperties(topicNavn)
        val container = KafkaMessageListenerContainer<String, String>(consumerFactory, containerProperties)
        val messageListener = MessageListener<String, String> { record -> println("Konsumerer melding:  $record") }
        container.setupMessageListener(messageListener)

        return container
    }

    companion object {

        init {
            // Start Mockserver in memory
            val port = randomFrom()
            System.setProperty("mockServerport", port.toString())
        }

        private fun randomFrom(from: Int = 1024, to: Int = 65535): Int {
            val random = Random()
            return random.nextInt(to - from) + from
        }
    }

}
*/
