package com.sixtyninefourtwenty.custompreferences

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.get
import androidx.preference.Preference
import com.google.android.material.button.MaterialButtonToggleGroup

/**
 * [AbstractToggleGroupPreference] that allows users to select a single option. This preference
 * saves a string value (from one of [copyOfEntryValues]).
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
open class ToggleGroupPreference : AbstractToggleGroupPreference, CanSetPreferenceChangeListener<String> {

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null) : super(context, attrs) {
        init(context.obtainStyledAttributes(attrs, R.styleable.ToggleGroupPreference))
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context.obtainStyledAttributes(attrs, R.styleable.ToggleGroupPreference, defStyleAttr, 0))
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context.obtainStyledAttributes(attrs, R.styleable.ToggleGroupPreference, defStyleAttr, defStyleRes))
    }

    private fun init(ta: TypedArray) {
        fun initIcons(): Array<Drawable?>? {
            val arrayRes = ta.getResourceId(R.styleable.ToggleGroupPreference_tgp_icons, 0)
            return if (arrayRes == 0) {
                null
            } else {
                // Map drawables manually to avoid a List allocation
                val drawableResIds = context.resources.getIntArray(arrayRes)
                val drawables: Array<Drawable?> = arrayOfNulls(drawableResIds.size)
                drawableResIds.forEachIndexed { index, resId ->
                    drawables[index] = ContextCompat.getDrawable(context, resId)
                }
                drawables
            }
        }

        setEntries(
            entries = ta.getTextArray(R.styleable.ToggleGroupPreference_tgp_entries),
            entryValues = ta.getTextArray(R.styleable.ToggleGroupPreference_tgp_entryValues),
            icons = initIcons()
        )
        if (ta.getBoolean(R.styleable.ToggleGroupPreference_tgp_useSimpleSummaryProvider, false)) {
            summaryProvider = getSimpleSummaryProvider()
        }
        ta.recycle()
    }

    override fun setOnPreferenceChange(block: ((newValue: String) -> Boolean)?) {
        setTypedPreferenceChangeListener(block)
    }

    override val isPreferenceSingleSelection: Boolean = true
    private var _value: String? = null
    var value: String?
        get() = _value
        set(value) = setValueInternal(value, true)

    private fun setValueInternal(value: String?, notifyChanged: Boolean) {
        this._value = value
        persistString(value)
        if (notifyChanged) {
            notifyChanged()
        }
    }

    private fun setValueOnToggleGroup(
        value: String?,
        entryValues: Array<CharSequence>?,
        toggleGroup: MaterialButtonToggleGroup
    ) {
        //toggleGroup.removeOnButtonCheckedListener(buttonCheckedListener)
        if (value == null || entryValues == null) {
            toggleGroup.clearChecked()
        } else {
            val valueIndex = entryValues.indexOf(value)
            if (valueIndex >= 0) {
                toggleGroup.check(toggleGroup[valueIndex].id)
            } else {
                toggleGroup.clearChecked()
            }
        }
        //toggleGroup.addOnButtonCheckedListener(buttonCheckedListener)
    }

    /* For some reason when I use a MaterialButtonToggleGroup.OnButtonCheckedListener it always
    gets called even when I previously removed it from the MaterialButtonToggleGroup.
    private val buttonCheckedListener = object : MaterialButtonToggleGroup.OnButtonCheckedListener {
        override fun onButtonChecked(
            group: MaterialButtonToggleGroup,
            checkedId: Int,
            isChecked: Boolean
        ) {
            group.removeOnButtonCheckedListener(this)
            val index = group.indexOfChild(group.children.first { it.id == checkedId })
            val strEntryValue = entryValues!![index].toString()
            if (callChangeListener(strEntryValue)) {
                value = strEntryValue
            } else {
                val oldButtonIndex = value?.let { entryValues!!.indexOf(it) } ?: -1
                if (oldButtonIndex >= 0) {
                    group.check(group[oldButtonIndex].id)
                } else {
                    group.clearChecked()
                }
            }
            group.addOnButtonCheckedListener(this)
        }
    }*/

    override fun bind(toggleGroup: MaterialButtonToggleGroup) {
        val entryValues = copyOfEntryValues()
        setValueOnToggleGroup(value, entryValues, toggleGroup)
        //toggleGroup.addOnButtonCheckedListener(buttonCheckedListener)
        if (entryValues != null) {
            toggleGroup.children.forEachIndexed { index, view ->
                view.setOnClickListener {
                    val strEntryValue = entryValues[index].toString()
                    if (callChangeListener(strEntryValue)) {
                        setValueInternal(strEntryValue, false)
                    } else {
                        val oldButtonIndex = value?.let { entryValues.indexOf(it) } ?: -1
                        if (oldButtonIndex >= 0) {
                            toggleGroup.check(toggleGroup[oldButtonIndex].id)
                        } else {
                            toggleGroup.clearChecked()
                        }
                    }
                }
            }
        }
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any? {
        return a.getString(index)
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        value = getPersistedString(defaultValue as String?)
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        if (isPersistent) {
            return superState
        }

        return SavedState(superState).apply {
            value = this@ToggleGroupPreference.value
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }

        super.onRestoreInstanceState(state.superState)
        value = state.value
    }

    private class SavedState : AbstractToggleGroupPreference.SavedState {

        @JvmField
        var value: String? = null

        constructor(source: Parcel): super(source) {
            value = source.readString()
        }

        constructor(superState: Parcelable?): super(superState)

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeString(value)
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(source: Parcel): SavedState = SavedState(source)
            override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
        }

    }

    companion object {
        @JvmStatic
        fun getSimpleSummaryProvider() = summaryProvider
        private val summaryProvider by lazy(LazyThreadSafetyMode.NONE) {
            Preference.SummaryProvider<ToggleGroupPreference> { preference ->
                val value = preference.value
                val entryValues = preference.copyOfEntryValues()
                val entries = preference.copyOfEntries()
                val valueIndex = value?.let {
                    entryValues?.indexOf(it) ?: -1
                } ?: -1
                if (entries != null && valueIndex >= 0) {
                    entries[valueIndex]
                } else {
                    preference.context.getString(androidx.preference.R.string.not_set)
                }
            }
        }
    }

}