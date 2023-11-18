package com.sixtyninefourtwenty.custompreferences

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.CallSuper
import androidx.core.content.res.TypedArrayUtils
import androidx.preference.PreferenceViewHolder
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.sixtyninefourtwenty.custompreferences.AbstractToggleGroupPreference.SavedState

/**
 * Base class for inline preferences that host a [MaterialButtonToggleGroup]. This has:
 * - A human-readable array of entries corresponding to each option, will be displayed on
 * each button
 * - An array of values corresponding to each option, will be persisted when the option is selected
 * - An array of icons corresponding to each option (optional), will be displayed on each button if
 * available
 *
 * Style attribute: [R.attr.abstractToggleGroupPreferenceStyle], default
 * [R.style.Preference_AbstractToggleGroup_Material3].
 *
 * **Note:** If this preference is not persistent, it cannot restore its icon array via saved state.
 *
 * Implementation notes:
 * - This class only sets up the buttons for you to match the number of choices, you have to do the
 * rest (set listeners, check the appropriate button, etc.) on [bind].
 * - Subclasses' saved state classes must extend [SavedState].
 */
@Suppress("unused")
abstract class AbstractToggleGroupPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @SuppressLint("RestrictedApi")
    defStyleAttr: Int = TypedArrayUtils.getAttr(context, R.attr.abstractToggleGroupPreferenceStyle, 0),
    defStyleRes: Int = R.style.Preference_AbstractToggleGroup_Material3
) : PreferenceScrollableCustomViewUnderneath(context, attrs, defStyleAttr, defStyleRes) {

    /**
     * Whether the preference can only have one value. Will be passed to
     * [MaterialButtonToggleGroup.setSingleSelection] on [onBindViewHolder].
     */
    protected abstract val isPreferenceSingleSelection: Boolean

    private var entries: Array<CharSequence>? = null
    private var entryValues: Array<CharSequence>? = null
    private var icons: Array<Drawable?>? = null
    fun copyOfEntries() = entries?.clone()
    fun copyOfEntryValues() = entryValues?.clone()
    fun copyOfIcons() = icons?.clone()

    private fun checkEntries(
        entries: Array<CharSequence>?,
        entryValues: Array<CharSequence>?,
        icons: Array<Drawable?>?
    ) {
        require((entries == null && entryValues == null) || (entries != null && entryValues != null)) {
            "entries and entryValues must be both null or non-null"
        }
        if (entries != null && entryValues != null) {
            require(entries.size == entryValues.size) {
                "entries and entryValues must have the same number of items"
            }
            if (icons != null) {
                require(entries.size == icons.size) {
                    "entries, entryValues and icons must have the same number of items"
                }
            }
        }
    }

    @JvmOverloads
    fun setEntries(
        entries: Array<CharSequence>?,
        entryValues: Array<CharSequence>?,
        icons: Array<Drawable?>? = null
    ) {
        checkEntries(entries, entryValues, icons)
        this.entries = entries
        this.entryValues = entryValues
        this.icons = icons
        notifyChanged()
    }

    private fun setupButtonsOnToggleGroup(
        entries: Array<CharSequence>?,
        entryValues: Array<CharSequence>?,
        icons: Array<Drawable?>?,
        toggleGroup: MaterialButtonToggleGroup
    ) {
        if (entries == null || entryValues == null) {
            toggleGroup.removeAllViews()
            return
        }
        val difference = toggleGroup.childCount - entryValues.size
        if (difference > 0) {
            toggleGroup.removeViews(entryValues.size - 1, difference)
        } else if (difference < 0) {
            for (i in toggleGroup.childCount..< entryValues.size) {
                toggleGroup.addView(MaterialButton(
                    context,
                    null,
                    com.google.android.material.R.attr.materialButtonOutlinedStyle
                ), LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ))
            }
        }
        for (i in 0 ..< toggleGroup.childCount) {
            val entry = entries[i]
            val button = toggleGroup.getChildAt(i) as MaterialButton
            with(button) {
                id = View.generateViewId()
                text = entry
                icon = icons?.get(i)
            }
        }
    }

    final override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        with(holder.findViewById(R.id.toggle_group) as MaterialButtonToggleGroup) {
            isSingleSelection = isPreferenceSingleSelection
            setupButtonsOnToggleGroup(entries, entryValues, icons, this)
            bind(this)
        }
    }

    /**
     * Called in [onBindViewHolder] after the [toggleGroup] is populated with buttons
     * associated with the preference's entries and [isPreferenceSingleSelection] is set.
     * Subclasses should perform their own setup (check the appropriate button(s), set click listeners,
     * etc.) here.
     */
    abstract fun bind(toggleGroup: MaterialButtonToggleGroup)

    @CallSuper
    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        if (isPersistent) {
            return superState
        }

        return SavedState(superState).also {
            it.entries = this.entries
            it.entryValues = this.entryValues
        }
    }

    @CallSuper
    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }

        super.onRestoreInstanceState(state.superState)
        Log.w(this::class.java.simpleName, "Icons can't be restored through instance state")
        entries = state.entries
        entryValues = state.entryValues
    }

    protected open class SavedState : BaseSavedState {

        @JvmField
        var entries: Array<CharSequence>? = null
        @JvmField
        var entryValues: Array<CharSequence>? = null

        constructor(source: Parcel): super(source) {
            with(source.readBundle(this::class.java.classLoader)!!) {
                entries = getCharSequenceArray(ENTRIES_KEY)
                entryValues = getCharSequenceArray(ENTRY_VALUES_KEY)
            }
        }

        constructor(superState: Parcelable?): super(superState)

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeBundle(Bundle().apply {
                putCharSequenceArray(ENTRIES_KEY, entries)
                putCharSequenceArray(ENTRY_VALUES_KEY, entryValues)
            })
        }

        companion object {
            private const val ENTRIES_KEY = "entries"
            private const val ENTRY_VALUES_KEY = "entry_values"
            private const val ICONS_KEY = "icons"
            @JvmField
            val CREATOR = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(source: Parcel): SavedState = SavedState(source)
                override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
            }
        }
    }

}