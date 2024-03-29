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
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import com.fbiego.dt78.R


class AlarmAdapter(alarmData: ArrayList<AlarmData>, private val listener: (AlarmData) -> Unit): RecyclerView.Adapter<AlarmAdapter.DataHolder>() {
    private val data = mutableListOf<AlarmData>()



    init {
        data.addAll(alarmData)
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): DataHolder {
        val inflater = LayoutInflater.from(p0.context)
        val view = inflater.inflate(R.layout.alarm_item, p0, false)
        return DataHolder(view, p0.context)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(p0: DataHolder, p1: Int) {
        p0.bind(data[p1], listener)
    }

    fun update(alarm: ArrayList<AlarmData>){
        this.data.clear()
        this.data.addAll(alarm)
        notifyDataSetChanged()
    }



    class DataHolder(itemView: View, context: Context) : RecyclerView.ViewHolder(itemView){
        private val mName: TextView = itemView.findViewById(R.id.alName)
        private val mTime: TextView = itemView.findViewById(R.id.alTime)
        private val mDays: TextView = itemView.findViewById(R.id.alRepeat)
        private val mSwitch: SwitchCompat = itemView.findViewById(R.id.alEnable)
        private val cnt = context

        fun bind (alarm: AlarmData, listener: (AlarmData) -> Unit){
            val name = cnt.resources.getString(R.string.alarm)+" ${alarm.id + 1}"

            mName.text = name
            val time = String.format("%02d:%02d", alarm.hour, alarm.minute)
            mTime.text = time
            mDays.text = alarmRepeat(alarm.repeat, cnt)
            mSwitch.isChecked = alarm.enable

            itemView.setOnClickListener {
                listener(alarm)
            }

        }

    }

}