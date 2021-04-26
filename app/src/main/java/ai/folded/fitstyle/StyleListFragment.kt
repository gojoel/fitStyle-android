package ai.folded.fitstyle

import ai.folded.fitstyle.adapters.StyleListAdapter
import ai.folded.fitstyle.adapters.StyleListener
import ai.folded.fitstyle.data.StyleOptions
import ai.folded.fitstyle.databinding.FragmentStyleListBinding
import ai.folded.fitstyle.utils.PermissionUtils
import ai.folded.fitstyle.viewmodels.StyleListViewModel
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
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StyleListFragment: Fragment() {

    lateinit var binding: FragmentStyleListBinding

    private val styleViewModel: StyleListViewModel by viewModels()

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
                    onPhotoUploaded(selectedImageUri);
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStyleListBinding.inflate(inflater, container, false)
        context ?: return binding.root

        binding.toolbar.setTitle(R.string.select_style_title)
        binding.toolbar.setNavigationOnClickListener { view ->
            view.findNavController().navigateUp()
        }

        val adapter = StyleListAdapter(StyleListener({
            onUploadPhoto()
        }, {styleImage ->
            val option = StyleOptions(styleImage = styleImage)
            onStyleSelected(option)
        }))

        binding.styleList.adapter = adapter
        subscribeUi(adapter)

        setHasOptionsMenu(true)
        return binding.root
    }

    private fun subscribeUi(adapter: StyleListAdapter) {
        styleViewModel.images.observe(viewLifecycleOwner, Observer {
            it?.let {
                binding.shimmerView.visibility = View.GONE
                binding.styleList.visibility = View.VISIBLE
                adapter.data = it
            }
        })
    }

    private fun onStyleSelected(styleOptions: StyleOptions) {
        val direction = StyleListFragmentDirections.actionStyleListToUploadFragment(styleOptions)
        findNavController().navigate(direction)
    }

    private fun onUploadPhoto() {
        activity?.let {
            if (PermissionUtils.isStoragePermissionGranted(it)) {
                launchGallery()
            } else {
                //TODO: check if can request permission
                requestPermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    private fun onPhotoUploaded(imageUri: Uri) {
        val options = StyleOptions(customStyleUri = imageUri)
        onStyleSelected(options)
    }

    private fun launchGallery() {
        openGallery.launch(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI))
    }

    private fun showPermissionDeniedDialog() {
        //TODO: handle this
    }

    override fun onResume() {
        super.onResume()
        binding.shimmerView.startShimmer()

    }

    override fun onPause() {
        super.onPause()
        binding.shimmerView.stopShimmer()
    }
}