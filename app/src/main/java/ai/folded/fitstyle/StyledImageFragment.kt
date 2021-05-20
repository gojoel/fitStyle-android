package ai.folded.fitstyle

import ai.folded.fitstyle.databinding.FragmentStyledImageBinding
import ai.folded.fitstyle.utils.STYLED_IMG_VIEW_SRC_TRANSFER
import ai.folded.fitstyle.viewmodels.StyledImageViewModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class StyledImageFragment: Fragment() {
    private val args: StyledImageFragmentArgs by navArgs()

    private val viewModel: StyledImageViewModel by viewModels {
        StyledImageViewModel.Factory(
            args.styledImage
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding  = FragmentStyledImageBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel

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

        return binding.root
    }
}