package no.nav.eessi.pensjon.statistikk.integrationtest

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.eessi.pensjon.StatistikkApplicationIntegration
import no.nav.eessi.pensjon.eux.BucMetadata
import no.nav.eessi.pensjon.eux.EuxService
import no.nav.eessi.pensjon.eux.model.buc.BucType
import no.nav.eessi.pensjon.json.toJson
import no.nav.eessi.pensjon.statistikk.listener.OpprettelseMelding
import no.nav.eessi.pensjon.statistikk.models.OpprettelseType
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(classes = [IntegrationBase.TestConfig::class, IntegrationtestConfig::class, StatistikkApplicationIntegration::class], value = ["SPRING_PROFILES_ACTIVE", "integrationtest"])
@ActiveProfiles("integrationtest")
@DirtiesContext
@EmbeddedKafka(
    topics = [STATISTIKK_TOPIC]
)
@Disabled
class OpprettelseMeldingIntegrasjonsTest : IntegrationBase() {

    @Test
    fun `En buc hendelse skal sendes videre til riktig kanal  `() {
        //init mock server
        CustomMockServer()
            .medBuc("/buc/123", "src/test/resources/buc/bucMedP2000.json")

        val bucMetadata  = BucMetadata (listOf(), BucType.P_BUC_01, "2020-12-08T09:52:55.345+0000")

        every{ mockk<EuxService>().getBucMetadata(any()) } returns bucMetadata

        val budMelding = OpprettelseMelding(
            opprettelseType = OpprettelseType.BUC,
            rinaId = "123",
            dokumentId = "d740047e730f475aa34ae59f62e3bb99",
            vedtaksId = null
        )

        initAndRunContainer(STATISTIKK_TOPIC).also {
            it.sendMsgOnDefaultTopic(budMelding.toJson())
            it.waitForlatch(statistikkListener)
        }
        verify(exactly = 1) { statistikkPublisher.publiserBucOpprettetStatistikk(any()) }
    }
}
