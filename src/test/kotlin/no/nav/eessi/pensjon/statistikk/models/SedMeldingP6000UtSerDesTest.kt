package no.nav.eessi.pensjon.statistikk.models

import no.nav.eessi.pensjon.eux.BucType
import no.nav.eessi.pensjon.eux.SedType
import no.nav.eessi.pensjon.json.mapAnyToJson
import no.nav.eessi.pensjon.json.toJson
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode

internal class SedMeldingP6000UtSerDesTest{
    @Test
    fun `Sjekker at serialisering virker`() {
        val model = SedMeldingP6000Ut(
            dokumentId = "111",
            bucType = BucType.P_BUC_01,
            rinaid = "222",
            mottakerLand = listOf("NO"),
            rinaDokumentVersjon = "333",
            sedType = SedType.P6000,
            pid = "444",
            hendelseType = HendelseType.SED_OPPRETTET,
            pesysSakId = "555",
            opprettetTidspunkt = "2020-12-08T09:52:55.345Z",
            vedtaksId = "666",
            bostedsland = "NO",
            pensjonsType = "03",
            vedtakStatus = "02",
            bruttoBelop = "1000",
            nettoBelop = "800",
            valuta = "NOK",
            anmodningOmRevurdering = "1"
        )

        val serialized = model.toJson()
        print(serialized)
        val result = SedMeldingP6000Ut.fromJson(serialized)

        JSONAssert.assertEquals(serialized, result.toJson(), JSONCompareMode.STRICT)
    }

    @Test
    fun `Sjekker at deserialisering gir riktig verdi`() {
        val json = """{
              "dokumentId" : "111",
              "bucType" : "P_BUC_01",
              "rinaid" : "222",
              "mottakerLand" : [ "NO" ],
              "rinaDokumentVersjon" : "333",
              "sedType" : "P6000",
              "pid" : "444",
              "hendelseType" : "SED_OPPRETTET",
              "pesysSakId" : "555",
              "opprettetTidspunkt" : "2020-12-08T09:52:55.345Z",
              "vedtaksId" : "666",
              "bostedsland" : "NO",
              "pensjonsType" : "03",
              "vedtakStatus" : "02",
              "bruttoBelop" : "1000",
              "nettoBelop" : "800",
              "valuta" : "NOK",
              "anmodningOmRevurdering" : "1"}""".trimMargin()

        val model = SedMeldingP6000Ut.fromJson(json)

        val result = mapAnyToJson(model)
        JSONAssert.assertEquals(json, result, JSONCompareMode.STRICT)
    }
}