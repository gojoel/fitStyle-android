package ai.folded.fitstyle.adapters

import ai.folded.fitstyle.data.SettingsItem
import ai.folded.fitstyle.databinding.ListItemSettingsBinding
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class SettingsAdapter(private val settingsClickListener: SettingsListener) : RecyclerView.Adapter<SettingsAdapter.SettingsViewHolder>() {

    var settings = listOf<SettingsItem>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = settings.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingsViewHolder {
        return SettingsViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: SettingsViewHolder, position: Int) {
        holder.bind(settings[position], settingsClickListener);
    }

    class SettingsViewHolder private constructor(val binding: ListItemSettingsBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SettingsItem, clickListener: SettingsListener) {
            binding.settings = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): SettingsViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemSettingsBinding.inflate(layoutInflater, parent, false)
                return SettingsViewHolder(binding)
            }
        }
    }
}

class SettingsListener(val settingsClickListener: (settingsItem: SettingsItem) -> Unit) {
    fun onSettingItemSelected(settingsItem: SettingsItem) = settingsClickListener(settingsItem)
}
