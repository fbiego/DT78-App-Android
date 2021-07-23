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
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.fbiego.dt78.R

class ErrorAdapter(private val errorData: ArrayList<ErrorData>,private val context: Context): RecyclerView.Adapter<ErrorAdapter.DataHolder>() {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): DataHolder {
        val inflater = LayoutInflater.from(p0.context)
        val view = inflater.inflate(R.layout.error_item, p0, false)
        return DataHolder(view)
    }

    override fun getItemCount(): Int = errorData.size

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onBindViewHolder(p0: DataHolder, p1: Int) {
        p0.bind(errorData[p1], context)
    }



    class DataHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        private val mName: TextView = itemView.findViewById(R.id.errorName)
        private val mDate: TextView = itemView.findViewById(R.id.errorDate)
        private val mIcon: ImageView = itemView.findViewById(R.id.errorIcon)

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        fun bind (errorData: ErrorData, context: Context){

            mName.text = getErrorName(errorData.error)
            mDate.text = String.format("%02d:%02d:%02d", errorData.hour, errorData.minute, errorData.second)
            mIcon.setImageResource(getErrorIcon(errorData.error))
            mIcon.imageTintList = ColorStateList.valueOf(context.getColorFromAttr(R.attr.colorIcons))

        }

    }

}