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

package com.fbiego.dt78

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fbiego.dt78.app.CrashLogger
import com.fbiego.dt78.app.MeasureReceiver
import com.fbiego.dt78.app.SettingsActivity
import com.fbiego.dt78.data.*
import com.llollox.androidtoggleswitch.widgets.ToggleSwitch
import kotlinx.android.synthetic.main.activity_measure.*
import java.util.*
import kotlin.collections.ArrayList
import com.fbiego.dt78.app.SettingsActivity as ST
import com.fbiego.dt78.app.ForegroundService as FG

class MeasureActivity : AppCompatActivity() {

    private var measureList = ArrayList<MeasureData>()
    private var measureAdapter = MeasureAdapter(measureList)
    var start = 0
    var interval = 2
    var end = 23
    var measure = false
    var setPref: SharedPreferences? = null

    companion object {
        lateinit var measureRecycler: RecyclerView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        setTheme(myTheme(pref.getInt(SettingsActivity.PREF_ACCENT, 0)))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_measure)

        Thread.setDefaultUncaughtExceptionHandler(CrashLogger(this))

        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)

        setPref =  PreferenceManager.getDefaultSharedPreferences(this)

        measureRecycler = findViewById<View>(R.id.recyclerMeasure) as RecyclerView
        measureRecycler.layoutManager =
            LinearLayoutManager(this)

        measureRecycler.isNestedScrollingEnabled = false

        measureRecycler.apply {
            layoutManager =
                LinearLayoutManager(this@MeasureActivity)
            adapter = measureAdapter
        }

        hourly.setOnCheckedChangeListener { _, b ->
            if (b){
                scheduled.isChecked = false
            }
            setPref!!.edit().putBoolean(ST.PREF_HOURLY, b).apply()
            FG.hourly = b
            FG().updateHourly(b)
        }

        scheduled.setOnCheckedChangeListener { _, b ->
            if (b){
                hourly.isChecked = false
            }
            setPref!!.edit().putBoolean(ST.PREF_SCHEDULED, b).apply()
            measure = b
        }

        sendWatch.setOnCheckedChangeListener { _, b ->
            setPref!!.edit().putBoolean(ST.PREF_SEND_WATCH, b).apply()
            FG.sendWatch = b
        }

        val adapter = NotifyAdapter(this, false, Watch(FG.dt78).iconSet)
        spinnerIcon.adapter = adapter


        spinnerIcon.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val ic = spinnerIcon.getItemAtPosition(p2) as Int
                setPref!!.edit().putInt(ST.PREF_SEND_ICON, ic).apply()
                FG.msIcon = ic
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }


    }

    private fun getMsName(id: Int): String{
        return when(id){
            MEASURE_TRIGGERED -> getString(R.string.m_triggered)
            M_SERVICE_STOPPED -> getString(R.string.m_stopped)
            M_MEASURE_START -> getString(R.string.m_start)
            M_MEASURE_END -> getString(R.string.m_end)
            M_MEASURE_RECEIVED -> getString(R.string.m_received)
            M_MEASURE_NULL -> getString(R.string.m_null)
            M_MEASURE_DISC -> getString(R.string.m_disconnect)
            M_MEASURE_FAIL -> getString(R.string.m_failed)
            else -> getString(R.string.m_no_data)
        }
    }


    private fun updateList(){
        val hr = Calendar.getInstance(Locale.getDefault()).get(Calendar.HOUR_OF_DAY)
        val dbHandler = MyDBHandler(this, null, null, 1)
        measureList.clear()
        for (x in start..end step interval){
            val dat = getMsName(dbHandler.getMeasure(x))
            measureList.add(MeasureData("$x:00", if (x > hr ) getString(R.string.waiting) else dat))
        }
        measureAdapter.update(measureList)
    }

    fun onClickItem(view: View){
        val items = ArrayList<String>()
        val value = ArrayList<Int>()
        var title =  getString(R.string.start)
        var selected = 0
        when (view.id){
            R.id.startTime -> {
                title =  getString(R.string.start)
                selected = start
                for (x in 0 until end){
                    items.add("$x:00")
                    value.add(x)
                }
            }
            R.id.interval -> {
                title =  getString(R.string.interval)
                for (x in 2..4){
                    items.add("$x "+ getString(R.string.hours))
                    value.add(x)
                }
                selected = interval
            }
            R.id.endTime -> {
                title = getString(R.string.end)
                for (x in start+interval..23 step interval){
                    items.add("$x:00")
                    value.add(x)
                }
                selected = when {
                    value.contains(end) -> end
                    value.contains(end+1) -> end+1
                    value.contains(end-1) -> end-1
                    value.contains(end+2) -> end+2
                    value.contains(end-2) -> end-2
                    else -> value[0]
                }
            }
        }

        val index = value.indexOf(selected)

        val outer = LayoutInflater.from(this).inflate(R.layout.wheel_view, null)
        val wheelView = outer.findViewById<WheelView>(R.id.wheel_view_wv)
        wheelView.setItems(items)
        wheelView.setSelection(index)
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle(title)
            .setView(outer)
            .setPositiveButton(R.string.save) { _, _ ->
                when (view.id){
                    R.id.startTime -> {
                        start = value[wheelView.selectedIndex]
                        textStart.text = "$start:00"
                        setPref!!.edit().putInt(ST.PREF_M_START, start).apply()
                    }
                    R.id.interval -> {
                        interval = value[wheelView.selectedIndex]
                        textInterval.text = "$interval "+ getString(R.string.hours)
                        setPref!!.edit().putInt(ST.PREF_M_INTERVAL, interval).apply()
                    }
                    R.id.endTime -> {
                        end = value[wheelView.selectedIndex]
                        textEnd.text = "$end:00"
                        setPref!!.edit().putInt(ST.PREF_M_END, end).apply()
                    }
                }
                updateList()
            }
            .show()
    }

    override fun onResume() {
        super.onResume()
        hourly.isChecked = setPref!!.getBoolean(ST.PREF_HOURLY, false)
        measure = setPref!!.getBoolean(ST.PREF_SCHEDULED, false)
        sendWatch.isChecked = setPref!!.getBoolean(ST.PREF_SEND_WATCH, false)
        scheduled.isChecked = measure

        start = setPref!!.getInt(ST.PREF_M_START, 0)
        interval = setPref!!.getInt(ST.PREF_M_INTERVAL, 2)
        end = setPref!!.getInt(ST.PREF_M_END, 23)
        textStart.text = "$start:00"
        textInterval.text = "$interval "+ getString(R.string.hours)
        textEnd.text = "$end:00"

        updateList()

        var ic = setPref!!.getInt(ST.PREF_SEND_ICON, 0)
        if (ic > 2){
            ic -= 1
        }
        if (!Watch(FG.dt78).iconSet.contains(ic)){
            ic = 0
        }
        spinnerIcon.setSelection(ic)


    }

    override fun onPause() {
        super.onPause()
        setSchedule(this, measure, start, end, interval)

    }

    fun setSchedule(context: Context, measure: Boolean, start: Int, end: Int, interval: Int){
        val als = ArrayList<Int>()
        for (i in start..end step interval){
            als.add(i)
        }
        val cal = Calendar.getInstance(Locale.getDefault())
        val hr = cal.get(Calendar.HOUR_OF_DAY)
        cal.set(Calendar.MILLISECOND, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        for (x in 0..23){
            if (als.contains(x) && measure){
                cal.set(Calendar.HOUR_OF_DAY, x)
                val time = if (x > hr){
                    cal.timeInMillis
                } else {
                    cal.timeInMillis + 86400000
                }
                setMeasure(context, x, time)
            } else {
                setMeasure(context, x, null)
            }
        }
    }

    private fun setMeasure(context: Context, id: Int, time: Long?){
        val intent = Intent(context, MeasureReceiver::class.java)
        val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
        if (time != null){
            intent.putExtra("id", id)
            val pending = PendingIntent.getBroadcast(context.applicationContext, 54300 + id, intent, PendingIntent.FLAG_CANCEL_CURRENT)
            alarmManager.setRepeating(AlarmManager.RTC, time, AlarmManager.INTERVAL_DAY, pending)
        } else {
            val pendingIntent = PendingIntent.getBroadcast(context.applicationContext, 54300 + id, intent, PendingIntent.FLAG_NO_CREATE)
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
            }
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

}