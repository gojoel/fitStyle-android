package ai.folded.fitstyle

import ai.folded.fitstyle.adapters.SettingsAdapter
import ai.folded.fitstyle.adapters.SettingsListener
import ai.folded.fitstyle.data.SettingType
import ai.folded.fitstyle.data.SettingsItem
import ai.folded.fitstyle.databinding.FragmentSettingsBinding
import ai.folded.fitstyle.utils.CONTACT_EMAIL
import ai.folded.fitstyle.utils.PREF_KEY_USER_ID
import android.content.Context
import android.content.Intent
import android.content.Intent.EXTRA_EMAIL
import android.content.Intent.EXTRA_SUBJECT
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSettingsBinding.inflate(inflater, container, false)
        context ?: return binding.root

        binding.toolbar.setTitle(R.string.settings)
        binding.toolbar.setNavigationOnClickListener { view ->
            view.findNavController().navigateUp()
        }

        val adapter = SettingsAdapter(SettingsListener {
            when (it.type) {
                SettingType.FEEDBACK -> { sendFeedbackEmail() }
                else -> {}
            }
        })

        adapter.settings = createSettings()
        binding.recyclerView.adapter = adapter

        setHasOptionsMenu(true)
        return binding.root
    }

    private fun createSettings(): List<SettingsItem> {
        var appVersion = ""
        var userId = ""

        activity?.let {
            val manager = it.packageManager
            val info = manager.getPackageInfo(it.packageName, PackageManager.GET_ACTIVITIES)
            appVersion = info.versionName
        }

        activity?.getPreferences(Context.MODE_PRIVATE)?.let { sharedPref ->
            userId = sharedPref.getString(PREF_KEY_USER_ID, "") ?: userId
            userId = parseUserId(userId)
        }

        val userIdItem = SettingsItem(getString(R.string.user_id), userId, SettingType.USER_ID)
        val versionItem = SettingsItem(getString(R.string.version), appVersion, SettingType.APP_VERSION)
        val feedbackItem = SettingsItem(getString(R.string.feedback), getString(R.string.feedback_details), SettingType.FEEDBACK)

        return listOf(
            userIdItem, versionItem, feedbackItem
        )
    }

    private fun sendFeedbackEmail() {
        val selectorIntent = Intent(Intent.ACTION_SENDTO)
        selectorIntent.data = Uri.parse("mailto:")

        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(EXTRA_EMAIL, arrayOf(CONTACT_EMAIL))
        intent.putExtra(EXTRA_SUBJECT, getString(R.string.email_subject))
        intent.putExtra(Intent.EXTRA_TEXT, "")
        intent.selector = selectorIntent

        activity?.startActivity(Intent.createChooser(intent, "Send feedback"))
    }

    private fun parseUserId(userId: String): String {
        val index = userId.indexOf(":")
        if (index < 0 || index + 1 > userId.length) {
            return ""
        }

        val uid = userId.substring(index + 1)
        val splitComponents = uid.split("-")

        return if (splitComponents.isNotEmpty()) {
            splitComponents[0]
        } else {
            ""
        }
    }
}