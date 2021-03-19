package ai.folded.fitstyle

import ai.folded.fitstyle.databinding.FragmentStyleResultBinding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StyleResultFragment: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentStyleResultBinding.inflate(inflater, container, false)
            .apply {
                lifecycleOwner = viewLifecycleOwner
            }

        binding.toolbar.setTitle("Result")
        binding.toolbar.setNavigationOnClickListener { view ->
            view.findNavController().navigateUp()
        }

        Glide.with(this)
            .load("https://cdn.shopify.com/s/files/1/0267/6834/3122/articles/7-reasons-why-you-should-stop-using-watermarks-now-525024_3024x.jpg?v=1599125023")
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(binding.resultImageView)



        return binding.root

    }
}