package ai.folded.fitstyle

import ai.folded.fitstyle.databinding.FragmentWelcomeBinding
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

        setHasOptionsMenu(true)
        return binding.root
    }
}