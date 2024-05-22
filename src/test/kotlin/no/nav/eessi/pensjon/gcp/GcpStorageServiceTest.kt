package no.nav.eessi.pensjon.gcp

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class GcpStorageServiceTest {

    @Test
    fun `Test unscramble funksjonalitet`() {
        val input = GcpStorageService.scramble("Hello World")
        val expectedOutput = "Hello World"

        assertEquals(expectedOutput, GcpStorageService.unscramble(input))
    }
}