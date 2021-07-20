package ai.folded.fitstyle

import ai.folded.fitstyle.data.Status
import ai.folded.fitstyle.databinding.FragmentPaymentBinding
import ai.folded.fitstyle.repository.StyledImageRepository
import ai.folded.fitstyle.repository.UserRepository
import ai.folded.fitstyle.utils.COUNTRY_CODE
import ai.folded.fitstyle.utils.MERCHANT
import ai.folded.fitstyle.viewmodels.PaymentViewModel
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.databinding.library.BuildConfig
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PaymentFragment: Fragment() {
    private var _binding: FragmentPaymentBinding? = null

    private val binding get() = _binding!!

    private val args: PaymentFragmentArgs by navArgs()

    @Inject
    lateinit var styledImageRepository: StyledImageRepository

    @Inject
    lateinit var userRepository: UserRepository

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

    private val paymentViewModel: PaymentViewModel by viewModels {
        PaymentViewModel.Factory(
            requireNotNull(activity).application,
            args.styledImage,
            styledImageRepository = styledImageRepository,
            userRepository = userRepository,
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaymentBinding.inflate(inflater, container, false)
            .apply {
                viewModel = paymentViewModel
                lifecycleOwner = viewLifecycleOwner
            }

        binding.toolbar.setTitle(R.string.remove_watermark)
        binding.toolbar.setNavigationOnClickListener { view ->
            view.findNavController().navigateUp()
        }

        binding.confirmPaymentButton.setOnClickListener {
            onPurchase()
        }

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        paymentSheet = PaymentSheet(this, ::onPaymentSheetResult)

        paymentViewModel.progress.observe(this) {
            binding.progressGroup.isInvisible = !it
        }

        paymentViewModel.status.observe(this) { status ->
            when (status) {
                Status.WAITING -> {
                    binding.progressDetails.text = getString( R.string.status_preparing_purchase)
                }
                Status.FAILED -> {
                    binding.confirmPaymentButton.isEnabled = true
                    showPaymentRequestErrorDialog()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onPurchase() {
        binding.confirmPaymentButton.isEnabled = false
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

    private fun prepareCheckout(
        onSuccess: (PaymentSheet.CustomerConfiguration?, String) -> Unit
    ) {
        paymentViewModel.preparePaymentRequest()
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
            is PaymentSheetResult.Canceled -> {
                binding.confirmPaymentButton.isEnabled = true
                showPaymentErrorDialog(true)
            }
            is PaymentSheetResult.Failed -> {
                binding.confirmPaymentButton.isEnabled = true
                showPaymentErrorDialog()
            }
            is PaymentSheetResult.Completed -> {
                binding.progressDetails.text = getString(R.string.status_removing_watermark)

                paymentViewModel.removeWatermark(args.styledImage).observe(this) { status ->
                    if (status == Status.SUCCESS) {
                        showSuccessDialog()
                    } else {
                        showRemovalErrorDialog()
                    }
                }
            }
        }
    }

    private fun showSuccessDialog() {
        val dialog = SimpleDialogFragment.newInstance(
            messageRes = R.string.remove_watermark_payment_success,
            titleRes = R.string.all_set,
            imageRes = R.drawable.ic_circle_checkmark,
            positiveButtonTitleRes = R.string.view_image,
            dismissible = false
        )

        dialog.positiveButtonClick.observe(this) {
            dialog.dismiss()
            binding.root.findNavController().navigateUp()
        }

        dialog.show(childFragmentManager, SimpleDialogFragment.TAG)
    }

    private fun showPaymentRequestErrorDialog() {
        val dialog = SimpleDialogFragment.newInstance(
            messageRes = R.string.remove_watermark_payment_failure,
            titleRes = R.string.payment_failed,
            imageRes = R.drawable.ic_circle_close,
            positiveButtonTitleRes = R.string.ok,
        )

        dialog.positiveButtonClick.observe(this) {
            dialog.dismiss()
        }

        dialog.show(childFragmentManager, SimpleDialogFragment.TAG)
    }

    private fun showPaymentErrorDialog(isCancelled: Boolean = false) {
        val messageRes = if (isCancelled) {
            R.string.remove_watermark_payment_cancelled
        } else {
            R.string.remove_watermark_payment_failure
        }

        val titleRes = if (isCancelled) {
            R.string.payment_cancelled
        } else {
            R.string.payment_failed
        }

        val dialog = SimpleDialogFragment.newInstance(
            messageRes = messageRes,
            titleRes = titleRes,
            imageRes = R.drawable.ic_circle_close,
            positiveButtonTitleRes = R.string.ok,
        )

        dialog.positiveButtonClick.observe(this) {
            dialog.dismiss()
        }

        dialog.show(childFragmentManager, SimpleDialogFragment.TAG)
    }

    private fun showRemovalErrorDialog() {
        val dialog = SimpleDialogFragment.newInstance(
            messageRes = R.string.remove_watermark_failed,
            titleRes = R.string.oh_no,
            imageRes = R.drawable.ic_circle_close,
            positiveButtonTitleRes = R.string.ok,
            dismissible = false
        )

        dialog.positiveButtonClick.observe(this) {
            dialog.dismiss()
            binding.root.findNavController().navigateUp()
        }

        dialog.show(childFragmentManager, SimpleDialogFragment.TAG)
    }
}