package ai.folded.fitstyle.api

import ai.folded.fitstyle.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
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
    .connectTimeout(5, TimeUnit.MINUTES)
    .writeTimeout(5, TimeUnit.MINUTES)
    .readTimeout(5, TimeUnit.MINUTES)
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
    @FormUrlEncoded
    @POST("api/style_transfer")
    suspend fun styleTransfer(@Field("user_id") userId: String,
                              @Field("content") content: String,
                              @Field("custom_style") customStyle: String?,
                              @Field("style_id") styleKey: String?): ResultImage
}

/**
 * A public Api object that exposes the lazy-initialized Retrofit service
 */
object FitStyleApi {
    val retrofitService : FitStyleApiService by lazy { retrofit.create(FitStyleApiService::class.java) }
}