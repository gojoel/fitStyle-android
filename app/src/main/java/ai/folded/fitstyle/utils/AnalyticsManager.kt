package ai.folded.fitstyle.utils

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.os.bundleOf
import com.amplifyframework.auth.cognito.AWSCognitoAuthSession
import com.amplifyframework.core.Amplify
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@SuppressLint("MissingPermission")
class AnalyticsManager @Inject constructor(
    @ApplicationContext private val context: Context,) {

    enum class FitstyleError(val type: String) {
        STYLE_TRANSFER("error_style_transfer"),
        STORAGE("error_storage"),
        AMPLIFY("error_amplify"),
        STYLED_IMAGE("error_styled_image"),
        SHARE("error_share"),
        DOWNLOAD("error_download"),
        WATERMARK("error_watermark"),
        USER("error_user"),
        PAYMENT("error_payment"),
    }

    enum class FitstyleEvent(val type: String) {
        TAPPED_PURCHASE("clicked_purchase_button"),
        TAPPED_SHARE("clicked_share_button"),
        COMPLETED_TUTORIAL("completed_tutorial"),
        SKIPPED_TUTORIAL("skipped_tutorial")
    }

    private var firebaseAnalytics : FirebaseAnalytics = FirebaseAnalytics.getInstance(context)

    fun logEvent(event: FitstyleEvent) {
        getUserId {
            firebaseAnalytics.logEvent("android_event", bundleOf(
                "type" to event.type,
                "user_id" to it
            ))
        }
    }

    fun logError(error: FitstyleError, description: String?) {
        getUserId {
            firebaseAnalytics.logEvent("android_error", bundleOf(
                "type" to error.type,
                "description" to description,
                "user_id" to it
            ))
        }
    }

    private fun getUserId(callback: ((userId: String?) -> Unit)) {
        Amplify.Auth.fetchAuthSession(
            {
                val session = it as AWSCognitoAuthSession
                callback.invoke(session.identityId.value)
            },
            {
                callback.invoke("")
            }
        )
    }
}