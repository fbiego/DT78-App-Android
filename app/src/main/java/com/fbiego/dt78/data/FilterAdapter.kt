package com.fbiego.dt78.data

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.fbiego.dt78.R

class FilterAdapter(private val activity: Activity, private val name: ArrayList<String>)
    : ArrayAdapter<String>(activity, R.layout.text_item, name) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = activity.layoutInflater
        val rowView = inflater.inflate(R.layout.text_item, null, true)
        val nameR = rowView.findViewById<TextView>(R.id.textFilter)

        nameR.text = name[position]

        return rowView
    }
}