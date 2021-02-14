package com.fbiego.dt78.data

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.fbiego.dt78.R

class ThemeAdapter(private val context: Context, private val mode: Boolean): BaseAdapter() {

    var inflater: LayoutInflater = LayoutInflater.from(context)
    private val item = arrayListOf("Dark", "Light", "Battery Saver", "System Default")
    private val colors = arrayListOf("Green", "Red", "Purple", "Blue")
    private val colId = arrayListOf(R.color.colorAccentGreen, R.color.colorAccentRed,
        R.color.colorAccentPurple, R.color.colorAccentBlue)
    private val themId = arrayListOf(Color.DKGRAY, Color.LTGRAY, Color.GRAY, Color.TRANSPARENT)

    override fun getView(i: Int, view1: View?, viewGroup: ViewGroup): View {

        val view = inflater.inflate(R.layout.spinner_item_2, null)
        val card = view.findViewById<CardView>(R.id.cardView)
        val text = view.findViewById<TextView>(R.id.text)

        if (mode) {
            card.backgroundTintList = ColorStateList.valueOf(themId[i])
            text.text = item[i]
        } else {
            //icon.setColorFilter(ContextCompat.getColor(context, colId[i]))
            card.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, colId[i]))
            text.text = colors[i]
        }

        return view

    }

    override fun getItem(p0: Int): Any {
        return if (mode){
            item[p0]
        } else {
            colors[p0]
        }
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getCount(): Int {
        return if (mode){
            item.size
        } else {
            colors.size
        }
    }
}