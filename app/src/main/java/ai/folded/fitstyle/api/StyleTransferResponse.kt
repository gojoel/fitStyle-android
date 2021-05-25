package ai.folded.fitstyle.api

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

@Parcelize
data class StyleTransferResponse(
    @Json(name = "request_id") val requestId: String) : Parcelable