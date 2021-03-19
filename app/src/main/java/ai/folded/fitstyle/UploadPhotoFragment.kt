package ai.folded.fitstyle

import ai.folded.fitstyle.databinding.FragmentUploadPhotoBinding
import ai.folded.fitstyle.utils.PermissionUtils
import ai.folded.fitstyle.viewmodels.StyleTransferViewModel
import ai.folded.fitstyle.viewmodels.StyleTransferViewModelFactory
import ai.folded.fitstyle.viewmodels.UploadPhotoViewModel
import ai.folded.fitstyle.viewmodels.UploadPhotoViewModelFactory
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
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

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) launchGallery()
            else showPermissionDeniedDialog()
        }

    private val openGallery =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val selectedImageUri: Uri? = result.data?.data
                if (selectedImageUri != null) {
                    uploadPhotoViewModel.onPhotoUploaded(selectedImageUri)
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentUploadPhotoBinding.inflate(inflater, container, false)
        context ?: return binding.root

        binding.uploadPhotoViewModel = uploadPhotoViewModel
        binding.lifecycleOwner = this

        binding.toolbar.setTitle(R.string.upload_photo_title)
        binding.toolbar.setNavigationOnClickListener { view ->
            view.findNavController().navigateUp()
        }

        binding.uploadPhotoButton.setOnClickListener {
            activity?.let {
                if (PermissionUtils.isStoragePermissionGranted(it)) {
                    launchGallery()
                } else {
                    //TODO: check if can request permission
                    requestPermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
        }

        uploadPhotoViewModel.navigateToStyleTransfer.observe(viewLifecycleOwner, Observer {
            this.findNavController().navigate(
                UploadPhotoFragmentDirections.actionUploadPhotoToStyleTransferFragment(it))
        })

        setHasOptionsMenu(true)
        return binding.root
    }

    private fun launchGallery() {
        openGallery.launch(
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        )
    }

    private fun showPermissionDeniedDialog() {
        //TODO: handle this
    }
}