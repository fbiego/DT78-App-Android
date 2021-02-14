package com.fbiego.dt78.data

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import com.fbiego.dt78.R

class UserListAdapter(private val activity: Activity, private val icon: ArrayList<Int>, private val name: ArrayList<String>, private val value: ArrayList<String>?, private val state:ArrayList<Boolean?>?)
    : ArrayAdapter<String>(activity, R.layout.user_item, name){

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = activity.layoutInflater
        val rowView = inflater.inflate(R.layout.user_item, null, true)
        val nameR = rowView.findViewById<TextView>(R.id.settingsName)
        val valueR = rowView.findViewById<TextView>(R.id.settingsValue)
        val switchR = rowView.findViewById<SwitchCompat>(R.id.settingsState)
        val iconR = rowView.findViewById<ImageView>(R.id.userIcon)

        nameR.text = name[position]
        iconR.setImageResource(icon[position])

        if (value != null){
            switchR.visibility = View.GONE
            valueR.visibility = View.VISIBLE
            valueR.text = value[position]
        }
        if (state != null){
            if (state[position] != null){
                switchR.visibility = View.VISIBLE
                valueR.visibility = View.GONE
                switchR.isChecked = state[position]!!
            } else {
                switchR.visibility = View.GONE
                valueR.visibility = View.GONE
            }

        }

        return rowView
    }
}