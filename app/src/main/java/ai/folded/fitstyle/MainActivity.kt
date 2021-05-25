package ai.folded.fitstyle

import ai.folded.fitstyle.databinding.ActivityMainBinding
import android.os.Bundle
import androidx.databinding.DataBindingUtil.setContentView
import com.amplifyframework.auth.cognito.AWSCognitoAuthSession
import com.amplifyframework.auth.result.AuthSessionResult
import com.amplifyframework.core.Amplify
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView<ActivityMainBinding>(this, R.layout.activity_main)

        Amplify.Auth.fetchAuthSession(
            {
                val session = it as AWSCognitoAuthSession
                when (session.identityId.type) {
                    AuthSessionResult.Type.SUCCESS -> {

                    }
                    AuthSessionResult.Type.FAILURE -> {
                        //  TODO: log failure to retrieve identity id
                    }
                }
            },
            {
                // TODO: log failure to fetch session
            }
        )
    }
}