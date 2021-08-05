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
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.fbiego.dt78.R
import java.util.*
import kotlin.collections.ArrayList

class BatteryData(
    var time: Long,
    var level: Int,
    var type: Int
){
    fun year(): Int{
        val cal = Calendar.getInstance(Locale.getDefault())
        cal.timeInMillis = time
        return cal.get(Calendar.YEAR)
    }

    fun week(): Int{
        val cal = Calendar.getInstance(Locale.getDefault())
        cal.timeInMillis = time
        return cal.get(Calendar.WEEK_OF_YEAR)
    }

    fun hour(): Int{
        val cal = Calendar.getInstance(Locale.getDefault())
        cal.timeInMillis = time
        return cal.get(Calendar.HOUR_OF_DAY)
    }
    fun weekDay(): Int{
        val cal = Calendar.getInstance(Locale.getDefault())
        cal.timeInMillis = time
        return cal.get(Calendar.DAY_OF_WEEK)
    }

    fun timeString(): String{
        val cal = Calendar.getInstance(Locale.getDefault())
        cal.timeInMillis = time
        return String.format("%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
    }
}


class BatteryAdapter(private val data: ArrayList<BatteryData>)
    : RecyclerView.Adapter<BatteryAdapter.DataHolder>() {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): DataHolder {
        val inflater = LayoutInflater.from(p0.context)
        val view = inflater.inflate(R.layout.battery_item, p0, false)
        return DataHolder(view, p0.context)
    }

    override fun getItemCount(): Int = data.size

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onBindViewHolder(p0: DataHolder, p1: Int) {
        p0.bind(data[p1])
    }

    class DataHolder(itemView: View, context: Context) : RecyclerView.ViewHolder(itemView){
        private val mTime: TextView = itemView.findViewById(R.id.time)
        private val mDay: TextView = itemView.findViewById(R.id.day)
        private val mLevel: TextView = itemView.findViewById(R.id.batteryLevel)
        private val mState: TextView = itemView.findViewById(R.id.batteryState)
        private val card: CardView = itemView.findViewById(R.id.cardView)
        private val cnt = context

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        fun bind (battery: BatteryData){
            mDay.text = dayOfWeek(battery.weekDay(), cnt)
            mTime.text = battery.timeString()
            mLevel.text = "${battery.level}%"
            mState.text = batStatus(battery.type)
            card.backgroundTintList = ColorStateList.valueOf(cnt.getColorFromAttr(R.attr.colorCardBackgroundDark))
        }

    }
}