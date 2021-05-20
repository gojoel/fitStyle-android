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

    private val _buttonClick = MutableLiveData<Boolean>()

    val buttonClick: LiveData<Boolean>
        get() = _buttonClick

    companion object {

        const val TAG = "SingleButtonDialog"

        private const val IMAGE = "image"

        private const val TITLE = "title"

        private const val MESSAGE = "message"

        private const val BUTTON_TITLE = "button_title"

        private const val DISMISSIBLE = "dismissible"

        fun newInstance(messageRes: Int, titleRes: Int?, imageRes: Int?, buttonTitleRes: Int?, dismissible: Boolean = true): SimpleDialogFragment {
            return SimpleDialogFragment().apply {
                arguments = bundleOf(
                    IMAGE to imageRes,
                    TITLE to titleRes,
                    MESSAGE to messageRes,
                    BUTTON_TITLE to buttonTitleRes,
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

        arguments?.getInt(BUTTON_TITLE, -1)?.let {
            if (it != -1) {
                binding.button.text = getString(it)
            }
        }

        binding.button.setOnClickListener {
            _buttonClick.value = true
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