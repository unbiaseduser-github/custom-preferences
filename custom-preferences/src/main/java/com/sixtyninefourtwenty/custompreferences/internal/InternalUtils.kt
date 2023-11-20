package com.sixtyninefourtwenty.custompreferences.internal

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import androidx.preference.Preference

@SuppressLint("PrivateResource")
internal fun Resources.getAndroidXNotSetString() =
    getString(androidx.preference.R.string.not_set)

internal fun Context.getAndroidXNotSetString() = resources.getAndroidXNotSetString()

internal fun Preference.getAndroidXNotSetString() = context.getAndroidXNotSetString()
