package ai.folded.fitstyle

import ai.folded.fitstyle.adapters.StyledImageClickListener
import ai.folded.fitstyle.adapters.StyledImagesAdapter
import ai.folded.fitstyle.data.StyledImage
import ai.folded.fitstyle.databinding.FragmentStyledListBinding
import ai.folded.fitstyle.utils.STYLED_IMG_VIEW_SRC_DEFAULT
import ai.folded.fitstyle.viewmodels.StyledListViewModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StyledListFragment: Fragment() {

    private val styledImageViewModel: StyledListViewModel by viewModels()

    lateinit var binding: FragmentStyledListBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStyledListBinding.inflate(inflater, container, false)
        context ?: return binding.root

        binding.toolbar.setTitle(R.string.styled_images_title)
        binding.toolbar.setNavigationOnClickListener { view ->
            view.findNavController().navigateUp()
        }

        binding.styleImageClickListener = View.OnClickListener {
            this.findNavController().navigate(
                StyledListFragmentDirections.actionStyledImagesToStyleListFragment())
        }

        styledImageViewModel.emptyStatus.observe(viewLifecycleOwner, Observer {
            binding.shimmerView.visibility = View.GONE
            binding.emptyView.visibility = View.VISIBLE
        })


        val adapter = StyledImagesAdapter(StyledImageClickListener { imageKey ->
            val styledImage = StyledImage(imageKey)
            val direction = StyledListFragmentDirections.actionStyledImagesToStyleDetailsFragment(styledImage, STYLED_IMG_VIEW_SRC_DEFAULT)
            findNavController().navigate(direction)
        })

        binding.styledRecyclerView.adapter = adapter

        styledImageViewModel.images.observe(viewLifecycleOwner, Observer {
            it?.let {
                binding.shimmerView.visibility = View.GONE
                binding.emptyView.visibility = View.GONE
                adapter.data = it
            }
        })

        setHasOptionsMenu(true)
        return binding.root
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