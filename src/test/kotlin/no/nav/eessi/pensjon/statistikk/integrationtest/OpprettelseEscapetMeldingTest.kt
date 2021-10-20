package no.nav.eessi.pensjon.statistikk.integrationtest

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.eessi.pensjon.eux.BucMetadata
import no.nav.eessi.pensjon.eux.EuxService
import no.nav.eessi.pensjon.eux.model.buc.BucType
import no.nav.eessi.pensjon.statistikk.listener.StatistikkListener
import no.nav.eessi.pensjon.statistikk.services.StatistikkPublisher
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import java.util.concurrent.TimeUnit

private const val STATISTIKK_TOPIC = "eessi-pensjon-statistikk-inn"

@SpringBootTest(classes = [IntegrationBase.TestConfig::class])
@ActiveProfiles("integrationtest")
@DirtiesContext
@EmbeddedKafka(
    topics = [STATISTIKK_TOPIC]
)
class OpprettelseEscapetMeldingTest : IntegrationBase() {

    @Autowired
    lateinit var statistikkListener: StatistikkListener

    @Autowired
    lateinit var statistikkPublisher: StatistikkPublisher

    @Autowired
    private lateinit var template: KafkaTemplate<String, String>

    @BeforeEach
    fun before() {
        statistikkListener.initMetrics()
    }

    @Test
    fun `En buc hendelse med escapet character skal sendes videre til riktig kanal  `() {
        //init mock server
        CustomMockServer()
            .mockSTSToken()
            .medBuc("/buc/9209925", "src/test/resources/buc/bucMedP2000.json")

        val bucMetadata  = BucMetadata (listOf(), BucType.P_BUC_01, "2020-12-08T09:52:55.345+0000")

        every{ mockk<EuxService>().getBucMetadata(any()) } returns bucMetadata

        val json = """{\n  \"opprettelseType\" : \"BUC\",\n  \"rinaId\" : \"9209925\",\n  \"dokumentId\" : null,\n  \"vedtaksId\" : null\n} """.trimMargin()

        //send msg
        template.send(STATISTIKK_TOPIC, json).let {
            statistikkListener.getLatch().await(10, TimeUnit.SECONDS)
        }

        verify(exactly = 1) { statistikkPublisher.publiserBucOpprettetStatistikk(any()) }
        assertThat(statistikkListener.getLatch().await(10, TimeUnit.SECONDS))
    }

}