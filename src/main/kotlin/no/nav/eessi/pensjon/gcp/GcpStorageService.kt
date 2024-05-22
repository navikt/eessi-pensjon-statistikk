package no.nav.eessi.pensjon.gcp

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.ByteBuffer


//TODO: Slettes når vi er sikre på at gamle verdier ikke vil bli hentet
private const val GCP_SCRAMBLE_KEY = "_SRCAMBLE"

@Component
class GcpStorageService( @param:Value("\${GCP_BUCKET_NAME}") var bucketname: String,  private val gcpStorage: Storage) {
    private val logger = LoggerFactory.getLogger(GcpStorageService::class.java)

    init {
        ensureBucketExists()
    }

    companion object {
        private const val SCRAMBLE_KEY = 5

        fun scramble(input: String): String = input.mapIndexed { index, char ->
            (char.code + SCRAMBLE_KEY + index).toChar()
        }.joinToString("")

        fun unscramble(input: String): String =
            input.mapIndexed { index, char -> (char.code - SCRAMBLE_KEY - index).toChar() }.joinToString("")
    }


    private fun ensureBucketExists() {
        when (gcpStorage.get(bucketname) != null) {
            false -> throw IllegalStateException("Fant ikke bucket med navn $bucketname. Må provisjoneres")
            true -> logger.info("Bucket $bucketname funnet.")
        }
    }

    fun lagre(storageKey: String, storageValue: String) {
        val blobInfo =  BlobInfo.newBuilder(BlobId.of(bucketname, storageKey + GCP_SCRAMBLE_KEY)).setContentType("application/json").build()

        // legger til en enkel obfuskering av data
        val scrambledString  = scramble(storageValue)

        kotlin.runCatching {
            gcpStorage.writer(blobInfo).use {
                it.write(ByteBuffer.wrap(scrambledString.toByteArray()))
            }
        }.onFailure { e ->
            logger.warn("Feilet med å lagre dokument med id: ${blobInfo.blobId.name}", e)
        }.onSuccess {
            logger.info("Lagret fil med blobid:  ${blobInfo.blobId.name} og bytes: $it")
        }
    }

    fun hent(storageKey: String): String? {
            // søker etter keys med og uten scramble
            for (key in listOf(storageKey, storageKey + GCP_SCRAMBLE_KEY)) {
                try {
                    val blob = gcpStorage.get(BlobId.of(bucketname, key))
                    if (blob.exists()) {
                        val content = blob.getContent().decodeToString()
                        return if (key == storageKey) {
                            logger.info("Blob med key:$storageKey funnet")
                            content
                        } else {
                            logger.info("Blob med key fra obfuskert mapping :$storageKey funnet")
                            unscramble(content)
                        }
                    }
                } catch (ex: Exception) {
                    logger.warn("En feil oppstod under henting av objekt: $storageKey i bucket", ex)
                }
            }
        return null
    }

    fun list(keyPrefix: String) : List<String> {
        return gcpStorage.list(bucketname , Storage.BlobListOption.prefix(keyPrefix))?.values?.map { v -> v.name}  ?:  emptyList()
    }

}