package no.nav.eessi.pensjon.statistikk.listener

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.eessi.pensjon.StatistikkApplicationIntegration
import no.nav.eessi.pensjon.eux.model.BucType
import no.nav.eessi.pensjon.eux.model.SedType
import no.nav.eessi.pensjon.eux.model.buc.BucMetadata
import no.nav.eessi.pensjon.eux.model.buc.Document
import no.nav.eessi.pensjon.statistikk.services.HendelsesAggregeringsService
import no.nav.eessi.pensjon.statistikk.services.StatistikkPublisher
import no.nav.eessi.pensjon.statistikk.models.HendelseType
import no.nav.eessi.pensjon.statistikk.models.OpprettelseType
import no.nav.eessi.pensjon.statistikk.models.SedMeldingUt
import no.nav.eessi.pensjon.utils.toJson
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.kafka.support.Acknowledgment
import org.springframework.test.context.ContextConfiguration
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest


@WebMvcTest(StatistikkListener::class)
@ContextConfiguration(classes = [StatistikkListenerTest.StatistikkListenerTestConfig::class, StatistikkApplicationIntegration::class])
@AutoConfigureMockMvc
internal class StatistikkListenerTest{

    @Autowired
    lateinit var hendelsesAggregeringsService: HendelsesAggregeringsService

    @Autowired
    lateinit var sedListener: StatistikkListener

    @ParameterizedTest
    @CsvSource(
        "2016-01-01, 2016-01-01",
        "2019-08-30T09:37:37.318+0100, 2019-08-30T09:37:37.318+0100",
        "2022-12-20T06:14:38.516+00:00, 2022-12-20T06:14:38.516+00:00")
    fun `dato med tidzone skal formateres`(dato: String, formatertDato: String) {
        val document = Document(id ="11", creationDate = dato, conversations = emptyList(), versions = emptyList())
        val bucMetadata  = BucMetadata (listOf(document), BucType.P_BUC_01, "not relevant")

        val result = hendelsesAggregeringsService.getCreationDateFromSedMetaData(bucMetadata, "11")

        assertEquals(formatertDato, result)
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `incoming tracking metadata does not change outgoing statistics`(enriched: Boolean) {
        val aggregator = mockk<HendelsesAggregeringsService>()
        val publisher = mockk<StatistikkPublisher>(relaxed = true)
        val acknowledgment = mockk<Acknowledgment>(relaxed = true)
        val listener = StatistikkListener(aggregator, publisher, "test")
        val originalStatistics = SedMeldingUt(
            dokumentId = "document-id",
            bucType = BucType.P_BUC_06,
            rinaId = "123",
            mottakerLand = emptyList(),
            rinaDokumentVersjon = "1",
            sedType = SedType.P6000,
            hendelseType = HendelseType.SED_OPPRETTET,
            pesysSakId = "original-statistics-sak",
            opprettetTidspunkt = "2026-01-01"
        )
        every { aggregator.aggregateSedOpprettetData("123", "document-id", null) } returns originalStatistics
        val payload = OpprettelseMelding(
            opprettelseType = OpprettelseType.SED,
            rinaId = "123",
            dokumentId = "document-id",
            vedtaksId = null,
            sedtype = SedType.P6000,
            buctype = if (enriched) BucType.P_BUC_01 else null,
            sakId = if (enriched) "tracking-only-sak" else null
        ).toJson()

        listener.consumeOpprettelseMelding(
            payload, ConsumerRecord("topic", 0, 0L, "key", payload), acknowledgment
        )

        verify(exactly = 1) { publisher.publiserSedHendelse(originalStatistics) }
        verify(exactly = 1) { acknowledgment.acknowledge() }
    }

    @TestConfiguration
    class StatistikkListenerTestConfig {
        @Bean
        fun hendelsesAggregeringsService(): HendelsesAggregeringsService {
            return HendelsesAggregeringsService(mockk(), mockk())
        }
        @Bean
        fun statistikkListener(): StatistikkListener {
            return StatistikkListener(hendelsesAggregeringsService(), mockk(relaxed = true), "SPRING_PROFILES_ACTIVE")
        }
    }
}