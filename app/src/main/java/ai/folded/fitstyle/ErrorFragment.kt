package ai.folded.fitstyle

import ai.folded.fitstyle.databinding.FragmentErrorBinding
import ai.folded.fitstyle.utils.ERROR_TYPE_STYLE_TRANSFER
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ErrorFragment: Fragment() {

    private val args: ErrorFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentErrorBinding.inflate(inflater, container, false)
        context ?: return binding.root

        binding.restartClickListener = View.OnClickListener {
            this.findNavController().navigate(
                ErrorFragmentDirections.actionErrorToStyleListFragment())
        }

        binding.retryClickListener = View.OnClickListener {
            retry()
        }

        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                retry()
            }
        })
    }

    private fun retry() {
        when (args.errorType) {
            ERROR_TYPE_STYLE_TRANSFER -> {
                args.styleOptions?.let {
                    this.findNavController().navigate(
                        ErrorFragmentDirections.actionErrorToStyleTransferFragment())
                }
            }
        }
    }
}