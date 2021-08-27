package ai.folded.fitstyle.glide

import ai.folded.fitstyle.data.StyledImage
import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import java.io.InputStream

@GlideModule
class CustomGlideModule: AppGlideModule() {
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.prepend(StyledImage::class.java,  InputStream::class.java, S3ModelLoaderFactory())
    }
}