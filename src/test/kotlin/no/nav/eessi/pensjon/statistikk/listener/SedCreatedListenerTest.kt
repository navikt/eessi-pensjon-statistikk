package no.nav.eessi.pensjon.statistikk.listener

import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import no.nav.eessi.pensjon.eux.model.BucType
import no.nav.eessi.pensjon.eux.model.SedType
import no.nav.eessi.pensjon.gcp.SedCreatedRecord
import no.nav.eessi.pensjon.gcp.SedCreatedStorage
import no.nav.eessi.pensjon.statistikk.models.OpprettelseType
import no.nav.eessi.pensjon.utils.toJson
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.NullSource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.kafka.support.Acknowledgment

class SedCreatedListenerTest {
    private val storage = mockk<SedCreatedStorage>()
    private val acknowledgment = mockk<Acknowledgment>(relaxed = true)
    private val listener = SedCreatedListener(storage)

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = ["sak-id"])
    fun `ack for sed etter lagring`(sakId: String?) {
        justRun { storage.store(any()) }

        listener.consume(event(sakId).toJson(), acknowledgment)

        verifyOrder {
            storage.store(SedCreatedRecord(BucType.P_BUC_01, SedType.P2000, "case-id", "document-id", sakId))
            acknowledgment.acknowledge()
        }
    }

    @Test
    fun `opprettelse av BUC skjer med ack`() {
        listener.consume(event().copy(opprettelseType = OpprettelseType.BUC).toJson(), acknowledgment)

        verify(exactly = 0) { storage.store(any()) }
        verify(exactly = 1) { acknowledgment.acknowledge() }
    }

    @Test
    fun `eldre versjoner av SED events uten buctype blir eksplisitt hoppet over`() {
        val legacyEvent = """{
            "opprettelseType": "SED",
            "rinaId": "case-id",
            "dokumentId": "document-id",
            "sedtype": "P2000"
        }"""

        listener.consume(legacyEvent, acknowledgment)

        verify(exactly = 0) { storage.store(any()) }
        verify(exactly = 1) { acknowledgment.acknowledge() }
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `JSON string skal ikke stoppe prosessering`(enriched: Boolean) {
        val sakId = "sak-\"quoted\"\\path\nnext"
        val payload = if (enriched) event(sakId) else event().copy(buctype = null)
        justRun { storage.store(any()) }

        listener.consume("  ${payload.toJson().toJson()}  ", acknowledgment)

        if (enriched) {
            verifyOrder {
                storage.store(SedCreatedRecord(BucType.P_BUC_01, SedType.P2000, "case-id", "document-id", sakId))
                acknowledgment.acknowledge()
            }
        } else {
            verify(exactly = 0) { storage.store(any()) }
            verify(exactly = 1) { acknowledgment.acknowledge() }
        }
    }

    @Test
    fun `meldinger med feil format skal skal ikke faa ack`() {
        assertThrows<RuntimeException> {
            listener.consume("not an event".toJson(), acknowledgment)
        }

        verify(exactly = 0) { storage.store(any()) }
        verify(exactly = 0) { acknowledgment.acknowledge() }
    }

    @Test
    fun `feil i gcp skal kaste exception`() {
        every { storage.store(any()) } throws IllegalStateException("Storage unavailable") andThen Unit
        val payload = event().toJson()

        assertThrows<IllegalStateException> { listener.consume(payload, acknowledgment) }
        verify(exactly = 0) { acknowledgment.acknowledge() }

        listener.consume(payload, acknowledgment)

        verify(exactly = 2) { storage.store(any()) }
        verify(exactly = 1) { acknowledgment.acknowledge() }
    }

    @Test
    fun `nye hendelser med manglende dokument-IDer feiler uten acknowledgment`() {
        assertThrows<IllegalArgumentException> {
            listener.consume(event().copy(dokumentId = null).toJson(), acknowledgment)
        }

        verify(exactly = 0) { storage.store(any()) }
        verify(exactly = 0) { acknowledgment.acknowledge() }
    }

    private fun event(sakId: String? = "sak-id") = OpprettelseMelding(
        opprettelseType = OpprettelseType.SED,
        rinaId = "case-id",
        dokumentId = "document-id",
        vedtaksId = null,
        sedtype = SedType.P2000,
        buctype = BucType.P_BUC_01,
        sakId = sakId
    )
}
