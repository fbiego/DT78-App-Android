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