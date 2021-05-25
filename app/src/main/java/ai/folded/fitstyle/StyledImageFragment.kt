package ai.folded.fitstyle

import ai.folded.fitstyle.databinding.FragmentStyledImageBinding
import ai.folded.fitstyle.utils.STYLED_IMG_VIEW_SRC_TRANSFER
import ai.folded.fitstyle.viewmodels.StyledImageViewModel
import ai.folded.fitstyle.viewmodels.StyledImageViewModelFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class StyledImageFragment: Fragment() {
    private val args: StyledImageFragmentArgs by navArgs()

    @Inject
    lateinit var styledImageViewModelFactory: StyledImageViewModelFactory

    private val styledImageViewModel: StyledImageViewModel by viewModels {
        StyledImageViewModel.provideFactory(styledImageViewModelFactory, args.styledImage.requestId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding  = FragmentStyledImageBinding.inflate(inflater, container, false)
            .apply {
                viewModel = styledImageViewModel
                lifecycleOwner = viewLifecycleOwner
            }

        binding.toolbar.setTitle(R.string.styled_image_title)
        binding.toolbar.setNavigationOnClickListener { view ->
            view.findNavController().navigateUp()
        }

        when (args.navSource) {
            STYLED_IMG_VIEW_SRC_TRANSFER -> {}
            else -> {
                binding.retryButton.visibility = View.GONE
            }
        }

        binding.purchaseButton.setOnClickListener {
            this.findNavController().navigate(
                StyledImageFragmentDirections.actionStyledImageToPaymentFragment(args.styledImage))
        }

        styledImageViewModel.styledImage.observe(viewLifecycleOwner, {
            if (it.purchased) {
                binding.purchaseButton.visibility = View.GONE

                val constraintSet = ConstraintSet()
                constraintSet.clone(binding.topButtonLayout)
                constraintSet.connect(R.id.save_button, ConstraintSet.END, R.id.top_button_layout, ConstraintSet.END, 0)
                constraintSet.connect(R.id.share_button, ConstraintSet.TOP, R.id.save_button, ConstraintSet.BOTTOM, 16)
                constraintSet.connect(R.id.share_button, ConstraintSet.START, R.id.top_button_layout, ConstraintSet.START, 0)
                constraintSet.connect(R.id.share_button, ConstraintSet.END, R.id.top_button_layout, ConstraintSet.END, 0)
                constraintSet.applyTo(binding.topButtonLayout)
            }
        })

        return binding.root
    }
}