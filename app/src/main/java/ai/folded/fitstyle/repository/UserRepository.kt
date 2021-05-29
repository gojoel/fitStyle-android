package ai.folded.fitstyle.repository

import com.amplifyframework.auth.cognito.AWSCognitoAuthSession
import com.amplifyframework.auth.result.AuthSessionResult
import com.amplifyframework.core.Amplify
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Singleton
class UserRepository @Inject constructor() {
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
}