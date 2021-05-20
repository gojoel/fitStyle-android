package ai.folded.fitstyle.repository

import ai.folded.fitstyle.api.FitStyleApiService
import ai.folded.fitstyle.api.PaymentRequest
import ai.folded.fitstyle.utils.CURRENCY
import com.amplifyframework.auth.cognito.AWSCognitoAuthSession
import com.amplifyframework.auth.result.AuthSessionResult
import com.amplifyframework.core.Amplify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal class PaymentRepository(
    private val retrofitService : FitStyleApiService,
    private val workContext: CoroutineContext
) {

    suspend fun getUserId() : String = suspendCoroutine { continuation ->
        Amplify.Auth.fetchAuthSession(
            {
                val session = it as AWSCognitoAuthSession
                when (session.identityId.type) {
                    AuthSessionResult.Type.SUCCESS -> {
                        continuation.resume(session.identityId.value ?: "")
                    }
                    AuthSessionResult.Type.FAILURE -> {
                        //  TODO: log failure to retrieve identity id
                        continuation.resume("")
                    }
                }
            },
            {
                // TODO: log failure to fetch session
                continuation.resumeWithException(it)
            }
        )
    }

    suspend fun createWatermarkPayment(userId: String, imageKey: String) = withContext(workContext) {
        flowOf(
            kotlin.runCatching {
                retrofitService.createWatermarkPayment(PaymentRequest(userId = userId, imageKey = imageKey, currency = CURRENCY))
            }
        )
    }

    suspend fun removeWatermark(imagePath: String) = withContext(workContext) {
        retrofitService.removeWatermark(imagePath)
    }
}