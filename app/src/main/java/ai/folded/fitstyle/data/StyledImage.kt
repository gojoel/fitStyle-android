package ai.folded.fitstyle.data

import ai.folded.fitstyle.utils.PREVIEW_IMAGE_NAME
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class StyledImage(
    val imagePath: String,
    val imageKey: String
) : Parcelable {
    fun getPreviewImageKey(): String {
        return "$imagePath$PREVIEW_IMAGE_NAME"
    }
}