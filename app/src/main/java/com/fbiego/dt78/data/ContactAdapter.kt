package com.fbiego.dt78.data

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.fbiego.dt78.R

class ContactAdapter (private val activity: Activity, private val contacts: ArrayList<ContactData>, private val sos: Int)
    : ArrayAdapter<ContactData>(activity, R.layout.bt_item, contacts) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val inflater = activity.layoutInflater
            val rowView = inflater.inflate(R.layout.bt_item, null, true)
            val nameR = rowView.findViewById<TextView>(R.id.btName)
            val addressR = rowView.findViewById<TextView>(R.id.btAddress)
            val iconR = rowView.findViewById<ImageView>(R.id.btIcon)

            nameR.text = contacts[position].name
            addressR.text = contacts[position].number
            if (sos == position){
                iconR.setImageResource(R.drawable.ic_sos)
            } else {
                iconR.setImageResource(R.drawable.ic_person)
            }


            return rowView
        }

    fun updateSOS(){

    }


    }