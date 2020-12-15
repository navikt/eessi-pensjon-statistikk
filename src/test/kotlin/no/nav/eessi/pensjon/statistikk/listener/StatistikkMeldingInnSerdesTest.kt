package no.nav.eessi.pensjon.statistikk.listener

import no.nav.eessi.pensjon.json.mapAnyToJson
import no.nav.eessi.pensjon.json.toJson
import no.nav.eessi.pensjon.statistikk.models.HendelseType
import no.nav.eessi.pensjon.statistikk.models.StatistikkMeldingInn
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode

class StatistikkMeldingInnSerdesTest {
    @Test
    fun `Sjekker at serialisering virker`() {
        val model = StatistikkMeldingInn(hendelseType = HendelseType.OPPRETTBUC, rinaid =  "1208875", dokumentId = "djksdfsdl3435kj3452")
        val serialized = model.toJson()

        val result = StatistikkMeldingInn.fromJson(serialized)

        JSONAssert.assertEquals(serialized, result.toJson(), JSONCompareMode.LENIENT)
    }

    @Test
    fun `Sjekker at deserialisering gir riktig verdi`() {
        val json = """{
              "hendelseType" : "OPPRETTBUC",
              "rinaid" : "1208875",
              "dokumentId" : "32456365464564"
        }""".trimMargin()

        val model = StatistikkMeldingInn.fromJson(json)

        val result = mapAnyToJson(model)
        JSONAssert.assertEquals(json, result, JSONCompareMode.LENIENT)
    }
}