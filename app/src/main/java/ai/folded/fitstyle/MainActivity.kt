package ai.folded.fitstyle

import ai.folded.fitstyle.databinding.ActivityMainBinding
import ai.folded.fitstyle.viewmodels.MainViewModel
import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil.setContentView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        viewModel.fetchUser()
    }
}