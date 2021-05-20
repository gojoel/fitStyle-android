package ai.folded.fitstyle

import ai.folded.fitstyle.data.Status
import ai.folded.fitstyle.databinding.FragmentPaymentBinding
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

@AndroidEntryPoint
class PaymentFragment: Fragment() {
    private var _binding: FragmentPaymentBinding? = null

    private val binding get() = _binding!!

    private val args: PaymentFragmentArgs by navArgs()

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
            args.styledImage
        )
    }

    private lateinit var paymentSheet: PaymentSheet

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
        prepareCheckout {customerConfig, clientSecret ->
            paymentSheet.presentWithPaymentIntent(
                clientSecret,
                PaymentSheet.Configuration(
                    merchantDisplayName = MERCHANT,
                    googlePay = googlePayConfig,
                    customer = customerConfig,
                    primaryButtonColor = ColorStateList.valueOf(ContextCompat.getColor(
                        requireNotNull(activity), R.color.purple_200))
                )
            )
        }
    }

    private fun prepareCheckout(
        onSuccess: (PaymentSheet.CustomerConfiguration, String) -> Unit
    ) {
        paymentViewModel.preparePaymentRequest()
            .observe(viewLifecycleOwner) { checkoutResponse ->
                if (checkoutResponse != null) {
                    // Re-initing here because the ExampleApplication inits with the key from
                    // gradle properties
//                    PaymentConfiguration.init(this, checkoutResponse.publishableKey)

                    onSuccess(
                        PaymentSheet.CustomerConfiguration(
                            id = checkoutResponse.customerId,
                            ephemeralKeySecret = checkoutResponse.ephemeralKey
                        ),
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

                paymentViewModel.removeWatermark(args.styledImage.imagePath).observe(this) { status ->
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
            R.string.remove_watermark_payment_success,
            R.string.all_set,
            R.drawable.ic_circle_checkmark,
            R.string.view_image,
            false
        )

        dialog.buttonClick.observe(this) {
            dialog.dismiss()
            binding.root.findNavController().navigateUp()
        }

        dialog.show(childFragmentManager, SimpleDialogFragment.TAG)
    }

    private fun showPaymentRequestErrorDialog() {
        val dialog = SimpleDialogFragment.newInstance(
            R.string.remove_watermark_payment_failure,
            R.string.payment_failed,
            R.drawable.ic_circle_close,
            R.string.ok,
        )

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
            messageRes,
            titleRes,
            R.drawable.ic_circle_close,
            R.string.ok,
        )

        dialog.show(childFragmentManager, SimpleDialogFragment.TAG)
    }

    private fun showRemovalErrorDialog() {
        val dialog = SimpleDialogFragment.newInstance(
            R.string.remove_watermark_failed,
            R.string.oh_no,
            R.drawable.ic_circle_close,
            R.string.ok,
            dismissible = false
        )

        dialog.buttonClick.observe(this) {
            dialog.dismiss()
            binding.root.findNavController().navigateUp()
        }

        dialog.show(childFragmentManager, SimpleDialogFragment.TAG)
    }
}