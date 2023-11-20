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
import com.sixtyninefourtwenty.custompreferences.internal.getAndroidXNotSetString

/**
 * [AbstractToggleGroupPreference] that allows users to select multiple options. This preference
 * saves a string set value (from those in [copyOfEntryValues]).
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
open class MultiSelectToggleGroupPreference : AbstractToggleGroupPreference, CanSetPreferenceChangeListener<Set<String>> {

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null) : super(context, attrs) {
        init(context.obtainStyledAttributes(attrs, R.styleable.MultiSelectToggleGroupPreference))
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context.obtainStyledAttributes(attrs, R.styleable.MultiSelectToggleGroupPreference))
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context.obtainStyledAttributes(attrs, R.styleable.MultiSelectToggleGroupPreference))
    }

    private fun init(ta: TypedArray) {
        fun initIcons(): Array<Drawable?>? {
            val arrayRes = ta.getResourceId(R.styleable.MultiSelectToggleGroupPreference_mstgp_icons, 0)
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
            entries = ta.getTextArray(R.styleable.MultiSelectToggleGroupPreference_mstgp_entries),
            entryValues = ta.getTextArray(R.styleable.MultiSelectToggleGroupPreference_mstgp_entryValues),
            icons = initIcons()
        )
        ta.recycle()
    }

    override val isPreferenceSingleSelection: Boolean = false
    private var _values: Set<String>? = null
    var values: Set<String>?
        get() = _values
        set(value) = setValueInternal(value, true)

    private fun setValueInternal(values: Set<String>?, notifyChanged: Boolean) {
        this._values = values
        persistStringSet(values)
        if (notifyChanged) {
            notifyChanged()
        }
    }

    private fun setValuesOnToggleGroup(
        values: Set<String>?,
        entryValues: Array<CharSequence>?,
        toggleGroup: MaterialButtonToggleGroup
    ) {
        toggleGroup.removeOnButtonCheckedListener(buttonCheckedListener)
        if (values.isNullOrEmpty() || entryValues == null) {
            toggleGroup.clearChecked()
        } else {
            toggleGroup.clearChecked()
            // Use int array to avoid boxing
            val indices = IntArray(entryValues.size) { -1 }
            var indexInIndices = 0
            for (value in values) {
                indices[indexInIndices] = entryValues.indexOf(value)
                indexInIndices++
            }
            for (index in indices) {
                if (index >= 0) {
                    toggleGroup.check(toggleGroup[index].id)
                }
            }
        }
        toggleGroup.addOnButtonCheckedListener(buttonCheckedListener)
    }

    private val buttonCheckedListener = object : MaterialButtonToggleGroup.OnButtonCheckedListener {
        override fun onButtonChecked(
            group: MaterialButtonToggleGroup,
            checkedId: Int,
            isChecked: Boolean
        ) {
            group.removeOnButtonCheckedListener(this)
            val entryValues = copyOfEntryValues()
            val index = group.indexOfChild(group.children.first { it.id == checkedId })
            val newValues = if (isChecked) {
                values.orEmpty() + entryValues!![index].toString()
            } else {
                values.orEmpty() - entryValues!![index].toString()
            }
            if (callChangeListener(newValues)) {
                setValueInternal(newValues, false)
            } else {
                if (isChecked) {
                    group.uncheck(checkedId)
                } else {
                    group.check(checkedId)
                }
            }
            group.addOnButtonCheckedListener(this)
        }
    }

    override fun bind(toggleGroup: MaterialButtonToggleGroup) {
        setValuesOnToggleGroup(values, copyOfEntryValues(), toggleGroup)
        toggleGroup.clearOnButtonCheckedListeners()
        toggleGroup.addOnButtonCheckedListener(buttonCheckedListener)
    }

    override fun setOnPreferenceChange(block: ((newValue: Set<String>) -> Boolean)?) {
        setTypedPreferenceChangeListener(block)
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any {
        return a.getTextArray(index).mapTo(mutableSetOf()) { it.toString() }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onSetInitialValue(defaultValue: Any?) {
        values = getPersistedStringSet(defaultValue as Set<String>?)
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        if (isPersistent) {
            return superState
        }

        return SavedState(superState).apply {
            values = this@MultiSelectToggleGroupPreference.values
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }

        super.onRestoreInstanceState(state.superState)
        values = state.values
    }

    private class SavedState : AbstractToggleGroupPreference.SavedState {

        @JvmField
        var values: Set<String>? = null

        constructor(source: Parcel): super(source) {
            values = source.createStringArray()?.toSet()
        }

        constructor(superState: Parcelable?): super(superState)

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeStringArray(values?.toTypedArray())
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(source: Parcel): SavedState = SavedState(source)
            override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
        }

    }

    companion object {
        @JvmStatic
        fun createSummaryProvider(
            summaryWhenSet: (Set<String>) -> CharSequence
        ) = Preference.SummaryProvider<MultiSelectToggleGroupPreference> {
            val values = it.values
            if (values != null) {
                summaryWhenSet(values)
            } else {
                it.getAndroidXNotSetString()
            }
        }
    }

}