package com.fbiego.dt78

import android.app.TimePickerDialog
import android.os.Bundle
import androidx.preference.PreferenceManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import com.fbiego.dt78.app.SettingsActivity
import com.fbiego.dt78.data.AlarmAdapter
import com.fbiego.dt78.data.AlarmData
import com.fbiego.dt78.data.MyDBHandler
import com.fbiego.dt78.data.myTheme
import kotlinx.android.synthetic.main.activity_reminder.*
import com.fbiego.dt78.app.ForegroundService as FG

class ReminderActivity : AppCompatActivity() {

    private var alarmList = ArrayList<AlarmData>()
    private val alarmAdapter = AlarmAdapter(alarmList, this@ReminderActivity::alarmClicked)
    private var hr24 = false

    companion object {
        lateinit var alarmRecycler: RecyclerView
        var view = 0
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        setTheme(myTheme(pref.getInt(SettingsActivity.PREF_ACCENT, 0)))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder)

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


    }

    override fun onResume() {
        super.onResume()

        alarmList.clear()
        val dbHandler = MyDBHandler(this, null, null, 1)
        val setPref =  PreferenceManager.getDefaultSharedPreferences(this)
        alarmList = dbHandler.getAlarms()

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
            layoutManager =
                LinearLayoutManager(this@ReminderActivity)
            adapter = alarmAdapter
        }
        alarmAdapter.update(alarmList)
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

//    private fun checked(alarm: AlarmData, boolean: Boolean){
//        val state = if (boolean) "On" else "Off"
//        Toast.makeText(this@ReminderActivity, "Switched ${alarm.name} $state", Toast.LENGTH_SHORT).show()
//    }

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