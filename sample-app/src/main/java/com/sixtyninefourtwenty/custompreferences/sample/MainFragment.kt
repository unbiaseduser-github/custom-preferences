package com.sixtyninefourtwenty.custompreferences.sample

import android.os.Bundle
import android.widget.Toast
import com.sixtyninefourtwenty.custompreferences.MultiSelectToggleGroupPreference
import com.sixtyninefourtwenty.custompreferences.PredefinedColorPickerPreference
import com.sixtyninefourtwenty.custompreferences.PreferenceFragmentCompatAccommodateCustomDialogPreferences
import com.sixtyninefourtwenty.custompreferences.SliderPreference
import com.sixtyninefourtwenty.custompreferences.TimePickerPreference
import com.sixtyninefourtwenty.custompreferences.ToggleGroupPreference
import com.sixtyninefourtwenty.custompreferences.installConfigurationChangePatch
import java.time.format.DateTimeFormatter

private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

class MainFragment : PreferenceFragmentCompatAccommodateCustomDialogPreferences() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        installConfigurationChangePatch()
        val context = requireContext()
        preferenceScreen = preferenceManager.createPreferenceScreen(context).apply {
            addPreference(PredefinedColorPickerPreference(context).apply {
                key = "pcpp"
                title = "Color picker preference"
                summaryProvider = PredefinedColorPickerPreference.getSimpleSummaryProvider()
                setOnPreferenceChange {
                    Toast.makeText(context, getString(R.string.value, "$it (#${Integer.toHexString(it)})"), Toast.LENGTH_SHORT).show()
                    true
                }
            })
            addPreference(TimePickerPreference(context).apply {
                key = "tpp"
                title = "Time picker preference"
                summaryProvider = TimePickerPreference.getSimpleSummaryProvider()
                setOnPreferenceChange {
                    Toast.makeText(context, getString(R.string.value, timeFormatter.format(it)), Toast.LENGTH_SHORT).show()
                    true
                }
            })
            addPreference(ToggleGroupPreference(context).apply {
                key = "tgp"
                title = "Toggle group preference"
                summaryProvider = ToggleGroupPreference.getSimpleSummaryProvider()
                setEntries(
                    entries = arrayOf("one", "two", "three", "four"),
                    entryValues = arrayOf("One", "Two", "Three", "Four")
                )
                setOnPreferenceChange {
                    Toast.makeText(context, context.getString(R.string.value, it), Toast.LENGTH_SHORT).show()
                    if (it == "Two") {
                        return@setOnPreferenceChange false
                    }
                    true
                }
            })
            addPreference(MultiSelectToggleGroupPreference(context).apply {
                key = "mstgp"
                title = "Multi-select toggle group preference"
                setEntries(
                    entries = arrayOf("one1", "two2", "three3", "four4"),
                    entryValues = arrayOf("One", "Two", "Three", "Four")
                )
                setOnPreferenceChange {
                    Toast.makeText(context, context.getString(R.string.value, it), Toast.LENGTH_SHORT).show()
                    if ("Two" in it) {
                        return@setOnPreferenceChange false
                    }
                    true
                }
            })
            addPreference(MultiSelectToggleGroupPreference(context).apply {
                key = "mstgp2"
                title = "Multi-select toggle group preference"
                setEntries(
                    entries = arrayOf("one1", "two2", "three3"),
                    entryValues = arrayOf("One", "Two", "Three")
                )
                setOnPreferenceChange {
                    Toast.makeText(context, context.getString(R.string.value, it), Toast.LENGTH_SHORT).show()
                    if ("Two" in it) {
                        return@setOnPreferenceChange false
                    }
                    true
                }
            })
            addPreference(SliderPreference(context).apply {
                key = "sp"
                title = "Slider preference"
                summary = "foo"
                setProperties(
                    valueFrom = 0F,
                    valueTo = 10F,
                    stepSize = 1F
                )
                setOnPreferenceChange {
                    Toast.makeText(context, getString(R.string.value, it.toString()), Toast.LENGTH_SHORT).show()
                    if (it > 6) {
                        return@setOnPreferenceChange false
                    }
                    true
                }
            })
        }
    }

}