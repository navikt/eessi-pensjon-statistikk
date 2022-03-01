package no.nav.eessi.pensjon.services.storage.amazons3

import com.google.cloud.NoCredentials
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import no.nav.eessi.pensjon.gcp.GcpStorageService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Testcontainers
import java.net.ServerSocket
import java.time.LocalDateTime

@Testcontainers
@ActiveProfiles("integrationtest")
@Disabled
class S3StorageServiceTest {

    private lateinit var gcpStorageService: GcpStorageService
    private lateinit var gcs : GcpTestContainer

    @BeforeEach
    fun setup() {
        gcs = GcpTestContainer("fsouza/fake-gcs-server")
            .withExposedPorts(4443)
            .withClasspathResourceMapping("data", "/data", BindMode.READ_WRITE)
            .withCreateContainerCmdModifier {
                it.withEntrypoint("/bin/fake-gcs-server", "-data", "/data", "-scheme", "http")
            }
        gcs.start()

        val storage: Storage = StorageOptions.newBuilder()
            .setCredentials(NoCredentials.getInstance())
            .setHost("http://${gcs.host}:${gcs.firstMappedPort}")
            .build()
            .service

        gcpStorageService = GcpStorageService( "bucket", storage)
    }

    @AfterEach
    fun teardown() {
        gcs.stop()
    }

    @Test
    fun `add files in different directories and list them all`() {
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

/*    @Test
    fun `add multiple files and list them`() {
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
    fun `lagre file into bucket, list it, read it back and finally delete it`() {
        val directory = "P2000"
        val aktoerId = "12435678910"
        val value = "A string that has to be persisted.\nAnd this line too."

        gcpStorageService.lagre(aktoerId + "___" + "$directory/testfile.txt", value)

        val fileList = gcpStorageService.list(aktoerId + "___" + directory)
        assertEquals(1, fileList.size, "Expect that 1 entry is returned")

        val fetchedValue = gcpStorageService.hent(fileList[0])
        assertEquals(value, fetchedValue, "The stored and fetched values should be equal")

        gcpStorageService.slett(fileList[0])

        val fileListAfterDelete = gcpStorageService.list(aktoerId + "___" + directory)
        assertEquals(0, fileListAfterDelete.size, "Expect that 0 entries are returned")
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
    }*/

    fun randomOpenPort(): Int = ServerSocket(0).use { it.localPort }
}
class GcpTestContainer (imageName: String) : GenericContainer<GcpTestContainer>(imageName)