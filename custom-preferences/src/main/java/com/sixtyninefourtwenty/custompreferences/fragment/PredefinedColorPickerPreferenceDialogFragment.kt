package com.sixtyninefourtwenty.custompreferences.fragment

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.preference.DialogPreference
import com.github.dhaval2404.colorpicker.MaterialColorPickerDialog
import com.github.dhaval2404.colorpicker.MaterialColorPickerDialogFragment
import com.sixtyninefourtwenty.custompreferences.PredefinedColorPickerPreference

open class PredefinedColorPickerPreferenceDialogFragment : MaterialColorPickerDialogFragment {

    constructor() : super()
    constructor(dialog: MaterialColorPickerDialog) : super(dialog)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            (this as AlertDialog).setButton(DialogInterface.BUTTON_POSITIVE, null, null as DialogInterface.OnClickListener?)
        }
    }

    // Have item clicks dismiss the dialog without the user having to press "Ok".
    @Suppress("DEPRECATION")
    override fun onColorSelected(color: Int, colorHex: String) {
        (targetFragment as DialogPreference.TargetFragment).findPreference<PredefinedColorPickerPreference>(
            requireArguments().getString("key")!!
        )?.handleNewlyPickedValue(color)
        dismiss()
    }

}