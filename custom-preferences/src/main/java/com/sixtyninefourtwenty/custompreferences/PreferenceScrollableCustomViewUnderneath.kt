package com.sixtyninefourtwenty.custompreferences

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import androidx.core.content.res.TypedArrayUtils
import androidx.preference.Preference

/**
 * Base class for preferences that have a horizontally scrollable widget underneath its summary.
 *
 * Style attribute: [R.attr.preferenceScrollableCustomViewUnderneathStyle], default
 * [R.style.Preference_ScrollableCustomViewUnderneath]
 */
open class PreferenceScrollableCustomViewUnderneath @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @SuppressLint("RestrictedApi")
    defStyleAttr: Int = TypedArrayUtils.getAttr(context, R.attr.preferenceScrollableCustomViewUnderneathStyle, 0),
    defStyleRes: Int = R.style.Preference_ScrollableCustomViewUnderneath
) : Preference(context, attrs, defStyleAttr, defStyleRes)