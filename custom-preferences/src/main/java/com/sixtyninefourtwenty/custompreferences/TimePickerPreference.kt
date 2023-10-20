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
import androidx.preference.Preference.SummaryProvider
import com.google.android.material.timepicker.MaterialTimePicker
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Suppress("unused")
class TimePickerPreference : AbstractCustomDialogPreference {

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

    private fun init(typedArray: TypedArray) {
        if (typedArray.getBoolean(R.styleable.TimePickerPreference_tpp_useSimpleSummaryProvider, false)) {
            summaryProvider = mySummaryProvider
        }
        typedArray.recycle()
    }

    var time: LocalTime? = null
        set(value) {
            field = value
            persistString(value?.let(timeFormatPattern::format))
            notifyChanged()
        }

    override fun createDialog(): DialogFragment {
        return MaterialTimePicker.Builder()
            .setTitleText(dialogTitle)
            .setHour(time?.hour ?: 0)
            .setMinute(time?.minute ?: 0)
            .build()
            .apply {
                addOnPositiveButtonClickListener {
                    val newTime = LocalTime.of(hour, minute)
                    if (callChangeListener(newTime)) {
                        time = newTime
                    }
                }
            }
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any? {
        return a.getString(index)
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        val actualDefaultValue = defaultValue as String?
        val value: String? = getPersistedString(actualDefaultValue)
        time = value?.let { LocalTime.parse(it, timeFormatPattern) }
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
        fun getPersistedLocalTime(preferences: SharedPreferences, key: String): LocalTime? = preferences.getString(key, null)?.let { LocalTime.parse(it, timeFormatPattern) }
        private val timeFormatPattern: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        private val timeFormatPattern12h: DateTimeFormatter = DateTimeFormatter.ofPattern("KK:mm a")
        private val mySummaryProvider: SummaryProvider<TimePickerPreference> by lazy {
            SummaryProvider<TimePickerPreference> {
                val time = it.time
                return@SummaryProvider if (time != null) {
                    if (DateFormat.is24HourFormat(it.context)) {
                        timeFormatPattern.format(time)
                    } else {
                        timeFormatPattern12h.format(time)
                    }
                } else {
                    it.context.getString(androidx.preference.R.string.not_set)
                }
            }
        }
    }

}