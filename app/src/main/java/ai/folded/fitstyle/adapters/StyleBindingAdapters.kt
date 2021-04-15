package ai.folded.fitstyle.adapters

import ai.folded.fitstyle.utils.AwsUtils
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

@BindingAdapter("imageFromUrl")
fun bindImageFromUrl(view: ImageView, imageUrl: String?) {
    if (!imageUrl.isNullOrEmpty()) {
        Glide.with(view.context)
                .load(imageUrl)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(view)
    }
}

@BindingAdapter("imageFromKey")
fun bindImageFromKey(view: ImageView, imageKey: String?) {
    if (!imageKey.isNullOrEmpty()) {
        val url = AwsUtils.generateUrl(imageKey)
        Glide.with(view.context)
            .load(url.toString())
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(view)
    }
}