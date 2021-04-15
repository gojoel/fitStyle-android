package ai.folded.fitstyle.utils

import ai.folded.fitstyle.BuildConfig
import com.amazonaws.AmazonClientException
import com.amazonaws.HttpMethod
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest
import com.amplifyframework.core.Amplify
import com.amplifyframework.storage.s3.AWSS3StoragePlugin
import java.net.URL
import java.util.*
import java.util.concurrent.TimeUnit

object AwsUtils {
    fun generateUrl(key: String): URL? {
        val plugin = Amplify.Storage.getPlugin("awsS3StoragePlugin") as AWSS3StoragePlugin
        val client = plugin.escapeHatch

        val request =
            GeneratePresignedUrlRequest(BuildConfig.BUCKET_NAME, key)
                .withMethod(HttpMethod.GET)
                .withExpiration(getExpiration(URL_EXPIRATION_SEC))

        return try {
            client.generatePresignedUrl(request)
        } catch (e: AmazonClientException) {
            // TODO: log error
            null
        }
    }

    private fun getExpiration(timeInSeconds: Long): Date? {
        val expiration = Date()
        var msec: Long = expiration.time
        msec += TimeUnit.SECONDS.toMillis(timeInSeconds);
        expiration.time = msec
        return expiration
    }
}