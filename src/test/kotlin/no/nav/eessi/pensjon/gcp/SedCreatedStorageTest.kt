package no.nav.eessi.pensjon.gcp

import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageException
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.nav.eessi.pensjon.eux.model.BucType
import no.nav.eessi.pensjon.eux.model.SedType
import no.nav.eessi.pensjon.utils.mapJsonToAny
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.NullSource
import org.junit.jupiter.params.provider.ValueSource

class SedCreatedStorageTest {
    private val gcpStorage = mockk<Storage>()
    private val storage = SedCreatedStorage("tracking-bucket", gcpStorage)

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = ["sak-id"])
    fun `lagrer kun de fem feltene`(sakId: String?) {
        val blob = slot<BlobInfo>()
        val content = slot<ByteArray>()
        every {
            gcpStorage.create(capture(blob), capture(content), Storage.BlobTargetOption.doesNotExist())
        } returns mockk()

        storage.store(record(sakId))

        assertEquals("tracking-bucket", blob.captured.bucket)
        assertEquals("sed-created/case-id/document-id.json", blob.captured.name)
        assertEquals("application/json", blob.captured.contentType)
        assertEquals(
            mapOf(
                "buctype" to "P_BUC_01",
                "sedtype" to "P2000",
                "euxCaseId" to "case-id",
                "documentId" to "document-id",
                "sakId" to sakId
            ),
            mapJsonToAny<Map<String, Any?>>(content.captured.decodeToString())
        )
    }

    @Test
    fun `replay skal ikke overskrive det opprinnelige objektet`() {
        every {
            gcpStorage.create(any<BlobInfo>(), any<ByteArray>(), Storage.BlobTargetOption.doesNotExist())
        } throws StorageException(412, "Object already exists")

        storage.store(record())

        verify(exactly = 1) {
            gcpStorage.create(any<BlobInfo>(), any<ByteArray>(), Storage.BlobTargetOption.doesNotExist())
        }
    }

    @Test
    fun `storage failures blir propagert slik at Kafka kan retry`() {
        val failure = StorageException(503, "Storage unavailable")
        every {
            gcpStorage.create(any<BlobInfo>(), any<ByteArray>(), Storage.BlobTargetOption.doesNotExist())
        } throws failure

        assertEquals(failure, assertThrows<StorageException> { storage.store(record()) })
    }

    private fun record(sakId: String? = "sak-id") =
        SedCreatedRecord(BucType.P_BUC_01, SedType.P2000, "case-id", "document-id", sakId)
}
