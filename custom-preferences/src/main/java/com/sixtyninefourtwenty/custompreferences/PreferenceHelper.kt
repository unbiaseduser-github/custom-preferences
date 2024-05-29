@file:JvmName("PreferenceHelper")

package com.sixtyninefourtwenty.custompreferences

import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.timepicker.MaterialTimePicker
import java.time.LocalTime

@Suppress("UNCHECKED_CAST")
fun <T> Preference.setTypedPreferenceChangeListener(block: ((T) -> Boolean)?) {
    if (block != null) {
        setOnPreferenceChangeListener { _, newValue -> block(newValue as T) }
    } else {
        onPreferenceChangeListener = null
    }
}

/**
 * Make [TimePickerPreference]'s dialog not break on configuration changes. Call this in
 * [PreferenceFragmentCompat.onCreatePreferences].
 */
fun PreferenceFragmentCompat.installConfigurationChangePatch() {
    parentFragmentManager.addFragmentOnAttachListener { _, fragment ->
        if (fragment is MaterialTimePicker && TAG == fragment.tag) {
            // Guard against multiple configuration changes without the fragment being dismissed
            fragment.clearOnPositiveButtonClickListeners()
            fragment.addOnPositiveButtonClickListener {
                val key = fragment.requireArguments().getString("key") ?: return@addOnPositiveButtonClickListener
                val pref = findPreference<TimePickerPreference>(key) ?: return@addOnPositiveButtonClickListener
                pref.handleNewlyPickedValue(LocalTime.of(fragment.hour, fragment.minute))
            }
        }
    }
}
