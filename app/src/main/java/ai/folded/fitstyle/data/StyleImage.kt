package ai.folded.fitstyle.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class StyleImage(
    val key: String = ""
) : Parcelable {
    fun imageName() : String {
        val values = key.split("/")
        return if (values.isNotEmpty()) values[values.size - 1] else ""
    }
}
