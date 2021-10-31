package ai.folded.fitstyle.adapters

import ai.folded.fitstyle.data.StyleImage
import ai.folded.fitstyle.data.StyledImage
import ai.folded.fitstyle.utils.AwsUtils
import ai.folded.fitstyle.glide.CustomGlideUrl
import ai.folded.fitstyle.views.BlurTransformation
import android.graphics.Bitmap
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

@BindingAdapter("styleImage")
fun bindStyleImage(view: ImageView, styleImage: StyleImage?) {
    styleImage?.let {
        val url = AwsUtils.generateUrl(it.key)
        Glide.with(view.context)
            .load(url.toString())
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(view)
    }
}

@BindingAdapter("styledImage")
fun bindStyledImage(view: ImageView, styledImage: StyledImage?) {
    styledImage?.let { image ->
        Glide.with(view.context)
            .load(image)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(view)
    }
}

@BindingAdapter("imageFromBitmap")
fun bindBitmapImage(view: ImageView, bitmap: Bitmap?) {
    bitmap?.let {
        view.setImageBitmap(it)
    }
}
