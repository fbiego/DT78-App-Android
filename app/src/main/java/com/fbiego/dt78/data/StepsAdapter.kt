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
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.fbiego.dt78.R
import com.fbiego.dt78.app.ForegroundService as FG

class StepsAdapter(private val data: ArrayList<StepsData>, private val step: Int, private val view: Int)
    : RecyclerView.Adapter<StepsAdapter.DataHolder>() {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): DataHolder {
        val inflater = LayoutInflater.from(p0.context)
        val view = inflater.inflate(R.layout.step_item, p0, false)
        return DataHolder(view, p0.context, step)
    }

    override fun getItemCount(): Int = data.size

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onBindViewHolder(p0: DataHolder, p1: Int) {
        p0.bind(data[p1], view)
    }

    class DataHolder(itemView: View, context: Context, stepz: Int) : RecyclerView.ViewHolder(itemView){
        private val mDistance: TextView = itemView.findViewById(R.id.distance)
        private val mTime: TextView = itemView.findViewById(R.id.time)
        private val mStep: TextView = itemView.findViewById(R.id.steps)
        private val mCalorie: TextView = itemView.findViewById(R.id.calories)
        private val card: CardView = itemView.findViewById(R.id.cardView)
        private val cnt = context
        private val stepsize = stepz

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        fun bind (step: StepsData, view: Int){
            val metres = step.steps * stepsize
            val dis = distance(metres, FG.unit!=0, cnt)

            val time = when (view) {
                DAY -> String.format("%02d:00 - %02d:00", step.hour, step.hour+1)
                WEEK -> dayOfWeek(step.day, cnt)
                MONTH -> String.format("%02d-%02d-20%02d", step.day, step.month, step.year)
                else -> String.format("%02d:00 - %02d:00", step.hour, step.hour+1)
            }
            mDistance.text = dis
            mTime.text = time
            mStep.text = "${step.steps} "+cnt.resources.getString(R.string.steps)
            mCalorie.text = "${step.calories} "+cnt.resources.getString(R.string.kcal)

            if (step.steps > 0){
                //card.setCardBackgroundColor(R.attr.colorCardBackgroundDark)
                card.backgroundTintList = ColorStateList.valueOf(cnt.getColorFromAttr(R.attr.colorCardBackgroundDark))
            } else {
                card.backgroundTintList = ColorStateList.valueOf(cnt.getColorFromAttr(R.attr.colorCardBackgroundLight))
            }


        }

    }
}