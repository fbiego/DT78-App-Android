package com.fbiego.dt78

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.MediaStore
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.db.williamchart.data.DonutDataPoint
import com.db.williamchart.data.Scale
import com.db.williamchart.view.DonutChartView
import com.fbiego.dt78.app.ConnectionListener
import com.fbiego.dt78.app.ConnectionReceiver
import com.fbiego.dt78.app.MainApplication
import com.fbiego.dt78.app.RootUtil
import com.fbiego.dt78.data.*
import com.hadiidbouk.charts.BarData
import com.hadiidbouk.charts.ChartProgressBar
import kotlinx.android.synthetic.main.activity_main.*
import no.nordicsemi.android.ble.data.Data
import timber.log.Timber
import java.lang.reflect.Method
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt
import com.fbiego.dt78.app.ForegroundService as FG
import com.fbiego.dt78.app.SettingsActivity as ST


class MainActivity : AppCompatActivity(), ConnectionListener {

    private lateinit var menu: Menu
    private var alertDialog: AlertDialog? = null

    private lateinit var timer: Timer
    private val noDelay = 500L
    private val duration = 1000L * 30
    private lateinit var btAdapter: BluetoothAdapter

    private val REQUEST_ENABLE_BT = 37
    private lateinit var pref: SharedPreferences



    private var sleepList = ArrayList<SleepData>()
    private var sleep = ArrayList<SleepData>()
    private var daysList = ArrayList<String>()
    private var daily = ArrayList<SleepData>()
    private var current = 0
    private var maxDay = 0
    private var start = 0
    private var end = 0




    companion object{

        const val PERMISSIONS_CONTACTS = 100
        const val PERMISSION_CONTACT = 101
        const val PERMISSION_SMS = 42
        const val PERMISSION_CALL = 52
        const val PERMISSION_CALL_LOG = 54
        const val PERMISSION_STORAGE = 57
        const val PERMISSION_CAMERA = 58

        var target = 1000


        var bat: TextView? = null
        var watch: TextView? = null
        var bt: ImageView? = null
        var per: ImageView? = null
        var step: TextView? = null
        var cal: TextView? = null
        var dis: TextView? = null
        var progress: TextView? = null
        var donut: DonutChartView? = null
        var contxt: Context? = null


    }

    private fun barChart(){
        val dbHandler = MyDBHandler(this, null, null, 1)
        val todaySteps = dbHandler.getStepsToday()
        var max = 2000
        val data = ArrayList<Pair<String, Float>>()

        todaySteps.forEach {
            data.add(Pair("",
                (it.steps).toFloat(),))
            if (it.steps > max){
                max = it.steps
            }
        }
//        val mChart = findViewById<ChartProgressBar>(R.id.ChartProgressBar)
//        mChart.setMaxValue(max.toFloat())
//        mChart.setDataList(dataList)
//        mChart.build()

        barChart.fillColor = this.getColorFromAttr(R.attr.colorIcons)
        //barChart.gradientFillColors = intArrayOf(this.getColorFromAttr(R.attr.colorIcons), ContextCompat.getColor(this, R.color.colorTransparent))
        barChart.scale = Scale((max * -0.03).toFloat(), max.toFloat()+500)
        //barChart.labelsFormatter = { "${it.roundToInt()}" }
        barChart.animate(data)


    }

    override fun onNightModeChanged(mode: Int) {
        super.onNightModeChanged(mode)
        Timber.w("Night mode changed to $mode")

    }

    private fun updateDonut(context: Context, current: Int, target: Int, update: Boolean){
        val plot = ((current.toFloat()/target)*100)
        val donutData = arrayListOf(plot)

        //donutChart.
        Timber.w("Current: $current, Plot: $plot")
        progress?.text = "${plot.toInt()}%"
        donut?.donutColors = intArrayOf(contxt?.getColorFromAttr(R.attr.colorIcons)!!)

        if (update){
            donut?.show(donutData)
        } else {
            donut?.animate(donutData)
        }




    }

    override fun onCreate(savedInstanceState: Bundle?) {
        pref = PreferenceManager.getDefaultSharedPreferences(this)
        setTheme(myTheme(pref.getInt(ST.PREF_ACCENT, 0)))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btAdapter = BluetoothAdapter.getDefaultAdapter()
        if (!btAdapter.isEnabled){
            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BT)
        }

        val theme = pref.getInt(ST.PREF_THEME, AppCompatDelegate.MODE_NIGHT_NO)

        if (theme != AppCompatDelegate.getDefaultNightMode()){
            AppCompatDelegate.setDefaultNightMode(theme)
        }

        bat = findViewById(R.id.battery)
        watch = findViewById(R.id.watchName)
        bt = findViewById(R.id.connect)
        per = findViewById(R.id.batIcon)
        step = findViewById(R.id.stepsText)
        cal = findViewById(R.id.caloriesText)
        dis = findViewById(R.id.distanceText)
        progress = findViewById(R.id.targetSteps)
        donut = findViewById(R.id.donutChart)
        contxt = this

        ConnectionReceiver.bindListener(this)

        shakeCamera.setOnLongClickListener {
            val cur = pref.getBoolean(ST.PREF_CAMERA, false)
            pref.edit().putBoolean(ST.PREF_CAMERA, !cur).apply()
            setCamera(!cur, RootUtil.isDeviceRooted)
            if (!cur && !RootUtil.isDeviceRooted){
                Toast.makeText(this, R.string.not_rooted, Toast.LENGTH_SHORT).show()
                pref.edit().putBoolean(ST.PREF_CAMERA, false).apply()
            }
            true
        }

        Timber.d("Main Activity onCreate ")



    }

    private fun appsList(){

        val enabled = NotificationManagerCompat.getEnabledListenerPackages(this).contains(
            BuildConfig.APPLICATION_ID
        )
        Timber.d("Notification Listener Enabled $enabled")

        if (alertDialog == null || !(alertDialog!!.isShowing)) {
            if (enabled) {

                startActivity(Intent(this, AppsActivity::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

            } else {
                val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                    .setTitle(R.string.choose_app)
                    .setMessage(R.string.grant_notification)
                    .setNegativeButton(android.R.string.no, null)
                    .setPositiveButton(android.R.string.yes) { _: DialogInterface?, _: Int ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                        } else {
                            startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
                        }
                    }
                    .setOnDismissListener { alertDialog = null }
                    .setOnCancelListener { alertDialog = null }
                alertDialog = builder.create()
                alertDialog!!.show()

            }

        }
    }

    fun showDialog(context: Context){
        val alert = AlertDialog.Builder(context)
        alert.setTitle(context.getString(R.string.self_test_fail))
        alert.setMessage(context.getString(R.string.re_enable))
        alert.setPositiveButton(android.R.string.yes) { _: DialogInterface?, _: Int ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            } else {
                context.startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
            }
        }
        alert.setNegativeButton(android.R.string.no, null)
        alert.show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        this.menu = menu
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_prefs -> {
                startActivity(Intent(this, ST::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                true
            }
            R.id.menu_item_kill -> {
                ConnectionReceiver().notifyStatus(false)
                Toast.makeText(this, R.string.stop_service, Toast.LENGTH_SHORT).show()
                stopService(Intent(this, FG::class.java))
                item.isVisible = false
                menu.findItem(R.id.menu_item_start)?.isVisible = true
                true
            }
            R.id.menu_item_start -> {
                item.isVisible = false
                menu.findItem(R.id.menu_item_kill)?.isVisible = true

                val remoteMacAddress = pref.getString(
                    ST.PREF_KEY_REMOTE_MAC_ADDRESS,
                    FG.VESPA_DEVICE_ADDRESS
                )
                //val id = pref.getInt(ST.PREF_WATCH_ID, UNKNOWN)
                if (btAdapter.isEnabled) {
                    if (remoteMacAddress != FG.VESPA_DEVICE_ADDRESS) {

//                        if (isConnected(btAdapter.getRemoteDevice(remoteMacAddress)) && id == UNKNOWN){
//                            Toast.makeText(this, "Device already connected", Toast.LENGTH_SHORT).show()
//                            deviceConnected()
//                        } else {
//                            Toast.makeText(this, R.string.start_service, Toast.LENGTH_SHORT).show()
//                            startService(Intent(this, FG::class.java))
//                        }

                        Toast.makeText(this, R.string.start_service, Toast.LENGTH_SHORT).show()
                        startService(Intent(this, FG::class.java))
                    }

                } else {
                    //enable bt
                    val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BT)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }


    override fun onStart() {
        super.onStart()

        Timber.d("Mainactivity on start")
        val remoteMacAddress = pref.getString(
            ST.PREF_KEY_REMOTE_MAC_ADDRESS,
            FG.VESPA_DEVICE_ADDRESS
        )
        val id = pref.getInt(ST.PREF_WATCH_ID, UNKNOWN)
        if (btAdapter.isEnabled){

            if (remoteMacAddress != FG.VESPA_DEVICE_ADDRESS){
//                if (isConnected(btAdapter.getRemoteDevice(remoteMacAddress)) && id == UNKNOWN){
//                    Toast.makeText(this, "Device already connected", Toast.LENGTH_SHORT).show()
//                    deviceConnected()
//                } else {
//                    startService(Intent(this, FG::class.java))
//                }
                startService(Intent(this, FG::class.java))

            }

        }
        if (intent.hasExtra("RING")){

            Timber.w("Stopping service due to ring")
            stopService(Intent(this, FG::class.java))
            intent.removeExtra("RING")
        }

        Timber.w("onStart")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, result: Intent?) {
        super.onActivityResult(requestCode, resultCode, result)
        if (requestCode == REQUEST_ENABLE_BT) {  // Match the request code
            if (resultCode == Activity.RESULT_OK) {
                val remoteMacAddress = pref.getString(
                    ST.PREF_KEY_REMOTE_MAC_ADDRESS,
                    FG.VESPA_DEVICE_ADDRESS
                )
                //val id = pref.getInt(ST.PREF_WATCH_ID, UNKNOWN)
                if (remoteMacAddress != FG.VESPA_DEVICE_ADDRESS){
                    //Toast.makeText(this, "Bluetooth Turned on", Toast.LENGTH_LONG).show()
//                    if (isConnected(btAdapter.getRemoteDevice(remoteMacAddress)) && id == UNKNOWN){
//                        Toast.makeText(this, "Device already connected", Toast.LENGTH_SHORT).show()
//                        deviceConnected()
//                    } else {
//                        startService(Intent(this, FG::class.java))
//                    }
                    startService(Intent(this, FG::class.java))
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onResume() {
        super.onResume()
        Timber.d("onResume")

        setIcon(FG.connected)


        if (FG.camera){
            FG().shakeCamera()
        }

        if (FG.connected){
            watch?.text = FG.deviceName
        }
        checkPermission()
        checkOptimization()
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        val tp = pref.getInt(ST.PREF_WATCH_ID, -1)
        title = Watch(tp).name
        checkEnabled(tp)
        FG.lst_sync = pref.getLong(ST.PREF_SYNC, System.currentTimeMillis() - 604800000)
        if (System.currentTimeMillis() > FG.lst_sync + (3600000 * 3) && tp != ESP32 && tp != UNKNOWN){
            if (FG().syncData()){
                Toast.makeText(this, R.string.sync_watch, Toast.LENGTH_SHORT).show()
                val editor: SharedPreferences.Editor = pref.edit()
                val time = System.currentTimeMillis()
                editor.putLong(ST.PREF_SYNC, time)
                editor.apply()
                editor.commit()
            }
//            else  {
//                Toast.makeText(this, R.string.unable_sync, Toast.LENGTH_SHORT).show()
//            }

        }
        setCamera(pref.getBoolean(ST.PREF_CAMERA, false), RootUtil.isDeviceRooted)

        bat?.text = "${FG.bat}%"
        watch?.text = FG.deviceName
        per?.setImageResource(battery(FG.bat, 1))
        per?.imageTintList = ColorStateList.valueOf(color(FG.bat, this))



        appsNo.text = MainApplication.sharedPrefs.getStringSet(
            MainApplication.PREFS_KEY_ALLOWED_PACKAGES,
            mutableSetOf()
        )?.size.toString()


        val dbHandler = MyDBHandler(this, null, null, 1)
        target = dbHandler.getUser().target
        val stepsCal = dbHandler.getStepCalToday()
        val stepSize = dbHandler.getUser().step

        updateDash(dbHandler.getHeartToday(), dbHandler.getBpToday()[0], dbHandler.getBpToday()[1], dbHandler.getSp02Today())

        step?.text = stepsCal.steps.toString()
        cal?.text = "${stepsCal.calories} "+this.resources.getString(R.string.kcal)
        dis?.text = distance(stepsCal.steps * stepSize, FG.unit != 0, this)

        updateDonut(this, stepsCal.steps, target, false)

        barChart()



        val timerTask = object: TimerTask(){
            override fun run() {
                if (tp != ESP32 && tp != UNKNOWN) {
                    FG().stepRQ()
                    Timber.w("Timer Task: Steps requested")
                }

            }
        }


        dailySleep()
        val cal = Calendar.getInstance(Locale.getDefault())
        val today = String.format(
            "%02d-%02d-%04d", cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1, cal.get(
                Calendar.YEAR
            )
        )
        Timber.w(today)
        sleepList.clear()
        sleepList = getSleepDay(today)

//        sleepList.add(SleepData(21, 2, 11, 1, 5, 1, 69))
//        sleepList.add(SleepData(21, 2, 11, 5, 5, 2, 354))

        textSleep.text = if (sleepList.isNotEmpty()){
            val lightS = sleepList.sumBy { if (it.type == 1) it.duration else 0 }
            val deepS = sleepList.sumBy { if (it.type == 2) it.duration else 0 }

            sleepDonut.donutColors= intArrayOf(this.getColorFromAttr(R.attr.colorButtonEnabled), this.getColorFromAttr(R.attr.colorIcons))
            sleepDonut.animate(arrayListOf(((lightS.toFloat()/(lightS+deepS))*100), ((deepS.toFloat()/(lightS+deepS))*100)))
            time(lightS + deepS)+"\n"+this.getString(R.string.sleep_txt)
        } else {
            sleepDonut.donutColors= intArrayOf(this.getColorFromAttr(R.attr.colorButtonEnabled), this.getColorFromAttr(R.attr.colorIcons))
            sleepDonut.animate(arrayListOf(0f, 0f))
            "0h 0m\n"+this.getString(R.string.sleep_txt)
        }

        quietActive.visibility = if (isQuietA(dbHandler.getSet(2))) View.VISIBLE else View.GONE

//        if (::menu.isInitialized){
//            menu.findItem(R.id.menu_item_kill)?.isVisible = FG.serviceRunning
//            menu.findItem(R.id.menu_item_start)?.isVisible = !FG.serviceRunning
//        }


        timer = Timer()
        timer.schedule(timerTask, noDelay, duration)
    }


    private fun updateDash(hrm: Int, bpH: Int, bpL: Int, sp02: Int){
        textHrm.text = "$hrm\n"+this.getString(R.string.bpm)
        textBp.text = "$bpH/$bpL\n"+this.getString(R.string.mmHg)
        textSp.text = "$sp02%\nO²"



        hrmDonut.donutColors = intArrayOf(this.getColorFromAttr(R.attr.colorIcons))
        hrmDonut.animate(arrayListOf(map(hrm, 40, 100).toFloat()))
        bpDonut.donutColors = intArrayOf(this.getColorFromAttr(R.attr.colorIcons), this.getColorFromAttr(R.attr.colorButtonEnabled))
        bpDonut.animate(arrayListOf((bpH.toFloat()/(bpH+bpL)*100), (bpL.toFloat()/(bpH+bpL)*100)))
        spDonut.donutColors = intArrayOf(this.getColorFromAttr(R.attr.colorIcons))
        spDonut.animate(arrayListOf(map(sp02, 80, 100).toFloat()))

    }


    override fun onPause() {
        super.onPause()

        timer.cancel()
        timer.purge()
    }

//    override fun onDestroy() {
//        super.onDestroy()
//        // stop the service
//
////        val isRunAsAService = PreferenceManager.getDefaultSharedPreferences(this)
////                .getBoolean(ST.PREF_KEY_RUN_AS_A_SERVICE, true)
////        Timber.w("onDestroy {isService=$isRunAsAService}")
////        if (!isRunAsAService) {
////            stopService(Intent(this, FG::class.java))
////        }
//    }


    private fun checkEnabled(id: Int){
        if (id == ESP32){
            cardInfo.isClickable = false
            layoutSteps.isClickable = false
            hrmDonut.isClickable = false
            bpDonut.isClickable = false
            spDonut.isClickable = false
            userInfo.isClickable = false
            reminder.isClickable = false
            findWatch.isClickable = false
            shakeCamera.isClickable = false
            sleepDonut.isClickable = false
            layoutSteps.backgroundTintList = ColorStateList.valueOf(this.getColorFromAttr(R.attr.colorCardBackgroundDark))
            linearLayout.backgroundTintList = ColorStateList.valueOf(this.getColorFromAttr(R.attr.colorCardBackgroundDark))
            userInfo.backgroundTintList = ColorStateList.valueOf(this.getColorFromAttr(R.attr.colorCardBackgroundDark))
            reminder.backgroundTintList = ColorStateList.valueOf(this.getColorFromAttr(R.attr.colorCardBackgroundDark))
            findWatch.backgroundTintList = ColorStateList.valueOf(this.getColorFromAttr(R.attr.colorCardBackgroundDark))
            shakeCamera.backgroundTintList = ColorStateList.valueOf(this.getColorFromAttr(R.attr.colorCardBackgroundDark))
        } else {
            cardInfo.isClickable = true
            layoutSteps.isClickable = true
            hrmDonut.isClickable = true
            bpDonut.isClickable = true
            spDonut.isClickable = true
            userInfo.isClickable = true
            reminder.isClickable = true
            findWatch.isClickable = true
            shakeCamera.isClickable = true
            sleepDonut.isClickable = true
            layoutSteps.setCardBackgroundColor(ContextCompat.getColor(this, R.color.colorCardBackground))
            linearLayout.setCardBackgroundColor(ContextCompat.getColor(this, R.color.colorCardBackground))
            userInfo.setCardBackgroundColor(ContextCompat.getColor(this, R.color.colorCardBackground))
            reminder.setCardBackgroundColor(ContextCompat.getColor(this, R.color.colorCardBackground))
            findWatch.setCardBackgroundColor(ContextCompat.getColor(this, R.color.colorCardBackground))
            shakeCamera.setCardBackgroundColor(ContextCompat.getColor(this, R.color.colorCardBackground))
        }
    }

    private fun testNotify(){
        val builder = AlertDialog.Builder(this)
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        val tp = pref.getInt(ST.PREF_WATCH_ID, -1)
        builder.setTitle(R.string.test_notification)
        builder.setMessage(R.string.test_notification_desc)
        val inflater = layoutInflater
        val dialogInflater = inflater.inflate(R.layout.notify_layout, null)
        val editText = dialogInflater.findViewById<EditText>(R.id.editText)
        val spinner = dialogInflater.findViewById<Spinner>(R.id.spinner)
        val adapter = NotifyAdapter(this, true, Watch(tp).iconSet)
        spinner.adapter = adapter

        builder.setView(dialogInflater)
        builder.setPositiveButton(R.string.send){ _, _ ->
            if (!FG().testNotification(
                    editText.text.toString(),
                    spinner.selectedItem as Int,
                    applicationContext
                )){
                Toast.makeText(this, R.string.not_connect, Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton(R.string.cancel){ _, _ ->

        }
        builder.setNeutralButton(R.string.self_test){ _, _ ->
            FG().selfTest(this, spinner.selectedItem as Int)
        }
        builder.show()

    }


    private fun setCamera(external: Boolean, rooted: Boolean){
        if (external && rooted){
            cameraIcon.setImageResource(R.drawable.ic_camera_ext)
            cameraText.setText(R.string.ext_camera)
        } else {
            cameraIcon.setImageResource(R.drawable.ic_camera)
            cameraText.setText(R.string.camera)
        }
    }



    @SuppressLint("SetTextI18n")
    fun onDataReceived(data: Data, context: Context , stepsZ: Int){


        run {}

            Timber.w("Data received")
            if (data.size() == 8) {
                if (data.getByte(4) == (0x91).toByte()) {
                    FG.bat = data.getByte(7)!!.toPInt()
                    Timber.w("Battery: ${FG.bat}%")
                    bat?.text = "${FG.bat}%"
                    watch?.text = FG.deviceName
                    per?.setImageResource(battery(FG.bat, 1))
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        per?.imageTintList = ColorStateList.valueOf(color(FG.bat, context))
                    }
                }
            }

            if (data.size() == 17 && data.getByte(4) == (0x51).toByte() && data.getByte(5) == (0x08).toByte()) {
                val steps = ((data.getByte(7)!!.toPInt() * 256) + (data.getByte(8)!!).toPInt())
                step?.text = steps.toString()
                val cl = (((data.getByte(10)!!).toPInt() * 256) + (data.getByte(11)!!).toPInt())
                cal?.text = "$cl " + context.resources.getString(R.string.kcal)
                dis?.text = distance(steps * stepsZ, FG.unit != 0, context)

                //updateDonut(this@MainActivity, steps, target, true)

            }



    }

    override fun onConnectionChanged(state: Boolean) {

        runOnUiThread{
            FG.connected = state
            setIcon(FG.connected)
            if (FG.connected){
                watch?.text = FG.deviceName
            }


            val pref = PreferenceManager.getDefaultSharedPreferences(this)
            val id = pref.getInt(ST.PREF_WATCH_ID, -1)
            FG.lst_sync = pref.getLong(ST.PREF_SYNC, System.currentTimeMillis() - 604800000)
            if (System.currentTimeMillis() > FG.lst_sync + (3600000 * 3) && id != ESP32 ){
                if (FG().syncData()){
                    Toast.makeText(this, R.string.sync_watch, Toast.LENGTH_SHORT).show()
                    val editor: SharedPreferences.Editor = pref.edit()
                    val time = System.currentTimeMillis()
                    editor.putLong(ST.PREF_SYNC, time)
                    editor.apply()
                    editor.commit()
                }
            }
        }


    }

    fun onClick(view: View){

        when (view.id) {
            R.id.cardInfo -> {
                if (FG().syncData()) {
                    Toast.makeText(this, R.string.sync_watch, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, R.string.not_connect, Toast.LENGTH_SHORT).show()
                }
            }
            R.id.hrmDonut -> {
                HealthActivity.viewH = 0
                startActivity(Intent(this, HealthActivity::class.javaObjectType))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            R.id.bpDonut -> {
                HealthActivity.viewH = 1
                startActivity(Intent(this, HealthActivity::class.javaObjectType))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            R.id.spDonut -> {
                HealthActivity.viewH = 2
                startActivity(Intent(this, HealthActivity::class.javaObjectType))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            R.id.notificationApps -> {
                appsList()
            }
            R.id.reminder -> {
                startActivity(Intent(this, ReminderActivity::class.javaObjectType))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            R.id.layoutSteps  -> {
                startActivity(Intent(this, StepsActivity::class.javaObjectType))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            R.id.barChart  -> {
                startActivity(Intent(this, StepsActivity::class.javaObjectType))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            R.id.sleepDonut -> {
                startActivity(Intent(this, SleepActivity::class.javaObjectType))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            R.id.userInfo -> {
                startActivity(Intent(this, UserActivity::class.javaObjectType))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            R.id.settings -> {
                startActivity(Intent(this, SettingsWatchActivity::class.javaObjectType))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            R.id.findWatch -> {
                if (FG().findWatch()) {
                    Toast.makeText(this, R.string.find_watch, Toast.LENGTH_SHORT).show()

                } else {
                    Toast.makeText(this, R.string.not_connect, Toast.LENGTH_SHORT).show()
                }
            }
            R.id.shakeCamera -> {
//                if (FG().shakeCamera()) {
//                    //start camera
//                    val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
//                    this.startActivity(intent)
//                } else {
//                    Toast.makeText(this, R.string.not_connect, Toast.LENGTH_SHORT).show()
//                }
                if (pref.getBoolean(ST.PREF_CAMERA, false) && RootUtil.isDeviceRooted){
                    if (FG().shakeCamera()) {
                        //start camera
                        val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
                        this.startActivity(intent)
                    } else {
                        Toast.makeText(this, R.string.not_connect, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    if (startCamera()){
                        startActivity(Intent(this, CameraActivity::class.javaObjectType))
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    }
                }

//                if (RootUtil.isDeviceRooted){
//                    val dialog = AlertDialog.Builder(this)
//                    dialog.setTitle(title)
//                        .setMessage("Choose camera")
//                        .setPositiveButton("In-App"){_, _ ->
//                            if (startCamera()){
//                                startActivity(Intent(this, CameraActivity::class.javaObjectType))
//                                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
//                            }
//
//                        }
//                        .setNeutralButton("External"){_, _ ->
//                            if (FG().shakeCamera()) {
//                                //start camera
//                                val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
//                                this.startActivity(intent)
//                            } else {
//                                Toast.makeText(this, R.string.not_connect, Toast.LENGTH_SHORT).show()
//                            }
//                        }
//                        .show()
//                } else {
//                    if (startCamera()){
//                        startActivity(Intent(this, CameraActivity::class.javaObjectType))
//                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
//                    }
//
//                }



            }
            R.id.testNotify -> {
                testNotify()
            }

        }


    }

    private fun setIcon(state: Boolean){
        if (state){
            bt?.setImageResource(R.drawable.ic_bt)
            bt?.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this@MainActivity, R.color.colorBluetooth))
        } else {
            bt?.setImageResource(R.drawable.ic_disc)
            bt?.imageTintList = ColorStateList.valueOf(Color.DKGRAY)
            per?.imageTintList = ColorStateList.valueOf(Color.DKGRAY)
        }
    }

//    private fun deviceConnected(){
//        val builder = AlertDialog.Builder(this)
//        builder.setTitle("First time connect")
//        builder.setMessage("It is recommended to disconnect the device from bluetooth settings first before connecting the app")
//        builder.setPositiveButton(R.string.bt_settings){_, _ ->
//            val intentOpenBluetoothSettings = Intent()
//            intentOpenBluetoothSettings.action = Settings.ACTION_BLUETOOTH_SETTINGS
//            startActivity(intentOpenBluetoothSettings)
//        }
//        builder.setNegativeButton(R.string.cancel, null)
//        builder.show()
//    }

    @SuppressLint("BatteryLife")
    private fun checkOptimization(){
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        val optimize = pref.getBoolean(ST.PREF_OPTIMIZE, true)


        val packageName = this.packageName
        val pm = this.getSystemService(Context.POWER_SERVICE) as PowerManager

        if (optimize && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !pm.isIgnoringBatteryOptimizations(
                packageName
            )){
            val builder = AlertDialog.Builder(this)
            builder.setTitle(R.string.ignore_optimize)
            builder.setMessage(R.string.request_optimize)
            val editor: SharedPreferences.Editor = pref.edit()
            builder.setNeutralButton(R.string.never_ask){ _, _ ->
                editor.putBoolean(ST.PREF_OPTIMIZE, false)
                editor.apply()
                editor.commit()
            }
            builder.setNegativeButton(android.R.string.no, null)
            builder.setPositiveButton(R.string.yes){ _, _ ->
                val intent = Intent()
                intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
            builder.show()
        }
    }

//    private fun isConnected(device: BluetoothDevice): Boolean {
//        return try {
//            val m: Method = device.javaClass.getMethod("isConnected")
//            m.invoke(device) as Boolean
//        } catch (e: Exception) {
//            throw IllegalStateException(e)
//        }
//    }
//
//    private fun getProfiles(device: BluetoothDevice){
//        val manager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
//        val connectedDevices = manager.getConnectedDevices(BluetoothProfile.GATT)
//        connectedDevices.contains(device)
//    }

    private fun checkPermission(){
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        val remoteMacAddress = pref.getString(
            ST.PREF_KEY_REMOTE_MAC_ADDRESS,
            FG.VESPA_DEVICE_ADDRESS
        )
        val later = pref.getBoolean("later", false)
        if (remoteMacAddress == "00:00:00:00:00:00" && !later){
            val builder = AlertDialog.Builder(this)
            builder.setTitle(R.string.mac_addr)
            builder.setMessage(R.string.setup_desc)
            val editor: SharedPreferences.Editor = pref.edit()
            builder.setNegativeButton(R.string.later){ _, _ ->
                editor.putBoolean("later", true)
                editor.apply()
                editor.commit()
            }
            builder.setPositiveButton(R.string.setup_now){ _, _ ->
                startActivity(Intent(this, ST::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            builder.show()
        }



    }

    //sleep
    private fun dailySleep(){
        val dbHandler = MyDBHandler(this, null, null, 1)
        val slp = dbHandler.getSleepData()
        val current = dbHandler.getSet(1)
        start = current[1]*100 + current[2]
        end = current[3]*100 +  current[4]


        sleep.clear()
        sleep = slp

        val qr = ArrayList<String>()

        slp.forEach {

            if (start > end) {
                if (it.hour * 100 + it.minute >= start || it.hour * 100 + it.minute <= end) {

                    if (it.hour * 100 + it.minute >= start){
                        val cal = Calendar.getInstance(Locale.getDefault())
                        cal.set(Calendar.DAY_OF_MONTH, it.day)
                        cal.set(Calendar.MONTH, it.month - 1)
                        cal.set(Calendar.YEAR, it.year + 2000)
                        cal.add(Calendar.DATE, 1)
                        val el = String.format(
                            "%02d-%02d-20%02d", cal.get(Calendar.DAY_OF_MONTH), cal.get(
                                Calendar.MONTH
                            ) + 1, cal.get(Calendar.YEAR) - 2000
                        )
                        if (!qr.contains(el)){
                            qr.add(el)
                        }
                    } else {
                        val el = String.format("%02d-%02d-20%02d", it.day, it.month, it.year)
                        if (!qr.contains(el)){
                            qr.add(el)
                        }
                    }
                }
            } else {
                if (it.hour * 100 + it.minute in start..end){
                    val el = String.format("%02d-%02d-20%02d", it.day, it.month, it.year)
                    if (!qr.contains(el)){
                        qr.add(el)
                    }
                }
            }
        }

        daysList.clear()
        daysList = qr

    }

    private fun getSleepDay(day: String): ArrayList<SleepData>{
        val today = ArrayList<SleepData>()
        if (sleep.isNotEmpty()){
            sleep.forEach {
                if (start > end) {
                    if (it.hour * 100 + it.minute >= start || it.hour * 100 + it.minute <= end) {

                        if (it.hour * 100 + it.minute >= start){
                            val cal = Calendar.getInstance(Locale.getDefault())
                            cal.set(Calendar.DAY_OF_MONTH, it.day)
                            cal.set(Calendar.MONTH, it.month - 1)
                            cal.set(Calendar.YEAR, it.year + 2000)
                            cal.add(Calendar.DATE, 1)
                            val date = String.format(
                                "%02d-%02d-20%02d", cal.get(Calendar.DAY_OF_MONTH), cal.get(
                                    Calendar.MONTH
                                ) + 1, cal.get(Calendar.YEAR) - 2000
                            )
                            if (day == date){
                                today.add(it)
                            }
                        } else {
                            val date = String.format("%02d-%02d-20%02d", it.day, it.month, it.year)
                            if (day == date){
                                today.add(it)
                            }
                        }
                    }
                } else {
                    if (it.hour * 100 + it.minute in start..end){
                        val date = String.format("%02d-%02d-20%02d", it.day, it.month, it.year)
                        if (day == date){
                            today.add(it)
                        }
                    }
                }
            }
        }
        return SleepActivity().parseData(today)
    }

    private fun requestCameraPermissions(){
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.CAMERA), PERMISSION_CAMERA
        )
    }

    private fun requestWriteStoragePermission(){
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_STORAGE
        )
    }

    private fun checkStoragePermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ActivityCompat.checkSelfPermission( this@MainActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            return false
        }
        return true
    }

    private fun checkCameraPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ActivityCompat.checkSelfPermission( this@MainActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return false
        }
        return true
    }

    private fun startCamera(): Boolean{
        return if (!checkCameraPermission() && !checkStoragePermission()){
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA), 60
            )
            false
        } else if (!checkStoragePermission()){
            requestWriteStoragePermission()
            false
        } else if (!checkCameraPermission()){
            requestCameraPermissions()
            false
        } else {
            true
        }
    }

}
