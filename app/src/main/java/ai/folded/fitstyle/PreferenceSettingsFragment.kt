package ai.folded.fitstyle

import ai.folded.fitstyle.utils.CONTACT_EMAIL
import ai.folded.fitstyle.utils.PREF_KEY_USER_ID
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

class PreferenceSettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        preferenceManager.findPreference<Preference>("userId")?.let {
            activity?.getPreferences(Context.MODE_PRIVATE)?.let { sharedPref ->
                val userId = sharedPref.getString(PREF_KEY_USER_ID, "")
                it.summary = parseUserId(userId ?: "")
            }
        }

        preferenceManager.findPreference<Preference>("version")?.let {
            activity?.let { activity ->
                val manager = activity.packageManager
                val info = manager.getPackageInfo(activity.packageName, PackageManager.GET_ACTIVITIES)
                it.summary = info.versionName
            }
        }

        preferenceManager.findPreference<Preference>("feedback")?.setOnPreferenceClickListener {
            sendFeedbackEmail()
            true
        }

        return super.onCreateView(inflater, container, savedInstanceState)
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

    private fun sendFeedbackEmail() {
        val selectorIntent = Intent(Intent.ACTION_SENDTO)
        selectorIntent.data = Uri.parse("mailto:")

        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(CONTACT_EMAIL))
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject))
        intent.putExtra(Intent.EXTRA_TEXT, "")
        intent.selector = selectorIntent

        activity?.startActivity(Intent.createChooser(intent, "Send feedback"))
    }
}