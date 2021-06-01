package com.fbiego.dt78

import android.app.TimePickerDialog
import android.content.*
import android.os.Bundle
import androidx.preference.PreferenceManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.SeekBar
import android.widget.Toast
import com.fbiego.dt78.data.*
import kotlinx.android.synthetic.main.activity_setings.*
import timber.log.Timber
import com.fbiego.dt78.app.ForegroundService as FG
import com.fbiego.dt78.app.SettingsActivity as ST

class SettingsWatchActivity : AppCompatActivity() {
    private var dt78 = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        setTheme(myTheme(pref.getInt(ST.PREF_ACCENT, 0)))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setings)

        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)


        val dbHandler = MyDBHandler(this, null, null, 1)
        val setPref =  PreferenceManager.getDefaultSharedPreferences(this)
        val hr24 = !(setPref.getBoolean(ST.PREF_12H, false))

        val current = ArrayList<Int>()
        val quiet = dbHandler.getSet(2)
        current.clear()
        if (quiet.isNotEmpty()){
            current.addAll(quiet)
        } else {
            val slp = arrayOf(2, 22, 0, 7, 0, 0, 0)
            current.addAll(slp)
            dbHandler.insertSet(current)
            FG().updateQuiet(current)

        }

        val start = String.format("%02d:%02d", current[1], current[2])
        val end = String.format("%02d:%02d", current[3], current[4])
        quietStart.text = start
        quietEnd.text = end
        quietEnable.isChecked = current[5]!=0

        quietStart.setOnClickListener{
            val picker = TimePickerDialog(this, { _, i, i2 ->
                current[1] = i
                current[2] = i2
                quietStart.text = String.format("%02d:%02d", i, i2)
                dbHandler.insertSet(current)
                FG().updateQuiet(current)
            }, current[1], current[2], hr24)
            picker.show()
        }
        quietEnd.setOnClickListener {
            val picker = TimePickerDialog(this, { _, i, i2 ->
                current[3] = i
                current[4] = i2
                quietEnd.text = String.format("%02d:%02d", i, i2)
                dbHandler.insertSet(current)
                FG().updateQuiet(current)
            }, current[3], current[4], hr24)
            picker.show()
        }
        quietEnable.setOnCheckedChangeListener { _, b ->
            val st = if (b) 1 else 0
            current[5] = st
            dbHandler.insertSet(current)
            FG().updateQuiet(current)
        }



        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                FG().sendData(byteArrayOfInts(0xAB, 0x00, 0x04, 0xFF, 0x01, 0x01, p1))
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                setPref.edit().putInt(ST.PREF_BRIGHT, p0!!.progress).apply()
            }

        })

    }

    override fun onResume() {
        super.onResume()

        val setPref =  PreferenceManager.getDefaultSharedPreferences(this)
        var hr24 = !(setPref.getBoolean(ST.PREF_12H, false))
        dt78 = setPref.getInt(ST.PREF_WATCH_ID, -1)
        FG.watchVersion = setPref.getString(ST.PREF_VERSION, "").toString()
        val dbHandler = MyDBHandler(this, null, null, 1)
        val items = Watch(dt78).lang.string
        val values = Watch(dt78).lang.int
        var timeout = setPref.getInt(ST.PREF_TIMEOUT, 10)
        val display = setPref.getBoolean(ST.PREF_DISPLAY_OFF, false)

        seekBar.progress = setPref.getInt(ST.PREF_BRIGHT, 100)



        val names = arrayListOf(getString(R.string.hr12_sys), getString(R.string.hourly), unit(setPref.getBoolean(ST.PREF_UNITS, false), this),
            getString(R.string.watch_type)+Watch(dt78).name+"\t\t"+FG.watchVersion, getString(R.string.watch_lang), getString(R.string.reset_watch))

        var units = setPref.getBoolean(ST.PREF_UNITS, false)

        val states : ArrayList<Boolean?> = arrayListOf( !hr24,
            setPref.getBoolean(ST.PREF_HOURLY, false), null, null, null, null)
        val icons = arrayListOf(R.drawable.ic_12hr, R.drawable.ic_hourly, R.drawable.ic_klm, R.drawable.ic_watch,R.drawable.ic_lang, R.drawable.ic_reset)

        if (Watch(dt78).rtw) {
            names.add(getString(R.string.raise_wake))
            states.add(setPref.getBoolean(ST.PREF_HOURLY, false))
            icons.add(R.drawable.ic_rtw)
        }
        if (Watch(dt78).contact) {
            names.add(getString(R.string.frequent))
            states.add(null)
            icons.add(R.drawable.ic_people)
        }

        if (dt78 == ESP32){
            names[1] = getString(R.string.rotate_disp)
            names[2] = getString(R.string.flip_disp)
            names[4] = getString(R.string.timeout)+":\t"+items[values.indexOf(timeout)]
            names[5] = getString(R.string.screen_off)
            states[2] = units
            states[5] = display
            icons[1] = R.drawable.ic_rotate
            icons[2] = R.drawable.ic_flip
            icons[4] = R.drawable.ic_timeout
            icons[5] = R.drawable.ic_screen
        }

        val myUserList = UserListAdapter(this, icons, names, null, states)
        settingsListView.adapter = myUserList
        setListViewHeightBasedOnChildren(settingsListView)
        settingsListView.setOnItemClickListener { _, _, i, _ ->

            val editor: SharedPreferences.Editor = setPref.edit()
            when (i){
                0 -> {
                    states[i] = !states[i]!!
                    editor.putBoolean(ST.PREF_12H, states[i]!!)
                    hr24 = states[i]!!
                    FG().update12hr(states[i]!!)
                }
                1 -> {
                    states[i] = !states[i]!!
                    editor.putBoolean(ST.PREF_HOURLY, states[i]!!)
                    val hrl = byteArrayOfInts(0xAB, 0x00, 0x04, 0xFF, 0x78, 0x80, if (states[i]!!) 1 else 0)
                    FG().sendData(hrl)
                }
                2 -> {
                    units = !units
                    editor.putBoolean(ST.PREF_UNITS, units)
                    val user = dbHandler.getUser()
                    FG().updateUser(user, if (units) 1 else 0)
                    if (dt78 != ESP32) {
                        FG.unit = if (units) 1 else 0
                        names[i] = unit(units, this)
                        states[i] = null
                    } else {
                        states[i] = units
                    }
                }
                3 -> {

                    val alert = AlertDialog.Builder(this)
                    alert.setTitle(getString(R.string.watch_info))
                    val pref = PreferenceManager.getDefaultSharedPreferences(this)
                    val id = pref.getString(ST.PREF_HEX_ID, "AB-00-11...")
                    alert.setMessage(getString(R.string.type)+": ${Watch(dt78).name}\n"+getString(R.string.version)+": ${FG.watchVersion}\nID: $id")
                    alert.setPositiveButton(R.string.copy_id){_, _ ->
                        val clipBoard = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val data = ClipData.newPlainText("Watch ID", id)
                        clipBoard.setPrimaryClip(data)
                        Toast.makeText(this, R.string.copied, Toast.LENGTH_SHORT).show()
                    }
                    alert.setNegativeButton(R.string.cancel){_, _ ->

                    }
                    alert.show()

                }
                4 -> {

                    val outer = LayoutInflater.from(this).inflate(R.layout.wheel_view, null)
                    val wheelView = outer.findViewById<WheelView>(R.id.wheel_view_wv)
                    wheelView.setItems(items)
                    val lang = setPref.getInt(ST.PREF_LANG, 0)
                    if (dt78 == ESP32){
                        wheelView.setSelection(values.indexOf(timeout))
                    } else {
                        if (values.contains(lang)){
                            wheelView.setSelection(values.indexOf(lang))
                        } else {
                            wheelView.setSelection(0)
                        }

                    }
//                    wheelView.onWheelViewListener = object: WheelView.OnWheelViewListener() {
//                        override fun onSelected(selectedIndex: Int, item: String?) {
//                            Timber.d("[Dialog]selectedIndex: $selectedIndex , item: $item ")
//                        }
//                    }

                    val dialog = AlertDialog.Builder(this)
                    dialog.setTitle(title)
                        .setView(outer)
                        .setPositiveButton(android.R.string.yes){_, _ ->

                            if (dt78 == ESP32){
                                timeout = values[wheelView.selectedIndex]
                                names[4] = getString(R.string.timeout)+":\t"+items[values.indexOf(timeout)]
                                myUserList.notifyDataSetChanged()
                                editor.putInt(ST.PREF_TIMEOUT, values[wheelView.selectedIndex]).apply()
                            } else {
                                editor.putInt(ST.PREF_LANG, values[wheelView.selectedIndex]).apply()
                            }

                            if (!FG().sendData(byteArrayOfInts(0xAB, 0x00, 0x04, 0xFF, 0x7B, 0x80, values[wheelView.selectedIndex]))){
                                Toast.makeText(this@SettingsWatchActivity, R.string.not_connect, Toast.LENGTH_SHORT).show()
                            }
                        }
                        .show()

                }
                5 -> {
                    if (dt78 != ESP32) {
                        val alert = AlertDialog.Builder(this)
                        alert.setTitle(getString(R.string.reset_watch))
                        alert.setMessage(R.string.reset_info)
                        alert.setPositiveButton(R.string.yes) { _, _ ->
                            val reset = byteArrayOfInts(0xAB, 0x00, 0x04, 0xFF, 0x23, 0x80, 0x00)
                            if (!FG().sendData(reset)) {
                                Toast.makeText(this, R.string.not_connect, Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                        alert.setNegativeButton(R.string.cancel) { _, _ ->

                        }
                        alert.show()
                    } else {
                        states[i] = !states[i]!!
                        editor.putBoolean(ST.PREF_DISPLAY_OFF, states[i]!!)
                        val reset = byteArrayOfInts(0xAB, 0x00, 0x04, 0xFF, 0x23, 0x80, if (states[i]!!) 1 else 0)
                        if (!FG().sendData(reset)){
                            Toast.makeText(this, R.string.not_connect, Toast.LENGTH_SHORT).show()
                        }
                    }

                }
                6 -> {
                    states[i] = !states[i]!!
                    editor.putBoolean(ST.PREF_RTW, states[i]!!)

                    val rtw = byteArrayOfInts(0xAB, 0x00, 0x04, 0xFF, 0x77, 0x80, if (states[i]!!) 1 else 0)
                    FG().sendData(rtw)
                }
                7 -> {
                    startActivity(Intent(this, ContactsActivity::class.java))
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }
            }

            myUserList.notifyDataSetChanged()
            setListViewHeightBasedOnChildren(settingsListView)
            editor.apply()
            editor.commit()
        }

        if (dt78 == DT78 || dt78 == DT78_2){
            watchFaces.visibility = View.VISIBLE
        } else {
            watchFaces.visibility = View.GONE
        }
        if (dt78 == DT66 || dt78 == MI_AIR){
            linearLayout2.visibility = View.VISIBLE
        } else {
            linearLayout2.visibility = View.GONE
        }
        if (dt78 != ESP32){
            linearLayout3.visibility = View.GONE
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


    fun watchFace(view: View){
         when (view.id){
             R.id.watch1 -> {
                 if (!FG().sendData(byteArrayOfInts(0xAB, 0x00, 0x06, 0xFF, 0x95, 0x80, 0x01, 0x0F, 0x0C))){
                     Toast.makeText(this, R.string.not_connect, Toast.LENGTH_SHORT).show()
                 }
             }
             R.id.watch2 -> {
                 if (!FG().sendData(byteArrayOfInts(0xAB, 0x00, 0x06, 0xFF, 0x95, 0x80, 0x01, 0x0F, 0x0D))){
                     Toast.makeText(this, R.string.not_connect, Toast.LENGTH_SHORT).show()
                 }
             }
             R.id.watch3 -> {
                 if (!FG().sendData(byteArrayOfInts(0xAB, 0x00, 0x06, 0xFF, 0x95, 0x80, 0x01, 0x0F, 0x0E))){
                     Toast.makeText(this, R.string.not_connect, Toast.LENGTH_SHORT).show()
                 }
             }
             R.id.linearLayout2 -> {
                 startActivity(Intent(this, UploadActivity::class.java))
                 overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
             }
         }
    }

    private fun setListViewHeightBasedOnChildren(listView: ListView) {
        val listAdapter = listView.adapter ?: return
        val desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.width,
            View.MeasureSpec.UNSPECIFIED)
        var totalHeight = 0
        var view: View? = null
        for (i in 0 until listAdapter.count)  {
            view = listAdapter.getView(i, view, listView)
            if (i == 0)
                view.layoutParams = ViewGroup.LayoutParams(desiredWidth,
                    LinearLayout.LayoutParams.WRAP_CONTENT)
            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED)
            totalHeight += view.measuredHeight
        }
        val params = listView.layoutParams
        params.height = totalHeight + (listView.dividerHeight  * (listAdapter.count - 1))
        listView.layoutParams = params
        listView.requestLayout()
    }


}