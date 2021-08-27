package ai.folded.fitstyle.glide

import com.bumptech.glide.load.model.GlideUrl

/**
 * Provides a way to retrieve and cache S3 images with dynamic url by setting
 * the cache key based on image id.
 * See https://github.com/bumptech/glide/issues/501#issuecomment-122580199 for a potentially
 * more optimal solution.
 */
class CustomGlideUrl(url: String) : GlideUrl(url) {
    override fun getCacheKey(): String {
        val url = toStringUrl()
        return if (url.contains("?")) {
            url.substring(0, url.lastIndexOf("?"))
        } else {
            url;
        }
    }
}