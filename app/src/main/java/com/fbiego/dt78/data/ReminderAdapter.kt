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

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import com.fbiego.dt78.R

class ReminderAdapter(reminderData: ArrayList<ReminderData>, private val listener: (ReminderData) -> Unit): RecyclerView.Adapter<ReminderAdapter.DataHolder>() {
    private val data = mutableListOf<ReminderData>()


    init {
        data.addAll(reminderData)
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): DataHolder {
        val inflater = LayoutInflater.from(p0.context)
        val view = inflater.inflate(R.layout.reminder_item, p0, false)
        return DataHolder(view, p0.context)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(p0: DataHolder, p1: Int) {
        p0.bind(data[p1], listener)
    }

    fun update(reminders: ArrayList<ReminderData>){
        this.data.clear()
        this.data.addAll(reminders)
        notifyDataSetChanged()
    }



    class DataHolder(itemView: View, context: Context) : RecyclerView.ViewHolder(itemView){
        private val mName: TextView = itemView.findViewById(R.id.reminderText)
        private val mTime: TextView = itemView.findViewById(R.id.reminderTime)
        private val mIcon: ImageView = itemView.findViewById(R.id.reminderIcon)
        private val mState: SwitchCompat = itemView.findViewById(R.id.reminderEnable)
        private val cnt = context

        @SuppressLint("ResourceType")
        fun bind (reminder: ReminderData, listener: (ReminderData) -> Unit){

            mName.text = reminder.text
            val time = String.format("%02d:%02d", reminder.hour, reminder.minute)
            mTime.text = time
            mIcon.setImageResource(appIcon(reminder.icon))
            mState.isChecked = reminder.state

            itemView.setOnClickListener {
                listener(reminder)
            }

        }

    }

}