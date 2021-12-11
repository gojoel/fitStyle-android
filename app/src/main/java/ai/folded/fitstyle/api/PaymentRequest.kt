package ai.folded.fitstyle.api

import com.squareup.moshi.Json

data class PaymentRequest(
    @Json(name = "currency")
    var currency: String,
    @Json(name = "userId")
    var userId: String,
    @Json(name = "requestId")
    var requestId: String,
    @Json(name = "customer")
    var customer: String = "",
    @Json(name = "mode")
    var mode: String = ""
)

data class PaymentResponse(
    @Json(name = "publishableKey")
    val publishableKey: String?,
    @Json(name = "clientSecret")
    val clientSecret: String,
    @Json(name = "customerId")
    val customerId: String?,
    @Json(name = "ephemeralKey")
    val ephemeralKey: String?,
)