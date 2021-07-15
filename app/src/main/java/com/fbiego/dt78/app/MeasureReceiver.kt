package com.fbiego.dt78.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.fbiego.dt78.data.MEASURE_TRIGGERED
import com.fbiego.dt78.data.M_MEASURE_DISC
import com.fbiego.dt78.data.M_SERVICE_STOPPED
import com.fbiego.dt78.data.MyDBHandler
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

    fun startMeasure(id: Int){
        mListener.onMeasureStart(id)
    }
    override fun onReceive(p0: Context, p1: Intent) {
        val id = p1.getIntExtra("id", -1)
        val dbHandler = MyDBHandler(p0, null, null, 1)
        val cal = Calendar.getInstance(Locale.getDefault())
        dbHandler.writeMeasure(cal, MEASURE_TRIGGERED, id)
        if (isInit){
            startMeasure(id)
        } else {
            dbHandler.writeMeasure(cal, M_SERVICE_STOPPED, id)
        }

    }
}

interface MeasureListener{
    fun onMeasureStart(id: Int)
}