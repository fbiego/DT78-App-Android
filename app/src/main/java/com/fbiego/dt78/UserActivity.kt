package com.fbiego.dt78

import android.app.TimePickerDialog
import android.os.Bundle
import androidx.preference.PreferenceManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.view.LayoutInflater
import com.fbiego.dt78.app.SettingsActivity
import com.fbiego.dt78.data.MyDBHandler
import com.fbiego.dt78.data.UserListAdapter
import com.fbiego.dt78.data.WheelView
import com.fbiego.dt78.data.myTheme
import kotlinx.android.synthetic.main.activity_user.*
import com.fbiego.dt78.app.ForegroundService as FG

class UserActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        setTheme(myTheme(pref.getInt(SettingsActivity.PREF_ACCENT, 0)))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)

        val setPref =  PreferenceManager.getDefaultSharedPreferences(this)
        val unit = setPref.getBoolean(SettingsActivity.PREF_UNITS, false)
        val hr24 = !(setPref.getBoolean(SettingsActivity.PREF_12H, false))

        val current = ArrayList<Int>()

        val names = arrayListOf(getString(R.string.age), getString(R.string.step_len), getString(R.string.height),
            getString(R.string.weight), getString(R.string.target))
        val dbHandler = MyDBHandler(this, null, null, 1)


        val sleep = dbHandler.getSet(1)
        current.clear()
        if (sleep.isNotEmpty()){
            current.addAll(sleep)
        } else {
            val slp = arrayOf(1, 22, 0, 7, 0, 1, 0)
            current.addAll(slp)
            dbHandler.insertSet(current)
        }
        val start = String.format("%02d:%02d", current[1], current[2])
        val end = String.format("%02d:%02d", current[3], current[4])
        sleepStart.text = start
        sleepEnd.text = end
        sleepEnable.isChecked = current[5]==1
        sleepEnable.setOnCheckedChangeListener { _, b ->
            current[5] = if (b) 1 else 0
            dbHandler.insertSet(current)
            FG().updateSleep(current)
        }
        sleepStart.setOnClickListener{
            val picker = TimePickerDialog(this, { _, i, i2 ->
                current[1] = i
                current[2] = i2
                sleepStart.text = String.format("%02d:%02d", i, i2)
                dbHandler.insertSet(current)
                FG().updateSleep(current)
            }, current[1], current[2], hr24)
            picker.show()
        }
        sleepEnd.setOnClickListener {
            val picker = TimePickerDialog(this, { _, i, i2 ->
                current[3] = i
                current[4] = i2
                sleepEnd.text = String.format("%02d:%02d", i, i2)
                dbHandler.insertSet(current)
                FG().updateSleep(current)
            }, current[3], current[4], hr24)
            picker.show()
        }

        val user = dbHandler.getUser()
        val values = arrayListOf("${user.age} "+getString(R.string.years), "${user.step} "+getString(R.string.cm),
            "${user.height} "+getString(R.string.cm), "${user.weight} "+getString(R.string.kg), "${user.target} "+getString(R.string.steps))
        val icons = arrayListOf(R.drawable.ic_user, R.drawable.ic_length, R.drawable.ic_height, R.drawable.ic_weight, R.drawable.ic_steps)




        val myUserList = UserListAdapter(this, icons, names, values, null)
        userListView.adapter = myUserList
        userListView.setOnItemClickListener { _, _, i, _ ->
            val items = ArrayList<String>()
            val value = ArrayList<Int>()
            var title = "Dialog"

            when (i) {
                0 -> {
                    title = getString(R.string.age)
                    for (x in 10 until 151){
                        items.add("$x "+getString(R.string.years))
                        value.add(x)
                    }
                }
                1 -> {
                    title = getString(R.string.step_len)
                    for (x in 20 until 151){
                        items.add("$x "+getString(R.string.cm))
                        value.add(x)
                    }
                }
                2 -> {
                    title = getString(R.string.height)
                    for (x in 100 until 200){
                        items.add("$x "+getString(R.string.cm))
                        value.add(x)
                    }
                }
                3 -> {
                    title = getString(R.string.weight)
                    for (x in 30 until 201){
                        items.add("$x "+getString(R.string.kg))
                        value.add(x)
                    }
                }
                4 -> {
                    title = getString(R.string.target)
                    for (x in 4 until 50){
                        items.add("${x*1000} "+getString(R.string.steps))
                        value.add(x*1000)
                    }
                }
            }

            val outer = LayoutInflater.from(this).inflate(R.layout.wheel_view, null)
            val wheelView = outer.findViewById<WheelView>(R.id.wheel_view_wv)
            wheelView.setItems(items)
            wheelView.setSelection(items.indexOf(values[i]))

            val dialog = AlertDialog.Builder(this)
            dialog.setTitle(title)
                .setView(outer)
                .setPositiveButton(R.string.save){_, _ ->
                    when (i){
                        0 -> {
                            user.age = value[wheelView.selectedIndex]
                        }
                        1 -> {
                            user.step = value[wheelView.selectedIndex]
                        }
                        2 -> {
                            user.height = value[wheelView.selectedIndex]
                        }
                        3 -> {
                            user.weight = value[wheelView.selectedIndex]
                        }
                        4 -> {
                            user.target = value[wheelView.selectedIndex]
                        }
                    }
                    values[i] = wheelView.selectedItem
                    myUserList.notifyDataSetChanged()
                    dbHandler.insertUser(user)
                    FG().updateUser(user, if (unit) 1 else 0)
                }
                .show()

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