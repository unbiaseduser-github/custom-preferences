@file:JvmName("CustomPreferences")
@file:Suppress("unused")

package com.sixtyninefourtwenty.custompreferences

import android.content.SharedPreferences
import androidx.preference.PreferenceDataStore
import java.time.LocalTime

/**
 * Like [getLocalTimeOrNull], but returns [defValue] instead of `null`.
 */
fun SharedPreferences.getLocalTime(key: String, defValue: LocalTime): LocalTime =
    getLocalTimeOrNull(key) ?: defValue

/**
 * Retrieve a [LocalTime] that has been persisted by a [TimePickerPreference], or `null` if the preference value doesn't exist.
 */
fun SharedPreferences.getLocalTimeOrNull(key: String): LocalTime? =
    getString(key, null)?.let { LocalTime.parse(it, TimePickerPreference.timeFormatPattern) }

fun SharedPreferences.Editor.putLocalTime(key: String, value: LocalTime?): SharedPreferences.Editor =
    putString(key, value?.let { TimePickerPreference.timeFormatPattern.format(it) })

fun PreferenceDataStore.getLocalTime(key: String, defValue: LocalTime): LocalTime =
    getLocalTimeOrNull(key) ?: defValue

fun PreferenceDataStore.getLocalTimeOrNull(key: String): LocalTime? =
    getString(key, null)?.let { LocalTime.parse(it, TimePickerPreference.timeFormatPattern) }

fun PreferenceDataStore.putLocalTime(key: String, value: LocalTime?) =
    putString(key, value?.let { TimePickerPreference.timeFormatPattern.format(it) })
