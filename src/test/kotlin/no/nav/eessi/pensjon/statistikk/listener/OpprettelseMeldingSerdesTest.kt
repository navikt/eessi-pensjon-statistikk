package no.nav.eessi.pensjon.statistikk.listener

import no.nav.eessi.pensjon.statistikk.models.OpprettelseType
import no.nav.eessi.pensjon.utils.mapAnyToJson
import no.nav.eessi.pensjon.utils.mapJsonToAny
import no.nav.eessi.pensjon.utils.toJson
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode

class OpprettelseMeldingSerdesTest {

    @Test
    fun `Sjekker at serialisering virker`() {
        val model = OpprettelseMelding(opprettelseType = OpprettelseType.BUC, rinaId =  "1208875", dokumentId = "djksdfsdl3435kj3452", vedtaksId = null)
        val serialized = model.toJson()

        val result = mapJsonToAny<OpprettelseMelding>(serialized)

        JSONAssert.assertEquals(serialized, result.toJson(), JSONCompareMode.LENIENT)
    }

    @Test
    fun `Sjekker at deserialisering gir riktig verdi`() {
        val json = """{
              "opprettelseType" : "BUC",
              "rinaId" : "1208875",
              "dokumentId" : "32456365464564"
        }""".trimMargin()

        val model = mapJsonToAny<OpprettelseMelding>(json)


        val result = mapAnyToJson(model)
        JSONAssert.assertEquals(json, result, JSONCompareMode.LENIENT)
    }
}