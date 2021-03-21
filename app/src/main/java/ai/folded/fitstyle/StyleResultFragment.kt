package ai.folded.fitstyle

import ai.folded.fitstyle.databinding.FragmentStyleResultBinding
import ai.folded.fitstyle.viewmodels.StyleResultViewModel
import ai.folded.fitstyle.viewmodels.StyleResultViewModelFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class StyleResultFragment: Fragment() {

    private val args: StyleResultFragmentArgs by navArgs()

    @Inject
    lateinit var styleResultViewModelFactory: StyleResultViewModelFactory

    private val styleResultViewModel: StyleResultViewModel by viewModels {
        StyleResultViewModel.provideFactory(styleResultViewModelFactory, args.result)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentStyleResultBinding.inflate(inflater, container, false)
            .apply {
                viewModel = styleResultViewModel
                lifecycleOwner = viewLifecycleOwner
            }

        binding.toolbar.setTitle(R.string.style_result_title)
        binding.toolbar.setNavigationOnClickListener { view ->
            view.findNavController().navigateUp()
        }

        return binding.root

    }
}