package ai.folded.fitstyle.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.net.URL

@Parcelize
data class StyleImage(
    val key: String = "",
    var url: URL? = null
) : Parcelable {
    fun imageName() : String {
        val values = key.split("/")
        return if (values.isNotEmpty()) values[values.size - 1] else ""
    }
}
