package no.nav.eessi.pensjon.statistikk.listener

import no.nav.eessi.pensjon.statistikk.json.mapAnyToJson
import no.nav.eessi.pensjon.statistikk.json.toJson
import no.nav.eessi.pensjon.statistikk.models.HendelseType
import no.nav.eessi.pensjon.statistikk.models.StatistikkMelding
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode

class StatistikkMeldingSerdesTest {
    @Test
    fun `Sjekk at serialisering virker`() {
        val model = StatistikkMelding(hendelseType = HendelseType.OPPRETTBUC,rinaid =  "1208875", bucType = "P_BUC_01", timeStamp = 1000L, saksNummer = "20000", vetaksId ="30000", hendelseVersjon = 1)
        val serialized = model.toJson()

        val result = StatistikkMelding.fromJson(serialized)

        JSONAssert.assertEquals(serialized, result.toJson(), JSONCompareMode.LENIENT)
    }

    @Test
    fun `Sjekker at deserialisering gir riktig verdi`() {
        val json = """{
              "hendelseType" : "OPPRETTBUC",
              "rinaid" : "1208875",
              "bucType" : "P_BUC_01",
              "timeStamp" : 1606901824349,
              "saksNummer" : "1212121",
              "vetaksId" : "11111",
              "hendelseVersjon" : 222
        }""".trimMargin()

        val model = StatistikkMelding.fromJson(json)

        val result = mapAnyToJson(model)
        JSONAssert.assertEquals(json, result, JSONCompareMode.LENIENT)
    }
}