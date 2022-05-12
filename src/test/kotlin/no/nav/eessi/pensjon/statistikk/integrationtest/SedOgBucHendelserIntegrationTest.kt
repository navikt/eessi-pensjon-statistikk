package no.nav.eessi.pensjon.statistikk.integrationtest

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.eessi.pensjon.ResourceHelper
import no.nav.eessi.pensjon.StatistikkApplicationIntegration
import no.nav.eessi.pensjon.eux.BucMetadata
import no.nav.eessi.pensjon.eux.EuxKlient
import no.nav.eessi.pensjon.eux.EuxService
import no.nav.eessi.pensjon.eux.model.buc.BucType
import no.nav.eessi.pensjon.json.mapJsonToAny
import no.nav.eessi.pensjon.json.toJson
import no.nav.eessi.pensjon.json.typeRefs
import no.nav.eessi.pensjon.statistikk.listener.OpprettelseMelding
import no.nav.eessi.pensjon.statistikk.listener.SedHendelseRina
import no.nav.eessi.pensjon.statistikk.models.OpprettelseType
import no.nav.eessi.pensjon.statistikk.models.SedMeldingP6000Ut
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
    topics = [STATISTIKK_TOPIC]
)
class SedOgBucHendelserIntegrationTest : IntegrationBase() {

    var euxService: EuxService = mockk()

    @Autowired
    lateinit var euxKlient: EuxKlient

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


    @Test
    fun `En sed hendelse skal sendes videre til riktig kanal  `() {
        euxKlient.initMetrics()

        CustomMockServer()
            .medBuc("/buc/147729", "src/test/resources/buc/bucMedP6000.json")
            .medBuc("/buc/147729/sed/ae000ec3d718416a934e94e22c844ba6", "src/test/resources/sed/P6000-komplett.json")

        val bucMetadata  = BucMetadata (listOf(), BucType.P_BUC_01, "2020-12-08T09:52:55.345+0000")

        every{ euxService.getBucMetadata(any()) } returns bucMetadata

        val sedHendelse = ResourceHelper.getResourceSedHendelseRina("eux/P_BUC_01_P2000.json").toJson()
        val model = mapJsonToAny(sedHendelse, typeRefs<SedHendelseRina>())

        initAndRunContainer(STATISTIKK_TOPIC_MOTATT).also {
            it.sendMsgOnDefaultTopic(model.toJson())
            it.waitForlatchMottatt(statistikkListener)
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

}
