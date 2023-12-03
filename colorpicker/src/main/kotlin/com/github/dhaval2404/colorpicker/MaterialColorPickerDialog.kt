package com.github.dhaval2404.colorpicker

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.os.BundleCompat
import androidx.fragment.app.DialogFragment
import com.github.dhaval2404.colorpicker.adapter.MaterialColorPickerAdapter
import com.github.dhaval2404.colorpicker.databinding.DialogMaterialColorPickerBinding
import com.github.dhaval2404.colorpicker.listener.ColorListener
import com.github.dhaval2404.colorpicker.listener.DismissListener
import com.github.dhaval2404.colorpicker.model.ColorShape
import com.github.dhaval2404.colorpicker.model.ColorSwatch
import com.github.dhaval2404.colorpicker.util.ColorUtil
import com.github.dhaval2404.colorpicker.util.setButtonTextColor
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MaterialColorPickerDialog : DialogFragment() {

    private var title: String? = null
    private var positiveButton: String? = null
    private var negativeButton: String? = null
    private var colorListener: ColorListener? = null
    private var dismissListener: DismissListener? = null
    private var defaultColor: String? = null
    private var colorShape: ColorShape = ColorShape.CIRCLE
    private var colorSwatch: ColorSwatch = ColorSwatch._300
    private var colors: List<String>? = null
    private var isTickColorPerCard: Boolean = false
    private lateinit var binding: DialogMaterialColorPickerBinding
    private lateinit var adapter: MaterialColorPickerAdapter
    private var positiveButtonClicked: Boolean = false

    companion object {

        private const val EXTRA_TITLE = "extra.title"
        private const val EXTRA_POSITIVE_BUTTON = "extra.positive_Button"
        private const val EXTRA_NEGATIVE_BUTTON = "extra.negative_button"

        private const val EXTRA_DEFAULT_COLOR = "extra.default_color"
        private const val EXTRA_COLOR_SHAPE = "extra.color_shape"
        private const val EXTRA_COLOR_SWATCH = "extra.color_swatch"
        private const val EXTRA_COLORS = "extra.colors"
        private const val EXTRA_IS_TICK_COLOR_PER_CARD = "extra.is_tick_color_per_card"

        fun newInstance(dialog: MaterialColorPicker): MaterialColorPickerDialog {
            val bundle = Bundle().apply {
                putString(EXTRA_TITLE, dialog.title)
                putString(EXTRA_POSITIVE_BUTTON, dialog.positiveButton)
                putString(EXTRA_NEGATIVE_BUTTON, dialog.negativeButton)

                putString(EXTRA_DEFAULT_COLOR, dialog.defaultColor)
                putParcelable(EXTRA_COLOR_SWATCH, dialog.colorSwatch)
                putParcelable(EXTRA_COLOR_SHAPE, dialog.colorShape)
                putBoolean(EXTRA_IS_TICK_COLOR_PER_CARD, dialog.isTickColorPerCard)

                var list: ArrayList<String>? = null
                if (dialog.colors != null) {
                    list = ArrayList(dialog.colors)
                }
                putStringArrayList(EXTRA_COLORS, list)
            }

            return MaterialColorPickerDialog().apply {
                this.colorListener = dialog.colorListener
                this.dismissListener = dialog.dismissListener
                arguments = bundle
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        arguments?.let {
            title = it.getString(EXTRA_TITLE)
            positiveButton = it.getString(EXTRA_POSITIVE_BUTTON)
            negativeButton = it.getString(EXTRA_NEGATIVE_BUTTON)

            defaultColor = it.getString(EXTRA_DEFAULT_COLOR)
            colorSwatch = BundleCompat.getParcelable(it, EXTRA_COLOR_SWATCH, ColorSwatch::class.java)!!
            colorShape = BundleCompat.getParcelable(it, EXTRA_COLOR_SHAPE, ColorShape::class.java)!!

            colors = it.getStringArrayList(EXTRA_COLORS)
            isTickColorPerCard = it.getBoolean(EXTRA_IS_TICK_COLOR_PER_CARD)
        }
        val context = requireContext()
        // Create Dialog Instance
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setNegativeButton(negativeButton, null)

        // Create Custom View for Dialog
        binding = DialogMaterialColorPickerBinding.inflate(LayoutInflater.from(context))
        dialog.setView(binding.root)

        // Setup Color Listing Adapter
        val colorList = colors ?: ColorUtil.getColors(context, colorSwatch.value)
        adapter = MaterialColorPickerAdapter(colorList)
        adapter.setColorShape(colorShape)
        adapter.setTickColorPerCard(isTickColorPerCard)
        if (!defaultColor.isNullOrBlank()) {
            adapter.setDefaultColor(defaultColor!!)
        }

        // Setup Color RecyclerView
        val materialColorRV = binding.materialColorRV
        materialColorRV.setHasFixedSize(true)
        materialColorRV.layoutManager = FlexboxLayoutManager(context)
        materialColorRV.adapter = adapter

        // Set Submit Click Listener
        dialog.setPositiveButton(positiveButton) { _, _ ->
            positiveButtonClicked = true
        }

        return dialog.create().apply { setButtonTextColor() }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dismissListener?.onDismiss()
        if (positiveButtonClicked) {
            val color = adapter.getSelectedColor()
            if (color.isNotBlank()) {
                colorListener?.onColorSelected(ColorUtil.parseColor(color), color)
            }
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        dismissListener?.onDismiss()
    }

}