package no.nav.eessi.pensjon.statistikk

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.AnonymousAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import io.findify.s3mock.S3Mock
import no.nav.eessi.pensjon.s3.S3StorageService
import java.net.ServerSocket

class S3StorageHelper {

    companion object {

        fun createStoreService(): S3StorageService {
            val s3Port = ServerSocket(0).use { it.localPort }

            S3Mock.Builder().withPort(s3Port).withInMemoryBackend()
                .build()
                .also { it.start() }

            val s3MockClient = AmazonS3ClientBuilder.standard()
                .withPathStyleAccessEnabled(true)
                .withCredentials(AWSStaticCredentialsProvider(AnonymousAWSCredentials()))
                .withEndpointConfiguration(
                    AwsClientBuilder.EndpointConfiguration("http://localhost:$s3Port", "us-east-1")
                )
                .build()
                .also { it.createBucket("eessipensjon") }

            return S3StorageService(s3MockClient).also {
                it.bucketname  = s3MockClient.listBuckets().get(0).name
                it.env = "q1"
            }
        }
    }
}