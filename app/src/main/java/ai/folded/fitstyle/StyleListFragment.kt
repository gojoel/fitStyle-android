package ai.folded.fitstyle

import ai.folded.fitstyle.adapters.StyleListAdapter
import ai.folded.fitstyle.adapters.StyleListener
import ai.folded.fitstyle.data.StyleOptions
import ai.folded.fitstyle.databinding.FragmentStyleListBinding
import ai.folded.fitstyle.utils.PhotoUtils
import ai.folded.fitstyle.viewmodels.StyleListViewModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StyleListFragment: Fragment() {

    lateinit var binding: FragmentStyleListBinding

    private val styleViewModel: StyleListViewModel by viewModels()

    private val photoUtils = PhotoUtils(this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStyleListBinding.inflate(inflater, container, false)
        context ?: return binding.root

        binding.toolbar.setTitle(R.string.select_style_title)
        binding.toolbar.setNavigationOnClickListener { view ->
            view.findNavController().navigateUp()
        }

        val adapter = StyleListAdapter(StyleListener({
            onUploadPhoto()
        }, {styleImage ->
            val option = StyleOptions(styleImage = styleImage)
            onStyleSelected(option)
        }))

        binding.styleList.adapter = adapter
        subscribeUi(adapter)

        setHasOptionsMenu(true)
        return binding.root
    }

    private fun subscribeUi(adapter: StyleListAdapter) {
        styleViewModel.images.observe(viewLifecycleOwner, {
            it?.let {
                binding.shimmerView.visibility = View.GONE
                binding.styleList.visibility = View.VISIBLE
                adapter.data = it
            }
        })
    }

    private fun onStyleSelected(styleOptions: StyleOptions) {
        val direction = StyleListFragmentDirections.actionStyleListToUploadFragment(styleOptions)
        findNavController().navigate(direction)
    }

    private fun onUploadPhoto() {
        photoUtils.accessGallery {
            val options = StyleOptions(customStyleUri = it)
            onStyleSelected(options)
        }
    }

    override fun onResume() {
        super.onResume()
        binding.shimmerView.startShimmer()

    }

    override fun onPause() {
        super.onPause()
        binding.shimmerView.stopShimmer()
    }
}