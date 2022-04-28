package no.nav.eessi.pensjon.gcp

import com.google.cloud.NoCredentials
import com.google.cloud.storage.BucketInfo
import com.google.cloud.storage.StorageOptions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@ActiveProfiles("integrationtest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // Because we are using fixedPort testcontainer
class S3StorageServiceTest {

    companion object {
        private const val FIXED_HOST_PORT = 44444
        private const val testBucket = "test-bucket"
    }

    private val gcpStorageService by lazy {
        val gcs = GoogleCloudStorageTestcontainer.createAndStart(FIXED_HOST_PORT)
        val fakeGcsExternalUrl = "http://" + gcs.host + ":" + FIXED_HOST_PORT

        val storage=  StorageOptions.newBuilder()
            .setCredentials(NoCredentials.getInstance())
            .setHost(fakeGcsExternalUrl)
            .build()
            .service.also { it.create(BucketInfo.of(testBucket)) }

        GcpStorageService( "test-bucket", storage)
    }

    @Test
    fun `Add files in different directories and list them all`() {
        val aktoerId1 = "14725802541"
        val p2000Directory = "P2000"
        val p4000Directory = "P4000"

        val p2000value = "Final P2000-document"
        val p4000value = "Final P4000-document"

        gcpStorageService.lagre(aktoerId1 + "___" + "$p2000Directory/${LocalDateTime.now()}/document.txt", p2000value)
        gcpStorageService.lagre(aktoerId1 + "___" + "$p4000Directory/${LocalDateTime.now()}/document.txt", p4000value)

        val fileListAktoer1 = gcpStorageService.list(aktoerId1)
        assertEquals(2, fileListAktoer1.size)

        val aktoerId2 = "25896302020"

        gcpStorageService.lagre(aktoerId2 + "___" + "$p2000Directory/${LocalDateTime.now()}/document.txt", p2000value)
        gcpStorageService.lagre(aktoerId2 + "___" + "$p4000Directory/${LocalDateTime.now()}/document.txt", p4000value)

        val fileListAktoer2 = gcpStorageService.list(aktoerId2)
        assertEquals(2, fileListAktoer2.size)
    }

    @Test
    fun `Add multiple files and list them`() {
        val directory = "P4000"
        val aktoerId = "12345678910"

        val value1 = "First draft"
        val timestamp1 = LocalDateTime.now().minusHours(5)

        gcpStorageService.lagre(aktoerId + "___" + "$directory/$timestamp1/document.txt", value1)

        val value2 = "Second draft"
        val timestamp2 = LocalDateTime.now().minusHours(2)
        gcpStorageService.lagre(aktoerId + "___" + "$directory/$timestamp2/document.txt", value2)

        val value3 = "Final document"
        val timestamp3 = LocalDateTime.now()
        gcpStorageService.lagre(aktoerId + "___" + "$directory/$timestamp3/document.txt", value3)

        val fileList = gcpStorageService.list(aktoerId + "___" + directory)
        assertEquals(3, fileList.size)

        val fetchtedValue1 = gcpStorageService.hent(fileList[0])
        val fetchtedValue2 = gcpStorageService.hent(fileList[1])
        val fetchtedValue3 = gcpStorageService.hent(fileList[2])

        assertEquals(value1, fetchtedValue1)
        assertEquals(value2, fetchtedValue2)
        assertEquals(value3, fetchtedValue3)
    }
    @Test
    fun `Given a logged in saksbehandler when listing in S3 then allow listing files for all citizens`() {
        val aktoerId1 = "12345678910"
        val aktoerId2 = "12345678911"

        val p2000value = "Final P2000-document"
        val p4000value = "Final P4000-document"

        gcpStorageService.lagre(aktoerId1 + "___" + "${LocalDateTime.now()}/document.txt", p2000value)
        gcpStorageService.lagre(aktoerId2 + "___" + "${LocalDateTime.now()}/document.txt", p4000value)

        val fileListAktoer1 = gcpStorageService.list(aktoerId2)
        assertEquals(1, fileListAktoer1.size)
    }
}
