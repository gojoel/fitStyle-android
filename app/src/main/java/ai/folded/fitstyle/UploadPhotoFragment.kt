package ai.folded.fitstyle

import ai.folded.fitstyle.databinding.FragmentUploadPhotoBinding
import ai.folded.fitstyle.utils.PhotoUtils
import ai.folded.fitstyle.viewmodels.UploadPhotoViewModel
import ai.folded.fitstyle.viewmodels.UploadPhotoViewModelFactory
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
import javax.inject.Inject

/**
 * A fragment allowing user to upload a photo.
 */
@AndroidEntryPoint
class UploadPhotoFragment : Fragment() {

    private val args: UploadPhotoFragmentArgs by navArgs()

    @Inject
    lateinit var uploadPhotoViewModelFactory: UploadPhotoViewModelFactory

    private val uploadPhotoViewModel: UploadPhotoViewModel by viewModels {
        UploadPhotoViewModel.provideFactory(uploadPhotoViewModelFactory, args.styleOptions)
    }

    private var photoUtils = PhotoUtils(this)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentUploadPhotoBinding.inflate(inflater, container, false)
        context ?: return binding.root

        binding.uploadPhotoViewModel = uploadPhotoViewModel
        binding.lifecycleOwner = this

        binding.toolbar.setTitle(R.string.upload_photo_title)
        binding.toolbar.setNavigationOnClickListener { view ->
            view.findNavController().navigateUp()
        }

        binding.uploadPhotoButton.setOnClickListener {
            photoUtils.accessGallery {
                uploadPhotoViewModel.onPhotoUploaded(it)
            }
        }

        uploadPhotoViewModel.navigateToStyleTransfer.observe(viewLifecycleOwner, {
            it?.let {
                this.findNavController().navigate(
                    UploadPhotoFragmentDirections.actionUploadPhotoToStyleTransferFragment(it))
                uploadPhotoViewModel.clearSelection()
            }
        })

        setHasOptionsMenu(true)
        return binding.root
    }
}