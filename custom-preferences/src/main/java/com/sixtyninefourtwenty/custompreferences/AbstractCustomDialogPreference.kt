package com.sixtyninefourtwenty.custompreferences

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.preference.DialogPreference
import androidx.preference.PreferenceFragmentCompat

/**
 * Base class for all preferences that display a [DialogFragment]. The difference between this and
 * [DialogPreference] is that the library will apply behavior to the fragment to match that of
 * [PreferenceFragmentCompat.onDisplayPreferenceDialog] and the stock `...DialogFragmentCompat` classes.
 * The library will take care of showing the dialog for you if your preference fragment extends
 * [PreferenceFragmentCompatAccommodateCustomDialogPreferences].
 */
@Suppress("unused")
abstract class AbstractCustomDialogPreference : DialogPreference {

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    protected abstract fun createDialog(): DialogFragment

    //do some stuff that PreferenceFragmentCompat.onDisplayPreferenceDialog does. is it really needed? eh
    fun displayDialog(fragment: Fragment) = createDialog().apply {
        @Suppress("DEPRECATION")
        setTargetFragment(fragment, 0)
        arguments = Bundle(arguments ?: Bundle()).apply { putString("key", key) }
    }.show(fragment.parentFragmentManager, "androidx.preference.PreferenceFragment.DIALOG")

}