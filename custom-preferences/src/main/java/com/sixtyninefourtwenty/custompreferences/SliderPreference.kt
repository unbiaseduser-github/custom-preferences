package com.sixtyninefourtwenty.custompreferences

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import androidx.core.content.res.TypedArrayUtils
import androidx.core.os.ParcelCompat
import androidx.preference.PreferenceViewHolder
import com.google.android.material.slider.LabelFormatter
import com.google.android.material.slider.Slider
import com.sixtyninefourtwenty.custompreferences.SliderPreference.Companion.DEFAULT_VALUE

/**
 * Inline preference that hosts a [Slider], supporting these attributes:
 * - [Slider.valueFrom]
 * - [Slider.valueTo]
 * - [Slider.stepSize]
 * - [Slider.tickVisible]
 * - [Slider.labelBehavior] (represented as [isLabelVisible] - true for
 * [LabelFormatter.LABEL_FLOATING] and false for [LabelFormatter.LABEL_GONE])
 *
 * This preference saves a float value.
 *
 * Style attribute: [R.attr.sliderPreferenceStyle], default
 * [R.style.Preference_SliderPreference]
 *
 * Note that this preference always has a value (see [DEFAULT_VALUE]), due to the fact that it's
 * impossible to represent a "no value" state on the UI.
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
open class SliderPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @SuppressLint("RestrictedApi")
    defStyleAttr: Int = TypedArrayUtils.getAttr(context, R.attr.sliderPreferenceStyle, 0),
    defStyleRes: Int = R.style.Preference_SliderPreference
) : PreferenceCustomViewUnderneath(context, attrs, defStyleAttr, defStyleRes), CanSetPreferenceChangeListener<Float> {

    private var _value: Float = DEFAULT_VALUE
    var value: Float
        get() = _value
        set(value) = setValueInternal(value, true)
    private var _valueFrom: Float = DEFAULT_VALUE_FROM
    var valueFrom: Float
        get() = _valueFrom
        set(value) {
            _valueFrom = value
            notifyChanged()
        }
    private var _valueTo: Float = DEFAULT_VALUE_TO
    var valueTo: Float
        get() = _valueTo
        set(value) {
            _valueTo = value
            notifyChanged()
        }
    private var _stepSize: Float = DEFAULT_STEP_SIZE
    var stepSize: Float
        get() = _stepSize
        set(value) {
            _stepSize = value
            notifyChanged()
        }
    private var _isTickVisible: Boolean = DEFAULT_TICK_VISIBLE
    var isTickVisible: Boolean
        get() = _isTickVisible
        set(value) {
            _isTickVisible = value
            notifyChanged()
        }
    private var _isLabelVisible: Boolean = DEFAULT_LABEL_VISIBLE
    var isLabelVisible: Boolean
        get() = _isLabelVisible
        set(value) {
            _isLabelVisible = value
            notifyChanged()
        }

    /**
     * Sets multiple properties to this preference at once. This method is recommended over
     * setting multiple individual properties since this doesn't make the preference refresh
     * the UI multiple times needlessly.
     */
    @JvmOverloads
    fun setProperties(
        value: Float? = null,
        valueFrom: Float? = null,
        valueTo: Float? = null,
        stepSize: Float? = null,
        isTickVisible: Boolean? = null,
        isLabelVisible: Boolean? = null
    ) {
        if (value != null) {
            this._value = value
        }
        if (valueFrom != null) {
            this._valueFrom = valueFrom
        }
        if (valueTo != null) {
            this._valueTo = valueTo
        }
        if (stepSize != null) {
            this._stepSize = stepSize
        }
        if (isTickVisible != null) {
            this._isTickVisible = isTickVisible
        }
        if (isLabelVisible != null) {
            this._isLabelVisible = isLabelVisible
        }
        notifyChanged()
    }

    /**
     * @see setProperties
     */
    fun setProperties(properties: Properties) = setProperties(
        value = properties.value,
        valueFrom = properties.valueFrom,
        valueTo = properties.valueTo,
        stepSize = properties.stepSize,
        isTickVisible = properties.isTickVisible,
        isLabelVisible = properties.isLabelVisible
    )

    class Properties private constructor(
        @JvmField internal val value: Float?,
        @JvmField internal val valueFrom: Float?,
        @JvmField internal val valueTo: Float?,
        @JvmField internal val stepSize: Float?,
        @JvmField internal val isTickVisible: Boolean?,
        @JvmField internal val isLabelVisible: Boolean?,
    ) {

        class Builder {
            private var value: Float? = null
            private var valueFrom: Float? = null
            private var valueTo: Float? = null
            private var stepSize: Float? = null
            private var isTickVisible: Boolean? = null
            private var isLabelVisible: Boolean? = null

            fun setValue(value: Float?) = apply { this.value = value }
            fun setValueFrom(valueFrom: Float?) = apply { this.valueFrom = valueFrom }
            fun setValueTo(valueTo: Float?) = apply { this.valueTo = valueTo }
            fun setStepSize(stepSize: Float?) = apply { this.stepSize = stepSize }
            fun setTickVisible(isTickVisible: Boolean?) = apply { this.isTickVisible = isTickVisible }
            fun setLabelVisible(isLabelVisible: Boolean?) = apply { this.isLabelVisible = isLabelVisible }
            fun build() = Properties(value, valueFrom, valueTo, stepSize, isTickVisible, isLabelVisible)
        }

    }

    private val onSliderTouchListener = object : Slider.OnSliderTouchListener {
        override fun onStartTrackingTouch(slider: Slider) = Unit

        override fun onStopTrackingTouch(slider: Slider) {
            val newValue = slider.value
            if (callChangeListener(newValue)) {
                setValueInternal(newValue, false)
            } else {
                slider.value = this@SliderPreference.value
            }
        }
    }

    private fun setValueInternal(value: Float, notifyChanged: Boolean) {
        this._value = value
        persistFloat(value)
        if (notifyChanged) {
            notifyChanged()
        }
    }

    init {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.SliderPreference, defStyleAttr, defStyleRes)
        setProperties(
            valueFrom = ta.getFloat(R.styleable.SliderPreference_sp_valueFrom, DEFAULT_VALUE_FROM),
            valueTo = ta.getFloat(R.styleable.SliderPreference_sp_valueTo, DEFAULT_VALUE_TO),
            stepSize = ta.getFloat(R.styleable.SliderPreference_sp_stepSize, DEFAULT_STEP_SIZE),
            isTickVisible = ta.getBoolean(R.styleable.SliderPreference_sp_tickVisible, DEFAULT_TICK_VISIBLE),
            isLabelVisible = ta.getBoolean(R.styleable.SliderPreference_sp_labelVisible, DEFAULT_LABEL_VISIBLE)
        )
        ta.recycle()
    }

    override fun setOnPreferenceChange(block: ((newValue: Float) -> Boolean)?) {
        setTypedPreferenceChangeListener(block)
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        (holder.findViewById(R.id.slider) as Slider).also {
            it.valueFrom = valueFrom
            it.valueTo = valueTo
            it.stepSize = stepSize
            it.isTickVisible = isTickVisible
            it.labelBehavior = if (isLabelVisible) LabelFormatter.LABEL_FLOATING else LabelFormatter.LABEL_GONE
            it.value = value
            it.clearOnSliderTouchListeners()
            it.addOnSliderTouchListener(onSliderTouchListener)
        }
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any? {
        return a.getFloat(index, DEFAULT_VALUE)
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        value = getPersistedFloat((defaultValue as Float?) ?: DEFAULT_VALUE)
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        if (isPersistent) {
            return superState
        }

        return SavedState(superState).also {
            it.value = this.value
            it.valueFrom = this.valueFrom
            it.valueTo = this.valueTo
            it.stepSize = this.stepSize
            it.isTickVisible = this.isTickVisible
            it.isLabelVisible = this.isLabelVisible
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }

        super.onRestoreInstanceState(state.superState)
        setProperties(
            value = state.value,
            valueFrom = state.valueFrom,
            valueTo = state.valueTo,
            stepSize = state.stepSize,
            isTickVisible = state.isTickVisible,
            isLabelVisible = state.isLabelVisible
        )
    }

    private class SavedState : BaseSavedState {

        @JvmField
        var value: Float = DEFAULT_VALUE
        @JvmField
        var valueFrom: Float = DEFAULT_VALUE_FROM
        @JvmField
        var valueTo: Float = DEFAULT_VALUE_TO
        @JvmField
        var stepSize: Float = DEFAULT_STEP_SIZE
        @JvmField
        var isTickVisible: Boolean = true
        @JvmField
        var isLabelVisible: Boolean = true

        constructor(source: Parcel): super(source) {
            value = source.readFloat()
            valueFrom = source.readFloat()
            valueTo = source.readFloat()
            stepSize = source.readFloat()
            isTickVisible = ParcelCompat.readBoolean(source)
            isLabelVisible = ParcelCompat.readBoolean(source)
        }

        constructor(superState: Parcelable?): super(superState)

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            with(dest) {
                writeFloat(value)
                writeFloat(valueFrom)
                writeFloat(valueTo)
                writeFloat(stepSize)
                ParcelCompat.writeBoolean(this, isTickVisible)
                ParcelCompat.writeBoolean(this, isLabelVisible)
            }
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(source: Parcel): SavedState = SavedState(source)
            override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
        }

    }

    companion object {
        const val DEFAULT_VALUE = 0F
        const val DEFAULT_VALUE_FROM = 0F
        const val DEFAULT_VALUE_TO = 100F
        const val DEFAULT_STEP_SIZE = 1F
        const val DEFAULT_TICK_VISIBLE = true
        const val DEFAULT_LABEL_VISIBLE = true
    }

}