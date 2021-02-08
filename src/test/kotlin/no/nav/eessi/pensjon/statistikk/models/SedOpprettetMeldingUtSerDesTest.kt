package no.nav.eessi.pensjon.statistikk.models

import no.nav.eessi.pensjon.eux.BucType
import no.nav.eessi.pensjon.eux.SedType
import no.nav.eessi.pensjon.json.mapAnyToJson
import no.nav.eessi.pensjon.json.toJson
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode

internal class SedOpprettetMeldingUtSerDesTest {
    @Test
    fun `Sjekker at serialisering virker`() {
        val model = SedOpprettetMeldingUt(
            dokumentId = "111",
            bucType = BucType.P_BUC_01,
            rinaid = "222",
            mottakerLand = listOf("NO"),
            rinaDokumentVersjon = "333",
            sedType = SedType.H001,
            pid = "444",
            hendelseType = HendelseType.SED_OPPRETTET,
            pesysSakId = "555",
            opprettetDato = "2020-12-08T09:52:55.345Z",
            vedtaksId = "666"
        )
        val serialized = model.toJson()
        print(serialized)
        val result = SedOpprettetMeldingUt.fromJson(serialized)

        JSONAssert.assertEquals(serialized, result.toJson(), JSONCompareMode.LENIENT)
    }


    @Test
    fun `Sjekker at deserialisering gir riktig verdi`() {
        val json = """{
          "dokumentId" : "111",
          "bucType" : "P_BUC_01",
          "rinaid" : "222",
          "mottakerLand" : ["NO"],
          "rinaDokumentVersjon" : "333",
          "sedType" : "H001",
          "pid" : "444",
          "hendelseType" : "SED_OPPRETTET",
          "pesysSakId" : "555",
          "opprettetDato" : "2020-12-08T09:52:55.345Z",
          "vedtaksId" : "666"
        }""".trimMargin()

        val model = SedOpprettetMeldingUt.fromJson(json)

        val result = mapAnyToJson(model)
        JSONAssert.assertEquals(json, result, JSONCompareMode.LENIENT)
    }
}