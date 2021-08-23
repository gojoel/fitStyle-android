package ai.folded.fitstyle

import ai.folded.fitstyle.databinding.FragmentSimpleDialogBinding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class SimpleDialogFragment : DialogFragment() {

    private var _binding: FragmentSimpleDialogBinding? = null

    private val binding get() = _binding!!

    private val _positiveButtonClick = MutableLiveData<Boolean>()

    val positiveButtonClick: LiveData<Boolean>
        get() = _positiveButtonClick

    private val _negativeButtonClick = MutableLiveData<Boolean>()

    val negativeButtonClick: LiveData<Boolean>
        get() = _negativeButtonClick

    companion object {

        const val TAG = "SingleButtonDialog"

        private const val IMAGE = "image"

        private const val TITLE = "title"

        private const val MESSAGE = "message"

        private const val POSITIVE_BUTTON_TITLE = "positive_button_title"

        private const val NEGATIVE_BUTTON_TITLE = "negative_button_title"

        private const val DISMISSIBLE = "dismissible"

        fun newInstance(messageRes: Int, titleRes: Int?, imageRes: Int? = null, positiveButtonTitleRes: Int?,
                        negativeButtonTitleRes: Int? = null, dismissible: Boolean = true): SimpleDialogFragment {
            return SimpleDialogFragment().apply {
                arguments = bundleOf(
                    IMAGE to imageRes,
                    TITLE to titleRes,
                    MESSAGE to messageRes,
                    POSITIVE_BUTTON_TITLE to positiveButtonTitleRes,
                    NEGATIVE_BUTTON_TITLE to negativeButtonTitleRes,
                    DISMISSIBLE to dismissible
                )
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSimpleDialogBinding.inflate(inflater, container, false)

        arguments?.getBoolean(DISMISSIBLE, false)?.let {
            isCancelable = it
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.getInt(IMAGE, -1)?.let {
            if (it != -1) {
                binding.image.setImageResource(it)
            } else {
                binding.image.visibility = View.GONE
            }
        }

        arguments?.getInt(TITLE, -1)?.let {
            if (it != -1) {
                binding.title.text = getString(it)
            }
        }

        arguments?.getInt(MESSAGE, -1)?.let {
            if (it != -1) {
                binding.message.text = getString(it)
            }
        }

        arguments?.getInt(POSITIVE_BUTTON_TITLE, -1)?.let {
            if (it != -1) {
                binding.button.text = getString(it)
            }
        }

        arguments?.getInt(NEGATIVE_BUTTON_TITLE, -1)?.let {
            if (it != -1) {
                binding.cancelButton.visibility = View.VISIBLE
                binding.cancelButton.text = getString(it)
            }
        }

        binding.button.setOnClickListener {
            _positiveButtonClick.value = true
        }

        binding.cancelButton.setOnClickListener {
            _negativeButtonClick.value = true
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }
}