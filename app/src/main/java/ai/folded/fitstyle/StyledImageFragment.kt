package ai.folded.fitstyle

import ai.folded.fitstyle.data.Status
import ai.folded.fitstyle.databinding.FragmentStyledImageBinding
import ai.folded.fitstyle.utils.COUNTRY_CODE
import ai.folded.fitstyle.utils.MERCHANT
import ai.folded.fitstyle.utils.STYLED_IMG_VIEW_SRC_TRANSFER
import ai.folded.fitstyle.viewmodels.StyledImageViewModel
import ai.folded.fitstyle.viewmodels.StyledImageViewModelFactory
import android.content.Intent
import android.content.Intent.*
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.core.content.FileProvider
import androidx.databinding.library.BuildConfig
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.facebook.shimmer.ShimmerFrameLayout
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult

@AndroidEntryPoint
class StyledImageFragment: Fragment() {

    private val args: StyledImageFragmentArgs by navArgs()

    @Inject
    lateinit var styledImageViewModelFactory: StyledImageViewModelFactory

    lateinit var binding: FragmentStyledImageBinding

    private val styledImageViewModel: StyledImageViewModel by viewModels {
        StyledImageViewModel.provideFactory(styledImageViewModelFactory, args.styledImage.requestId)
    }

    private lateinit var paymentSheet: PaymentSheet

    private val googlePayConfig: PaymentSheet.GooglePayConfiguration
        get() {
            val env = if (BuildConfig.DEBUG) {
                PaymentSheet.GooglePayConfiguration.Environment.Test
            } else {
                PaymentSheet.GooglePayConfiguration.Environment.Production
            }

            return PaymentSheet.GooglePayConfiguration(
                environment = env,
                countryCode = COUNTRY_CODE
            )
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPressed()
            }
        })

        paymentSheet = PaymentSheet(this, ::onPaymentSheetResult)

        styledImageViewModel.paymentProgress.observe(this) {
            binding.progressBar.visibility = if (it) { View.VISIBLE } else { View.GONE }
        }

        styledImageViewModel.paymentStatus.observe(this) { status ->
            when (status) {
                Status.WAITING -> {
                    updatePurchaseButtonState(true)
                }
                Status.FAILED -> {
                    updatePurchaseButtonState(false)
                    showPaymentFailureDialog()
                }
                else -> {}
            }
        }
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
        binding.toolbar.setNavigationOnClickListener { _ ->
            if (styledImageViewModel.paymentProgress.value != true) {
                onBackPressed()
            }
        }

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

        binding.purchaseButton.setOnClickListener {
            onPurchase()
        }

        binding.shareButton.setOnClickListener {
            binding.shareButton.isEnabled = false
            binding.progressBar.visibility = View.VISIBLE

            // disallow other click events when save is in progress
            binding.purchaseButton.isClickable = false

            styledImageViewModel.shareImage()
        }

        styledImageViewModel.shareableImage.observe(viewLifecycleOwner, { file ->
            binding.shareButton.isEnabled = true
            binding.purchaseButton.isClickable = true
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

    private fun showStyleListFragment() {
        this.findNavController().navigate(
            StyledImageFragmentDirections.actionStyledImageToStyleListFragment())
    }

    private fun updatePurchaseButtonState(purchasing: Boolean) {
        context?.let {
            binding.purchaseButton.text = if (purchasing) {
                it.getString(R.string.removing_watermark)
            } else { it.getString(R.string.remove_watermark) }

            binding.purchaseButton.isEnabled = !purchasing
            binding.shareButton.isClickable = !purchasing
        }
    }

    private fun onPurchase() {
        styledImageViewModel.styledImage.value.let {
            if (it == null) { return }

            updatePurchaseButtonState(true)
            prepareCheckout {_, clientSecret ->
                paymentSheet.presentWithPaymentIntent(
                    clientSecret,
                    PaymentSheet.Configuration(
                        merchantDisplayName = MERCHANT,
                        googlePay = googlePayConfig,
                        primaryButtonColor = ColorStateList.valueOf(ContextCompat.getColor(
                            requireNotNull(activity), R.color.purple_200))
                    )
                )
            }
        }
    }

    private fun prepareCheckout(
        onSuccess: (PaymentSheet.CustomerConfiguration?, String) -> Unit
    ) {
        styledImageViewModel.preparePaymentRequest()
            .observe(viewLifecycleOwner) { checkoutResponse ->
                if (checkoutResponse != null) {
                    onSuccess(
                        null,
                        checkoutResponse.clientSecret
                    )
                }
            }
    }

    private fun onPaymentSheetResult(
        paymentSheetResult: PaymentSheetResult
    ) {
        when(paymentSheetResult) {
            is PaymentSheetResult.Canceled -> {}
            is PaymentSheetResult.Failed -> { showPaymentFailureDialog() }
            is PaymentSheetResult.Completed -> {
                styledImageViewModel.removeWatermark(args.styledImage).observe(this) { status ->
                    if (status == Status.SUCCESS) {
                        showSuccessDialog()
                    } else {
                        showPaymentFailureDialog(R.string.oh_no, R.string.remove_watermark_failed)
                    }
                }
            }
        }

        updatePurchaseButtonState(false)
    }

    private fun showSuccessDialog() {
        val dialog = SimpleDialogFragment.newInstance(
            messageRes = R.string.remove_watermark_payment_success,
            titleRes = R.string.all_set,
            imageRes = R.drawable.ic_circle_checkmark,
            positiveButtonTitleRes = R.string.ok,
            dismissible = false
        )

        dialog.positiveButtonClick.observe(this) {
            dialog.dismiss()
            styledImageViewModel.updateStyledImage()
        }

        dialog.show(childFragmentManager, SimpleDialogFragment.TAG)
    }

    private fun showPaymentFailureDialog(titleRes: Int = R.string.payment_failed,
                                         messageRes: Int = R.string.remove_watermark_payment_failure) {
        val dialog = SimpleDialogFragment.newInstance(
            titleRes = titleRes,
            messageRes = messageRes,
            imageRes = R.drawable.ic_circle_close,
            positiveButtonTitleRes = R.string.ok,
        )

        dialog.positiveButtonClick.observe(this) {
            dialog.dismiss()
        }

        dialog.show(childFragmentManager, SimpleDialogFragment.TAG)
    }
}

