package com.sixtyninefourtwenty.custompreferences

interface CanSetPreferenceChangeListener<T> {
    fun setOnPreferenceChange(block: ((newValue: T) -> Boolean)?)
}