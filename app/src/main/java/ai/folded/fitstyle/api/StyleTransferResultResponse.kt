package ai.folded.fitstyle.api

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

@Parcelize
data class StyleTransferResultResponse(
    @Json(name = "status") val status: String,
    @Json(name = "req_id") val requestId: String) : Parcelable