package ai.folded.fitstyle.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class StyledImage(
    val imageKey: String
) : Parcelable