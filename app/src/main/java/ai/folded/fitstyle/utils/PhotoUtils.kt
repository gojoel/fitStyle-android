package ai.folded.fitstyle.utils

import ai.folded.fitstyle.BuildConfig
import ai.folded.fitstyle.R
import ai.folded.fitstyle.SimpleDialogFragment
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment

class PhotoUtils(val fragment: Fragment) {

    private var photoSelectedListener: ((uri: Uri) -> Unit)? = null

    fun accessGallery(callback: ((uri: Uri) -> Unit)) {
        photoSelectedListener = callback

        fragment.context?.let {
            if (PermissionUtils.isStoragePermissionGranted(it)) {
                launchGallery()
            } else {
                requestPermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    private val requestPermission =
        fragment.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                launchGallery()
            } else  {
                if (!fragment.shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    // user has denied permission and selected "Never ask again"
                    showPermissionDeniedWithoutRetry()
                } else {
                    showPermissionDeniedDialog()
                }
            }
        }

    private val openGallery =
        fragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val selectedImageUri: Uri? = result.data?.data
                if (selectedImageUri != null) {
                    photoSelectedListener?.invoke(selectedImageUri)
                }
            }
        }

    private fun showPermissionDeniedDialog() {
        val dialog = SimpleDialogFragment.newInstance(
            R.string.gallery_permission_rationale,
            R.string.permission_denied_title,
            R.drawable.ic_warning,
            R.string.ok,
        )

        dialog.positiveButtonClick.observe(fragment) {
            dialog.dismiss()
        }

        dialog.show(fragment.childFragmentManager, SimpleDialogFragment.TAG)
    }

    private fun showPermissionDeniedWithoutRetry() {
        val dialog = SimpleDialogFragment.newInstance(
            R.string.gallery_permission_settings_access,
            R.string.permission_denied_title,
            R.drawable.ic_warning,
            R.string.ok,
        )

        dialog.positiveButtonClick.observe(fragment) {
            dialog.dismiss()
            fragment.startActivity(
                Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + BuildConfig.APPLICATION_ID)
                )
            )
        }

        dialog.show(fragment.childFragmentManager, SimpleDialogFragment.TAG)
    }

    private fun launchGallery() {
        openGallery.launch(
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        )
    }
}