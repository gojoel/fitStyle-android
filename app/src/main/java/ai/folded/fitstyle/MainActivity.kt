package ai.folded.fitstyle

import ai.folded.fitstyle.databinding.ActivityMainBinding
import ai.folded.fitstyle.utils.PREF_KEY_USER_ID
import ai.folded.fitstyle.viewmodels.MainViewModel
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil.setContentView
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.NavHostFragment
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private var navController: NavController? = null

    private val listener = NavController.OnDestinationChangedListener { controller, destination, _ ->
        val bundle = Bundle()
        val currentFragmentClassName = (controller.currentDestination as FragmentNavigator.Destination).className
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, destination.label.toString())
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, currentFragmentClassName)
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView<ActivityMainBinding>(this, R.layout.activity_main)

        firebaseAnalytics = Firebase.analytics
        setupNavigationListener()

        viewModel.userId.observe(this, { userId ->
            if (userId.isNotEmpty()) {
                with (this.getPreferences(Context.MODE_PRIVATE).edit()) {
                    putString(PREF_KEY_USER_ID, userId)
                    apply()
                }
            }
        })

        viewModel.fetchUser()
    }

    private fun setupNavigationListener() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        navController = navHostFragment.navController
        navController?.addOnDestinationChangedListener(listener)
    }

    override fun onDestroy() {
        super.onDestroy()
        navController?.removeOnDestinationChangedListener(listener)
    }
}