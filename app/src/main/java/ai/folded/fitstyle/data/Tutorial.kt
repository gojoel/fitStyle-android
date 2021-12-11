package ai.folded.fitstyle.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Tutorial(val title: Int, val details: Int, val image: Int? = null) : Parcelable