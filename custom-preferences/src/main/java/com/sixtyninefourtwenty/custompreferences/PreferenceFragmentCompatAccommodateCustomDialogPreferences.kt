package com.sixtyninefourtwenty.custompreferences

import androidx.annotation.CallSuper
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

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