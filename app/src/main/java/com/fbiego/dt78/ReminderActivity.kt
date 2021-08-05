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
import android.app.Notification
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.preference.PreferenceManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import androidx.cardview.widget.CardView
import androidx.core.widget.addTextChangedListener
import com.fbiego.dt78.app.CrashLogger
import com.fbiego.dt78.app.MeasureReceiver
import com.fbiego.dt78.app.SettingsActivity
import com.fbiego.dt78.data.*
import kotlinx.android.synthetic.main.activity_reminder.*
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList
import com.fbiego.dt78.app.ForegroundService as FG

class ReminderActivity : AppCompatActivity() {

    private var alarmList = ArrayList<AlarmData>()
    private val alarmAdapter = AlarmAdapter(alarmList, this@ReminderActivity::alarmClicked)
    private var scheduledList = ArrayList<ReminderData>()
    private val reminderAdapter = ReminderAdapter(scheduledList, this@ReminderActivity::reminderClicked)
    private var hr24 = false
    var ed = 0

    companion object {
        lateinit var alarmRecycler: RecyclerView
        var view = 0
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        setTheme(myTheme(pref.getInt(SettingsActivity.PREF_ACCENT, 0)))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder)

        Thread.setDefaultUncaughtExceptionHandler(CrashLogger(this))

        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)

        alarmRecycler = findViewById<View>(R.id.recyclerAlarmList) as RecyclerView
        alarmRecycler.layoutManager =
            LinearLayoutManager(this)
        val div = DividerItemDecoration(
            alarmRecycler.context,
            LinearLayoutManager.VERTICAL
        )
        alarmRecycler.addItemDecoration(div)
        alarmRecycler.isNestedScrollingEnabled = false

        recyclerScheduled.layoutManager = LinearLayoutManager(this)
        recyclerScheduled.addItemDecoration(div)
        recyclerScheduled.isNestedScrollingEnabled = false

//        scheduledReminder.setOnClickListener {
//            startActivity(Intent(this, ScheduledActivity::class.java))
//            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
//        }


    }

    override fun onStart() {
        super.onStart()

        if (intent.hasExtra("reminder")){
            val id = intent.getIntExtra("reminder", 0)
            val text = intent.getStringExtra("text")
            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.reminder_name) + " $id")
            builder.setMessage(text)
            builder.setPositiveButton(android.R.string.ok, null)
            builder.setNegativeButton(R.string.cancel) {_, _ ->

            }
            builder.show()
        }
    }

    override fun onResume() {
        super.onResume()

        alarmList.clear()
        scheduledList.clear()
        val dbHandler = MyDBHandler(this, null, null, 1)
        val setPref =  PreferenceManager.getDefaultSharedPreferences(this)
        alarmList = dbHandler.getAlarms()
        scheduledList = dbHandler.getReminders()

        if (alarmList.size != 8){
            dbHandler.createAlarms()
        }
        alarmList = dbHandler.getAlarms()

        val current = ArrayList<Int>()
        val sed = dbHandler.getSet(0)
        hr24 = !(setPref.getBoolean(SettingsActivity.PREF_12H, false))

        if (sed.isNotEmpty()){
            current.addAll(sed)
        } else {
            val sd = arrayOf(0, 7, 0, 22, 0, 0, 45)
            current.addAll(sd)
            dbHandler.insertSet(current)
        }
        val start = String.format("%02d:%02d", current[1], current[2])
        val end = String.format("%02d:%02d", current[3], current[4])
        sedStart.text = start
        sedEnd.text = end
        sedEnable.isChecked = current[5]!=0
        when (current[6]){
            45 -> {
                sedInterval.setSelection(0, true)
            }
            60 -> {
                sedInterval.setSelection(1, true)
            }
            else -> {
                sedInterval.setSelection(0, true)
            }
        }

        sedStart.setOnClickListener{
            val picker = TimePickerDialog(this, { _, i, i2 ->
                current[1] = i
                current[2] = i2
                sedStart.text = String.format("%02d:%02d", i, i2)
                dbHandler.insertSet(current)
                FG().updateSed(current)
            }, current[1], current[2], hr24)
            picker.show()
        }
        sedEnd.setOnClickListener {
            val picker = TimePickerDialog(this, { _, i, i2 ->
                current[3] = i
                current[4] = i2
                sedEnd.text = String.format("%02d:%02d", i, i2)
                dbHandler.insertSet(current)
                FG().updateSed(current)
            }, current[3], current[4], hr24)
            picker.show()
        }

        sedEnable.setOnCheckedChangeListener { _, b ->
            val st = if (b) 1 else 0
            current[5] = st
            dbHandler.insertSet(current)
            FG().updateSed(current)
        }


        sedInterval.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(p0: AdapterView<*>?) {


            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                when (p2) {
                    0 -> {
                        current[6] = 45
                    }
                    1 -> {
                        current[6] = 60
                    }
                    else -> {
                        current[6] = 90
                    }
                }
                dbHandler.insertSet(current)
                FG().updateSed(current)
            }

        }



        alarmRecycler.apply {
            layoutManager = LinearLayoutManager(this@ReminderActivity)
            adapter = alarmAdapter
        }
        recyclerScheduled.apply{
            layoutManager = LinearLayoutManager(this@ReminderActivity)
            adapter = reminderAdapter
        }
        alarmAdapter.update(alarmList)
        reminderAdapter.update(scheduledList)
    }

//    private fun format25(input: String): String{
//        val l = 25
//        val x = (input.length-1)/l
//        var str = input
//        for (y in 1..x){
//            val z = y*l+y-1
//            str = str.replaceRange(z, z, "\n")
//        }
//        return str
//    }

    private fun reminderClicked(reminder: ReminderData){
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.reminder_name) +"${reminder.id}")
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        val showHint = pref.getBoolean(SettingsActivity.PREF_HINT, true)
        if (showHint) {
            builder.setMessage(getString(R.string.reminder_hint))
        }
        var state = reminder.state
        var hr = reminder.hour
        var min = reminder.minute
        val inflater = layoutInflater
        val watchLayout = inflater.inflate(R.layout.reminder_layout, null)
        val edit = watchLayout.findViewById<EditText>(R.id.watchText)
        val spinner = watchLayout.findViewById<Spinner>(R.id.watchIcon)
        val card = watchLayout.findViewById<CardView>(R.id.outerCard)
        val time = watchLayout.findViewById<TextView>(R.id.watchTime)
        time.text = String.format("%02d:%02d",hr, min)
        edit.setText(reminder.text)
        if (state){
            card.setCardBackgroundColor(ColorStateList.valueOf(this.getColorFromAttr(R.attr.colorIcons)))
        } else {
            card.setCardBackgroundColor(Color.GRAY)
        }

        val adapter = NotifyAdapter(this, false, Watch(FG.dt78).iconSet)
        spinner.adapter = adapter
        val index = Watch(FG.dt78).iconSet.indexOf(reminder.icon)
        val pos = if (index != -1){
            index
        } else {
            0
        }
        spinner.setSelection(pos)


//        edit.setOnFocusChangeListener { view, b ->
//            if (b){
//                edit.setText(format25(edit.text.toString().replace("\n", "")))
//            } else {
//                edit.setText(edit.text.toString().replace("\n", ""))
//            }
//        }

        time.setOnClickListener {
            val picker = TimePickerDialog(this, { _, i, i2 ->
                hr = i
                min = i2
                time.text = String.format("%02d:%02d", i, i2)
            }, hr, min, hr24)
            picker.show()
        }

        card.setOnClickListener {
            if (showHint){
                pref.edit().putBoolean(SettingsActivity.PREF_HINT, false).apply()
            }
            state = !state
            if (state){
                card.setCardBackgroundColor(ColorStateList.valueOf(this.getColorFromAttr(R.attr.colorIcons)))
            } else {
                card.setCardBackgroundColor(Color.GRAY)
            }

        }


        builder.setView(watchLayout)
        builder.setPositiveButton(R.string.save) {_, _ ->

            val db = MyDBHandler(this, null, null, 1)
            db.insertReminder(ReminderData(reminder.id, hr, min, spinner.selectedItem as Int, state, edit.text.toString().replace("\n", "")))
            val reminders = db.getReminders()
            reminderAdapter.update(reminders)
            setReminders(this, reminders)

        }
        builder.setNegativeButton(R.string.cancel) {_, _ ->

        }
        builder.show()
    }


    private fun alarmClicked(alarm: AlarmData){

        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.alarm)+" ${alarm.id +1}")
        var hr = alarm.hour
        var min = alarm.minute
        val inflater = layoutInflater
        val alarmLayout = inflater.inflate(R.layout.alarm_edit, null)
        val time = alarmLayout.findViewById<TextView>(R.id.alarmTime)
        val switch = alarmLayout.findViewById<SwitchCompat>(R.id.enableSwitch)
        val week = alarmLayout.findViewById<LinearLayout>(R.id.week)
        val spinner = alarmLayout.findViewById<Spinner>(R.id.alarmRepeat)

        when (alarm.repeat){
            128 -> {
                spinner.setSelection(0, true)
            }
            127 -> {
                spinner.setSelection(1, true)
            }
            31 -> {
                spinner.setSelection(2, true)
            }
            else -> {
                spinner.setSelection(3, true)
                var y = alarm.repeat
                week.visibility = View.VISIBLE
                if (y/64 == 1){
                    week.findViewById<CheckBox>(R.id.sun).isChecked = true
                }
                y %= 64
                if (y/32 == 1){
                    week.findViewById<CheckBox>(R.id.sat).isChecked = true
                }
                y %= 32
                if (y/16 == 1){
                    week.findViewById<CheckBox>(R.id.fri).isChecked = true
                }
                y %= 16
                if (y/8 == 1){
                    week.findViewById<CheckBox>(R.id.thur).isChecked = true
                }
                y %= 8
                if (y/4 == 1){
                    week.findViewById<CheckBox>(R.id.wed).isChecked = true
                }
                y %= 4
                if (y/2 == 1){
                    week.findViewById<CheckBox>(R.id.tue).isChecked = true
                }
                y %= 2
                if (y/1 == 1){
                    week.findViewById<CheckBox>(R.id.mon).isChecked = true
                }
            }
        }
        switch.isChecked = alarm.enable
        time.text = String.format("%02d:%02d", hr, min)
        time.setOnClickListener {
            val picker = TimePickerDialog(this, { _, i, i2 ->
                hr = i
                min = i2
                time.text = String.format("%02d:%02d", hr, min)
            }, alarm.hour, alarm.minute, hr24)
            picker.show()
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (getString(R.string.custom_alarm) == p0?.getItemAtPosition(p2).toString()){
                    week.visibility = View.VISIBLE
                } else {
                    week.visibility = View.GONE
                }
            }

        }
        builder.setView(alarmLayout)
        builder.setPositiveButton(R.string.save) {_, _ ->

            var repeat = 0
            when (spinner.selectedItemPosition){
                0 -> repeat = 128
                1 -> repeat = 127
                2 -> repeat = 31
                3 -> {

                    if (week.findViewById<CheckBox>(R.id.sun).isChecked) repeat += 64
                    if (week.findViewById<CheckBox>(R.id.sat).isChecked) repeat += 32
                    if (week.findViewById<CheckBox>(R.id.fri).isChecked) repeat += 16
                    if (week.findViewById<CheckBox>(R.id.thur).isChecked) repeat += 8
                    if (week.findViewById<CheckBox>(R.id.wed).isChecked) repeat += 4
                    if (week.findViewById<CheckBox>(R.id.tue).isChecked) repeat += 2
                    if (week.findViewById<CheckBox>(R.id.mon).isChecked) repeat += 1

                }
            }
            if (repeat == 0){
                repeat = 128
            }
            val dbHandler = MyDBHandler(this, null, null, 1)
            val alar = AlarmData(alarm.id, switch.isChecked, hr, min, repeat )

            val success = FG().updateAlarm(alar)
            if (!success && alar.repeat == 128){
                Toast.makeText(this, R.string.connect_set, Toast.LENGTH_LONG).show()
            } else {
                dbHandler.insertAlarm(alar)
            }
            val newAlarms = dbHandler.getAlarms()
            alarmAdapter.update(newAlarms)

        }
        builder.setNegativeButton(R.string.cancel) {_, _ ->

        }
        builder.show()
    }

    fun setReminders(context: Context, reminders: ArrayList<ReminderData>){

        val cal = Calendar.getInstance(Locale.getDefault())
        cal.set(Calendar.MILLISECOND, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)

        for (rem in reminders){
            cal.set(Calendar.HOUR_OF_DAY, rem.hour)
            cal.set(Calendar.MINUTE, rem.minute)
            if (rem.state){
                val time = if (cal.timeInMillis > System.currentTimeMillis()){
                    cal.timeInMillis
                } else {
                    cal.timeInMillis + 86400000
                }
                setRemind(context, time, rem)
            } else {
                setRemind(context, null, rem)
            }
        }
    }

    private fun setRemind(context: Context, time: Long?, rem: ReminderData){
        Timber.e("Set reminder? $time")
        val intent = Intent(context, MeasureReceiver::class.java)
        val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
        if (time != null){
            intent.putExtra("id", rem.id+30)
            intent.putExtra("text", rem.text)
            intent.putExtra("icon", rem.icon)
            val pending = PendingIntent.getBroadcast(context.applicationContext, 57600 + rem.id, intent, PendingIntent.FLAG_CANCEL_CURRENT)
            alarmManager.setRepeating(AlarmManager.RTC, time, AlarmManager.INTERVAL_DAY, pending)
        } else {
            val pendingIntent = PendingIntent.getBroadcast(context.applicationContext, 57600 + rem.id, intent, PendingIntent.FLAG_NO_CREATE)
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