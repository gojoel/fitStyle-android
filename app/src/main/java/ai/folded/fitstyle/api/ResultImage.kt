package ai.folded.fitstyle.api

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ResultImage(
    @Json(name = "url") val imageUrl: String,
    @Json(name = "height") val height: Int,
    @Json(name = "width") val width: Int,
    @Json(name = "created_at") val createdAt: String) : Parcelable