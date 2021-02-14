package com.fbiego.dt78.data

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.fbiego.dt78.R

class NotifyAdapter(context: Context, private val showText: Boolean, private val icons: ArrayList<Int>): BaseAdapter() {

    var inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getView(i: Int, view1: View?, viewGroup: ViewGroup): View {

        val view = inflater.inflate(R.layout.spinner_item, null)
        val icon = view.findViewById<ImageView>(R.id.icon)
        val text = view.findViewById<TextView>(R.id.text)
        icon.setImageResource(appIcon(icons[i]))
        text.text = appName(icons[i])
        if (showText){
            text.visibility = View.VISIBLE
        } else {
            text.visibility = View.GONE
        }
        return view

    }

    override fun getItem(p0: Int): Any {
        return icons[p0]
    }

    override fun getItemId(p0: Int): Long {
        return icons[p0].toLong()
    }

    override fun getCount(): Int {
        return icons.size
    }
}