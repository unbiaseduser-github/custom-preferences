package com.sixtyninefourtwenty.custompreferences

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.widget.ImageView
import androidx.annotation.ArrayRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.preference.Preference
import androidx.preference.Preference.SummaryProvider
import androidx.preference.PreferenceViewHolder
import com.github.dhaval2404.colorpicker.MaterialColorPickerDialog
import com.sixtyninefourtwenty.custompreferences.internal.useCompat

@Suppress("unused")
class PredefinedColorPickerPreference : Preference {

    @SuppressLint("Recycle")
    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null) : super(context, attrs) {
        context.obtainStyledAttributes(attrs, R.styleable.PredefinedColorPickerPreference).useCompat {
            availableColors = initAvailableColors(it)
            initSummaryProvider(it)
        }
        widgetLayoutResource = R.layout.preference_widget_color_swatch
    }

    @SuppressLint("Recycle")
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        context.obtainStyledAttributes(attrs, R.styleable.PredefinedColorPickerPreference, defStyleAttr, 0).useCompat {
            availableColors = initAvailableColors(it)
            initSummaryProvider(it)
        }
        widgetLayoutResource = R.layout.preference_widget_color_swatch
    }

    @SuppressLint("Recycle")
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        context.obtainStyledAttributes(attrs, R.styleable.PredefinedColorPickerPreference, defStyleAttr, defStyleRes).useCompat {
            availableColors = initAvailableColors(it)
            initSummaryProvider(it)
        }
        widgetLayoutResource = R.layout.preference_widget_color_swatch
    }

    private fun initAvailableColors(typedArray: TypedArray) =
        context.resources.getIntArray(typedArray.getResourceId(R.styleable.PredefinedColorPickerPreference_colors, R.array.color_picker_default_colors))

    private fun initSummaryProvider(typedArray: TypedArray) {
        if (typedArray.getBoolean(R.styleable.PredefinedColorPickerPreference_pcpp_useSimpleSummaryProvider, false)) {
            summaryProvider = SUMMARY_PROVIDER
        }
    }

    private var colorWidget: ImageView? = null
    private var currentColor: Int = Color.BLACK
    private var availableColors: IntArray

    fun setAvailableColorsArrayRes(@ArrayRes arrayRes: Int) {
        availableColors = context.resources.getIntArray(arrayRes)
    }

    @SuppressLint("ResourceType")
    override fun onClick() {
        super.onClick()
        MaterialColorPickerDialog.Builder(context)
            .also {
                val prefTitle = title
                if (prefTitle != null) {
                    it.setTitle(prefTitle.toString())
                }
            }
            .setColorRes(availableColors)
            .setDefaultColor(currentColor)
            .setColorListener { color, _ ->
                if (callChangeListener(color)) {
                    setColor(color)
                }
            }
            .show()
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        colorWidget = holder.findViewById(R.id.color_picker_widget) as ImageView
        setColorOnWidget(currentColor)
    }

    private fun setColorOnWidget(color: Int) {
        val widget = colorWidget
        if (widget != null) {
            val drawable = ContextCompat.getDrawable(context, R.drawable.colorpicker_pref_swatch)
            drawable?.setTint(color)
            widget.setImageDrawable(drawable)
        }
    }

    private fun setColor(color: Int) {
        currentColor = color
        persistInt(color)
        setColorOnWidget(color)
        notifyChanged()
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any? {
        return a.getString(index)
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        val value = defaultValue as String?
        setColor(getPersistedInt(if (value.isNullOrBlank()) Color.BLACK else value.toColorInt()))
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        if (isPersistent) {
            return superState
        }
        return SavedState(superState).apply {
            color = currentColor
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
        } else {
            super.onRestoreInstanceState(state.superState)
            setColor(state.color)
        }
    }

    private class SavedState : BaseSavedState {

        var color: Int = Color.BLACK

        constructor(source: Parcel) : super(source) {
            color = source.readInt()
        }

        constructor(superState: Parcelable?) : super(superState)

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeInt(color)
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(source: Parcel): SavedState = SavedState(source)

            override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)

        }

    }

    companion object {
        val SUMMARY_PROVIDER by lazy {
            SummaryProvider<PredefinedColorPickerPreference> {
                "#${Integer.toHexString(it.currentColor)}"
            }
        }
    }

}