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