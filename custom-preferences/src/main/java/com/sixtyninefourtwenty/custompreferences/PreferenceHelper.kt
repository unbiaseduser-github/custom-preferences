@file:JvmName("PreferenceHelper")

package com.sixtyninefourtwenty.custompreferences

import androidx.preference.Preference

@Suppress("UNCHECKED_CAST")
fun <T> Preference.setTypedPreferenceChangeListener(block: ((T) -> Boolean)?) {
    if (block != null) {
        setOnPreferenceChangeListener { _, newValue -> block(newValue as T) }
    } else {
        onPreferenceChangeListener = null
    }
}
