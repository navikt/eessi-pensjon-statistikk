package no.nav.eessi.pensjon.statistikk.listener

import io.mockk.mockk
import no.nav.eessi.pensjon.StatistikkApplicationIntegration
import no.nav.eessi.pensjon.eux.model.BucType
import no.nav.eessi.pensjon.eux.model.buc.BucMetadata
import no.nav.eessi.pensjon.eux.model.buc.Document
import no.nav.eessi.pensjon.statistikk.services.HendelsesAggregeringsService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.context.ContextConfiguration

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