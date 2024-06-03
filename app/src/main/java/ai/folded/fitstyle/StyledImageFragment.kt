package ai.folded.fitstyle

import ai.folded.fitstyle.databinding.FragmentStyledImageBinding
import ai.folded.fitstyle.utils.AnalyticsManager
import ai.folded.fitstyle.utils.STYLED_IMG_VIEW_SRC_TRANSFER
import ai.folded.fitstyle.viewmodels.StyledImageViewModel
import ai.folded.fitstyle.viewmodels.StyledImageViewModelFactory
import android.content.Intent
import android.content.Intent.*
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.facebook.shimmer.ShimmerFrameLayout
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class StyledImageFragment: Fragment() {

    private val args: StyledImageFragmentArgs by navArgs()

    @Inject
    lateinit var styledImageViewModelFactory: StyledImageViewModelFactory

    @Inject
    lateinit var analyticsManager: AnalyticsManager

    lateinit var binding: FragmentStyledImageBinding

    private val styledImageViewModel: StyledImageViewModel by viewModels {
        StyledImageViewModel.provideFactory(styledImageViewModelFactory, args.styledImage.requestId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPressed()
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentStyledImageBinding.inflate(inflater, container, false)
            .apply {
                viewModel = styledImageViewModel
                lifecycleOwner = viewLifecycleOwner
            }

        binding.toolbar.setTitle(R.string.styled_image_title)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }

        when (args.navSource) {
            STYLED_IMG_VIEW_SRC_TRANSFER -> {
                binding.retryButton.setOnClickListener {
                    showStyleListFragment()
                }
            }
            else -> {
                binding.retryButton.visibility = View.GONE
            }
        }

        binding.shareButton.setOnClickListener {
            context?.let {
                analyticsManager.logEvent(AnalyticsManager.FitstyleEvent.TAPPED_SHARE)
            }

            binding.shareButton.isEnabled = false
            binding.progressBar.visibility = View.VISIBLE

            styledImageViewModel.shareImage()
        }

        styledImageViewModel.shareableImage.observe(viewLifecycleOwner, { file ->
            binding.shareButton.isEnabled = true
            binding.progressBar.visibility = View.GONE

            file?.let {
                activity?.let { context ->
                    val authority = context.getString(R.string.file_provider_authority)
                    val contentUri = FileProvider.getUriForFile(context, authority, file)
                    if (contentUri != null) {
                        val intent = Intent()
                        intent.action = ACTION_SEND
                        intent.addFlags(FLAG_GRANT_READ_URI_PERMISSION or FLAG_GRANT_WRITE_URI_PERMISSION)
                        intent.setDataAndType(contentUri, context.contentResolver.getType(contentUri))
                        intent.putExtra(EXTRA_STREAM, contentUri)
                        intent.type = "image/png"
                        context.startActivity(createChooser(intent, "Choose an app"))
                    }
                }

                styledImageViewModel.resetShareableState()
            }
        })

        styledImageViewModel.styledImage.observe(viewLifecycleOwner, {
            showLoadingImage(binding.shimmerView, true)
            Glide.with(binding.resultImageView.context)
                .load(it)
                .transition(DrawableTransitionOptions.withCrossFade())
                .listener(object: RequestListener<Drawable>{
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        analyticsManager.logError(AnalyticsManager.FitstyleError.STYLED_IMAGE, e?.localizedMessage)
                        showLoadingImage(binding.shimmerView, false)
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        showLoadingImage(binding.shimmerView, false)
                        return false
                    }
                })
                .into(binding.resultImageView)
        })

        this.binding = binding
        return binding.root
    }

    private fun showLoadingImage(shimmerView: ShimmerFrameLayout, visible: Boolean) {
        if (visible) {
            shimmerView.visibility = View.VISIBLE
            shimmerView.startShimmer()
        } else {
            shimmerView.visibility = View.GONE
            shimmerView.stopShimmer()
        }
    }

    private fun onBackPressed() {
        if (args.navSource == STYLED_IMG_VIEW_SRC_TRANSFER) {
            showStyleListFragment()
        } else {
            view?.findNavController()?.navigateUp()
        }
    }

    private fun showStyleListFragment() {
        this.findNavController().navigate(
            StyledImageFragmentDirections.actionStyledImageToStyleListFragment())
    }
}

