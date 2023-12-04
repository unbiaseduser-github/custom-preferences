package com.sixtyninefourtwenty.custompreferences

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import androidx.core.content.res.TypedArrayUtils
import androidx.preference.Preference

/**
 * Base class for preferences that have a widget underneath its summary.
 *
 * Style attribute: [R.attr.preferenceCustomViewUnderneathStyle], default
 * [R.style.Preference_CustomViewUnderneath]
 */
open class PreferenceCustomViewUnderneath @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @SuppressLint("RestrictedApi")
    defStyleAttr: Int = TypedArrayUtils.getAttr(context, R.attr.preferenceCustomViewUnderneathStyle, 0),
    defStyleRes: Int = R.style.Preference_CustomViewUnderneath
) : Preference(context, attrs, defStyleAttr, defStyleRes)