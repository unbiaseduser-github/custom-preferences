@file:JvmName("PreferenceHelper")

package com.sixtyninefourtwenty.custompreferences

import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.timepicker.MaterialTimePicker
import java.time.LocalTime

/**
 * Helper method for implementing [CanSetPreferenceChangeListener].
 */
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
 *
 * **Limitation:** If your preference doesn't have a key, this method is essentially useless.
 */
fun PreferenceFragmentCompat.installConfigurationChangePatch() {
    val existingDialogFragment = parentFragmentManager.findFragmentByTag(TAG) ?: return
    if (existingDialogFragment is MaterialTimePicker) {
        existingDialogFragment.addOnPositiveButtonClickListener {
            val key = existingDialogFragment.requireArguments().getString("key") ?: return@addOnPositiveButtonClickListener
            val pref = findPreference<TimePickerPreference>(key) ?: return@addOnPositiveButtonClickListener
            pref.handleNewlyPickedValue(LocalTime.of(existingDialogFragment.hour, existingDialogFragment.minute))
        }
    }
}
