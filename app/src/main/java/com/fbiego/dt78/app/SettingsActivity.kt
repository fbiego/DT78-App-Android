package com.fbiego.dt78.app

import android.annotation.SuppressLint
import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import androidx.preference.PreferenceManager
import android.provider.Settings
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import com.fbiego.dt78.AboutActivity
import com.fbiego.dt78.BuildConfig
import com.fbiego.dt78.R
import com.fbiego.dt78.data.*
import kotlinx.android.synthetic.main.activity_settings.*
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList
import com.fbiego.dt78.app.ForegroundService as FG
import kotlin.Boolean as Boolean1

/**
 *
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var deviceManager: DevicePolicyManager
    private lateinit var compName: ComponentName
    private lateinit var states: ArrayList<Boolean1?>
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var setPref: SharedPreferences

    companion object {
        const val PREF_KEY_REMOTE_MAC_ADDRESS = "pref_remote_mac_address"
        const val PREF_KEY_START_AT_BOOT = "pref_start_at_boot"
        const val PREF_NOTIFY_TYPE = "notify_level"
        const val PREF_RTW = "raise_to_wake"
        const val PREF_12H = "12_hour"
        const val PREF_HOURLY = "hourly_measure"
        const val PREF_UNITS = "units"
        const val PREF_LOCK = "lock"
        //const val PREF_WATCH_TYPE = "watch_type"
        const val PREF_WATCH_ID = "watch_id"
        const val PREF_ICONS = "ic_set"
        const val PREF_PRIORITY = "priority"
        const val PREF_CAPS = "capitalize"
        const val PREF_SOS = "sos_contact"
        const val PREF_CALL = "call_notify"
        const val PREF_SMS = "sms_notify"
        const val PREF_SNACK = "show_snack"
        const val PREF_BAT_IC = "bat_icon"
        const val PREF_FIND = "find_phone"
        const val PREF_PLUG = "plugged"
        const val PREF_UNPLUG = "unplugged"
        const val PREF_TODAY = "today"
        const val PREF_HEALTH = "health"
        const val PREF_HEX_ID = "identifier"
        const val PREF_CONVERT_EL = "convert_greek"
        const val PREF_VERSION = "watch_version"
        const val PREF_NEW_SEP = "separator"
        //val MAC_PATTERN = Pattern.compile("^([A-F0-9]{2}[:]?){5}[A-F0-9]{2}$")
        const val PREF_SYNC = "last_sync"
        const val PREF_DISPLAY_OFF = "esp_dsp_off"
        const val PREF_TIMEOUT = "esp_dsp_timeout"
        const val PREF_OPTIMIZE = "optimize_now"
        const val PREF_THEME = "dark_theme"
        const val PREF_ACCENT = "accent_color"
        const val PREF_LANG = "watch_language"
        const val PREF_SHOW_CALL = "show_call"
        const val PREF_SHOW_SMS = "show_sms"
        const val PREF_DND_UNLOCK = "unlocked"
        const val PREF_DND_SCREEN = "screen_on"
        const val PREF_CAMERA = "camera"
        const val PREF_BRIGHT = "brightness"
        lateinit var btAdapter: BluetoothAdapter

        var curLock = false

        internal const val RESULT_ENABLE = 1

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        setTheme(myTheme(pref.getInt(PREF_ACCENT, 0)))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        Timber.d("onCreate")
        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        setPref =  PreferenceManager.getDefaultSharedPreferences(this)

        deviceManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        compName = ComponentName(this, DeviceAdmin::class.java)

        btAdapter = BluetoothAdapter.getDefaultAdapter()

        val showSnack = setPref.getBoolean(PREF_SNACK, true)

        if (showSnack) {
            val snackBar = Snackbar.make(
                conView,
                getString(R.string.service_stopped),
                Snackbar.LENGTH_INDEFINITE
            )
            snackBar.setAction("Ok") {
                setPref.edit().putBoolean(PREF_SNACK, false).apply()
            }
            snackBar.show()
        }
    }



    @SuppressLint("BatteryLife")
    override fun onResume() {
        super.onResume()

        Timber.d("onResume")

        var mac = setPref.getString(PREF_KEY_REMOTE_MAC_ADDRESS, "00:00:00:00:00:00")

        curLock = setPref.getBoolean(PREF_LOCK, false)

        if (curLock){
            curLock = deviceManager.isAdminActive(compName)
        }
        if (deviceManager.isAdminActive(compName)){
            Timber.d("Admin active")
        } else {
            Timber.d("Admin disabled")
        }
        val items = arrayListOf("MIN", "LOW", "DEFAULT", "HIGH", "MAX")
        val notify = arrayListOf(getString(R.string.notify_off), getString(R.string.notify_only), getString(R.string.notify_ring))
        val item = arrayListOf("Dark", "Light", "Battery Saver", "System Default")
        val colors = arrayListOf("Green", "Red", "Purple", "Blue")
        var priority = setPref.getInt(PREF_PRIORITY, 0)
        var notifyLevel = setPref.getInt(PREF_NOTIFY_TYPE, 0)
        var icSet = setPref.getInt(PREF_ICONS, 1)
        val set = when(icSet){
            1 -> R.drawable.ic_bat40w
            2 -> R.drawable.ic_per40
            3 -> R.drawable.ic_bat40c
            else -> R.drawable.ic_bat40w
        }
        val theme = darkMode(AppCompatDelegate.getDefaultNightMode())

        val names = arrayListOf(getString(R.string.start_at_boot), getString(R.string.lock_phone), getString(R.string.notify_disconnect)+": "+notify[notifyLevel],
            getString(R.string.mac_addr)+": $mac",getString(R.string.bat_notifs),getString(R.string.accent)+": ${colors[setPref.getInt(PREF_ACCENT, 0)]}, "+getString(R.string.theme)+": $theme", getString(R.string.icon_set), getString(R.string.priority) + ": "+items[priority],
            getString(R.string.capitalize), getString(R.string.convert_greek), getString(R.string.error_log), getString(R.string.app_name)+ " v"+BuildConfig.VERSION_NAME)

        states = arrayListOf( setPref.getBoolean( PREF_KEY_START_AT_BOOT, false),
            curLock, null, null, null, null, null, null, setPref.getBoolean(PREF_CAPS, false),
            setPref.getBoolean(PREF_CONVERT_EL, Locale.getDefault().language == "el"), null, null)
        val icons = arrayListOf( R.drawable.ic_boot, R.drawable.ic_lock, R.drawable.ic_discon, R.drawable.ic_addr, R.drawable.ic_bat,
            R.drawable.ic_theme, set, R.drawable.ic_prt, R.drawable.ic_text, R.drawable.ic_font, R.drawable.ic_info, R.drawable.ic_app)

        val myUserList = UserListAdapter(this, icons, names, null, states)
        setListView.adapter = myUserList
        setListView.setOnItemClickListener { _, _, i, _ ->


            editor = setPref.edit()
            when (i){
                0 -> {
                    states[i] = !states[i]!!
                    editor.putBoolean(PREF_KEY_START_AT_BOOT, states[i]!!)
                }
                1 -> {

                    if (states[i]!!) {
                        deviceManager.removeActiveAdmin(compName)
                        states[i] = false
                        editor.putBoolean(PREF_LOCK, false)
                        Timber.d("Disabled")
                    } else {
                        Timber.d("Device admin intent")
                        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName)
                        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, R.string.enable_lock)
                        startActivityForResult(intent, RESULT_ENABLE)
                    }
                }
                2 -> {
                    val values = arrayListOf(0, 1, 2)
                    val outer = LayoutInflater.from(this).inflate(R.layout.wheel_view, null)
                    val wheelView = outer.findViewById<WheelView>(R.id.wheel_view_wv)
                    wheelView.setItems(notify)
                    wheelView.setSelection(values.indexOf(notifyLevel))

                    val dialog = AlertDialog.Builder(this)
                    dialog.setTitle(title)
                        .setView(outer)
                        .setPositiveButton(R.string.save){_, _ ->
                            notifyLevel = wheelView.selectedIndex
                            editor.putInt(PREF_NOTIFY_TYPE, notifyLevel)
                            names[i] = getString(R.string.notify_disconnect) + ": "+notify[notifyLevel]
                            myUserList.notifyDataSetChanged()
                            editor.apply()
                            editor.commit()
                        }
                        .show()
                }
                3 -> {
                    val alert = AlertDialog.Builder(this)
                    var alertDialog: AlertDialog? = null
                    alert.setTitle(R.string.mac_addr)
                    val devs: String
                    val btNames = ArrayList<String>()
                    val btAddress = ArrayList<String>()
                    if (btAdapter.isEnabled){
                        devs = getString(R.string.not_paired)
                        val devices: Set<BluetoothDevice> = btAdapter.bondedDevices
                        for (device in devices){
                            btNames.add(device.name)
                            btAddress.add(device.address)
//                            if (device.name.contains("Smart")) {
//                                //restricted mode
//                            }
                        }

                    } else {
                        devs = getString(R.string.turn_on_bt)
                    }
                    alert.setMessage(devs)
                    val layout = LinearLayout(this)
                    layout.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

                    val listView = ListView(this)
                    val myBTlist = BtListAdapter(this, btNames.toTypedArray(), btAddress.toTypedArray(), mac!!)
                    listView.adapter = myBTlist

                    listView.setOnItemClickListener { _, _, j, _ ->
                        //Toast.makeText(this, btAddress[j], Toast.LENGTH_SHORT).show()
                        editor.putString(PREF_KEY_REMOTE_MAC_ADDRESS, btAddress[j])
                        editor.apply()
                        editor.commit()
                        mac = btAddress[j]
                        names[i] = getString(R.string.mac_addr)+": $mac"
                        myUserList.notifyDataSetChanged()
                        alertDialog?.dismiss()
                    }
                    val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    params.setMargins(20, 20, 20, 20)
                    layout.addView(listView, params)
                    alert.setView(layout)
                    alert.setPositiveButton(R.string.bt_settings){_, _ ->
                        val intentOpenBluetoothSettings = Intent()
                        intentOpenBluetoothSettings.action = Settings.ACTION_BLUETOOTH_SETTINGS
                        startActivity(intentOpenBluetoothSettings)
                    }
                    alert.setNegativeButton(R.string.cancel){_, _ ->

                    }
                    alert.setOnCancelListener {
                        myUserList.notifyDataSetChanged()
                    }
                    alertDialog = alert.create()
                    alertDialog.show()
                }
                4 -> {
                    //battery
                    val alert = AlertDialog.Builder(this)
                    val pref = PreferenceManager.getDefaultSharedPreferences(this)
                    val tp = pref.getInt(PREF_WATCH_ID, -1)
                    alert.setTitle(R.string.battery)
                    val inflater = layoutInflater
                    val editLayout = inflater.inflate(R.layout.battery_layout, null)
                    val spinner = editLayout.findViewById<Spinner>(R.id.spinnerIcon)
                    val findPhone = editLayout.findViewById<CheckBox>(R.id.findPhone)
                    val plugged = editLayout.findViewById<CheckBox>(R.id.plugged)
                    val unplugged = editLayout.findViewById<CheckBox>(R.id.unplugged)
                    val adapter = NotifyAdapter(this, false, Watch(tp).iconSet)
                    spinner.adapter = adapter
                    var ic = setPref.getInt(PREF_BAT_IC, 0)
                    if (ic > 2){
                        ic -= 1
                    }
                    if (!Watch(tp).iconSet.contains(ic)){
                        ic = 0
                    }
                    spinner.setSelection(ic)  //icon int
                    findPhone.isChecked = setPref.getBoolean(PREF_FIND, true)
                    plugged.isChecked = setPref.getBoolean(PREF_PLUG, false)
                    unplugged.isChecked = setPref.getBoolean(PREF_UNPLUG, false)
                    alert.setView(editLayout)
                    alert.setPositiveButton(getString(R.string.save)){_, _ ->
                        editor.putInt(PREF_BAT_IC, spinner.selectedItem as Int)
                        editor.putBoolean(PREF_FIND, findPhone.isChecked)
                        editor.putBoolean(PREF_PLUG, plugged.isChecked)
                        editor.putBoolean(PREF_UNPLUG, unplugged.isChecked)
                        editor.apply()
                    }
                    alert.setNegativeButton(getString(R.string.cancel)){_, _ ->

                    }
                    val intent = Intent()
                    val packageName = this.packageName
                    val pm = this.getSystemService(Context.POWER_SERVICE) as PowerManager

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                        if (!pm.isIgnoringBatteryOptimizations(packageName)){
                            alert.setNeutralButton(R.string.optimize_app){_, _ ->
                                intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                                intent.data = Uri.parse("package:$packageName")
                                startActivity(intent)
                            }

                        }
                    }
                    alert.show()

                }
                5 -> {

                    val values = arrayListOf(AppCompatDelegate.MODE_NIGHT_YES, AppCompatDelegate.MODE_NIGHT_NO,
                        AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

                    val outer = LayoutInflater.from(this).inflate(R.layout.theme_chooser, null)
                    val accent = outer.findViewById<Spinner>(R.id.accentColor)
                    val mode = outer.findViewById<Spinner>(R.id.themeMode)
                    mode.adapter = ThemeAdapter(this, true)
                    accent.adapter = ThemeAdapter(this, false)
                    val prAcc = setPref.getInt(PREF_ACCENT, 0)
                    accent.setSelection(prAcc)
                    mode.setSelection(
                        if(values.contains(AppCompatDelegate.getDefaultNightMode())){
                            values.indexOf(AppCompatDelegate.getDefaultNightMode())
                        } else {
                            0
                        }
                    )


                    val dialog = AlertDialog.Builder(this)
                    dialog.setTitle(title)
                        .setMessage(R.string.choose_theme)
                        .setView(outer)
                        .setPositiveButton(R.string.save){_, _ ->

                            names[i] = getString(R.string.accent)+": ${colors[accent.selectedItemPosition]}, "+getString(R.string.theme)+": ${item[mode.selectedItemPosition]}"
                            editor.putInt(PREF_ACCENT, accent.selectedItemPosition)
                            editor.putInt(PREF_THEME, values[mode.selectedItemPosition])
                            editor.apply()
                            myUserList.notifyDataSetChanged()

                            if (AppCompatDelegate.getDefaultNightMode() == values[mode.selectedItemPosition]){
                                if (prAcc != accent.selectedItemPosition) {
                                    restartApp()
                                }
                            } else {
                                AppCompatDelegate.setDefaultNightMode(values[mode.selectedItemPosition])
                            }
                        }
                        .show()

                }
                6 -> {
                    icSet++
                    if (icSet > 3){
                        icSet = 1
                    }
                    editor.putInt(PREF_ICONS, icSet)
                    icons[i] =  when(icSet){
                        1 -> R.drawable.ic_bat40w
                        2 -> R.drawable.ic_per40
                        3 -> R.drawable.ic_bat40c
                        else -> R.drawable.ic_bat40w
                    }
                    FG.icSet = icSet

                }
                7 -> {
                    val values = arrayListOf(0, 1, 2, 3, 4)
                    val outer = LayoutInflater.from(this).inflate(R.layout.wheel_view, null)
                    val wheelView = outer.findViewById<WheelView>(R.id.wheel_view_wv)
                    wheelView.setItems(items)
                    wheelView.setSelection(values.indexOf(priority))

                    val dialog = AlertDialog.Builder(this)
                    dialog.setTitle(title)
                        .setView(outer)
                        .setPositiveButton(R.string.save){_, _ ->
                            priority = wheelView.selectedIndex
                            editor.putInt(PREF_PRIORITY, priority)
                            names[i] = getString(R.string.priority) + ": "+items[priority]
                            FG.prt = priority
                            myUserList.notifyDataSetChanged()
                            editor.apply()
                            editor.commit()
                        }
                        .show()
                }
                8 -> {
                    states[i] = !states[i]!!
                    editor.putBoolean(PREF_CAPS, states[i]!!)
                    FG.capitalize = states[i]!!
                }
                9 -> {
                    states[i] = !states[i]!!
                    editor.putBoolean(PREF_CONVERT_EL, states[i]!!)
                    FG.convert = states[i]!!
                }
                10 -> {
                    startActivity(Intent(this, ErrorLogActivity::class.java))
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }
                11 -> {
                    startActivity(Intent(this, AboutActivity::class.java))
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }

            }
            myUserList.notifyDataSetChanged()
            editor.apply()
            editor.commit()
        }


    }

    private fun restartApp(){
        val intent = baseContext.packageManager.getLaunchIntentForPackage(baseContext.packageName)!!
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RESULT_ENABLE -> {
                if (resultCode == Activity.RESULT_OK) {
                    states[2] = true
                    editor.putBoolean(PREF_LOCK, true)
                    editor.apply()
                    editor.commit()
                } else {
                    Toast.makeText(
                        applicationContext, R.string.not_enable,
                        Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }



    override fun onStart() {
        super.onStart()
        Timber.d("onStart")
        stopService(Intent(this, FG::class.java))
    }

    override fun onSupportNavigateUp(): kotlin.Boolean {
        onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    private fun darkMode(theme: Int): String{
        return when (theme){
            AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY -> "Battery Saver"
            AppCompatDelegate.MODE_NIGHT_YES -> "Dark"
            AppCompatDelegate.MODE_NIGHT_NO -> "Light"
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> "System Default"
            else -> "Not set"
        }
    }


}