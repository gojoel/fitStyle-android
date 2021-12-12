package ai.folded.fitstyle.api

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

@Parcelize
data class StyleTransferResultResponse(
    @Json(name = "status") val status: StyleTransferStatus,
    @Json(name = "req_id") val requestId: String?) : Parcelable

enum class StyleTransferStatus {
    @Json(name = "incomplete")
    INCOMPLETE,

    @Json(name = "complete")
    COMPLETE,

    @Json(name = "failed")
    FAILED
}