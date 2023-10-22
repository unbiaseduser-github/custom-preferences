package com.sixtyninefourtwenty.custompreferences

import androidx.annotation.CallSuper
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

/**
 * Base class for preference fragments whose [AbstractCustomDialogPreference]s will be automatically handled.
 *
 * In case you don't want to extend this class, replicating said functionality is simple:
 * ```
 *     override fun onDisplayPreferenceDialog(preference: Preference) {
 *         if (preference is AbstractCustomDialogPreference) {
 *             preference.displayDialog(this)
 *         } else {
 *             super.onDisplayPreferenceDialog(preference)
 *         }
 *     }
 * ```
 */
@Suppress("unused")
abstract class PreferenceFragmentCompatAccommodateCustomDialogPreferences : PreferenceFragmentCompat() {

    @CallSuper
    override fun onDisplayPreferenceDialog(preference: Preference) {
        if (preference is AbstractCustomDialogPreference) {
            preference.displayDialog(this)
        } else {
            super.onDisplayPreferenceDialog(preference)
        }
    }

}