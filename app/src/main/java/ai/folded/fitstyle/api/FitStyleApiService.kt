package ai.folded.fitstyle.api

import ai.folded.fitstyle.data.StyleImage
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

private const val BASE_URL = "http://10.0.2.2:5000/"

private val interceptor = run {
    val httpLoggingInterceptor = HttpLoggingInterceptor()
    httpLoggingInterceptor.apply {
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
    }
}

private val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(interceptor)
    .build()

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .client(okHttpClient)
    .build()

/**
 * A public interface that exposes the [getProperties] method
 */
interface FitStyleApiService {
    @FormUrlEncoded
    @POST("api/style_transfer")
    suspend fun styleTransfer(@Field("photo") photo: String,
                              @Field("custom_style_image") customStyleImage: String?,
                              @Field("style_image_id") styleImageId: Int?): ResultImage

    /**
     * Returns a Coroutine [List] of [StyleImage] which can be fetched with await() if
     * in a Coroutine scope.
     */
    @GET("api/style_images")
    suspend fun getStyleImages(): List<StyleImage>
}

/**
 * A public Api object that exposes the lazy-initialized Retrofit service
 */
object FitStyleApi {

    val retrofitService : FitStyleApiService by lazy { retrofit.create(FitStyleApiService::class.java) }
}