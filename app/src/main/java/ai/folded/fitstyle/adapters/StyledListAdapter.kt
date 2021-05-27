package ai.folded.fitstyle.adapters

import ai.folded.fitstyle.data.StyledImage
import ai.folded.fitstyle.databinding.ItemStyledImageBinding
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class StyledImagesAdapter(private val clickListener: StyledImageClickListener) : RecyclerView.Adapter<StyledImagesAdapter.ImageViewHolder>() {

    var data = listOf<StyledImage>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = data.size


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        return ImageViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val styledImage = data[position]
        holder.bind(styledImage, clickListener)
    }

    class ImageViewHolder private constructor(val binding: ItemStyledImageBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: StyledImage, clickListener: StyledImageClickListener) {
            binding.styledImage = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ImageViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemStyledImageBinding.inflate(layoutInflater, parent, false)
                return ImageViewHolder(binding)
            }
        }
    }
}

class StyledImageClickListener(val viewImageClickListener: (styledImage: StyledImage) -> Unit) {
    fun onViewImageClick(styledImage: StyledImage) = viewImageClickListener(styledImage)
}
