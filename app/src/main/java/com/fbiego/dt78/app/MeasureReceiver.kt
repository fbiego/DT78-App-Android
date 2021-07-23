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

package com.fbiego.dt78.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.fbiego.dt78.R
import com.fbiego.dt78.data.*
import java.util.*

class MeasureReceiver: BroadcastReceiver() {

    companion object {
        private lateinit var mListener:MeasureListener
        var isInit = false
        fun bindListener(listener:MeasureListener) {
            mListener = listener
            isInit = true
        }
        fun unBindListener(){
            isInit = false
        }
    }

    private fun startMeasure(id: Int){
        mListener.onMeasureStart(id)
    }

    private fun startReminder(icon: Int, text: String, id: Int){
        mListener.onReminder(icon, text, id)
    }
    override fun onReceive(p0: Context, p1: Intent) {
        val id = p1.getIntExtra("id", -1)
        val icon = p1.getIntExtra("icon", 0)
        val text = p1.getStringExtra("text")
        val dbHandler = MyDBHandler(p0, null, null, 1)
        val cal = Calendar.getInstance(Locale.getDefault())

        if (id >= 30){
            dbHandler.writeMeasure(cal, REMINDER_TRIGGERED, id)
            if (isInit) {
                startReminder(icon, text!!, id-30)
            } else {
                ForegroundService().notifyReminder(p0, 5800+id-30, p0.getString(R.string.reminder_name)+" ${id-30}", text!!)
            }
        } else {
            dbHandler.writeMeasure(cal, MEASURE_TRIGGERED, id)
            if (isInit){
                startMeasure(id)
            } else {
                dbHandler.writeMeasure(cal, M_SERVICE_STOPPED, id)
            }
        }

    }
}

interface MeasureListener{
    fun onMeasureStart(id: Int)
    fun onReminder(icon: Int, text: String, id: Int)
}