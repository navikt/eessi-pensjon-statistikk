package no.nav.eessi.pensjon.gcp

import com.fasterxml.jackson.annotation.JsonInclude
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageException
import no.nav.eessi.pensjon.eux.model.BucType
import no.nav.eessi.pensjon.eux.model.SedType
import no.nav.eessi.pensjon.utils.toJson
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@JsonInclude(JsonInclude.Include.ALWAYS)
data class SedCreatedRecord(
    val buctype: BucType,
    val sedtype: SedType,
    val euxCaseId: String,
    val documentId: String,
    val sakId: String?
)

@Component
@ConditionalOnProperty(name = ["sed-created.enabled"], havingValue = "true")
class SedCreatedStorage(
    @param:Value("\${sed-created.bucket}") private val bucket: String,
    private val gcpStorage: Storage
) {
    init {
        require(bucket.isNotBlank()) { "SED creation tracking requires a bucket" }
    }

    fun store(record: SedCreatedRecord) {
        val blobInfo = BlobInfo.newBuilder(
            BlobId.of(bucket, "sed-created/${record.euxCaseId}/${record.documentId}.json")
        ).setContentType("application/json").build()

        try {
            gcpStorage.create(
                blobInfo,
                record.toJson().toByteArray(Charsets.UTF_8),
                Storage.BlobTargetOption.doesNotExist()
            )
        } catch (ex: StorageException) {
            if (ex.code != 412) throw ex
        }
    }
}
