package ai.folded.fitstyle.data

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class StyleOptions(
    var styleImage: StyleImage? = null,
    var customStyleUri: Uri? = null,
    var photoUri: Uri? = null,
) : Parcelable