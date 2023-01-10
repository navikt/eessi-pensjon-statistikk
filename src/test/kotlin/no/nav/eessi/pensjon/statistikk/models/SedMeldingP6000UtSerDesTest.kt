package no.nav.eessi.pensjon.statistikk.models

import no.nav.eessi.pensjon.ResourceHelper
import no.nav.eessi.pensjon.eux.Sed
import no.nav.eessi.pensjon.eux.model.BucType
import no.nav.eessi.pensjon.eux.model.SedType
import no.nav.eessi.pensjon.utils.mapAnyToJson
import no.nav.eessi.pensjon.utils.mapJsonToAny
import no.nav.eessi.pensjon.utils.toJson
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode

internal class SedMeldingP6000UtSerDesTest{

    @Test
    fun `Sjekker at serialisering virker`() {
        val model = SedMeldingP6000Ut(
            dokumentId = "111",
            bucType = BucType.P_BUC_01,
            rinaId = "222",
            mottakerLand = listOf("NO"),
            avsenderLand = "SE",
            rinaDokumentVersjon = "333",
            sedType = SedType.P6000,
            pid = "444",
            hendelseType = HendelseType.SED_OPPRETTET,
            pesysSakId = "555",
            opprettetTidspunkt = "2020-12-08T09:52:55.345Z",
            vedtaksId = "666",
            bostedsland = "NO",
            pensjonsType = PensjonsType.fra("03"),
            vedtakStatus = VedtakStatus.fra("02"),
            bruttoBelop = "1000",
            valuta = "NOK",
            anmodningOmRevurdering = "nei"
        )

        val p6000Json = model.toJson()
        val result =  mapJsonToAny<SedMeldingP6000Ut>(p6000Json)

        JSONAssert.assertEquals(p6000Json, result.toJson(), JSONCompareMode.STRICT)
    }

    @Test
    fun `Sjekker at deserialisering gir riktig verdi`() {
        val p6000Json = """{
              "dokumentId" : "111",
              "bucType" : "P_BUC_01",
              "rinaId" : "222",
              "mottakerLand" : [ "NO" ],
              "avsenderLand" : "SE",
              "rinaDokumentVersjon" : "333",
              "sedType" : "P6000",
              "pid" : "444",
              "hendelseType" : "SED_OPPRETTET",
              "pesysSakId" : "555",
              "opprettetTidspunkt" : "2020-12-08T09:52:55.345Z",
              "vedtaksId" : "666",
              "bostedsland" : "NO",
              "pensjonsType" : "GJENLEV",
              "vedtakStatus" : "AVSLAG",
              "bruttoBelop" : "1000",
              "valuta" : "NOK",
              "anmodningOmRevurdering": "ja"
              }""".trimMargin()

        val model = mapJsonToAny<SedMeldingP6000Ut>(p6000Json)
        assertEquals(model.vedtakStatus, VedtakStatus.AVSLAG)
        assertEquals(model.pensjonsType, PensjonsType.GJENLEV)
        val result = mapAnyToJson(model)

        JSONAssert.assertEquals(p6000Json, result, JSONCompareMode.STRICT)
    }

    @Test
    fun `Sjekker at deserialisering fra model fungerer for alle felt`() {
        val sed = ResourceHelper.getResourceSed("sed/P6000-komplett.json").toJson()
        val model = Sed.fromJson(sed)

        val result = mapAnyToJson(model)
        JSONAssert.assertEquals(sed, result, JSONCompareMode.STRICT)

        val vedtak = model.pensjon?.vedtak?.firstOrNull()
        assertEquals("04", vedtak?.resultat)
        assertEquals("03", vedtak?.type)

        val beregning = model.pensjon?.vedtak?.firstOrNull()?.beregning?.first()!!
        assertEquals("12482", beregning.beloepBrutto?.beloep)
        assertEquals("NOK", beregning.valuta)

        assertEquals("1", model.pensjon?.tilleggsinformasjon?.artikkel48)
        assertEquals("HR", model.nav.bruker?.adresse?.land)
    }
}