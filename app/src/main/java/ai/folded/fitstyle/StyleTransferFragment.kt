package ai.folded.fitstyle

import ai.folded.fitstyle.data.Status
import ai.folded.fitstyle.databinding.FragmentStyleTransferBinding
import ai.folded.fitstyle.utils.ERROR_TYPE_STYLE_TRANSFER
import ai.folded.fitstyle.utils.STYLED_IMG_VIEW_SRC_TRANSFER
import ai.folded.fitstyle.viewmodels.StyleTransferViewModel
import ai.folded.fitstyle.viewmodels.StyleTransferViewModelFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class StyleTransferFragment: Fragment() {

    private val args: StyleTransferFragmentArgs by navArgs()

    @Inject
    lateinit var styleTransferViewModelFactory: StyleTransferViewModelFactory

    private val styleTransferViewModel: StyleTransferViewModel by viewModels {
        StyleTransferViewModel.provideFactory(styleTransferViewModelFactory, args.styleOptions)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentStyleTransferBinding.inflate(inflater, container, false)
            .apply {
                viewModel = styleTransferViewModel
                lifecycleOwner = viewLifecycleOwner
            }

        binding.toolbar.setTitle(R.string.apply_style_title)
        binding.toolbar.setNavigationOnClickListener { view ->
            onBackPressed()
        }

        styleTransferViewModel.response.observe(viewLifecycleOwner, {
            if (it != null) {
                this.findNavController().navigate(
                    StyleTransferFragmentDirections.actionStyleTransferToStyleResultFragment(
                        it,
                        STYLED_IMG_VIEW_SRC_TRANSFER
                    )
                )
                styleTransferViewModel.clearTransferState()
            }
        })

        styleTransferViewModel.status.observe(viewLifecycleOwner, {
            if (it != null && it == Status.FAILED) {

                // dismiss cancellation dialog if visible
                val dialogFragment = childFragmentManager.findFragmentByTag(SimpleDialogFragment.TAG)
                (dialogFragment as? DialogFragment)?.dismiss()

                this.findNavController().navigate(
                    StyleTransferFragmentDirections.actionStyleTransferToErrorFragment(
                        ERROR_TYPE_STYLE_TRANSFER,
                        styleOptions = args.styleOptions
                    )
                )
                // Tell the ViewModel we've made the navigate call to prevent multiple navigation
                styleTransferViewModel.clearTransferState()
            }
        })

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPressed()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (styleTransferViewModel.status.value == null) {
            styleTransferViewModel.startStyleTransfer()
        }
    }

    private fun onBackPressed() {
        val dialog = SimpleDialogFragment.newInstance(
            messageRes = R.string.style_transfer_cancel_details,
            titleRes = R.string.style_transfer_cancel_title,
            positiveButtonTitleRes = R.string.style_transfer_continue,
            negativeButtonTitleRes = R.string.style_transfer_confirm_cancel
        )

        dialog.negativeButtonClick.observe(viewLifecycleOwner) {
            activity?.lifecycleScope?.launch {
                styleTransferViewModel.cancelStyleTransfer()
            }

            dialog.dismiss()
            this.findNavController().navigateUp()
        }

        dialog.positiveButtonClick.observe(viewLifecycleOwner) {
            dialog.dismiss()
        }

        dialog.show(childFragmentManager, SimpleDialogFragment.TAG)
    }
}