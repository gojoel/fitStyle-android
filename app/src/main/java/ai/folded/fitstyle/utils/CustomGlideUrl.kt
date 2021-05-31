package ai.folded.fitstyle.utils

import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.Headers
import java.net.URL

/**
 * Provides a way to retrieve and cache S3 images with dynamic url by setting
 * the cache key based on image id.
 * See https://github.com/bumptech/glide/issues/501#issuecomment-122580199 for a potentially
 * more optimal solution.
 */
class CustomGlideUrl: GlideUrl {
    constructor(url: String) : super(url)
    constructor(url: URL) : super(url)
    constructor(url: String, headers: Headers) : super(url, headers)
    constructor(url: URL, headers: Headers) : super(url, headers)

    override fun getCacheKey(): String {
        val url = toStringUrl()
        return if (url.contains("?")) {
            url.substring(0, url.lastIndexOf("?"))
        } else {
            url;
        }
    }
}