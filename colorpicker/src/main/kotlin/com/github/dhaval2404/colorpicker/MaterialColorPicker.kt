package com.github.dhaval2404.colorpicker

import android.content.Context
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.github.dhaval2404.colorpicker.listener.ColorListener
import com.github.dhaval2404.colorpicker.listener.DismissListener
import com.github.dhaval2404.colorpicker.model.ColorShape
import com.github.dhaval2404.colorpicker.model.ColorSwatch
import com.github.dhaval2404.colorpicker.util.ColorUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * Color Picker from Predefined color set in AlertDialog
 *
 * @author Dhaval Patel
 * @version 1.0
 * @since 23 Dec 2019
 */
class MaterialColorPicker private constructor(
    val context: Context,
    val title: String,
    val positiveButton: String,
    val negativeButton: String,
    val colorListener: ColorListener?,
    val dismissListener: DismissListener?,
    val defaultColor: String?,
    val colorSwatch: ColorSwatch,
    var colorShape: ColorShape,
    val colors: List<String>? = null,
    var isTickColorPerCard: Boolean = false
) {

    class Builder(val context: Context) {

        private var title: String = context.getString(R.string.material_dialog_title)
        private var positiveButton: String = context.getString(R.string.material_dialog_positive_button)
        private var negativeButton: String = context.getString(R.string.material_dialog_negative_button)
        private var colorListener: ColorListener? = null
        private var dismissListener: DismissListener? = null
        private var defaultColor: String? = null
        private var colorSwatch: ColorSwatch = ColorSwatch._300
        private var colorShape: ColorShape = ColorShape.CIRCLE
        private var colors: List<String>? = null
        private var isTickColorPerCard: Boolean = false

        /**
         * Set Dialog Title
         *
         * @param title String
         */
        fun setTitle(title: String): Builder {
            this.title = title
            return this
        }

        /**
         * Set Dialog Title
         *
         * @param title StringRes
         */
        fun setTitle(@StringRes title: Int): Builder {
            this.title = context.getString(title)
            return this
        }

        /**
         * Set Positive Button Text
         *
         * @param text String
         */
        fun setPositiveButton(text: String): Builder {
            this.positiveButton = text
            return this
        }

        /**
         * Set Positive Button Text
         *
         * @param text StringRes
         */
        fun setPositiveButton(@StringRes text: Int): Builder {
            this.positiveButton = context.getString(text)
            return this
        }

        /**
         * Set Negative Button Text
         *
         * @param text String
         */
        fun setNegativeButton(text: String): Builder {
            this.negativeButton = text
            return this
        }

        /**
         * Set Negative Button Text
         *
         * @param text StringRes
         */
        fun setNegativeButton(@StringRes text: Int): Builder {
            this.negativeButton = context.getString(text)
            return this
        }

        /**
         * Set Default Selected Color
         *
         * @param color String Hex Color
         */
        fun setDefaultColor(color: String): Builder {
            this.defaultColor = color
            return this
        }

        /**
         * Set Default Selected Color
         *
         * @param color Int ColorRes
         */
        fun setDefaultColor(@ColorRes color: Int): Builder {
            this.defaultColor = ColorUtil.formatColor(color)
            return this
        }

        /**
         * Set Color CardView Shape,
         *
         * @param colorShape ColorShape
         */
        fun setColorShape(colorShape: ColorShape): Builder {
            this.colorShape = colorShape
            return this
        }

        /**
         * Set Color Swatch
         *
         * @param colorSwatch ColorSwatch
         */
        fun setColorSwatch(colorSwatch: ColorSwatch): Builder {
            this.colorSwatch = colorSwatch
            return this
        }

        /**
         * Set Color Listener
         *
         * @param listener ColorListener
         */
        fun setColorListener(listener: ColorListener): Builder {
            this.colorListener = listener
            return this
        }

        /**
         * Sets the callback that will be called when the dialog is dismissed for any reason.
         *
         * @param listener DismissListener
         */
        fun setDismissListener(listener: DismissListener?): Builder {
            this.dismissListener = listener
            return this
        }

        /**
         * Provide PreDefined Colors,
         *
         * If colors is not empty, User can choose colors from provided list
         * If colors is empty, User can choose colors based on ColorSwatch
         *
         * @param colors List<String> List of Hex Colors
         */
        fun setColors(colors: List<String>): Builder {
            this.colors = colors
            return this
        }

        fun setColors(colors: Array<String>): Builder = setColors(colors.toList())

        /**
         * Provide PreDefined Colors,
         *
         * If colors is not empty, User can choose colors from provided list
         * If colors is empty, User can choose colors based on ColorSwatch
         *
         * @param colors List<Int> List of Color Resource
         */
        fun setColorRes(colors: List<Int>): Builder {
            this.colors = colors.map { ColorUtil.formatColor(it) }
            return this
        }

        fun setColorRes(colors: IntArray): Builder {
            this.colors = colors.map { ColorUtil.formatColor(it) }
            return this
        }

        /**
         * Set tick icon color, Default will be false
         *
         * If false,
         *     First the majority of color(dark/light) will be calculated
         *     If dark color count > light color count
         *          tick color will be WHITE
         *     else
         *          tick color will be BLACK
         *     Here, Tick color will be same card,
         *     Which might create issue with black and white color in list
         *
         * If true,
         *      based on the each color(dark/light) the card tick color will be decided
         *      Here, Tick color will be different for each card
         *
         * @param tickColorPerCard Boolean
         */
        fun setTickColorPerCard(tickColorPerCard: Boolean): Builder {
            this.isTickColorPerCard = tickColorPerCard
            return this
        }

        /**
         * Creates an {@link MaterialColorPickerDialog} with the arguments supplied to this
         * builder.
         * <p>
         * Calling this method does not display the dialog. If no additional
         * processing is needed, {@link #show()} may be called instead to both
         * create and display the dialog.
         */
        fun build(): MaterialColorPicker {
            return MaterialColorPicker(
                context = context,
                title = title,
                positiveButton = positiveButton,
                negativeButton = negativeButton,
                colorListener = colorListener,
                dismissListener = dismissListener,
                defaultColor = defaultColor,
                colorShape = colorShape,
                colorSwatch = colorSwatch,
                colors = colors,
                isTickColorPerCard = isTickColorPerCard
            )
        }

        /**
         * Show Alert Dialog
         */
        fun show(fragmentManager: FragmentManager) {
            build().show(fragmentManager)
        }

        /**
         * Show BottomSheet Dialog
         */
        fun showBottomSheet(fragmentManager: FragmentManager) {
            build().showBottomSheet(fragmentManager)
        }
    }

    fun createDialog(): DialogFragment = MaterialColorPickerDialog.newInstance(this)

    fun createBottomSheetDialog(): BottomSheetDialogFragment = MaterialColorPickerBottomSheet.newInstance(this)

    /**
     * Show BottomSheet Dialog
     */
    fun showBottomSheet(fragmentManager: FragmentManager) {
        createBottomSheetDialog().show(fragmentManager, "")
    }

    /**
     * Show AlertDialog
     */
    fun show(fragmentManager: FragmentManager) {
        createDialog().show(fragmentManager, "")
    }
}
