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

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fbiego.dt78.app.ForegroundService
import com.fbiego.dt78.app.MeasureReceiver
import com.fbiego.dt78.app.SettingsActivity
import com.fbiego.dt78.data.*
import kotlinx.android.synthetic.main.activity_backup_restore.*
import timber.log.Timber
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import com.fbiego.dt78.app.MainApplication as MA

class BackupRestoreActivity : AppCompatActivity() {

    private var fileList = ArrayList<String>()
    var fileAdapter: RestoreAdapter? = null

    companion object {
        lateinit var recycler: RecyclerView

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        setTheme(myTheme(pref.getInt(SettingsActivity.PREF_ACCENT, 0)))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_backup_restore)

        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)

        backupCard.setOnClickListener {

            if (checkStoragePermission()) {
                backupAll(this)
            } else {
                requestWriteStoragePermission(MainActivity.PERMISSION_STORAGE + 2)
            }
        }
        fileAdapter = RestoreAdapter(fileList, this@BackupRestoreActivity::onClick)

        restoreList.layoutManager = LinearLayoutManager(this)
        restoreList.isNestedScrollingEnabled = false
        restoreList.apply {
            layoutManager =
                LinearLayoutManager(this@BackupRestoreActivity)
            adapter = fileAdapter
        }

        checkBackups()

        autoBackupSwitch.setOnCheckedChangeListener { _, b ->
            pref.edit().putBoolean(SettingsActivity.PREF_AUTO_BACKUP, b).apply()
            setAutoBackup(this, b)
        }

    }

    fun setAutoBackup(context: Context, state: Boolean){
        val id = 50
        val cal = Calendar.getInstance(Locale.getDefault())
        cal.set(Calendar.HOUR_OF_DAY, 2)
        cal.set(Calendar.MILLISECOND, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        val intent = Intent(context, MeasureReceiver::class.java)
        val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
        if (state){
            intent.putExtra("id", id)
            val pending = PendingIntent.getBroadcast(context.applicationContext, 54300 + id, intent, PendingIntent.FLAG_CANCEL_CURRENT)
            alarmManager.setRepeating(AlarmManager.RTC, cal.timeInMillis, AlarmManager.INTERVAL_DAY, pending)
        } else {
            val pendingIntent = PendingIntent.getBroadcast(context.applicationContext, 54300 + id, intent, PendingIntent.FLAG_NO_CREATE)
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
            }
        }

    }

    override fun onResume() {
        super.onResume()
        if (!checkStoragePermission()){
            requestWriteStoragePermission(MainActivity.PERMISSION_STORAGE)
        }
        val autoFile = File(Environment.getExternalStoragePublicDirectory("DT78"), "auto-backup.txt")

        textAutoBackup.text = if (autoFile.exists()){
            "Last Backup: "+autoFile.readLines()[0]
        } else {
            "Last Backup: Never"
        }
        autoBackupSwitch.isChecked = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(SettingsActivity.PREF_AUTO_BACKUP, false)
    }

    fun onClick(type: Int, name: String){
        when (type){
            0 -> {
                val alert = AlertDialog.Builder(this)
                alert.setTitle(getString(R.string.delete))
                alert.setMessage(getString(R.string.delete_file) + "\n$name")
                alert.setPositiveButton(android.R.string.yes) { _: DialogInterface?, _: Int ->
                    fileList.remove(name)
                    fileAdapter?.update(fileList)
                    val file =
                        File(Environment.getExternalStoragePublicDirectory("DT78"), name)
                    if (file.exists()) {
                        file.delete()
                        textStatus.text = getString(R.string.delete) + " $name"
                    }
                }
                alert.setNegativeButton(android.R.string.no, null)
                alert.show()
            }
            1 -> {
                val alert = AlertDialog.Builder(this)
                alert.setTitle(getString(R.string.restore))
                val file =
                    File(Environment.getExternalStoragePublicDirectory("DT78"), name)
                if (file.exists()) {
                    alert.setMessage(getString(R.string.restore_file)+ "\n$name")
                    alert.setPositiveButton(android.R.string.yes) { dialogInterface: DialogInterface?, _: Int ->
                        dialogInterface?.dismiss()
                        Thread {
                            restoreData(file)
                        }.start()
                    }
                } else {
                    alert.setMessage("Error loading $name")
                    textStatus.text = getString(R.string.load_error) + "$name"
                }

                alert.setNegativeButton(android.R.string.no, null)
                alert.show()
            }
        }

    }

    private fun checkBackups(){
        val dir = Environment.getExternalStoragePublicDirectory("DT78")

        if (!dir.exists()){
            dir.mkdirs()
        }

        val files = dir.listFiles()
        files?.sortByDescending { it.lastModified() }
        files?.forEach {
            //val name = it.readLines()[0]
            if (it.extension == "txt") {
                fileList.add(it.name)
            }
        }
        fileAdapter?.update(fileList)
        textStatus.text = "${fileList.size} "+ getString(R.string.backup_count)
    }


    private fun backupAll(context: Context){

        val cal = Calendar.getInstance(Locale.getDefault())
        Thread {

            runOnUiThread {
                progressBar.visibility = View.VISIBLE
                progressBar.isIndeterminate = true
                textStatus.text = getString(R.string.start_backup)
            }
            var data = "${cal.time}\n${cal.timeInMillis}\n" +
                    "DT78 Backup File\n"

            data += "## Steps \n" + backupSteps(context)
            data += "## Sleep \n" + backupSleep(context)
            data += "## Heart \n" + backupHeart(context)
            data += "## Pressure \n" + backupPressure(context)
            data += "## Oxygen \n" + backupOxygen(context)
            data += "## User \n" + backupUser(context)
            data += "## Settings \n" + backupSettings(context)
            data += "## Alarms \n" + backupAlarms(context)
            data += "## Notification List \n" + backupList()
            data += "## Preferences \n" + backupPrefs(context)

            val dir = Environment.getExternalStoragePublicDirectory("DT78")

            if (!dir.exists()) {
                dir.mkdirs()
            }

            val name = "${cal.timeInMillis}.txt"

            val file = File(dir, name)
            file.createNewFile()
            file.writeText(data)

            runOnUiThread {
                fileList.add(0, name)
                fileAdapter?.update(fileList)
                progressBar.visibility = View.GONE
                textStatus.text = getString(R.string.backup_complete)+ " $name"
            }
        }.start()


    }

    fun autoBackup(context: Context){
        val cal = Calendar.getInstance(Locale.getDefault())
        Thread {
            var data = "${cal.time}\n${cal.timeInMillis}\n" +
                    "DT78 Backup File\n"

            data += "## Steps \n" + backupSteps(context)
            data += "## Sleep \n" + backupSleep(context)
            data += "## Heart \n" + backupHeart(context)
            data += "## Pressure \n" + backupPressure(context)
            data += "## Oxygen \n" + backupOxygen(context)
            data += "## User \n" + backupUser(context)
            data += "## Settings \n" + backupSettings(context)
            data += "## Alarms \n" + backupAlarms(context)
            data += "## Notification List \n" + backupList()
            data += "## Preferences \n" + backupPrefs(context)

            val dir = Environment.getExternalStoragePublicDirectory("DT78")

            if (!dir.exists()) {
                dir.mkdirs()
            }

            val name = "auto-backup.txt"

            val file = File(dir, name)
            file.createNewFile()
            file.writeText(data)
        }.start()
    }

    private fun restoreData(file: File){

        runOnUiThread {
            progressBar.visibility = View.VISIBLE
            progressBar.isIndeterminate = false
            progressBar.max = 100
            progressBar.progress = 0
            textStatus.text = getString(R.string.restore_progress) + " ${file.name}"
        }
        stopService(Intent(this, ForegroundService::class.java))
        Timber.w("Start restore")

        try {

            val ln = file.readLines()
            val dbHandler = MyDBHandler(this, null, null, 1)
            val pref = PreferenceManager.getDefaultSharedPreferences(this)
            val list = ArrayList<String>()

            for (x in ln.indices) {
                val l = ln[x]
                when {
                    l.startsWith("AB:") -> {   //Steps
                        val data = l.substring(3, l.length).split("-").map { it.toInt() }
                        dbHandler.insertSteps(StepsData(data[0], data[1], data[2], data[3], data[4], data[5]))
                    }
                    l.startsWith("AC:") -> {    //Sleep
                        val data = l.substring(3, l.length).split("-").map { it.toInt() }
                        dbHandler.insertSleep(SleepData(data[0], data[1], data[2], data[3], data[4], data[5], data[6]))
                    }
                    l.startsWith("AD:") -> {    //Heart
                        val data = l.substring(3, l.length).split("-").map { it.toInt() }
                        dbHandler.insertHeart(HeartData(data[0], data[1], data[2], data[3], data[4], data[5], data[6]))

                    }
                    l.startsWith("AE:") -> {    //Pressure
                        val data = l.substring(3, l.length).split("-").map { it.toInt() }
                        dbHandler.insertBp(PressureData(data[0], data[1], data[2], data[3], data[4], data[5], data[6], data[7]))

                    }
                    l.startsWith("AF:") -> {    //Oxygen
                        val data = l.substring(3, l.length).split("-").map { it.toInt() }
                        dbHandler.insertSp02(OxygenData(data[0], data[1], data[2], data[3], data[4], data[5], data[6]))

                    }
                    l.startsWith("BA:") -> {    //user
                        val data = l.substring(3, l.length).split("-").map { it.toInt() }
                        dbHandler.insertUser(UserData(0, "name", data[1], data[0], data[2], data[3], data[4]))

                    }
                    l.startsWith("BB:") -> {    //settings
                        val data = l.substring(3, l.length).split("-").map { it.toInt() }
                        dbHandler.insertSet(data as ArrayList<Int>)

                    }
                    l.startsWith("BC:") -> {    //alarms
                        val data = l.substring(3, l.length).split("-").map { it.toInt() }
                        dbHandler.insertAlarm(AlarmData(data[0], data[1] != 0, data[2], data[3], data[4]))

                    }
                    l.startsWith("CA:") -> {    //notification list
                        list.add(l.substring(3, l.length))
                    }
                    l.startsWith("CB:") -> {    //prefs
                        val data = l.substring(3, l.length).split("-")
                        when (data[0]){
                            "String" -> {
                                pref.edit().putString(data[1], data[2]).apply()
                            }
                            "Int" -> {
                                pref.edit().putInt(data[1], data[2].toInt()).apply()
                            }
                            "Boolean" -> {
                                val b = when (data[2]){
                                    "true" -> true
                                    "false" -> false
                                    else -> false
                                }
                                pref.edit().putBoolean(data[1], b).apply()
                            }
                            "Long" -> {
                                pref.edit().putLong(data[1], data[2].toLong()).apply()
                            }
                        }
                    }
                }
                val progress = (x/(ln.size).toFloat()) *100

                Timber.w("Restore: $progress%")
                runOnUiThread {
                    progressBar.progress = progress.toInt()
                    textStatus.text = getString(R.string.restore_progress) + "...${progress.toInt()}%"
                }
            }

            MA.sharedPrefs.edit().putStringSet(MA.PREFS_KEY_ALLOWED_PACKAGES, list.toSet()).apply()

            runOnUiThread {
                progressBar.visibility = View.GONE
                progressBar.isIndeterminate = true
                textStatus.text = getString(R.string.restore_complete)
                Toast.makeText(this, getString(R.string.restore_success), Toast.LENGTH_SHORT).show()
                Handler(Looper.getMainLooper()).postDelayed({
                    restartApp()
                }, 2000)
            }

        } catch (e: Exception){

            Timber.e("Restore error: $e")
            runOnUiThread {
                progressBar.visibility = View.GONE
                progressBar.isIndeterminate = true
                textStatus.text = getString(R.string.restore_fail)
            }
        }
        Timber.w("Restore end")


    }

    private fun restartApp(){
        val intent = baseContext.packageManager.getLaunchIntentForPackage(baseContext.packageName)!!
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    private fun backupUser(context: Context): String{
        val dbHandler = MyDBHandler(context, null, null, 1)
        val user = dbHandler.getUser()
        return "BA:${user.age}-${user.step}-${user.height}-${user.weight}-${user.target}\n"
    }

    private fun backupSettings(context: Context): String{
        val dbHandler = MyDBHandler(context, null, null, 1)
        var out = ""
        for (i in 0..2){
            val s = dbHandler.getSet(i)
            out += "BB:$i-${s[1]}-${s[2]}-${s[3]}-${s[4]}-${s[5]}-${s[6]}\n"
        }
        return out

    }

    private fun backupAlarms(context: Context): String{
        val dbHandler = MyDBHandler(context, null, null, 1)
        val als = dbHandler.getAlarms()
        var out = ""
        for (a in als){
            out += "BC:${a.id}-${if (a.enable) 1 else 0}-${a.hour}-${a.minute}-${a.repeat}\n"
        }
        return out
    }

    private fun backupList(): String{
        val prefsPackages: MutableSet<String> = MA.sharedPrefs.getStringSet(
            MA.PREFS_KEY_ALLOWED_PACKAGES, mutableSetOf())!!
        var out = ""
        for (s in prefsPackages){
            out += "CA:$s\n"
        }
        return out
    }
    
    private fun backupPrefs(context: Context): String {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context).all

        var out = ""
        for (s in prefs){
            out += "CB:${s.value!!::class.simpleName}-${s.key}-${s.value}\n"
        }
        return out
    }



    private fun backupSteps(context: Context): String{
        val dbHandler = MyDBHandler(context, null, null, 1)
        val steps = dbHandler.getRawSteps()

        var out = ""
        for (step in steps){
            out += "AB:${step.year}-${step.month}-${step.day}-${step.hour}-${step.steps}-${step.calories}\n"
        }
        return out
    }

    private fun backupSleep(context: Context): String{
        val dbHandler = MyDBHandler(context, null, null, 1)
        val sleep = dbHandler.getSleepData()

        var out = ""
        for (s in sleep){
            out += "AC:${s.year}-${s.month}-${s.day}-${s.hour}-${s.minute}-${s.type}-${s.duration}\n"
        }
        return out
    }

    private fun backupHeart(context: Context): String{
        val dbHandler = MyDBHandler(context, null, null, 1)
        val data = dbHandler.getRawHeart()

        var out = ""
        for (s in data){
            out += "AD:${s.year}-${s.month}-${s.day}-${s.hour}-${s.minute}-${s.bpm}-${s.type}\n"
        }
        return out
    }

    private fun backupPressure(context: Context): String{
        val dbHandler = MyDBHandler(context, null, null, 1)
        val data = dbHandler.getRawBP()

        var out = ""
        for (s in data){

            out += "AE:${s.year}-${s.month}-${s.day}-${s.hour}-${s.minute}-${s.bpHigh}-${s.bpLow}-${s.type}\n"
        }
        return out
    }

     private fun backupOxygen(context: Context): String{
        val dbHandler = MyDBHandler(context, null, null, 1)
        val data = dbHandler.getRawSP()

        var out = ""
        for (s in data){

            out += "AF:${s.year}-${s.month}-${s.day}-${s.hour}-${s.minute}-${s.sp02}-${s.type}\n"
        }
        return out
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

    private fun requestWriteStoragePermission(code: Int){
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), code
        )
    }

    private fun checkStoragePermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ActivityCompat.checkSelfPermission( this@BackupRestoreActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        Timber.w("Activity result: Request $requestCode")
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
            if (requestCode == MainActivity.PERMISSION_STORAGE + 2 ) {
                //backupAll(this)
            }
            if (requestCode == MainActivity.PERMISSION_STORAGE){
                checkBackups()
            }

        }
    }

    class RestoreAdapter(restore: ArrayList<String>, private val listener: (Int, String) -> Unit): RecyclerView.Adapter<RestoreAdapter.DataHolder>() {

        private val data = mutableListOf<String>()

        init {
            data.addAll(restore)
        }
        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): DataHolder {
            val inflater = LayoutInflater.from(p0.context)
            val view = inflater.inflate(R.layout.backup_item, p0, false)
            return DataHolder(view)
        }

        override fun getItemCount(): Int = data.size

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onBindViewHolder(p0: DataHolder, p1: Int) {
            p0.bind(data[p1], listener)
        }
        fun update(data: ArrayList<String>){
            this.data.clear()
            this.data.addAll(data)
            this.notifyDataSetChanged()
        }

        class DataHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
            private val mName: TextView = itemView.findViewById(R.id.backupName)
            private val mDel: ImageView = itemView.findViewById(R.id.backupDelete)

            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            fun bind (data: String, listener: (Int, String) -> Unit){

                val time = data.replace(".txt", "").toLongOrNull()
                val file = if (time != null){
                    val cal = Calendar.getInstance(Locale.getDefault())
                    cal.timeInMillis = time
                    cal.time.toString()
                } else {
                    data.replace(".txt", "")
                }

                mName.text = file
                mDel.setOnClickListener {
                    listener(0, data)
                }
                mName.setOnClickListener {
                    listener(1, data)
                }

            }

        }

    }
}