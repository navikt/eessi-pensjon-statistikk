package no.nav.eessi.pensjon.statistikk

import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import io.findify.s3mock.S3Mock
import no.nav.eessi.pensjon.s3.S3StorageService
import java.net.ServerSocket
import java.util.*

class S3StorageHelper {

    companion object {

        fun createStoreService(): S3StorageService {
            val s3Port = ServerSocket(randomFrom()).use { it.localPort }

            S3Mock.Builder().withPort(s3Port).withInMemoryBackend()
                .build()
                .also { it.start() }

            val s3MockClient = AmazonS3ClientBuilder.standard()
                .withPathStyleAccessEnabled(true)
/*
                .withCredentials(AWSStaticCredentialsProvider(AnonymousAWSCredentials()))
*/
                .withEndpointConfiguration(
                    AwsClientBuilder.EndpointConfiguration("http://localhost:$s3Port", "us-east-1")
                )
                .build()
                .also { it.createBucket("eessi-pensjon-statistikk") }

            return S3StorageService(s3MockClient).also {
                it.bucketname  = s3MockClient.listBuckets().get(0).name
                it.env = "q1"
            }
        }
        fun randomFrom(from: Int = 1024, to: Int = 65535): Int {
            return Random().nextInt(to - from) + from
        }
    }

}