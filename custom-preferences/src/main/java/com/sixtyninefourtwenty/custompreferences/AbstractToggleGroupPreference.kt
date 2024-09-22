package com.sixtyninefourtwenty.custompreferences

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.CallSuper
import androidx.core.content.res.TypedArrayUtils
import androidx.core.view.get
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
 * [R.style.Preference_AbstractToggleGroup].
 *
 * **Note:** If this preference is not persistent, it cannot restore its icon array via saved state.
 *
 * Implementation notes:
 * - This class only sets up the buttons for you to match the number of choices, you have to do the
 * rest (set listeners, check the appropriate button, etc.) on [bind].
 * - Subclasses' saved state classes must extend [SavedState].
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
abstract class AbstractToggleGroupPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @SuppressLint("RestrictedApi")
    defStyleAttr: Int = TypedArrayUtils.getAttr(context, R.attr.abstractToggleGroupPreferenceStyle, 0),
    defStyleRes: Int = R.style.Preference_AbstractToggleGroup
) : PreferenceScrollableCustomViewUnderneath(context, attrs, defStyleAttr, defStyleRes) {

    /**
     * Whether the preference can only have one value. Will be passed to
     * [MaterialButtonToggleGroup.setSingleSelection] on [onBindViewHolder].
     */
    protected abstract val isPreferenceSingleSelection: Boolean

    var entries: List<CharSequence> = listOf()
        private set
    var entryValues: List<CharSequence> = listOf()
        private set
    var icons: List<Drawable?>? = null
        private set
    @Deprecated(message = "Use list property.", replaceWith = ReplaceWith(expression = "entries"))
    fun copyOfEntries() = entries.toTypedArray()
    @Deprecated(message = "Use list property.", replaceWith = ReplaceWith(expression = "entryValues"))
    fun copyOfEntryValues() = entryValues.toTypedArray()
    @Deprecated(message = "Use list property.", replaceWith = ReplaceWith(expression = "icons"))
    fun copyOfIcons() = icons?.toTypedArray()

    private fun checkEntries(
        entries: List<CharSequence>,
        entryValues: List<CharSequence>,
        icons: List<Drawable?>?
    ) {
        require(entries.size == entryValues.size) {
            "entries and entryValues must have the same number of items"
        }
        if (icons != null) {
            require(entries.size == icons.size) {
                "entries, entryValues and icons must have the same number of items"
            }
        }
    }

    @JvmOverloads
    fun setEntries(
        entries: List<CharSequence>,
        entryValues: List<CharSequence>,
        icons: List<Drawable?>? = null
    ) {
        checkEntries(entries, entryValues, icons)
        this.entries = entries
        this.entryValues = entryValues
        this.icons = icons
        notifyChanged()
    }

    @Deprecated(message = "Use the version that takes lists.", replaceWith =
        ReplaceWith("setEntries(entries?.toList().orEmpty(), entryValues?.toList().orEmpty(), icons?.toList())")
    )
    @JvmOverloads
    fun setEntries(
        entries: Array<CharSequence>?,
        entryValues: Array<CharSequence>?,
        icons: Array<Drawable?>? = null
    ) = setEntries(entries?.toList().orEmpty(), entryValues?.toList().orEmpty(), icons?.toList())

    private fun setupButtonsOnToggleGroup(
        entries: List<CharSequence>,
        entryValues: List<CharSequence>,
        icons: List<Drawable?>?,
        toggleGroup: MaterialButtonToggleGroup
    ) {
        if (entries.isEmpty()) {
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

    private fun indexOfFocusedButton(toggleGroup: MaterialButtonToggleGroup): Int {
        for (i in 0 ..< toggleGroup.childCount) {
            val button = toggleGroup[i]
            if (button.hasFocus()) {
                return i
            }
        }

        return -1
    }

    protected abstract fun handleButtonOnKeyInput(
        toggleGroup: MaterialButtonToggleGroup,
        buttonIndex: Int,
        buttonId: Int
    )

    final override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val toggleGroup = holder.findViewById(R.id.toggle_group) as MaterialButtonToggleGroup
        holder.itemView.setOnKeyListener { _, keyCode, event ->
            if (event.action != KeyEvent.ACTION_DOWN || toggleGroup.childCount == 0) {
                return@setOnKeyListener false
            }

            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    val index = indexOfFocusedButton(toggleGroup)
                    if (index < 0) {
                        toggleGroup[toggleGroup.childCount - 1].requestFocus()
                        return@setOnKeyListener true // Prevent system from stepping to the next button.
                    }
                }
                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    val index = indexOfFocusedButton(toggleGroup)
                    if (index < 0) {
                        toggleGroup[0].requestFocus()
                        return@setOnKeyListener true
                    }
                }
                KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                    val index = indexOfFocusedButton(toggleGroup)
                    if (index >= 0) {
                        handleButtonOnKeyInput(toggleGroup, index, toggleGroup[index].id)
                        return@setOnKeyListener true
                    }
                }
            }

            false
        }
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
            it.entries = this.entries.toTypedArray()
            it.entryValues = this.entryValues.toTypedArray()
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
        entries = state.entries.toList()
        entryValues = state.entryValues.toList()
    }

    protected open class SavedState : BaseSavedState {

        @JvmField
        var entries: Array<CharSequence> = arrayOf()
        @JvmField
        var entryValues: Array<CharSequence> = arrayOf()

        constructor(source: Parcel): super(source) {
            with(source.readBundle(this::class.java.classLoader)!!) {
                entries = getCharSequenceArray(ENTRIES_KEY)!!
                entryValues = getCharSequenceArray(ENTRY_VALUES_KEY)!!
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