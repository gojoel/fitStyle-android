package ai.folded.fitstyle.glide

import ai.folded.fitstyle.data.StyledImage
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import java.io.InputStream

class S3ModelLoaderFactory : ModelLoaderFactory<StyledImage, InputStream> {
    override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<StyledImage, InputStream> {
        return S3ImageLoader()
    }

    override fun teardown() {
        // Do nothing.
    }
}