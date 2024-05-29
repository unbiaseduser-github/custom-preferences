package com.sixtyninefourtwenty.custompreferences

import androidx.preference.Preference

/**
 * Interface that denotes a [Preference] subclass that has a type-safe wrapper for [Preference.setOnPreferenceChangeListener].
 * This is only for convenience of users of these classes, as they can simply look at the class
 * declaration to see what type of object to set a listener on.
 *
 * Implementation notes: In [setOnPreferenceChange], call [Preference.setTypedPreferenceChangeListener].
 * That will internally use [Preference.setOnPreferenceChangeListener].
 */
interface CanSetPreferenceChangeListener<T> {
    fun setOnPreferenceChange(block: ((newValue: T) -> Boolean)?)
}