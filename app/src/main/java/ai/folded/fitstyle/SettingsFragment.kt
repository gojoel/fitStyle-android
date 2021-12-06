package ai.folded.fitstyle

import ai.folded.fitstyle.databinding.FragmentSettingsBinding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSettingsBinding.inflate(inflater, container, false)
        context ?: return binding.root

        binding.toolbar.setTitle(R.string.settings)
        binding.toolbar.setNavigationOnClickListener { view ->
            view.findNavController().navigateUp()
        }

        childFragmentManager
            .beginTransaction()
            .replace(R.id.settings_container, PreferenceSettingsFragment())
            .commit()

        setHasOptionsMenu(true)
        return binding.root
    }
}