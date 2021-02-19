package com.fbiego.dt78.app

import android.annotation.TargetApi
import android.app.*
import android.app.admin.DevicePolicyManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.*
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import com.fbiego.dt78.*
import com.fbiego.dt78.ble.LEManager
import com.fbiego.dt78.ble.LeManagerCallbacks
import com.fbiego.dt78.data.*
import kotlinx.android.synthetic.main.activity_apps.*
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.data.Data
import timber.log.Timber
import java.io.File
import java.lang.reflect.Method
import java.util.*
import com.fbiego.dt78.app.SettingsActivity as ST

/**
 *
 */
class ForegroundService : Service(), MessageListener, PhonecallListener, DataListener, ChargeListener {

    companion object {

        var bleManager: BleManager<LeManagerCallbacks>? = null

        //val NOTIFICATION_DISPLAY_TIMEOUT = 2 * 60 * 1000 //2 minutes
        const val SERVICE_ID = 9001
        const val SERVICE_ID2 = 9002
        const val TEST_ID = 9456
        const val NOTIFICATION_CHANNEL = BuildConfig.APPLICATION_ID
        const val VESPA_DEVICE_ADDRESS = "00:00:00:00:00:00" // <--- YOUR MAC address here

        var notBody = ""
        var notAppName = ""
        var deviceName = ""
        var bat = 0
        var steps = 0
        var lst_sync = 0L
        var findPhone = false
        var unit = 1
        var dt78 = 0
        var lockPhone = false
        var notify = 0
        var icon = 20

        var serviceRunning = false
        var call = false
        var sms = false
        var find = false
        var bat_ic = 0
        var plug = false
        var unplug = false
        var camera = false
        var ext_camera = false

        var watchVersion = ""

        var connected = false
        var icSet = 1
        var capitalize = false
        var convert = false
        var prt = 0
        var acc = 0

        var smsShow = true
        var phoneShow = true

        var unlocked = false
        var screenOn = false

        var quietHours : ArrayList<Int> = arrayListOf(2, 22, 0, 7, 0, 0, 0)
        var isQuiet = false
        var selfTest = false


    }

    private var startID = 0
    private lateinit var ring: Ringtone
    lateinit var context: Context
    private var isReconnect = false

    private lateinit var deviceManager: DevicePolicyManager
    private lateinit var compName: ComponentName



    var lastPost: Long = 0L
    /**
     * Called by the system when the service is first created.  Do not call this method directly.
     */
    override fun onCreate() {
        super.onCreate()

        isReconnect = false
        val dbHandler = MyDBHandler(this, null, null, 1)
        val cal = Calendar.getInstance(Locale.getDefault())
        dbHandler.writeError(cal, SERVICE_STARTED, "Service")
        //Timber.e("Service created with startId: $startID at ${calendar.time}")
        context = this
        notificationChannel(false, this)
        if (BuildConfig.DEBUG) {
            //Timber.plant(Timber.DebugTree())
            //Timber.plant(FileLog(this, "service.txt"))
        }

        val not = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        ring = RingtoneManager.getRingtone(this,not)

        deviceManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        compName = ComponentName(this, DeviceAdmin::class.java)

        Timber.w("onCreate")
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        val remoteMacAddress = pref.getString(ST.PREF_KEY_REMOTE_MAC_ADDRESS, VESPA_DEVICE_ADDRESS)
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val leDevice = bluetoothManager.adapter.getRemoteDevice(remoteMacAddress)



        dt78 = pref.getInt(ST.PREF_WATCH_ID, UNKNOWN)
        bleManager = LEManager(this)
        (bleManager as LEManager).setGattCallbacks(bleManagerCallback)
        (bleManager as LEManager).connect(leDevice).enqueue()

        if (bluetoothManager.adapter.state == BluetoothAdapter.STATE_ON) {
            if (remoteMacAddress != VESPA_DEVICE_ADDRESS){
                if (isConnected(leDevice) && dt78 == UNKNOWN){
                    //Toast.makeText(this, "Device already connected", Toast.LENGTH_SHORT).show()
                    //deviceConnected()
                    //leDevice.
                } else {
                    //startService(Intent(this, FG::class.java))
                }
            }
            Timber.d("Bluetooth on. Connect leDevice")
        }
        Timber.w("onCreate: Bluetooth adapter state: ${bluetoothManager.adapter.state}")

        val intentFilter = IntentFilter(NotificationListener.EXTRA_ACTION)
        LocalBroadcastManager.getInstance(this).registerReceiver(localReceiver, intentFilter)

        registerReceiver(bluetoothReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
        PhonecallReceiver.bindListener(this)
        SMSReceiver.bindListener(this)
        DataReceiver.bindListener(this)
        ChargeStateReceiver.bindListener(this)

    }

    private fun isConnected(device: BluetoothDevice): Boolean {
        return try {
            val m: Method = device.javaClass.getMethod("isConnected")
            m.invoke(device) as Boolean
        } catch (e: Exception) {
            throw IllegalStateException(e)
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun notificationChannel(priority: Boolean, context: Context): NotificationManager {
        val notificationMgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val notificationChannel: NotificationChannel
            if (priority){
                notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL, BuildConfig.APPLICATION_ID, NotificationManager.IMPORTANCE_HIGH)
                notificationChannel.description = context.getString(R.string.channel_desc)
                notificationChannel.lightColor = ContextCompat.getColor(context, notIcon(acc))
                notificationChannel.enableLights(true)
                notificationChannel.enableVibration(true)
            } else {
                notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL, BuildConfig.APPLICATION_ID, NotificationManager.IMPORTANCE_MIN)
                notificationChannel.description = context.getString(R.string.channel_desc)
                notificationChannel.lightColor = ContextCompat.getColor(context, notIcon(acc))
                notificationChannel.enableLights(false)
                notificationChannel.enableVibration(false)
            }
            notificationMgr.createNotificationChannel(notificationChannel)
        }
        return notificationMgr

    }

    private fun alarmManager(){

        val dbHandler = MyDBHandler(this, null, null, 1)
        quietHours = dbHandler.getSet(2)


    }



    fun findWatch(): Boolean{

        return if(bleManager != null){
            (bleManager as LEManager).findWatch() && (bleManager as LEManager).stepsRequest() && (bleManager as LEManager).batRequest()
        } else {
            false
        }

    }

    fun stepRQ(): Boolean {
        return if (bleManager != null){
            (bleManager as LEManager).stepsRequest()
        } else {
            false
        }
    }

    fun shakeCamera(): Boolean {
        val cam = byteArrayOfInts(0xAB, 0x00, 0x04, 0xFF, 0x79, 0x80, if (!camera) 0x01 else 0x00)
        if (sendData(cam)) {
            camera = !camera
        }
        return camera
    }

    fun sendData(data: ByteArray): Boolean{
        return if (bleManager != null) {
            (bleManager as LEManager).writeBytes(data)
        } else {
            false
        }
    }

    private fun transmitData(data: ByteArray, progress: Int, context: Context): Boolean{
        return if (bleManager != null) {
            (bleManager as LEManager).transmitData(data, progress, context, this@ForegroundService::onProgress)
        } else {
            false
        }
    }

    private fun shouldNotify(): Boolean{
        isQuiet = isQuietA(quietHours)
        return !isQuiet
    }

    fun sendNotification(text: String, app: Int, show: Boolean): Boolean{
        val txt = if (convert){
            FontConverter().greek2English(text)
        } else {
            text 
        }
        val msg = if (capitalize){
            txt.toUpperCase(Locale.getDefault())
        } else {
            txt
        }
        return if (bleManager != null && !dndOn(!show)) {
            when {
                shouldNotify() -> {
                    (bleManager as LEManager).writeNotification(msg, app)
                }
                app == 20 -> {
                    (bleManager as LEManager).writeNotification(getString(R.string.quiet_active)+msg, app)
                }
                else -> {
                    false
                }
            }

        } else {
            false
        }
    }

    fun syncData(): Boolean{
        return if (bleManager != null){
            (bleManager as LEManager).syncData(lst_sync - (3600000 * 6))
        } else {
            false
        }
    }

    fun updateUser(user: UserData, si: Int){
        val usr = byteArrayOfInts(0xAB, 0x00, 0x0A, 0xFF, 0x74, 0x80, user.step, user.age, user.height, user.weight, si, user.target/1000, 0x01)
        sendData(usr)
    }

    fun updateAlarm(alarm: AlarmData): Boolean{
        val state = if (alarm.enable) 1 else 0
        val al = byteArrayOfInts(0xAB, 0x00, 0x08, 0xFF, 0x73, 0x80, alarm.id, state, alarm.hour, alarm.minute, alarm.repeat)
        return sendData(al)
    }

    fun updateSed(data: ArrayList<Int>){
        val sed = byteArrayOfInts(0xAB, 0x00, 0x09, 0xFF, 0x75, 0x80, data[5], data[1], data[2], data[3], data[4], data[6])
        sendData(sed)
    }

    fun updateSleep(data: ArrayList<Int>){
        val slp = byteArrayOfInts(0xAB, 0x00, 0x08, 0xFF, 0x7F, 0x80, data[5], data[1], data[2], data[3], data[4])
        sendData(slp)
    }

    fun updateQuiet(data: ArrayList<Int>){
        val qt = byteArrayOfInts(0xAB, 0x00, 0x08, 0xFF, 0x76, 0x80, data[5], data[1], data[2], data[3], data[4])
        sendData(qt)
    }

    fun update12hr(state: Boolean){
        val hr12 = byteArrayOfInts(0xAB, 0x00, 0x04, 0xFF, 0x7C, 0x80, if (state) 1 else 0)
        sendData(hr12)
    }

    private fun updateBat(percentage: Int){
        val bat = byteArrayOfInts(0xAB, 0x00, 0x05, 0xFF, 0x91, 0x80, 0x00, percentage)
        sendData(bat)
    }

    fun checkUpdate(){
        val setPref =  PreferenceManager.getDefaultSharedPreferences(this)
        val today = setPref.getInt(ST.PREF_TODAY, 0)
        val now = Calendar.getInstance(Locale.getDefault()).get(Calendar.DAY_OF_YEAR)
        if (today != now && dt78 != ESP32 && dt78 != UNKNOWN){
            updateWatch()
            setPref.edit().putInt(ST.PREF_TODAY, now).apply()
        }
    }

    private fun updateEsp32(context: Context){
        val dbHandler = MyDBHandler(context, null, null, 1)

        val user = dbHandler.getUser()
        updateUser(user, unit)
        val setPref =  PreferenceManager.getDefaultSharedPreferences(this)
        update12hr(setPref.getBoolean(ST.PREF_12H, false))
        val hrl = byteArrayOfInts(0xAB, 0x00, 0x04, 0xFF, 0x78, 0x80, if (setPref.getBoolean(ST.PREF_HOURLY, false)) 1 else 0)
        sendData(hrl)
        val disp = byteArrayOfInts(0xAB, 0x00, 0x04, 0xFF, 0x23, 0x80, if (setPref.getBoolean(ST.PREF_DISPLAY_OFF, false)) 1 else 0)
        sendData(disp)
        val timeout = byteArrayOfInts(0xAB, 0x00, 0x04, 0xFF, 0x7B, 0x80, setPref.getInt(ST.PREF_TIMEOUT, 10))
        sendData(timeout)

        val level = readBatteryLevel(context)
        updateBat(level)


    }

    private fun updateWatch(){
        val dbHandler = MyDBHandler(this, null, null, 1)

        val user = dbHandler.getUser()
        updateUser(user, unit)

        val setPref =  PreferenceManager.getDefaultSharedPreferences(this)
        update12hr(setPref.getBoolean(ST.PREF_12H, false))

        val als = dbHandler.getAlarms()
        if (als.isNotEmpty()){
            als.forEach {
                if (it.repeat != 128) {
                    updateAlarm(it)
                }

            }
        }

        val sed = dbHandler.getSet(0)
        if (sed.isNotEmpty()){
            updateSed(sed)
        }

        val sleep = dbHandler.getSet(1)
        if (sleep.isNotEmpty()){
            updateSleep(sleep)
        }



        val quiet = dbHandler.getSet(2)
        if (quiet.isNotEmpty()){
            updateQuiet(quiet)
        }

    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onDestroy() {
        Timber.w("onDestroy")
        serviceRunning = false
        connected = false
        ringDisconnect(false)
        ChargeStateReceiver.unBindListener()
        isReconnect = false


        val dbHandler = MyDBHandler(this, null, null, 1)
        val cal = Calendar.getInstance(Locale.getDefault())
        dbHandler.writeError(cal, SERVICE_STOPPED, "Service")
        //Timber.e("Service destroyed with startId: $startID at ${calendar.time}")

        startID = 0

        bleManager?.close()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(localReceiver)
        //unregisterReceiver(tickReceiver)
        unregisterReceiver(bluetoothReceiver)
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancelAll()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationMgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationMgr.deleteNotificationChannel(NOTIFICATION_CHANNEL)
        }

        super.onDestroy()
    }

    /**
     * Create/Update the notification
     */
    fun notify(text: String, priority: Boolean, bat: Int, id: Int): Notification {
        // Launch the MainAcivity when user taps on the Notification
        Timber.w("Context ${context.packageName}")
        val intent = Intent(context, MainActivity::class.java)

        val pendingIntent = PendingIntent.getActivity(context, 0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT)

        val notBuild = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL)
        notBuild.setSmallIcon(battery(bat, icSet))
        if (bat < 0){
            notBuild.color = ContextCompat.getColor(context, notIcon(acc))
        } else {
            notBuild.color = color(bat, context)      //ContextCompat.getColor(this, R.color.colorPrimary)
        }

        notBuild.setContentIntent(pendingIntent)
        //notBuild.setContentTitle(contentText)
        notBuild.setContentText(text)
        if (hasBat(bat)){
            notBuild.setSubText("$bat%")
        }
        if (priority) {
            notBuild.priority = NotificationCompat.PRIORITY_HIGH
            notBuild.setSound(Uri.parse("android.resource://${BuildConfig.APPLICATION_ID}/"+R.raw.notification))
            notBuild.setShowWhen(true)
        } else {
            notBuild.priority = priority(prt)
            notBuild.setSound(Uri.EMPTY)
            notBuild.setShowWhen(false)

        }
        notBuild.setOnlyAlertOnce(true)
        val notification= notBuild.build()
        notificationChannel(priority, context).notify(id, notification)
        return notification
    }

    private fun notifyProgress(text: String,  progress: Int, context: Context): Notification {

        val notBuild = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL)
        notBuild.setSmallIcon(android.R.drawable.stat_sys_upload)
        notBuild.color = ContextCompat.getColor(context, notIcon(acc))



        notBuild.setContentTitle(text)
        notBuild.setContentText("$progress%")
        if (progress == 0){
            notBuild.setProgress(0, 0, true)
        } else {
            notBuild.setProgress(100, progress, false)
        }
        notBuild.priority = NotificationCompat.PRIORITY_HIGH
        notBuild.setSound(Uri.EMPTY)
        notBuild.setShowWhen(true)
        notBuild.setOnlyAlertOnce(true)

        val notification= notBuild.build()
        notificationChannel(true, context).notify(SERVICE_ID2, notification)
        return notification
    }

    fun cancelNotification(notifyId: Int, context: Context) {
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(notifyId)
    }

    fun selfTest(cnt: Context, ic: Int){
        context = cnt
        icon = ic
        notify(cnt.getString(R.string.self_test_notif), true, -1, TEST_ID)
        selfTest = true

        Handler().postDelayed({
            if (selfTest) {

                Toast.makeText(cnt, cnt.getString(R.string.self_test_fail), Toast.LENGTH_SHORT).show()
                cancelNotification(TEST_ID, cnt)
                MainActivity().showDialog(cnt)
            }

        }, 2000)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Timber.w("onStartCommand {intent=${intent == null},flags=$flags,startId=$startId}")
        val setPref =  PreferenceManager.getDefaultSharedPreferences(this)
        acc = setPref.getInt(ST.PREF_ACCENT, 0)

        //val calendar = Calendar.getInstance(Locale.getDefault())
        //Timber.e("Service started with startId: ${this.startID} at ${calendar.time}")
        if (intent == null || this.startID != 0) {
            //service restarted
            Timber.w("onStartCommand - already running")
        } else {
            //started by intent or pending intent
            this.startID = startId
            val notification = notify(getString(R.string.scan), false, -1, SERVICE_ID)
            startForeground(SERVICE_ID, notification)

            if (intent.hasExtra("origin")){
                Timber.w("Service started on device boot")
            } else {
                connected = false
                ConnectionReceiver().notifyStatus(false)
            }
            watchVersion = ""


        }

        ringDisconnect(false)

        notify = setPref.getInt(ST.PREF_NOTIFY_TYPE, 0)
        icSet = setPref.getInt(ST.PREF_ICONS, 1)
        capitalize = setPref.getBoolean(ST.PREF_CAPS, false)
        prt = setPref.getInt(ST.PREF_PRIORITY, 0)
        unit = if ( setPref.getBoolean(ST.PREF_UNITS, false)) 1 else 0
        dt78 = setPref.getInt(ST.PREF_WATCH_ID, UNKNOWN)

        lockPhone = setPref.getBoolean(ST.PREF_LOCK, false)
        call = setPref.getBoolean(ST.PREF_CALL, false)
        sms = setPref.getBoolean(ST.PREF_SMS, false)
        find = setPref.getBoolean(ST.PREF_FIND, true)
        bat_ic = setPref.getInt(ST.PREF_BAT_IC, 0)
        plug = setPref.getBoolean(ST.PREF_PLUG, false)
        unplug = setPref.getBoolean(ST.PREF_UNPLUG, false)
        lst_sync = setPref.getLong(ST.PREF_SYNC, System.currentTimeMillis() - 604800000)
        convert = setPref.getBoolean(ST.PREF_CONVERT_EL, Locale.getDefault().language == "el")
        watchVersion = setPref.getString(ST.PREF_VERSION, "").toString()
        smsShow = !setPref.getBoolean(ST.PREF_SHOW_SMS, false)
        phoneShow = !setPref.getBoolean(ST.PREF_SHOW_CALL, false)

        unlocked = (setPref.getBoolean(ST.PREF_DND_UNLOCK, false) && isScreenLockSet(this))
        screenOn = setPref.getBoolean(ST.PREF_DND_SCREEN, false)
        ext_camera = setPref.getBoolean(ST.PREF_CAMERA, false)


        alarmManager()

        serviceRunning = true


        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun dndOn(enabled: Boolean): Boolean{
        return if (enabled) {
            if (unlocked && screenOn) {
                isScreenOn(this) && isUnlocked(this)
            } else if (unlocked) {
                isUnlocked(this)
            } else if (screenOn) {
                isScreenOn(this)
            } else {
                false
            }
        } else {
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun ringDisconnect(state: Boolean){
        if (state){
            if (!ring.isPlaying){
                ring.volume = 1.0f
                ring.isLooping = true
                ring.play()
            }
        } else {
            if (ring.isPlaying){
                ring.stop()
            }

        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun ring(state: Boolean){
        if (state){
            if (!ring.isPlaying){
                ring.volume = 1.0f

                ring.isLooping = true
                ring.play()
            }
            if (deviceManager.isAdminActive(compName) && lockPhone){
                deviceManager.lockNow()
            }
        } else {
            if (ring.isPlaying){
                ring.stop()
            }

        }

    }

    fun testNotification(text: String, app: Int, context: Context): Boolean{
        return if (bleManager != null){
            var msg = if (convert){
                FontConverter().greek2English(text)
            } else{
                text
            }
            if (capitalize){
                msg = msg.toUpperCase(Locale.getDefault())
            }
            val no = msg.toIntOrNull()
            if (no != null && Watch(dt78).line25){
                msg = largeFont(no, true)
                if (!shouldNotify()){
                    msg += context.getString(R.string.quiet_active)
                }
                (bleManager as LEManager).writeNotification(msg,app)

            } else {
                if (!shouldNotify()){
                    msg = context.getString(R.string.quiet_active)+msg
                }
                (bleManager as LEManager).writeNotification(msg,app)
            }

        } else {
            false
        }

    }



    val bleManagerCallback: LeManagerCallbacks = object : LeManagerCallbacks() {
        /**
         * Called when the device has been connected. This does not mean that the application may start communication.
         * A service discovery will be handled automatically after this call. Service discovery
         * may ends up with calling [.onServicesDiscovered] or
         * [.onDeviceNotSupported] if required services have not been found.
         * @param device the device that got connected
         */



        @RequiresApi(Build.VERSION_CODES.P)
        override fun onDeviceConnected(device: BluetoothDevice) {
            super.onDeviceConnected(device)
            Timber.d("onDeviceConnected ${device.name}")
            notify(getString(R.string.connected)+" ${device.name}", false, -1, SERVICE_ID)
            val dbHandler = MyDBHandler(context, null, null, 1)
            val cal = Calendar.getInstance(Locale.getDefault())
            if (isReconnect){
                dbHandler.writeError(cal, WATCH_RECONNECT, device.name)
            } else {
                dbHandler.writeError(cal, WATCH_CONNECTED, device.name)
            }
            isReconnect = true

            deviceName = device.name
            ringDisconnect(false)

        }

        override fun onDeviceReady(device: BluetoothDevice) {
            super.onDeviceReady(device)
            Timber.d("FG - Device ready ${device.name}")
            connected = true

            deviceName = device.name

            if (bleManager != null) {
                (bleManager as LEManager).setTime()
                (bleManager as LEManager).batRequest()
                checkUpdate()
            }

            //val calendar = Calendar.getInstance(Locale.getDefault())
            //Timber.e("Device ${device.name} ready at ${calendar.time}")

            ConnectionReceiver().notifyStatus(true)
        }


        /**
         * Called when the Android device started connecting to given device.
         * The [.onDeviceConnected] will be called when the device is connected,
         * or [.onError] in case of error.
         * @param device the device that got connected
         */
        override fun onDeviceConnecting(device: BluetoothDevice) {
            super.onDeviceConnecting(device)
            connected = false
            Timber.d("Connecting to ${if (device.name.isNullOrEmpty()) "device" else device.name}")
            notify(getString(R.string.connecting)+" ${if (device.name.isNullOrEmpty()) "device" else device.name}", false, -10, SERVICE_ID)
            //val calendar = Calendar.getInstance(Locale.getDefault())
            //Timber.e("Device ${device.name} connecting at ${calendar.time}")
        }

        /**
         * Called when user initialized disconnection.
         * @param device the device that gets disconnecting
         */
        override fun onDeviceDisconnecting(device: BluetoothDevice) {
            super.onDeviceDisconnecting(device)
            connected = false
            Timber.d("Disconnecting from ${device.name}")
            notify(getString(R.string.disconnecting)+" ${device.name}", false, -10, SERVICE_ID)
            //val calendar = Calendar.getInstance(Locale.getDefault())
            //Timber.e("Device ${device.name} disconnecting at ${calendar.time}")
            ConnectionReceiver().notifyStatus(false)
        }

        /**
         * Called when the device has disconnected (when the callback returned
         * [BluetoothGattCallback.onConnectionStateChange] with state DISCONNECTED),
         * but ONLY if the [BleManager.shouldAutoConnect] method returned false for this device when it was connecting.
         * Otherwise the [.onLinklossOccur] method will be called instead.
         * @param device the device that got disconnected
         */
        @RequiresApi(Build.VERSION_CODES.P)
        override fun onDeviceDisconnected(device: BluetoothDevice) {
            super.onDeviceDisconnected(device)
            connected = false
            Timber.d("Disconnected from ${device.name}")
            notify(getString(R.string.disconnected)+" ${device.name}", notify!=0, -10, SERVICE_ID)
            if (notify == 2 ){
                ringDisconnect(true)
            }
            val dbHandler = MyDBHandler(context, null, null, 1)
            val cal = Calendar.getInstance(Locale.getDefault())
            dbHandler.writeError(cal, WATCH_DISCONNECT, device.name)
            ConnectionReceiver().notifyStatus(false)
        }

        /**
         * This callback is invoked when the Ble Manager lost connection to a device that has been connected
         * with autoConnect option (see [BleManager.shouldAutoConnect].
         * Otherwise a [.onDeviceDisconnected] method will be called on such event.
         * @param device the device that got disconnected due to a link loss
         */
        @RequiresApi(Build.VERSION_CODES.P)
        override fun onLinkLossOccurred(device: BluetoothDevice) {
            super.onLinkLossOccurred(device)
            connected = false
            Timber.d("Lost link to ${device.name}")
            notify(getString(R.string.loss_link)+" ${device.name}", notify!=0, -10, SERVICE_ID)
            if (notify == 2 ){
                ringDisconnect(true)
            }
            val dbHandler = MyDBHandler(context, null, null, 1)
            val cal = Calendar.getInstance(Locale.getDefault())
            dbHandler.writeError(cal, LINK_LOSS, device.name)
            ConnectionReceiver().notifyStatus(false)
            //MainActivity().buttonEnable(false)
        }


        override fun onError(device: BluetoothDevice, message: String, errorCode: Int) {
            super.onError(device, message, errorCode)
            //Timber.e("Error: $errorCode, Device:${device.name}, Message: $message")
            //notify("Error:$errorCode on ${device.name}", notify!=0, -10, SERVICE_ID)
            connected = false
            //val calendar = Calendar.getInstance(Locale.getDefault())
            //Timber.e("Device ${device.name} error occurred at ${calendar.time}")
            val dbHandler = MyDBHandler(context, null, null, 1)
            val cal = Calendar.getInstance(Locale.getDefault())

            if (errorCode == 133){
                dbHandler.writeError(cal, WATCH_ERROR_133, device.name)
            } else {
                dbHandler.writeError(cal, WATCH_ERROR, device.name)
            }
            stopSelf(startID)
        }

    }



    private var localReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if ((bleManager as LEManager).isConnected && intent != null) {
                Timber.d("onReceive")
                var app: Int
                val notificationIcon = intent.getIntExtra(NotificationListener.EXTRA_ICON, 0)
                val notificationId = intent.getIntExtra(NotificationListener.EXTRA_NOTIFICATION_ID_INT, 0)
                val notificationPackage = intent.getStringExtra(NotificationListener.EXTRA_PACKAGE)
                val notificationAppName = intent.getStringExtra(NotificationListener.EXTRA_APP_NAME)
                val notificationTitle = intent.getStringExtra(NotificationListener.EXTRA_TITLE)
                val notificationBody = intent.getStringExtra(NotificationListener.EXTRA_BODY)
                val notificationTimestamp = intent.getLongExtra(NotificationListener.EXTRA_TIMESTAMP_LONG, 0)
                val notificationDismissed = intent.getBooleanExtra(NotificationListener.EXTRA_NOTIFICATION_DISMISSED, true)
                val notificationEnabled = intent.getBooleanExtra(NotificationListener.EXTRA_SHOW, true)
                //
                Timber.d("icon = $notificationIcon onNotificationPosted {app=$notificationAppName,id=$notificationId,title=$notificationTitle,body=$notificationBody,posted=$notificationTimestamp,package=$notificationPackage}")
                if (!(notBody == notificationBody && notAppName == notificationAppName) && notificationPackage != null){

                    if (notificationDismissed) {
                        //val success = (bleManager as LEManager).writeTimeAndBatt(formatter.format(Date()))
                        lastPost = notificationTimestamp
                        //Timber.d("writeTime {success=$success}")
                    } else {
                        var appN = false
                        var titleN = false
                        var bodyN = false
                        var buffer = StringBuffer(256)
                        app = if (notificationIcon == 0){

                            appN = true
                            0
                        } else {
                            if (!Watch(dt78).iconSet.contains(notificationIcon)){
                               0
                            } else {
                                notificationIcon
                            }
                        }

                        if (notificationTitle != "null" && notificationTitle != notificationAppName){

                            titleN = true

                        }
                        if (notificationBody != "null"){


                            bodyN = true
                        }

                        val line = 25

                        if (appN) {
                            buffer.append(notificationAppName)
                        }
                        if (titleN) {
                            if (appN){
                                val rem = if (bodyN){
                                    notificationTitle.length + notificationBody.length + 2
                                } else {
                                    notificationTitle.length
                                }
                                if (buffer.length < line && rem < 125 - line && Watch(dt78).line25){
                                    for (x in 0 until line-buffer.length){
                                        buffer.append(" ")
                                    }
                                } else {
                                    buffer.append(": ")
                                }
                            }
                            buffer.append(notificationTitle)
                        }
                        if (bodyN) {
                            if (!appN && titleN){
                                val rem = notificationBody.length
                                if (buffer.length < line && rem < 125 - line && Watch(dt78).line25){
                                    for (x in 0 until line -buffer.length){
                                        buffer.append(" ")
                                    }
                                } else {
                                    buffer.append(": ")
                                }
                            } else if (appN || titleN){
                                buffer.append(": ")
                            }
                            buffer.append(notificationBody)
                        }

                        if (notificationIcon == 20){
                            app = icon
                        }

                        if (notificationPackage == "com.google.android.googlequicksearchbox") {
                            val msg = if (notificationTitle.indexOf("°") in 1..5){
                                val deg = (notificationTitle.substring(0, notificationTitle.indexOf("°"))).toIntOrNull()
                                var city = notificationTitle.substring(notificationTitle.indexOf("°")+1, notificationTitle.length)
                                if (city.contains(" in ")){
                                    city = city.substring(city.indexOf(" in ")+4, city.length)
                                }
                                if (city.length < 25 && notificationBody.length <= 25){
                                    for (x in 0 until 25 - city.length){
                                        city = "$city "
                                    }
                                } else {
                                    city = "$city: "
                                }
                                if (deg != null && Watch(dt78).line25){
                                    largeFont(deg, true) + city + notificationBody
                                } else {
                                    buffer = trimStart(buffer)
                                    buffer.substring(0, buffer.length.coerceAtMost(256) )
                                }
                            } else {
                                buffer = trimStart(buffer)
                                buffer.substring(0, buffer.length.coerceAtMost(256) )
                            }

                            if (msg.isNotEmpty() && bleManager != null){
                                sendNotification(msg, app, notificationEnabled)
                            }
                            notBody = notificationBody
                            notAppName = notificationAppName

                        } else {
                            buffer = trimStart(buffer)
                            val msg = buffer.substring(0, buffer.length.coerceAtMost(256) )
                            if (msg.isNotEmpty() && bleManager != null){
                                sendNotification(msg, app, notificationEnabled)
                            }
                            notBody = notificationBody
                            notAppName = notificationAppName
                        }

                        lastPost = notificationTimestamp
                        //Timber.d("writeMessage {success=$success}")
                    }
                } else {
                    Timber.d("Previous notification detected: body=$notBody app=$notAppName or nullPackage=$notificationPackage")
                    if (notificationIcon == 20){
                        if (notificationBody.isNotEmpty() && bleManager != null){
                            sendNotification(notificationBody, icon, true)
                        }
                    }
                }

                if (notificationId == TEST_ID) {
                    selfTest = false
                    Toast.makeText(this@ForegroundService, this@ForegroundService.getString(R.string.self_test_ok), Toast.LENGTH_LONG).show()
                    cancelNotification(TEST_ID, this@ForegroundService)
                }

            }
        }
    }



    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent?.action?.equals(BluetoothAdapter.ACTION_STATE_CHANGED) == true) {
                Timber.d("Bluetooth adapter changed in receiver")
                Timber.d("BT adapter state: ${intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)}")
                when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {
                    BluetoothAdapter.STATE_ON -> {
                        // 2018/01/03 connect to remote
                        val remoteMacAddress = PreferenceManager.getDefaultSharedPreferences(context)
                                .getString(ST.PREF_KEY_REMOTE_MAC_ADDRESS, VESPA_DEVICE_ADDRESS)
                        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
                        val leDevice = bluetoothManager.adapter.getRemoteDevice(remoteMacAddress)

                        bleManager = LEManager(context)
                        bleManager?.setGattCallbacks(bleManagerCallback)
                        bleManager?.connect(leDevice)?.enqueue()

                        Timber.d("Bluetooth STATE ON")
                    }
                    BluetoothAdapter.STATE_TURNING_OFF -> {
                        // 2018/01/03 close connections
                        notify(getString(R.string.disconnected)+" Smart Watch", notify!=0, -10, SERVICE_ID)
                        ConnectionReceiver().notifyStatus(false)
                        val dbHandler = MyDBHandler(this@ForegroundService, null, null, 1)
                        val cal = Calendar.getInstance(Locale.getDefault())
                        dbHandler.writeError(cal, BLUETOOTH_OFF, "Bluetooth")

                        bleManager?.disconnect()
                        bleManager?.close()
                        Timber.d("Bluetooth TURNING OFF")


                    }
                }
            }
        }
    }

    override fun messageReceived(message: String) {
        if (sms && bleManager != null) {
            val buffer = StringBuffer()
            buffer.append(message)
            val msg = trimStart(buffer).toString()
            sendNotification(msg,0, smsShow)
        }
    }

    override fun callReceived(caller: String) {
        if (call && bleManager != null){
            sendNotification(caller, 3, phoneShow)
        }
    }

    override fun callEnded() {
        if (call && bleManager != null){
            (bleManager as LEManager).endCaller()
        }

    }


    @RequiresApi(Build.VERSION_CODES.P)
    override fun onDataReceived(data: Data) {

        Timber.w("Data received")
        if (data.size() == 8){
            if (data.getByte(4) == (0x91).toByte()){
                bat = data.getByte(7)!!.toPInt()
                Timber.w("Battery: $bat%")
                notify(getString(R.string.connected)+" $deviceName", false, bat, SERVICE_ID)
            }
        }

        if (data.size() == 5 && data.getByte(0) == (0xAD).toByte()){
            val pos = data.getByte(3)!!.toPInt()
            if (pos <= 112){
                //sendImage(pos, context)
                Timber.w("Request Location = $pos")
                //createBitmap(pos, context)
                uploadFile(pos, context)
            } else {
                Timber.w("Transfer complete")
                notifyProgress("Finishing up",  100, context)
                Handler().postDelayed({
                    cancelNotification(SERVICE_ID2, context)
                }, 5000)
                sendData(byteArrayOfInts(0xAB, 0x00, 0x04, 0xFF, 0x87, 0x80, 0x00))
            }
        }

        if (data.size() == 7){
            if (data.getByte(4) == (0x7D).toByte()){
                if(data.getByte(6) == (0x01).toByte()){
                    findPhone = true
                    ring(true)
                }
                if(data.getByte(6) == (0x00).toByte()){
                    if (findPhone){
                        ring(false)
                        batLevel(this, null)
                        findPhone = false
                    }
                }
            }
            val rooted = RootUtil.isDeviceRooted
            if (data.size() == 7 && rooted){
                if (data.getByte(4) == (0x99).toByte()){
                    when (data.getByte(6)) {
                        (0x00).toByte() -> {

                            try {
                                val p1 = Runtime.getRuntime().exec("su -c input keyevent 85")

                            } catch (e: Exception){

                            }

                        }
                        (0x01).toByte() -> {
                            try {
                                val su = Runtime.getRuntime().exec("su -c input keyevent 87")
                            } catch (e: Exception){

                            }
                        }
                        (0x02).toByte() -> {
                            try {
                                val su = Runtime.getRuntime().exec("su -c input keyevent 88")
                            } catch (e: Exception){

                            }
                        }
                    }
                }

                if (data.getByte(4) == (0x79).toByte() && data.getByte(6) == (0x01).toByte() && ext_camera){
                    try {
                        val su = Runtime.getRuntime().exec("su -c input keyevent 24")
                        //su.waitFor()
                    } catch (e: Exception){

                    }

                }
            }


        }

        if (data.size() == 20){
            if (data.getByte(4) == (0x92).toByte()){

                data.getByte(6) //major version
                data.getByte(7) //minor version

                when (data.getByte(17)) {
                    (0x60).toByte() -> {
                        //dt78
                        dt78 = DT78
                        if (data.getByte(15) == (0x08).toByte()){
                            dt78 = T03    //T03
                        }
                    }
                    (0xA2).toByte() -> {
                        //dt92
                        dt78 = DT92
                        if (data.getByte(15) == (0xFF).toByte()){
                            dt78 = ESP32    //ESP32
                            Toast.makeText(this, "Detected ESP32", Toast.LENGTH_SHORT).show()
                            updateEsp32(this)
                        }
                    }
                    (0x22).toByte() -> {
                        //dt66
                        dt78 = DT66
                        if (data.getByte(15) == (0x28).toByte()){
                            dt78 = DT78_2    //dt78 v2
                        }

                    }
                    (0x62).toByte() -> {
                        //mibro air
                        //dt78 = DT66
                        if (data.getByte(15) == (0x28).toByte()){
                            dt78 = MI_AIR    //mibro air
                        }

                    }
                    (0xE0).toByte() -> {
                        //lige l11
                        dt78 = L_11
                    }
                }
                var str = ""

                for (x in 0 until data.size()){
                    str += if(x == 0){
                        String.format("%02X", data.getByte(x)!!.toPInt())
                    } else {
                        String.format("-%02X", data.getByte(x)!!.toPInt())
                    }
                }

                watchVersion = String.format("v%01d.%02d", data.getByte(6)!!.toPInt(), data.getByte(7)!!.toPInt())
                val pref = PreferenceManager.getDefaultSharedPreferences(this)
                val edit = pref.edit()
                edit.putInt(ST.PREF_WATCH_ID, dt78)
                edit.putString(ST.PREF_HEX_ID, str)
                edit.putString(ST.PREF_VERSION, watchVersion)
                edit.apply()
                edit.commit()

                //Toast.makeText(this, str, Toast.LENGTH_SHORT).show()


                Timber.w(str)
            }
        }


        val dbHandler = MyDBHandler(this, null, null, 1)
        if (data.getByte(4) == (0x51).toByte()){
            if (data.getByte(5) == (0x20).toByte()){
                val st = (data.getByte(11)!!.toPInt() *256)+(data.getByte(12)!!.toPInt())
                val cl = (data.getByte(14)!!.toPInt()*256)+(data.getByte(15)!!.toPInt())
                if (st != 0){
                    dbHandler.insertSteps(StepsData(data.getByte(6)!!.toPInt(), data.getByte(7)!!.toPInt(),
                        data.getByte(8)!!.toPInt(), data.getByte(9)!!.toPInt(), st, cl))
                }

                val bp = data.getByte(16)!!.toPInt()
                val sp = data.getByte(17)!!.toPInt()
                val bph = data.getByte(18)!!.toPInt()
                val bpl = data.getByte(19)!!.toPInt()
                if (bp != 0){
                    dbHandler.insertHeart(HeartData(data.getByte(6)!!.toPInt(), data.getByte(7)!!.toPInt(),
                        data.getByte(8)!!.toPInt(), data.getByte(9)!!.toPInt(), data.getByte(10)!!.toPInt(), bp))
                }
                if (sp != 0){
                    dbHandler.insertSp02(OxygenData(data.getByte(6)!!.toPInt(), data.getByte(7)!!.toPInt(),
                        data.getByte(8)!!.toPInt(), data.getByte(9)!!.toPInt(), data.getByte(10)!!.toPInt(), sp))
                }
                if (bph != 0 ){
                    dbHandler.insertBp(PressureData(data.getByte(6)!!.toPInt(), data.getByte(7)!!.toPInt(),
                        data.getByte(8)!!.toPInt(), data.getByte(9)!!.toPInt(), data.getByte(10)!!.toPInt(), bph, bpl))
                }
            }
            if (data.getByte(5) == (0x11).toByte()){
                val bp = data.getByte(11)!!.toPInt()
                if (bp != 0){
                    dbHandler.insertHeart(HeartData(data.getByte(6)!!.toPInt(), data.getByte(7)!!.toPInt(),
                        data.getByte(8)!!.toPInt(), data.getByte(9)!!.toPInt(), data.getByte(10)!!.toPInt(), bp))
                }
            }

            if (data.getByte(5) == (0x12).toByte()){
                val sp = data.getByte(11)!!.toPInt()
                if (sp != 0){
                    dbHandler.insertSp02(OxygenData(data.getByte(6)!!.toPInt(), data.getByte(7)!!.toPInt(),
                        data.getByte(8)!!.toPInt(), data.getByte(9)!!.toPInt(), data.getByte(10)!!.toPInt(), sp))
                }
            }

            if (data.getByte(5) == (0x14).toByte()){
                val bph = data.getByte(11)!!.toPInt()
                val bpl = data.getByte(12)!!.toPInt()
                if (bph != 0 ){
                    dbHandler.insertBp(PressureData(data.getByte(6)!!.toPInt(), data.getByte(7)!!.toPInt(),
                        data.getByte(8)!!.toPInt(), data.getByte(9)!!.toPInt(), data.getByte(10)!!.toPInt(), bph, bpl))
                }
            }
        }

        if (data.getByte(4) == (0x52).toByte()){
            val dur = (data.getByte(12)!!.toPInt()*256)+data.getByte(13)!!.toPInt()
            if (dur != 0){
                dbHandler.insertSleep(
                    SleepData(data.getByte(6)!!.toPInt(), data.getByte(7)!!.toPInt(),
                        data.getByte(8)!!.toPInt(), data.getByte(9)!!.toPInt(), data.getByte(10)!!.toPInt(),
                            data.getByte(11)!!.toPInt(), dur)
                )
            }
        }

        MainActivity().onDataReceived(data, this, dbHandler.getUser().step)
    }


//    fun smartNotify(app: String, title: String, body: String): String{
//        var outString = ""
//        if (app.length < 25 && body.length + title.length < 125 - 25){
//            val rem = 25 - app.length
//            outString = app
//            for (x in 0 until rem){
//                outString += " "
//            }
//        } else {
//            outString += ": "
//        }
//        return outString
//    }

    fun trimStart(builder: StringBuffer): StringBuffer{
        if (Watch(dt78).line25) {
            var len = (builder.length - 1) / 25
            var i = 1
            while (i <= len) {
                if (builder[i * 25].toString() == " ") {
                    builder.deleteCharAt(i * 25)
                    len = (builder.length + 1) / 25
                }
                i++
                if (i > 4) {
                    break
                }
            }
        }
        return builder
    }

    override fun chargerChanged(context: Context, state: Boolean) {
        batLevel(context, state)
    }

    override fun batteryChanged(context: Context) {
        val level = readBatteryLevel(context)
        updateBat(level)
    }


    private fun readBatteryLevel(context: Context): Int {

        //.\adb shell su -c am broadcast -a android.intent.action.ACTION_POWER_CONNECTED -p com.fbiego.dt78
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus: Intent? = context.registerReceiver(null, intentFilter)
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1

        val batteryLevelPercent: Int = ((level.toFloat() / scale.toFloat()) * 100f).toInt()
        Timber.d("readTimeAndBatt {level=$level,scale=$scale,batteryLevel=$batteryLevelPercent%}")
        return batteryLevelPercent
    }

    private fun batLevel(context: Context, state: Boolean?){
        val batteryLevelPercent = readBatteryLevel(context)
        updateBat(batteryLevelPercent)
        if (Watch(dt78).line25){
            var batIc = ""
            for (x in 24 downTo 0){
                batIc = if (x <= batteryLevelPercent/4){
                    "$batIc@"
                } else {
                    "${batIc}-"
                }
            }
            val bat = if (state != null){
                if (state){
                    if (plug){
                        largeFont(batteryLevelPercent, false)+getString(R.string.bat_charging)+batIc
                    } else {
                        "+"
                    }
                } else {
                    if (unplug){
                        largeFont(batteryLevelPercent, false)+getString(R.string.bat_unplugged)+batIc
                    } else {
                        "+"
                    }

                }
            } else {
                if (find) {
                    largeFont(batteryLevelPercent, false) + getString(R.string.bat_phone) + batIc
                } else {
                    "+"
                }
            }
            if (bat != "+" && bleManager != null){
                sendNotification(bat, bat_ic, true)
            }

        } else {
            val bat2 =  if (state != null){
                if (state){
                    if (plug) {
                        getString(R.string.plugged)+": $batteryLevelPercent %"
                    } else {
                        "+"
                    }
                } else {
                    if (unplug){
                        getString(R.string.unplugged)+ ": $batteryLevelPercent %"
                    } else {
                        "+"
                    }

                }
            } else {
                if (find){
                    getString(R.string.phone_bat)+"$batteryLevelPercent %"
                } else {
                    "+"
                }

            }
            if (bat2 != "+" && bleManager != null){
                sendNotification(bat2, bat_ic, true)
            }

        }

    }

    private fun sendImage(pos: Int, context: Context){
        val checkG = byteArrayOfInts(0xAD, 0x04, 0x00, 0x00, 0x00, 0x37, 0x9D, 0x00)
        val checkR = byteArrayOfInts(0xAD, 0x04, 0x00, 0x00, 0x00, 0xB5, 0x8C, 0x00)
        val datB = byteArrayOfInts(0xAE, 0x12, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
        val datG = byteArrayOfInts(0xAE, 0x12, 0x00, 0x00, 0x07, 0xE0, 0x07, 0xE0, 0x07, 0xE0, 0x07, 0xE0, 0x07, 0xE0, 0x07, 0xE0, 0x07, 0xE0, 0x07, 0xE0)
        val datR = byteArrayOfInts(0xAE, 0x12, 0x00, 0x00, 0xF8, 0x00, 0xF8, 0x00, 0xF8, 0x00, 0xF8, 0x00, 0xF8, 0x00, 0xF8, 0x00, 0xF8, 0x00, 0xF8, 0x00)
        when {
            pos == 112 -> {
                checkG[1] = 0x02.toByte()
                checkG[3] = (pos/256).toByte()
                checkG[4] = (pos%256).toByte()
                checkG[5] = 0x37.toByte()
                checkG[6] = 0xDF.toByte()
                checkG[7] = 1
                transmitData(checkG, 100, context)
                for (x in 0x0E00 .. 0x0E1F){
                    datR[2] = (x/256).toByte()
                    datR[3] = (x%256).toByte()
                    sendData(datR)
                }
            }
            pos % 28 == 0 -> {
                for (x in pos * 64 until (pos + 1) * 64) { //0x1BFF
                    checkR[3] = (pos / 256).toByte()
                    checkR[4] = (pos % 256).toByte()
                    if (x % 64 == 0) {
                        transmitData(checkR, ((x.toFloat() / 0x1BFF) * 100).toInt(), context)

                    }
                    datR[2] = (x / 256).toByte()
                    datR[3] = (x % 256).toByte()
                    sendData(datR)
                }
            }
            else -> {
                for (x in pos * 64 until (pos + 1) * 64) { //0x1BFF
                    checkG[3] = (pos / 256).toByte()
                    checkG[4] = (pos % 256).toByte()
                    if (x % 64 == 0) {
                        transmitData(checkG, ((x.toFloat() / 0x1BFF) * 100).toInt(), context)

                    }
                    datG[2] = (x / 256).toByte()
                    datG[3] = (x % 256).toByte()
                    sendData(datG)
                }
            }
        }

        //cancelNotification(SERVICE_ID2, context)

    }


    fun uploadFile(pos: Int, context: Context){ val directory = context.cacheDir
        val upload = File(directory, "upload")

        val file = File(upload, "out$pos.hex")
        val check = File(upload, "check$pos.hex")
        if (file.exists() && check.exists()){
            val sum = check.readBytes()
            transmitData(sum, ((pos.toFloat() / 0x70) * 100).toInt(), context)
            val pxls = file.readBytes()
            if (pos == 112){
                val array = ByteArray(20)
                for (b in 0 until  32) {
                    for (y in 0 until 20){
                        array[y] = pxls[(b*20)+y]
                    }
                    sendData(array)
                }
            } else {
                for (b in 0 until  64) {
                    val array = ByteArray(20)
                    for (y in 0 until 20){
                        array[y] = pxls[(b*20)+y]
                    }
                    sendData(array)
                }
            }

        } else {
            sendImage(pos, context)
        }
    }


    private fun onProgress(progress: Int, context: Context){
        var txt = context.getString(R.string.send_data)
        notifyProgress(txt, progress, context)
        if (progress == 100) {
            txt = context.getString(R.string.transfer_complete)
            notifyProgress(txt, progress, context)
        }
        ProgressReceiver().getProgress(progress, txt)
    }



    fun sendDat(byteArray: ByteArray, pos: Int, context: Context){
        val checkSum = byteArrayOfInts(0xAD, 0x04, 0x00, 0x00, 0x00, 0x37, 0x9D, 0x00)
        val data = byteArrayOfInts(0xAE, 0x12, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,0x00, 0x00, 0x00, 0x00, 0x00, 0x00)

        val crc = CRC16Modbus()
        for (b in byteArray){
            crc.update(b.toInt())
        }
        val sum = crc.crcBytes
        checkSum[3] = (pos / 256).toByte()
        checkSum[4] = (pos % 256).toByte()
        checkSum[5] = sum[1]
        checkSum[6] = sum[0]
        if (pos == 112){
            checkSum[1] = 0x02
            checkSum[7] = 0x01
        }
        transmitData(checkSum, ((pos.toFloat() / 0x70) * 100).toInt(), context)
        val pxls = byteArray
        if (pos == 112){
            for (b in 0 until  32) {
                for (x in 0 until 16) {
                    data[x + 4] = pxls[x + (b * 16)]
                }
                data[2] = ((b + 0x0E00)/ 256).toByte()
                data[3] = ((b + 0x0E00) % 256).toByte()
                sendData(data)
            }
        } else {
            for (b in 0 until  64) {
                for (x in 0 until 16) {
                    data[x + 4] = pxls[x + (b * 16)]
                }
                data[2] = ((b + pos) / 256).toByte()
                data[3] = ((b + pos) % 256).toByte()
                sendData(data)
            }
        }
    }



}