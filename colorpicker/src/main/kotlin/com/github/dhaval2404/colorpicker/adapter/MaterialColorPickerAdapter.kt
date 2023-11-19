package com.github.dhaval2404.colorpicker.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.github.dhaval2404.colorpicker.databinding.AdapterMaterialColorPickerBinding
import com.github.dhaval2404.colorpicker.model.ColorShape
import com.github.dhaval2404.colorpicker.util.ColorUtil

/**
 * Material Color Listing
 *
 * @author Dhaval Patel
 * @version 1.0
 * @since 23 Dec 2019
 */
class MaterialColorPickerAdapter(private val colors: List<String>) :
    RecyclerView.Adapter<MaterialColorPickerAdapter.MaterialColorViewHolder>() {

    private var isDarkColor = false
    private var color = ""
    private var colorShape = ColorShape.CIRCLE
    private var isTickColorPerCard = false

    init {
        val darkColors = colors.count { ColorUtil.isDarkColor(it) }
        isDarkColor = (darkColors * 2) >= colors.size
    }

    fun setColorShape(colorShape: ColorShape) {
        this.colorShape = colorShape
    }

    fun setDefaultColor(color: String) {
        this.color = color
    }

    fun setTickColorPerCard(tickColorPerCard: Boolean) {
        this.isTickColorPerCard = tickColorPerCard
    }

    fun getSelectedColor() = color

    fun getItem(position: Int) = colors[position]

    override fun getItemCount() = colors.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MaterialColorViewHolder {
        val binding = AdapterMaterialColorPickerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MaterialColorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MaterialColorViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class MaterialColorViewHolder(binding: AdapterMaterialColorPickerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val colorView = binding.colorView
        private val checkIcon = binding.checkIcon

        init {
            itemView.setOnClickListener {
                val newIndex = it.tag as Int
                val color = getItem(newIndex)

                val oldIndex = colors.indexOf(this@MaterialColorPickerAdapter.color)
                this@MaterialColorPickerAdapter.color = color

                notifyItemChanged(oldIndex)
                notifyItemChanged(newIndex)
            }
        }

        fun bind(position: Int) {
            val color = getItem(position)

            itemView.tag = position

            ColorViewBinding.setBackgroundColor(colorView, color)
            ColorViewBinding.setCardRadius(colorView, colorShape)

            val isChecked = color == this@MaterialColorPickerAdapter.color
            checkIcon.isVisible = isChecked

            var darkColor = isDarkColor
            if (isTickColorPerCard) {
                darkColor = ColorUtil.isDarkColor(color)
            }

            checkIcon.setColorFilter(if (darkColor) Color.WHITE else Color.BLACK)
        }
    }
}
