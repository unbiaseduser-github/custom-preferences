package com.sixtyninefourtwenty.custompreferences

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.widget.ImageView
import androidx.annotation.ArrayRes
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.content.res.TypedArrayUtils
import androidx.core.graphics.toColorInt
import androidx.fragment.app.DialogFragment
import androidx.preference.Preference
import androidx.preference.Preference.SummaryProvider
import androidx.preference.PreferenceViewHolder
import com.github.dhaval2404.colorpicker.MaterialColorPicker
import com.sixtyninefourtwenty.custompreferences.internal.getAndroidXNotSetString
import com.sixtyninefourtwenty.custompreferences.internal.throwValueNotSetException

/**
 * A [Preference] that shows a [MaterialColorPicker]. This preference saves an int value.
 *
 * Style attribute: [R.attr.predefinedColorPickerPreferenceStyle], default
 * [R.style.Preference_PredefinedColorPicker]
 *
 * Default value: A String that can be processed by [Color.parseColor].
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
open class PredefinedColorPickerPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @SuppressLint("RestrictedApi")
    defStyleAttr: Int = TypedArrayUtils.getAttr(context, R.attr.predefinedColorPickerPreferenceStyle, 0),
    defStyleRes: Int = R.style.Preference_PredefinedColorPicker
) : AbstractCustomDialogPreference(context, attrs, defStyleAttr, defStyleRes), CanSetPreferenceChangeListener<Int> {

    override fun setOnPreferenceChange(block: ((newValue: Int) -> Boolean)?) {
        setTypedPreferenceChangeListener(block)
    }

    private fun initAvailableColors(typedArray: TypedArray) =
        context.resources.getIntArray(typedArray.getResourceId(R.styleable.PredefinedColorPickerPreference_pcpp_colors, R.array.color_picker_default_colors))

    private fun initSummaryProvider(typedArray: TypedArray) {
        if (typedArray.getBoolean(R.styleable.PredefinedColorPickerPreference_pcpp_useSimpleSummaryProvider, false)) {
            summaryProvider = SUMMARY_PROVIDER
        }
    }

    @ColorInt
    var color: Int? = null
        set(value) {
            field = value
            persistInt(value ?: Int.MIN_VALUE)
            notifyChanged()
        }

    /**
     * Return the color this preference has.
     * @throws IllegalStateException if the value has not been set, either from a default value or from user input
     * @see color
     */
    @ColorInt
    fun requireColor() = color ?: throwValueNotSetException()

    @ColorInt
    private var availableColors: IntArray

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.PredefinedColorPickerPreference)
        availableColors = initAvailableColors(typedArray)
        initSummaryProvider(typedArray)
        typedArray.recycle()
    }

    fun setAvailableColorsArrayRes(@ArrayRes arrayRes: Int) {
        setAvailableColors(context.resources.getIntArray(arrayRes))
    }

    fun setAvailableColors(@ColorInt colors: IntArray) {
        this.availableColors = colors
    }

    fun copyOfAvailableColors() = availableColors.clone()

    @SuppressLint("ResourceType") // MaterialColorPicker.Builder.setDefaultColor takes a color int, not a color resource :/
    override fun createDialog(): DialogFragment {
        return MaterialColorPicker.Builder(context)
            .also {
                val prefDialogTitle = dialogTitle
                if (prefDialogTitle != null) {
                    it.setTitle(prefDialogTitle.toString())
                }
            }
            .setColorRes(availableColors)
            .also {
                val prefColor = color
                if (prefColor != null) {
                    it.setDefaultColor(prefColor)
                }
            }
            .setColorListener { color, _ ->
                if (callChangeListener(color)) {
                    this.color = color
                }
            }
            .build()
            .createDialog()
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        with(holder.findViewById(R.id.color_picker_widget) as ImageView) {
            val drawable = ContextCompat.getDrawable(context, R.drawable.colorpicker_pref_swatch)?.mutate()?.apply {
                colorFilter = PorterDuffColorFilter(color ?: Color.TRANSPARENT, PorterDuff.Mode.SRC_OVER)
            }
            setImageDrawable(drawable)
        }
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any? {
        return a.getString(index)
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        val value = defaultValue as String?
        color = getPersistedInt(if (value.isNullOrBlank()) Int.MIN_VALUE else value.toColorInt())
            .takeIf { it != Int.MIN_VALUE }
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        if (isPersistent) {
            return superState
        }
        return SavedState(superState).also {
            it.color = this.color
            it.availableColors = this.availableColors
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
        } else {
            super.onRestoreInstanceState(state.superState)
            color = state.color
            availableColors = state.availableColors
        }
    }

    private class SavedState : BaseSavedState {

        var color: Int? = null
        lateinit var availableColors: IntArray

        constructor(source: Parcel) : super(source) {
            color = source.readInt().takeIf { it != Int.MIN_VALUE }
            availableColors = source.createIntArray()!!
        }

        constructor(superState: Parcelable?) : super(superState)

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            with(dest) {
                writeInt(color ?: Int.MIN_VALUE)
                writeIntArray(availableColors)
            }
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(source: Parcel): SavedState = SavedState(source)

            override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)

        }

    }

    companion object {
        @JvmStatic
        fun getSimpleSummaryProvider() = SUMMARY_PROVIDER
        val SUMMARY_PROVIDER by lazy {
            SummaryProvider<PredefinedColorPickerPreference> {
                val prefColor = it.color
                if (prefColor != null) {
                    "#${Integer.toHexString(prefColor)}"
                } else {
                    it.getAndroidXNotSetString()
                }
            }
        }
    }

}