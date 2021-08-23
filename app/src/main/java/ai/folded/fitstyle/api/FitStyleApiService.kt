package ai.folded.fitstyle.api

import ai.folded.fitstyle.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

private val interceptor = run {
    val httpLoggingInterceptor = HttpLoggingInterceptor()
    httpLoggingInterceptor.apply {
        httpLoggingInterceptor.level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }
}

private val okHttpClient = OkHttpClient.Builder()
    .connectTimeout(1, TimeUnit.MINUTES)
    .writeTimeout(1, TimeUnit.MINUTES)
    .readTimeout(1, TimeUnit.MINUTES)
    .addInterceptor(interceptor)
    .build()

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BuildConfig.BASE_URL)
    .client(okHttpClient)
    .build()

/**
 * A public interface that exposes the [getProperties] method
 */
interface FitStyleApiService {
    @POST("api/style_transfer")
    suspend fun styleTransfer(@Body body: RequestBody): StyleTransferResponse

    @GET("/api/style_transfer/results/{job_id}")
    suspend fun styleTransferResult(@Path("job_id") jobId: String): StyleTransferResultResponse

    @FormUrlEncoded
    @POST("/api/style_transfer/cancel")
    suspend fun cancelStyleTransferTask(@Field("job_id") jobId: String)

    @Headers("Content-Type: application/json")
    @POST("api/create_watermark_payment")
    suspend fun createWatermarkPayment(@Body paymentRequest: PaymentRequest): PaymentResponse

    @FormUrlEncoded
    @POST("api/remove_watermark")
    suspend fun removeWatermark(@Field("userId") userId: String, @Field("requestId") requestId: String)
}

/**
 * A public Api object that exposes the lazy-initialized Retrofit service
 */
object FitStyleApi {
    val retrofitService : FitStyleApiService by lazy { retrofit.create(FitStyleApiService::class.java) }
}