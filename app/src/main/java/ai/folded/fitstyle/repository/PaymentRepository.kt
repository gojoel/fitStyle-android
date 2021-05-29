package ai.folded.fitstyle.repository

import ai.folded.fitstyle.api.FitStyleApiService
import ai.folded.fitstyle.api.PaymentRequest
import ai.folded.fitstyle.utils.CURRENCY
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class PaymentRepository(
    private val retrofitService : FitStyleApiService,
    private val workContext: CoroutineContext
) {

    suspend fun createWatermarkPayment(userId: String, requestId: String) = withContext(workContext) {
        flowOf(
            kotlin.runCatching {
                retrofitService.createWatermarkPayment(PaymentRequest(userId = userId, requestId = requestId, currency = CURRENCY))
            }
        )
    }

    suspend fun removeWatermark(userId: String, requestId: String) = withContext(workContext) {
        retrofitService.removeWatermark(userId, requestId)
    }
}