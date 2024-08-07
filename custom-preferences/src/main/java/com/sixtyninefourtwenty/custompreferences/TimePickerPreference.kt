package com.sixtyninefourtwenty.custompreferences

import android.content.Context
import android.content.SharedPreferences
import android.content.res.TypedArray
import android.os.Parcel
import android.os.Parcelable
import android.text.format.DateFormat
import android.util.AttributeSet
import androidx.core.os.ParcelCompat
import androidx.fragment.app.DialogFragment
import androidx.preference.DialogPreference
import androidx.preference.Preference.SummaryProvider
import androidx.preference.PreferenceDataStore
import com.google.android.material.timepicker.MaterialTimePicker
import com.sixtyninefourtwenty.custompreferences.internal.getAndroidXNotSetString
import com.sixtyninefourtwenty.custompreferences.internal.throwValueNotSetException
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date

/**
 * A [DialogPreference] that shows a [MaterialTimePicker]. This preference saves a String value,
 * but its public API gives access to a [LocalTime] that's parsed from the underlying String.
 * To access the parsed time from [SharedPreferences] or [PreferenceDataStore], use
 * [SharedPreferences.getLocalTime] or [PreferenceDataStore.getLocalTime]. You can also set time
 * with [SharedPreferences.Editor.putLocalTime] or [PreferenceDataStore.putLocalTime].
 *
 * Default value: A String representing a time in format `HH:mm`. For more information see
 * [DateTimeFormatter], section "Patterns for Formatting and Parsing".
 *
 * **Note**: Due to the nature of the picker,
 * [androidx.preference.R.styleable.DialogPreference_dialogMessage] and
 * [setDialogMessage] have no effect.
 */
@Suppress("unused")
open class TimePickerPreference : AbstractCustomDialogPreference, CanSetPreferenceChangeListener<LocalTime> {

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null) : super(context, attrs) {
        init(context.obtainStyledAttributes(attrs, R.styleable.TimePickerPreference))
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context.obtainStyledAttributes(attrs, R.styleable.TimePickerPreference, defStyleAttr, 0))
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context.obtainStyledAttributes(attrs, R.styleable.TimePickerPreference, defStyleAttr, defStyleRes))
    }

    override fun setOnPreferenceChange(block: ((newValue: LocalTime) -> Boolean)?) {
        setTypedPreferenceChangeListener(block)
    }

    private fun init(typedArray: TypedArray) {
        if (typedArray.getBoolean(R.styleable.TimePickerPreference_tpp_useSimpleSummaryProvider, false)) {
            summaryProvider = mySummaryProvider
        }
        typedArray.recycle()
    }

    var time: LocalTime? = null
        set(value) {
            field = value
            persistString(value?.let { timeToString(it) })
            notifyChanged()
        }

    /**
     * Return the [LocalTime] this preference has.
     * @throws IllegalStateException if the time has not been set, either from a default value or from user input
     * @see time
     */
    fun requireTime() = time ?: throwValueNotSetException()

    override fun createDialog(): DialogFragment {
        return MaterialTimePicker.Builder()
            .setTitleText(dialogTitle)
            .setHour(time?.hour ?: 0)
            .setMinute(time?.minute ?: 0)
            .build()
            .apply {
                addOnPositiveButtonClickListener {
                    handleNewlyPickedValue(LocalTime.of(hour, minute))
                }
            }
    }

    internal fun handleNewlyPickedValue(newTime: LocalTime) {
        if (callChangeListener(newTime)) {
            time = newTime
        }
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any? {
        return a.getString(index)
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        val actualDefaultValue = defaultValue as String?
        val value: String? = getPersistedString(actualDefaultValue)
        time = value?.let { stringToTime(it) }
        persistString(value)
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        if (isPersistent) {
            return superState
        }

        return SavedState(superState).also {
            it.time = this.time
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }

        super.onRestoreInstanceState(state.superState)
        this.time = state.time
    }

    private class SavedState : BaseSavedState {

        constructor(source: Parcel) : super(source) {
            time = ParcelCompat.readSerializable(source, null, LocalTime::class.java)
        }

        constructor(superState: Parcelable?) : super(superState)

        var time: LocalTime? = null

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeSerializable(time)
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(source: Parcel): SavedState {
                return SavedState(source)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }

        }
    }

    companion object {
        @JvmStatic
        fun getSimpleSummaryProvider() = mySummaryProvider
        private val timeFormatPattern: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        /**
         * Converts a string that was created by [timeToString] to a [LocalTime].
         */
        @JvmStatic
        fun stringToTime(timeString: String): LocalTime = LocalTime.parse(timeString, timeFormatPattern)

        /**
         * Converts a [LocalTime] to a string that can later be parsed by [stringToTime].
         *
         * This pair of methods is used by [SharedPreferences.getLocalTime] and [SharedPreferences.Editor.putLocalTime]
         * and their [PreferenceDataStore] counterparts. If you need to work with this data without those methods,
         * this pair of methods can be used to do so.
         */
        @JvmStatic
        fun timeToString(time: LocalTime): String = timeFormatPattern.format(time)
        private val mySummaryProvider: SummaryProvider<TimePickerPreference> by lazy {
            SummaryProvider<TimePickerPreference> {
                val time = it.time
                return@SummaryProvider if (time != null) {
                    DateFormat.getTimeFormat(it.context)
                        .format(Date.from(
                            time.atDate(LocalDate.now())
                                .atZone(ZoneId.systemDefault())
                                .toInstant()
                        ))
                } else {
                    it.getAndroidXNotSetString()
                }
            }
        }
    }

}