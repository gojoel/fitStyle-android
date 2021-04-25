package ai.folded.fitstyle.adapters

import ai.folded.fitstyle.databinding.ItemStyledImageBinding
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class StyledImagesAdapter(private val clickListener: StyledImageClickListener) : RecyclerView.Adapter<StyledImagesAdapter.ImageViewHolder>() {

    var data = listOf<String>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = data.size


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        return ImageViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageKey = data[position]
        holder.bind(imageKey, clickListener)
    }

    class ImageViewHolder private constructor(val binding: ItemStyledImageBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: String, clickListener: StyledImageClickListener) {
            binding.imageKey = item
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

class StyledImageClickListener(val viewImageClickListener: (imageKey: String) -> Unit) {
    fun onViewImageClick(imageKey: String) = viewImageClickListener(imageKey)
}
