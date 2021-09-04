package ai.folded.fitstyle

import ai.folded.fitstyle.data.Status
import ai.folded.fitstyle.databinding.FragmentStyledImageBinding
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
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.core.content.FileProvider
import androidx.navigation.ui.onNavDestinationSelected
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.facebook.shimmer.ShimmerFrameLayout

@AndroidEntryPoint
class StyledImageFragment: Fragment() {
    private val args: StyledImageFragmentArgs by navArgs()

    @Inject
    lateinit var styledImageViewModelFactory: StyledImageViewModelFactory

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
        binding.toolbar.setNavigationOnClickListener { view ->
            onBackPressed()
        }

        when (args.navSource) {
            STYLED_IMG_VIEW_SRC_TRANSFER -> {
                binding.toolbar.inflateMenu(R.menu.home_menu)
                binding.toolbar.setOnMenuItemClickListener { menuItem ->
                    menuItem.onNavDestinationSelected(requireView().findNavController())
                }

                binding.retryButton.setOnClickListener {
                    showStyleListFragment()
                }
            }
            else -> {
                binding.retryButton.visibility = View.GONE
            }
        }

        binding.purchaseButton.setOnClickListener {
            this.findNavController().navigate(
                StyledImageFragmentDirections.actionStyledImageToPaymentFragment(args.styledImage))
            resetSaveButtonState()
        }

        binding.shareButton.setOnClickListener {
            binding.shareButton.isEnabled = false
            binding.progressBar.visibility = View.VISIBLE

            // disallow other click events when save is in progress
            binding.purchaseButton.isClickable = false
            binding.saveButton.isClickable = false

            styledImageViewModel.shareImage()
        }

        styledImageViewModel.shareableImage.observe(viewLifecycleOwner, { file ->
            binding.shareButton.isEnabled = true
            binding.purchaseButton.isClickable = true
            binding.saveButton.isClickable = true
            binding.progressBar.visibility = View.GONE

            file?.let {
                activity?.let { context ->
                    val authority = context.getString(R.string.file_provider_authority)
                    val contentUri = FileProvider.getUriForFile(context, authority, file)
                    if (contentUri != null) {
                        val intent = Intent()
                        intent.action = ACTION_SEND
                        intent.addFlags(FLAG_GRANT_READ_URI_PERMISSION)
                        intent.setDataAndType(contentUri, context.contentResolver.getType(contentUri))
                        intent.putExtra(EXTRA_STREAM, contentUri)
                        context.startActivity(createChooser(intent, "Choose an app"))
                    }
                }

                styledImageViewModel.resetShareableState()
            }
        })

        binding.saveButton.setOnClickListener {
            binding.saveButton.isEnabled = false

            // disallow other click events when save is in progress
            binding.purchaseButton.isClickable = false
            binding.shareButton.isClickable = false

            binding.progressBar.visibility = View.VISIBLE
            binding.saveButton.text = context?.getString(R.string.saving)
            styledImageViewModel.downloadImage()
        }

        styledImageViewModel.downloadStatus.observe(viewLifecycleOwner, { status ->
            binding.saveButton.isEnabled = true
            binding.purchaseButton.isClickable = true
            binding.shareButton.isClickable = true
            binding.progressBar.visibility = View.GONE

            status?.let {
                activity?.let { context ->
                    if (it == Status.SUCCESS) {
                        Toast.makeText(context, context.getString(R.string.saved_successfully), Toast.LENGTH_LONG).show()
                        binding.saveButton.text = context.getString(R.string.saved)
                        binding.saveButton.icon = ContextCompat.getDrawable(context, R.drawable.ic_checkmark)
                        binding.saveButton.isClickable = false
                    } else {
                        Toast.makeText(context, context.getString(R.string.saved_failed), Toast.LENGTH_LONG).show()
                        binding.saveButton.text = context.getString(R.string.save)
                    }
                }

                styledImageViewModel.resetSavedState()
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


            if (it.purchased) {
                binding.purchaseButton.visibility = View.GONE

                val constraintSet = ConstraintSet()
                constraintSet.clone(binding.topButtonLayout)
                constraintSet.connect(R.id.save_button, ConstraintSet.END, R.id.top_button_layout, ConstraintSet.END, 0)
                constraintSet.connect(R.id.share_button, ConstraintSet.TOP, R.id.save_button, ConstraintSet.BOTTOM, 16)
                constraintSet.connect(R.id.share_button, ConstraintSet.START, R.id.top_button_layout, ConstraintSet.START, 0)
                constraintSet.connect(R.id.share_button, ConstraintSet.END, R.id.top_button_layout, ConstraintSet.END, 0)
                constraintSet.applyTo(binding.topButtonLayout)
            }
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

    private fun resetSaveButtonState() {
        context?.let {
            binding.saveButton.text = it.getString(R.string.save)
            binding.saveButton.icon = ContextCompat.getDrawable(it, R.drawable.ic_download)
            binding.saveButton.isClickable = true
        }
    }

    private fun showStyleListFragment() {
        this.findNavController().navigate(
            StyledImageFragmentDirections.actionStyledImageToStyleListFragment())
    }

    override fun onResume() {
        super.onResume()
        styledImageViewModel.updateStyledImage()
    }
}

