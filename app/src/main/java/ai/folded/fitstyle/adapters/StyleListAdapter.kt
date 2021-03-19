package ai.folded.fitstyle.adapters

import ai.folded.fitstyle.data.StyleImage
import ai.folded.fitstyle.databinding.ListItemStyleImageBinding
import ai.folded.fitstyle.databinding.ListItemUploadStyleBinding
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class StyleListAdapter(private val styleClickListener: StyleListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_STYLE = 0
        const val VIEW_TYPE_UPLOAD = 1
    }

    var data = listOf<StyleImage>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    // account for first item as upload option
    override fun getItemCount() = data.size + 1

    override fun getItemViewType(position: Int): Int {
        if (position == 0) {
            return VIEW_TYPE_UPLOAD
        }

        return VIEW_TYPE_STYLE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == VIEW_TYPE_STYLE) {
            return StyleViewHolder.from(parent)
        }

        return UploadViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position > 0) {
            val image = data[position - 1]
            (holder as StyleViewHolder).bind(image, styleClickListener)
            return
        }

        (holder as UploadViewHolder).bind(styleClickListener)
    }

    class StyleViewHolder private constructor(val binding: ListItemStyleImageBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: StyleImage, clickListener: StyleListener) {
            binding.style = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): StyleViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemStyleImageBinding.inflate(layoutInflater, parent, false)
                return StyleViewHolder(binding)
            }
        }
    }

    class UploadViewHolder private constructor(val binding: ListItemUploadStyleBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(clickListener: StyleListener) {
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): UploadViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemUploadStyleBinding.inflate(layoutInflater, parent, false)
                return UploadViewHolder(binding)
            }
        }
    }
}

class StyleListener(val uploadClickListener: () -> Unit, val styleClickListener: (styleId: StyleImage) -> Unit) {
    fun onUploadClick() = uploadClickListener()
    fun onStyleClick(styleImage: StyleImage) = styleClickListener(styleImage)
}
