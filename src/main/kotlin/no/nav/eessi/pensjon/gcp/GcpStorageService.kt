package no.nav.eessi.pensjon.gcp

import com.google.cloud.storage.Blob
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.ByteBuffer
import java.nio.channels.WritableByteChannel


@Component
class GcpStorageService(
    @param:Value("\${eessi.pensjon.statistikk.s3.bucket.name}") var bucketname: String,
    private val gcpStorage: Storage
) {
    private val logger = LoggerFactory.getLogger(GcpStorageService::class.java)

    init {
        ensureBucketExists()
    }

    private fun ensureBucketExists() {
        when (gcpStorage.get(bucketname) != null) {
            false -> throw IllegalStateException("Fant ikke bucket med navn $bucketname. Må provisjoneres")
            true -> logger.info("Bucket $bucketname funnet.")
        }
    }

    fun lagre(storageKey: String, storageValue: String) {

        //val blobInfo =  BlobInfo.newBuilder(BucketInfo.of(bucketname), storageKey).build()
        val blob: Blob = gcpStorage.get(BlobId.of(bucketname, "bucket.txt"))

        if(blob.exists()){
            val channel: WritableByteChannel = blob.writer()
            channel.write(ByteBuffer.wrap(storageValue.toByteArray()))
            kotlin.runCatching {
                gcpStorage.writer(blob)
/*            gcpStorage.writer(blobInfo).use {
                it.write(ByteBuffer.wrap(storageValue.toByteArray()))
            }*/
            }.onFailure { e ->
                logger.error("Feilet med å lagre dokument med id: ${blob.blobId.name}", e)
            }.onSuccess {
                logger.info("Lagret fil med blobid:  ${blob.blobId.name} og bytes: $it  and value: $storageValue")
            }

        }
        else{
            logger.info("Finner ikke lagret bucket $bucketname, genererer ny")
        }
    }

    fun hent(storageKey: String): String {
        return gcpStorage.get(BlobId.of(bucketname, storageKey)).getContent().toString() ?: throw RuntimeException("FIXME")
    }

    fun slett(storageKey: String) : Boolean {
        val value = hent(storageKey)
        return if (value == null) false else {
            return try {
                gcpStorage.delete(bucketname, storageKey)
                true
            } catch (cause: StorageException) {
                logger.warn("Sletting av dokument med id ${storageKey} feilet.", cause)
                false
            }
        }
    }

    fun list(keyPrefix: String) : List<String> {
        return gcpStorage.list(bucketname, Storage.BlobListOption.prefix(keyPrefix)).values?.map { v -> v.name } ?: emptyList()
    }
}