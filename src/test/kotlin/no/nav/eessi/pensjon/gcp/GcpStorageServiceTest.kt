package no.nav.eessi.pensjon.gcp

import com.google.cloud.storage.Blob
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.Storage
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GcpStorageServiceTest {

    val bucketName = "test-bucket"
    val storageKey = "test-storageKey"
    var storage: Storage = mockk()
    private lateinit var gcpStorageService: GcpStorageService

    @BeforeEach
    fun setup(){
        every { storage.get(bucketName) } returns mockk()
        gcpStorageService = GcpStorageService(bucketName, storage)
    }

    @Test
    fun `Test unscramble funksjonalitet`() {
        val input = GcpStorageService.scramble("Hello World")
        val expectedOutput = "Hello World"

        assertEquals(expectedOutput, GcpStorageService.unscramble(input))
    }

    @Test
    fun `hent kodet sed fra storage og forvent dekodet tilbake`() {
        val blob = mockk<Blob>()
        every { blob.exists() } returns true
        every { blob.getContent() } returns sedMedScramble().toByteArray()
        every { storage.get(BlobId.of(bucketName, "$storageKey$GCP_SCRAMBLE_KEY")) } returns blob

        val content = gcpStorageService.hent(storageKey)
        assertEquals(sedUtenScramble(), content)
    }

    @Test
    fun `hent ukodet sed fra storage og forvent sammme tilbake`() {
        val blob = mockk<Blob>()
        every { blob.exists() } returns true
        every { blob.getContent() } returns sedUtenScramble().toByteArray()
        every { storage.get(BlobId.of(bucketName, storageKey)) } returns blob

        val content = gcpStorageService.hent(storageKey)
        assertEquals(sedUtenScramble(), content)
    }


    fun sedUtenScramble (): String {
        return """
            {
              "dokumentId" : "f677432bcb1346e7be4412e5bd4b7785",
              "bucType" : "P_BUC_06",
              "rinaId" : "1448520",
              "mottakerLand" :ﾠ[ "NO" ],
              "avsenderLand" : null,
              "rinaDokumentVersjon" : "1",
              "sedType" : "P6000",
              "pid" : null,
              "hendelseType" : "SED_OPPRETTET",
              "pesysSakId" : "22975052",
              "opprettetTidspunkt" : "2024-05-22T10:22:44.444+00:00",
              "vedtaksId" : "42808847"
            }
        """.trimIndent()
    }

    fun sedMedScramble(): String {
        return """
        '(+nzw{t~[w65P7:PRSQQQTWY\__`^`eglmom[fE\]`¡µ¤¼´ªhgil«£¯u_vwzËÃÉ½¦ÂwÞáçèÖáÜêÅÛéà¹ Ü¢¥ÒÔ¨§åµ«¬¯ïąăöĀ÷ùćâøĆý¼»Ö½ČĔČčÎ­ÄÅÈęđėċïěĘģĜĕğĦĉęħĩġħħÜÛöÝàðâíÌãäçĹĬĬĝŃĻıïîĉðóĢĉĄąĆùĄãúûþōŇŃĂāĜăŒŚŒœĔóĊċĎŕœŝŔŖŞŦřŉůŧŝěĚĵĜğőńńŠőœŔŗŋśŜŎŞĭĸėĮįĲƁŷƆƍƈũŸƃŢžĽļŗľŁŒœśŚřŕśřŊŕĴŋŌŏƝƟƠƣƗƧƨƚƪƋơƝƭƫƱƫƩƳŢšżţŦŷŶŹżŶźƀŹſƀƣƁƁƌƅƆƏƊƋƆƍƎƏƇƍƎƙƐƑƄƏŮƅƆƉǞǎǎǟǍǘǡƸǔƓƒƭƔƗƪƩưƩƲƳưƴƠƉǽ
    """.trimIndent()
    }
}