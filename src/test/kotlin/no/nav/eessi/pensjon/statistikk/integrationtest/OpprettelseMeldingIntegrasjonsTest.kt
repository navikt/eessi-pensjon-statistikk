package no.nav.eessi.pensjon.statistikk.integrationtest

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.eessi.pensjon.eux.BucMetadata
import no.nav.eessi.pensjon.eux.EuxService
import no.nav.eessi.pensjon.eux.model.buc.BucType
import no.nav.eessi.pensjon.json.toJson
import no.nav.eessi.pensjon.statistikk.listener.OpprettelseMelding
import no.nav.eessi.pensjon.statistikk.listener.StatistikkListener
import no.nav.eessi.pensjon.statistikk.models.OpprettelseType
import no.nav.eessi.pensjon.statistikk.services.StatistikkPublisher
import org.assertj.core.api.Assertions.assertThat
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
class OpprettelseMeldingIntegrasjonsTest : IntegrationBase() {

    @Autowired
    private lateinit var template: KafkaTemplate<String, String>

    @Test
    fun `En buc hendelse skal sendes videre til riktig kanal  `() {
        //init mock server
        CustomMockServer()
            .mockSTSToken()
            .medBuc("/buc/123", "src/test/resources/buc/bucMedP2000.json")

        val bucMetadata  = BucMetadata (listOf(), BucType.P_BUC_01, "2020-12-08T09:52:55.345+0000")

        every{ mockk<EuxService>().getBucMetadata(any()) } returns bucMetadata

        val budMelding = OpprettelseMelding(
            opprettelseType = OpprettelseType.BUC,
            rinaId = "123",
            dokumentId = "d740047e730f475aa34ae59f62e3bb99",
            vedtaksId = null
        )
        //send msg
        template.send(STATISTIKK_TOPIC, budMelding.toJson()).let {
            statistikkListener.getLatch().await(10, TimeUnit.SECONDS)
        }

        verify(exactly = 1) { statistikkPublisher.publiserBucOpprettetStatistikk(any()) }
        assertThat(statistikkListener.getLatch().await(10, TimeUnit.SECONDS))
    }
}
