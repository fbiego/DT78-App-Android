/*
 *
 * MIT License
 *
 * Copyright (c) 2021 Felix Biego
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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