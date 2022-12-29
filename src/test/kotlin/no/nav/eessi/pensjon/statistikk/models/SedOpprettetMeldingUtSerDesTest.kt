package no.nav.eessi.pensjon.statistikk.models

import no.nav.eessi.pensjon.eux.model.SedType
import no.nav.eessi.pensjon.eux.model.buc.BucType
import no.nav.eessi.pensjon.utils.mapAnyToJson
import no.nav.eessi.pensjon.utils.mapJsonToAny
import no.nav.eessi.pensjon.utils.toJson
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode

internal class SedOpprettetMeldingUtSerDesTest {

    @Test
    fun `Sjekker at serialisering virker`() {
        val model = SedMeldingUt(
            dokumentId = "111",
            bucType = BucType.P_BUC_01,
            rinaId = "222",
            mottakerLand = listOf("NO"),
            rinaDokumentVersjon = "333",
            sedType = SedType.H001,
            pid = "444",
            hendelseType = HendelseType.SED_OPPRETTET,
            pesysSakId = "555",
            opprettetTidspunkt = "2020-12-08T09:52:55.345Z",
            vedtaksId = "666"
        )
        val sedMeldingUtJson = model.toJson()
        val result = mapJsonToAny<SedMeldingUt>(sedMeldingUtJson)

        JSONAssert.assertEquals(sedMeldingUtJson, result.toJson(), JSONCompareMode.LENIENT)
    }


    @Test
    fun `Sjekker at deserialisering gir riktig verdi`() {
        val json = """{
          "dokumentId" : "111",
          "bucType" : "P_BUC_01",
          "rinaId" : "222",
          "mottakerLand" : ["NO"],
          "rinaDokumentVersjon" : "333",
          "sedType" : "H001",
          "pid" : "444",
          "hendelseType" : "SED_OPPRETTET",
          "pesysSakId" : "555",
          "opprettetTidspunkt" : "2020-12-08T09:52:55.345Z",
          "vedtaksId" : "666"
        }""".trimMargin()

        val model = mapJsonToAny<SedMeldingUt>(json)

        val result = mapAnyToJson(model)
        JSONAssert.assertEquals(json, result, JSONCompareMode.LENIENT)
    }
}