package no.nav.eessi.pensjon.statistikk.integrationtest

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.eessi.pensjon.StatistikkApplicationIntegration
import no.nav.eessi.pensjon.eux.BucMetadata
import no.nav.eessi.pensjon.eux.EuxService
import no.nav.eessi.pensjon.eux.model.buc.BucType
import no.nav.eessi.pensjon.gcp.GcpStorageService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(classes = [IntegrationBase.TestConfig::class, IntegrationtestConfig::class, StatistikkApplicationIntegration::class], value = ["SPRING_PROFILES_ACTIVE", "integrationtest"])
@ActiveProfiles("integrationtest")
@DirtiesContext
@EmbeddedKafka(
    topics = [STATISTIKK_TOPIC],
    brokerProperties = ["log.dir=/tmp/embedded-OpprettelseEscapetMeldingTest"]
)
class OpprettelseEscapetMeldingTest : IntegrationBase() {

    @Autowired
    lateinit var gcpStorageService: GcpStorageService

    @Test
    fun `En buc hendelse med escapet character skal sendes videre til riktig kanal  `() {

        //init mock server
        CustomMockServer()
            .medBuc("/buc/9209925", "src/test/resources/buc/bucMedP2000.json")

        val bucMetadata  = BucMetadata (listOf(), BucType.P_BUC_01, "2020-12-08T09:52:55.345+0000")

        every{ mockk<EuxService>().getBucMetadata(any()) } returns bucMetadata

        val json = """{\n  \"opprettelseType\" : \"BUC\",\n  \"rinaId\" : \"9209925\",\n  \"dokumentId\" : null,\n  \"vedtaksId\" : null\n} """.trimMargin()

        initAndRunContainer(STATISTIKK_TOPIC).also {
            it.sendMsgOnDefaultTopic(json)
            it.waitForlatch(statistikkListener)
        }

        verify(exactly = 1) { statistikkPublisher.publiserBucOpprettetStatistikk(any()) }
    }
}
