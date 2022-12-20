package no.nav.eessi.pensjon.statistikk.listener

import io.mockk.mockk
import no.nav.eessi.pensjon.StatistikkApplicationIntegration
import no.nav.eessi.pensjon.eux.BucMetadata
import no.nav.eessi.pensjon.eux.Document
import no.nav.eessi.pensjon.eux.model.buc.BucType
import no.nav.eessi.pensjon.statistikk.services.HendelsesAggregeringsService
import org.junit.jupiter.api.Test
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

    @Test
    fun `en hendelse med dato skal formatere korrekt`() {

        val document = Document(id ="11", creationDate = "2022-12-20T06:14:38.516+00:00", conversations = emptyList(), versions = emptyList())
        val bucMetadata  = BucMetadata (listOf(document), BucType.P_BUC_01, "2020-12-08T09:52:55.345+0000")
        hendelsesAggregeringsService.getTimeStampFromSedMetaDataInBuc(bucMetadata, "11")
        //justRun { hendelsesAggregeringsService.populerSedMeldingUt(any(), any(), any(), HendelseType.SED_MOTTATT, any()) }
        //sedListener.consumeSedMottatt(enSedHendelse().toJson(), mockk(relaxed = true), mockk())
    }

/*    fun enSedHendelse(): SedHendelseRina {
        return  SedHendelseRina(
            sektorKode = "P",
            bucType = BucType.P_BUC_01,
            sedType = SedType.P2100,
            rinaSakId = "74389487",
            rinaDokumentId = "743982",
            rinaDokumentVersjon = "1",
            avsenderNavn = "Svensk institusjon",
            avsenderLand = "SE",

        )
    }*/

    @TestConfiguration
    class StatistikkListenerTestConfig {
        @Bean
        fun hendelsesAggregeringsService(): HendelsesAggregeringsService {
            return HendelsesAggregeringsService(mockk(), mockk())
        }
        @Bean
        fun statistikkListener(): StatistikkListener {
            return StatistikkListener(hendelsesAggregeringsService(), mockk(relaxed = true), "SPRING_PROFILES_ACTIVE").apply { initMetrics() }
        }
    }
}