package ai.folded.fitstyle.adapters

import ai.folded.fitstyle.data.StyledImage
import ai.folded.fitstyle.utils.AwsUtils
import ai.folded.fitstyle.utils.CustomGlideUrl
import ai.folded.fitstyle.views.BlurTransformation
import android.graphics.Bitmap
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.signature.ObjectKey

@BindingAdapter("imageFromUrl")
fun bindImageFromUrl(view: ImageView, imageUrl: String?) {
    if (!imageUrl.isNullOrEmpty()) {
        Glide.with(view.context)
                .load(CustomGlideUrl(imageUrl))
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(view)
    }
}

@BindingAdapter("styleImage")
fun bindStyleImage(view: ImageView, imageKey: String?) {
    if (!imageKey.isNullOrEmpty()) {
        val url = AwsUtils.generateUrl(imageKey)
        Glide.with(view.context)
            .load(CustomGlideUrl(url.toString()))
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(view)
    }
}

@BindingAdapter("styledImage")
fun bindStyledImage(view: ImageView, styledImage: StyledImage?) {
    styledImage?.let {
        if (!it.imageKey().isNullOrEmpty()) {
            val url = AwsUtils.generateUrl(it.imageKey())
            Glide.with(view.context)
                .load(CustomGlideUrl(url.toString()))
                .signature(ObjectKey(it.purchased.toString()))
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(view)
        }
    }
}

@BindingAdapter("blurredStyledImage")
fun bindBlurredStyledImage(view: ImageView, styledImage: StyledImage?) {
    styledImage?.let {
        if (!it.imageKey().isNullOrEmpty()) {
            val url = AwsUtils.generateUrl(it.previewImageKey())
            Glide.with(view.context)
                .load(CustomGlideUrl(url.toString()))
                .signature(ObjectKey(it.purchased.toString()))
                .transition(DrawableTransitionOptions.withCrossFade())
                .transform( BlurTransformation(view.context) )
                .into(view)
        }
    }
}

@BindingAdapter("imageFromBitmap")
fun bindBitmapImage(view: ImageView, bitmap: Bitmap?) {
    bitmap?.let {
        view.setImageBitmap(it)
    }
}
