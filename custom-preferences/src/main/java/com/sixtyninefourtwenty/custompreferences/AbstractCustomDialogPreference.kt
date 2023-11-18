package com.sixtyninefourtwenty.custompreferences

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.preference.DialogPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceFragmentCompat.OnPreferenceDisplayDialogCallback

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

    @SuppressLint("RestrictedApi")
    fun displayDialog(fragment: PreferenceFragmentCompat) {
        var handled = false
        val cb1 = fragment.callbackFragment
        if (cb1 is OnPreferenceDisplayDialogCallback) {
            handled = cb1.onPreferenceDisplayDialog(fragment, this)
        }

        var cb2: Fragment? = fragment
        while (!handled && cb2 != null) {
            if (cb2 is OnPreferenceDisplayDialogCallback) {
                handled = cb2.onPreferenceDisplayDialog(fragment, this)
            }
            cb2 = cb2.parentFragment
        }

        val context = fragment.context
        if (!handled && context is OnPreferenceDisplayDialogCallback) {
            handled = context.onPreferenceDisplayDialog(fragment, this)
        }

        val activity = fragment.activity
        if (!handled && activity is OnPreferenceDisplayDialogCallback) {
            handled = activity.onPreferenceDisplayDialog(fragment, this)
        }

        if (handled) {
            return
        }

        if (fragment.parentFragmentManager.findFragmentByTag(TAG) != null) {
            return
        }

        createDialog().apply {
            @Suppress("DEPRECATION")
            setTargetFragment(fragment, 0)
            Bundle.EMPTY
            arguments = Bundle(arguments ?: Bundle()).apply { putString("key", key) }
        }.show(fragment.parentFragmentManager, TAG)
    }

}

private const val TAG = "androidx.preference.PreferenceFragment.DIALOG"
