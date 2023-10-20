@file:JvmName("CustomPreferences")
@file:Suppress("unused")

package com.sixtyninefourtwenty.custompreferences

import android.content.SharedPreferences
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
