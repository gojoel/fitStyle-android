package ai.folded.fitstyle

import ai.folded.fitstyle.databinding.FragmentWelcomeBinding
import ai.folded.fitstyle.utils.PREF_COMPLETED_TUTORIAL
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WelcomeFragment: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        context ?: return binding.root

        binding.startClickListener = View.OnClickListener {
            this.findNavController().navigate(
                WelcomeFragmentDirections.actionWelcomeToStyleListFragment())
        }

        binding.historyClickListener = View.OnClickListener {
            this.findNavController().navigate(
                WelcomeFragmentDirections.actionWelcomeToStyledImagesFragment())
        }

        binding.settingsClickListener = View.OnClickListener {
            this.findNavController().navigate(
                WelcomeFragmentDirections.actionWelcomeToSettingsFragment())
        }

        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!completedTutorial()) {
            this.findNavController().navigate(
                WelcomeFragmentDirections.actionWelcomeToTutorialFragment())
        }
    }

    private fun completedTutorial(): Boolean {
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)
        return sharedPref?.getBoolean(PREF_COMPLETED_TUTORIAL, false) ?: false
    }
}