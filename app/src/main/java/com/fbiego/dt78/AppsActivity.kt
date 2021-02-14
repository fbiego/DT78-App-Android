package com.fbiego.dt78

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import androidx.preference.PreferenceManager
import android.provider.Settings
import android.provider.Telephony
import androidx.core.app.ActivityCompat
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SwitchCompat
import com.fbiego.dt78.app.MainApplication
import com.fbiego.dt78.data.*
import kotlinx.android.synthetic.main.activity_apps.*
import timber.log.Timber
import com.fbiego.dt78.app.ForegroundService as FG
import com.fbiego.dt78.app.SettingsActivity as ST

class AppsActivity : AppCompatActivity() {
    
    private val appsList = ArrayList<AppsData>()
    private lateinit var appsAdapter : AppsAdapter

    private var appsPref = ArrayList<String>()
    private var appNames = ArrayList<String>()

    private var appChanel = ArrayList<Channel>()

    private var loadingApps = false

    private lateinit var pref : SharedPreferences
    
    companion object {
        lateinit var appsRecycler: RecyclerView
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        pref = PreferenceManager.getDefaultSharedPreferences(this)
        setTheme(myTheme(pref.getInt(ST.PREF_ACCENT, 0)))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apps)

        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)

        val tp = pref.getInt(ST.PREF_WATCH_ID, -1)
        appsAdapter = AppsAdapter(appsList, this@AppsActivity::appClicked, this, tp)


        appsRecycler = findViewById<View>(R.id.recyclerAppsList) as RecyclerView
        appsRecycler.layoutManager =
            LinearLayoutManager(this)
        val div = DividerItemDecoration(
            appsRecycler.context,
            LinearLayoutManager.VERTICAL
        )
        appsRecycler.addItemDecoration(div)
        appsRecycler.isNestedScrollingEnabled = false

        appsRecycler.apply {
            layoutManager =
                LinearLayoutManager(this@AppsActivity)
            adapter = appsAdapter
        }
        appsRecycler.itemAnimator?.changeDuration = 0


        val editor = pref.edit()

        smsState.setOnCheckedChangeListener { _, b ->
            if (b){
                if (smsCheck()){
                    FG.sms = b
                    editor.putBoolean(ST.PREF_SMS, b)
                    editor.apply()
                } else {
                    smsPermission()
                }
            } else {
                FG.sms = b
                editor.putBoolean(ST.PREF_SMS, b)
                editor.apply()
            }
        }

        callState.setOnCheckedChangeListener { _, b ->
            if (b){
                if (callCheck()){
                    FG.call = b
                    editor.putBoolean(ST.PREF_CALL, b).apply()
                } else {
                    callPermission()
                }
            } else {
                FG.call = b
                editor.putBoolean(ST.PREF_CALL, b).apply()
            }
        }

//        phoneChecked.setOnCheckedChangeListener { _, b ->
//            editor.putBoolean(ST.PREF_SHOW_PHONE, b).apply()
//        }
//
//        smsChecked.setOnCheckedChangeListener { _, b ->
//            editor.putBoolean(ST.PREF_SHOW_SMS, b).apply()
//        }
        dndCall.setOnClickListener {
            val state = pref.getBoolean(ST.PREF_SHOW_CALL, false)
            dndCall.imageTintList = dndColor(!state)
            FG.phoneShow = state
            editor.putBoolean(ST.PREF_SHOW_CALL, !state).apply()
        }
        dndSms.setOnClickListener {
            val state = pref.getBoolean(ST.PREF_SHOW_SMS, false)
            dndSms.imageTintList = dndColor(!state)
            FG.smsShow = state
            editor.putBoolean(ST.PREF_SHOW_SMS, !state).apply()
        }
        screenOn.setOnCheckedChangeListener { _, b ->
            dndIcon.imageTintList = dndColor(b || pref.getBoolean(ST.PREF_DND_UNLOCK, false))
            FG.unlocked = b
            editor.putBoolean(ST.PREF_DND_SCREEN, b).apply()
        }
        unlocked.setOnCheckedChangeListener { com, b ->
            var state = b
            if (b){
                if (isScreenLockSet(this)){
                    state = true
                } else if (com.isPressed) {
                    Toast.makeText(this, R.string.set_screen_lock, Toast.LENGTH_SHORT).show()
                    state = false
                    unlocked.isChecked = state
                }
            }
            dndIcon.imageTintList = dndColor(state || pref.getBoolean(ST.PREF_DND_SCREEN, false))
            FG.screenOn = state
            editor.putBoolean(ST.PREF_DND_UNLOCK, state).apply()

        }


    }

    override fun onResume() {
        super.onResume()

        if (!loadingApps){
            loadingLayout.visibility = View.VISIBLE
            loadingApps = true

            AsyncTask.THREAD_POOL_EXECUTOR.execute(AppsLoader(this@AppsActivity, pref.getBoolean(ST.PREF_NEW_SEP, false)))
        } else {
            loadingLayout.visibility = View.GONE
        }


        callState.isChecked = pref.getBoolean(ST.PREF_CALL, false) && callCheck()
        smsState.isChecked = pref.getBoolean(ST.PREF_SMS, false) && smsCheck()
        dndSms.imageTintList = dndColor(pref.getBoolean(ST.PREF_SHOW_SMS, false))
        dndCall.imageTintList = dndColor(pref.getBoolean(ST.PREF_SHOW_CALL, false))
        dndIcon.imageTintList = dndColor((pref.getBoolean(ST.PREF_DND_UNLOCK, false) && isScreenLockSet(this) )|| pref.getBoolean(ST.PREF_DND_SCREEN, false))
        unlocked.isChecked = (pref.getBoolean(ST.PREF_DND_UNLOCK, false) && isScreenLockSet(this))
        screenOn.isChecked = pref.getBoolean(ST.PREF_DND_SCREEN, false)

    }

    private fun dndColor(state: Boolean): ColorStateList{
        return if (state){
            ColorStateList.valueOf(this.getColorFromAttr(R.attr.colorIcons))
        } else {
            ColorStateList.valueOf(Color.DKGRAY)
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun appsLoaded(names: Array<String>, installedApps: MutableList<ApplicationInfo>, filter: ArrayList<Channel>, checkedItems: BooleanArray){

        runOnUiThread {
            val smsPackage = Telephony.Sms.getDefaultSmsPackage(this.applicationContext)
            appsList.clear()
            appChanel.clear()
            appsPref.clear()
            appNames.clear()

            names.forEach {
                val index = names.indexOf(it)
                if (installedApps[index].packageName != smsPackage || installedApps[index].packageName != application.packageName) {
                    appsList.add(
                        AppsData(
                            it,
                            installedApps[index].packageName,
                            filter[index].icon,
                            checkedItems[index],
                            true,
                            filter[index].filters
                        )
                    )
                    appNames.add(installedApps[index].packageName)
                    if (checkedItems[index]) {
                        appChanel.add(filter[index])
                        appsPref.add(filter[index].app)
                    }
                }
            }

            appsList.sortWith(compareBy({!it.enabled}, {it.name}))
            appsAdapter.swap(appsList)

            loadingApps = false
            loadingLayout.visibility = View.GONE
        }

    }

    
    private fun appClicked(appsData: AppsData, item: Int, icon: Int, state: Boolean){

        when (item){
            0 -> {
                if (appsData.enabled) {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle(appsData.name)

                    val exclude = ArrayList<String>()
                    exclude.addAll(appsData.filters)
                    builder.setMessage(appsData.packageName+"\n"+getString(R.string.filter_info))
                    val inflater = layoutInflater
                    val dialogInflater = inflater.inflate(R.layout.filter_layout, null)
                    val editText = dialogInflater.findViewById<EditText>(R.id.filterEdit)
                    val listView = dialogInflater.findViewById<ListView>(R.id.filterList)
                    val scrSwc = dialogInflater.findViewById<SwitchCompat>(R.id.scrSwitch)

                    scrSwc.isChecked = exclude.contains(sON)
                    if (scrSwc.isChecked) {
                        exclude.remove(sON)
                    }
                    val listAdapter = FilterAdapter(this, exclude)
                    listView.adapter = listAdapter
                    builder.setView(dialogInflater)

                    editText.setOnEditorActionListener{ _, i, _ ->
                        if (i == EditorInfo.IME_ACTION_GO){
                            val string = editText.text.toString()
                            if (!exclude.contains(string) && string.trim().isNotEmpty()){
                                exclude.add(editText.text.toString())
                                listAdapter.notifyDataSetChanged()
                            }
                            editText.text.clear()
                            true
                        } else {
                            false
                        }
                    }

                    listView.onItemLongClickListener = AdapterView.OnItemLongClickListener { _, _, p2, _ ->
                        exclude.removeAt(p2)
                        listAdapter.notifyDataSetChanged()
                        true
                    }

                    builder.setPositiveButton(R.string.save){_, _ ->
                        val string = editText.text.toString()
                        if (!exclude.contains(string) && string.trim().isNotEmpty()){
                            exclude.add(editText.text.toString())
                        }
                        if (scrSwc.isChecked) {exclude.add(sON)}
                        val index = appsPref.indexOf(appsData.packageName)
                        val index2 = appsList.indexOfFirst{it.packageName == appsData.packageName}
                        appChanel[index].filters = exclude
                        appsList[index2].filters = exclude
                        appsAdapter.notifyItemChanged(index2)
                    }
                    builder.setNegativeButton(R.string.cancel) { _, _ ->

                    }
                    builder.show()
                }
            }
            1 -> {

                //Toast.makeText(this, (if (state) "Enabled" else "Disabled") +" ${appsData.name}", Toast.LENGTH_SHORT).show()
                if (state){
                    appsPref.add(appsData.packageName)
                    appChanel.add(Channel(appsData.channel, appsData.packageName, appsData.filters))
                } else {
                    if (appsPref.contains(appsData.packageName)) {
                        val index = appsPref.indexOf(appsData.packageName)
                        //val index2 = appsList.indexOfFirst{it.packageName == appsData.packageName}
                        appsPref.removeAt(index)
                        appChanel.removeAt(index)
                    }
                }
            }
            2 -> {

                //Toast.makeText(this, "Clicked ${appsData.name} icon to: $icon", Toast.LENGTH_SHORT).show()
                if (appsPref.contains(appsData.packageName)){
                    val index = appsPref.indexOf(appsData.packageName)
                    appChanel[index].icon = icon
                }

            }
        }

    }

    override fun onPause() {
        super.onPause()

        val modifiedList = ArrayList<String>()

        if (appsPref.isNotEmpty()){
            appsPref.forEach {
                val index = appsPref.indexOf(it)
                modifiedList.add(appChanel[index].formatted())
                Timber.w(appChanel[index].formatted())
            }
        }
        if (!loadingApps){
            MainApplication.sharedPrefs.edit().putStringSet(MainApplication.PREFS_KEY_ALLOWED_PACKAGES, modifiedList.toSet()).apply()
        }

        pref.edit().putBoolean(ST.PREF_NEW_SEP, true).apply()


        //Toast.makeText(this, "Saving changes", Toast.LENGTH_SHORT).show()
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

    private fun versionO(): Boolean{
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.P
    }

    private fun checkContactPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ActivityCompat.checkSelfPermission( this@AppsActivity, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            return false
        }
        return true
    }

    private fun checkSmsPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission( this@AppsActivity, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            return false
        }
        return true
    }


    private fun checkCallPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ActivityCompat.checkSelfPermission( this@AppsActivity, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return false
        }
        return true
    }

    private fun checkCallLogPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ActivityCompat.checkSelfPermission( this@AppsActivity, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            return false
        }
        return true
    }

    private fun requestContactPermission(){
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.READ_CONTACTS), MainActivity.PERMISSION_CONTACT
        )
    }

    private fun requestContactPermissions(){
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.READ_CONTACTS), MainActivity.PERMISSIONS_CONTACTS
        )
    }

    private fun requestCallLogPermission(){
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.READ_CALL_LOG), MainActivity.PERMISSION_CALL_LOG
        )
    }

    private fun requestSMSPermissions() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.READ_SMS), MainActivity.PERMISSION_SMS
        )
    }
    private fun requestCallPermissions() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.READ_PHONE_STATE), MainActivity.PERMISSION_CALL
        )
    }

    private fun callPermission(): Boolean{
        return if (!checkCallPermission()){
            requestCallPermissions()
            false
        } else {
            if (!checkCallLogPermission() && !versionO()){
                requestCallLogPermission()
                false
            } else {
                if (!checkContactPermission()){
                    requestContactPermission()
                    false
                } else {
                    true
                }
            }
        }
    }

    private fun smsCheck(): Boolean{
        return checkSmsPermission() && checkContactPermission()
    }

    private fun callCheck(): Boolean{
        return if (versionO()){
            checkCallPermission() && checkContactPermission()
        } else {
            checkCallPermission() && checkContactPermission() && checkCallLogPermission()
        }
    }

    private fun smsPermission(): Boolean{
        return if (!checkSmsPermission()){
            requestSMSPermissions()
            false
        } else {
            if (!checkContactPermission()){
                requestContactPermissions()
                false
            } else {
                true
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.notification_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.notify -> {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                } else {
                    startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
                }

                return true
            }
            else  -> super.onOptionsItemSelected(item)
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        Timber.d("Request code: $requestCode  Result:${grantResults[0]}")

        if (requestCode == MainActivity.PERMISSION_CALL && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            if (callPermission()){
                val editor = pref.edit()
                editor.putBoolean(ST.PREF_CALL, true).apply()
            }
        }
        if (requestCode == MainActivity.PERMISSION_CALL_LOG && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            if (callPermission()){
                val editor = pref.edit()
                editor.putBoolean(ST.PREF_CALL, true).apply()
            }
        }
        if (requestCode == MainActivity.PERMISSION_CONTACT && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            if (callPermission()){
                val editor = pref.edit()
                editor.putBoolean(ST.PREF_CALL, true).apply()
            }
        }
        if (requestCode == MainActivity.PERMISSION_SMS && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            if (smsPermission()){
                val editor = pref.edit()
                editor.putBoolean(ST.PREF_SMS, true).apply()
            }
        }
        if (requestCode == MainActivity.PERMISSIONS_CONTACTS && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            if (smsPermission()){
                val editor = pref.edit()
                editor.putBoolean(ST.PREF_SMS, true).apply()
            }
        }
        if (requestCode == MainActivity.PERMISSIONS_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {

            }
        }
    }
}