package com.fbiego.dt78.data

import android.app.KeyguardManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.PowerManager
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.core.app.NotificationCompat
import com.fbiego.dt78.R
import java.util.*
import kotlin.collections.ArrayList


fun Byte.toPInt() = toInt() and 0xFF

fun byteArrayOfInts(vararg ints: Int) = ByteArray(ints.size) { pos -> ints[pos].toByte()
}

val a = arrayOf(" __ ", " _  ", " __ ", " __ ", "    ", " __ ", " __ ", " __ ", " __ ", " __ ", "    ", "    ", "    ")
val b = arrayOf("|  |", "  | ", " __|", " __|", "|__|", "|__ ", "|__ ", "  / ", "|__|", "|__|", " __ ", "    ", " o/ ")
val c = arrayOf("|__|", " _|_", "|__ ", " __|", "   |", " __|", "|__|", " /  ", "|__|", " __|", "    ", "    ", " /o ")

val dig = arrayOf(a, b, c)

const val sp = '/'  // _
const val sc = '|'  // ,
const val sp_old = '_'  //TODO
const val sc_old= ','
const val sON = "##"
const val UNKNOWN = -1
const val DT78 =    0
const val DT92 =    1
const val DT66 =    2
const val DT78_2 =  3
const val T03 =     4
const val ESP32 =   5
const val MI_AIR =  6
const val L_11 =    7

const val DAY =     0
const val WEEK =    1
const val MONTH =   2

const val SERVICE_STARTED =     0
const val SERVICE_STOPPED =     1
const val BLUETOOTH_OFF =       2
const val WATCH_CONNECTED =     3
const val WATCH_DISCONNECT =    4
const val LINK_LOSS =           5
const val WATCH_ERROR   =       6
const val WATCH_RECONNECT   =   7
const val WATCH_ERROR_133 =     8

fun getErrorName(id: Int): String{
    return when(id){
        SERVICE_STARTED -> "SERVICE_STARTED"
        SERVICE_STOPPED -> "SERVICE_STOPPED"
        BLUETOOTH_OFF -> "BLUETOOTH_TURNED_OFF"
        WATCH_CONNECTED -> "WATCH_CONNECTED"
        WATCH_DISCONNECT -> "WATCH_DISCONNECTED"
        WATCH_ERROR -> "WATCH_ERROR"
        LINK_LOSS -> "LOST_LINK"
        WATCH_RECONNECT -> "WATCH_RECONNECTED"
        WATCH_ERROR_133 -> "WATCH_ERROR_133"
        else -> "UNDEFINED"
    }
}

fun getErrorIcon(id: Int): Int{
    return when(id){
        SERVICE_STARTED -> R.drawable.ic_start
        SERVICE_STOPPED -> R.drawable.ic_off
        BLUETOOTH_OFF -> R.drawable.ic_disc
        WATCH_CONNECTED -> R.drawable.ic_bt
        WATCH_DISCONNECT -> R.drawable.ic_bt_disc
        WATCH_ERROR -> R.drawable.ic_info
        LINK_LOSS -> R.drawable.ic_loss
        WATCH_RECONNECT -> R.drawable.ic_sync
        WATCH_ERROR_133 -> R.drawable.ic_info
        else -> R.drawable.ic_watch
    }
}



fun myTheme(id: Int): Int{
    return when (id){
        0 -> R.style.AppTheme
        1 -> R.style.RedTheme
        2 -> R.style.PurpleTheme
        3 -> R.style.BlueTheme
        else -> R.style.AppTheme
    }
}


fun notIcon(id: Int): Int{
    return when (id){
        0 -> R.color.colorAccentGreen
        1 -> R.color.colorAccentRed
        2 -> R.color.colorAccentPurple
        3 -> R.color.colorAccentBlue
        else -> R.color.colorAccentGreen
    }
}

@ColorInt
fun Context.getColorFromAttr(
    @AttrRes attrColor: Int,
    typedValue: TypedValue = TypedValue(),
    resolveRefs: Boolean = true
): Int {
    theme.resolveAttribute(attrColor, typedValue, resolveRefs)
    return typedValue.data
}
fun largeFont(no: Int, deg: Boolean): String{
    var out = ""
    val neg = no < 0
    val num = if (neg){
        no*-1
    } else {
        no
    }
    var d1 = (num%1000)/100
    if (d1 == 0){

        d1 = 11
        if (neg){
            d1 = 10
        }
    }
    var d2 = (num%100)/10
    if ((d1 == 11 || d1 == 10) && d2 == 0){
        d2 = 11
        if (d1 == 10){
            d1 = 11
            d2 = 10
        }
    }
    val d3 = (num%100)%10
    for (x in 0 until 75){
        out = "$out "
    }

    if (d1 == 11){
        if (d2 == 11){
            if (deg){
                out = out.replaceRange(15, 16, "o")
            } else {
                out = out.replaceRange(41, 43, "o/")
                out = out.replaceRange(66, 68, "/o")
            }
            out = out.replaceRange(10, 14, a[d3])
            out = out.replaceRange(35, 39, b[d3])
            out = out.replaceRange(60, 64, c[d3])
        } else {
            if (deg) {
                out = out.replaceRange(19, 20, "o")
            } else {
                out = out.replaceRange(45, 47, "o/")
                out = out.replaceRange(70, 72, "/o")
            }
            out = out.replaceRange(7, 11, a[d2])
            out = out.replaceRange(32, 36, b[d2])
            out = out.replaceRange(57, 61, c[d2])

            out = out.replaceRange(14, 18, a[d3])
            out = out.replaceRange(39, 43, b[d3])
            out = out.replaceRange(64, 68, c[d3])
        }
    } else {
        if (deg){
            out = out.replaceRange(22, 23, "o")
        } else {
            out = out.replaceRange(48, 50, "o/")
            out = out.replaceRange(73, 75, "/o")
        }
        out = out.replaceRange(3, 7, a[d1])
        out = out.replaceRange(28, 32, b[d1])
        out = out.replaceRange(53, 57, c[d1])

        out = out.replaceRange(10, 14, a[d2])
        out = out.replaceRange(35, 39, b[d2])
        out = out.replaceRange(60, 64, c[d2])

        out = out.replaceRange(17, 21, a[d3])
        out = out.replaceRange(42, 46, b[d3])
        out = out.replaceRange(67, 71, c[d3])
    }


    return out
}


fun colors(col: Int): Int {
    return when (col) {
        0 -> Color.parseColor("#FF0000")
        1 -> Color.parseColor("#FFFF00")
        2 -> Color.parseColor("#00FF00")
        3 -> Color.parseColor("#008000")
        4 -> Color.parseColor("#00FFFF")
        5 -> Color.parseColor("#008080")
        6 -> Color.parseColor("#0000FF")
        7 -> Color.parseColor("#000080")
        8 -> Color.parseColor("#FF00FF")
        9 -> Color.parseColor("#800080")
        else -> Color.parseColor("#FFFFFF")
    }
}

fun distance(dis: Int, unit: Boolean, context: Context): String{
    val dist = dis.toFloat()
    return if (unit){
        "%.1f".format(dist / 100000)+" "+context.resources.getString(R.string.km)
    } else {
        "%.1f".format(dist / 160900)+" "+context.resources.getString(R.string.miles)
    }

}

fun map(value: Int, start: Int, end: Int): Int{
    return when {
        value < start -> 0
        value > end -> 100
        else -> ((value.toFloat() - start)/(end-start)*100).toInt()
    }

}

fun unit(unit: Boolean, context: Context): String{
    return if (unit){
        context.resources.getString(R.string.metric)+" ("+context.resources.getString(R.string.km)+")"
    } else {
        context.resources.getString(R.string.imperial)+" ("+context.resources.getString(R.string.miles)+")"
    }

}

fun appIcon(id: Int): Int{
    return when (id) {
        0 -> R.raw.sms
        1 -> R.raw.whatsapp
        2 -> R.raw.twitter
        4 -> R.raw.instagram
        5 -> R.raw.facebook
        6 -> R.raw.messenger
        7 -> R.raw.skype
        8 -> R.raw.penguin
        9 -> R.raw.wechat
        10 -> R.raw.line
        11 -> R.raw.weibo
        12 -> R.raw.kakao
        13 -> R.raw.telegram
        14 -> R.raw.viber
        else -> R.raw.sms
    }
}

fun appName(id: Int): String{
    return when (id) {
        0 -> "Message"
        1 -> "WhatsApp"
        2 -> "Twitter"
        4 -> "Twitter"
        5 -> "Facebook"
        6 -> "Messenger"
        7 -> "Skype"
        8 -> "QQ"
        9 -> "Wechat"
        10 -> "Line"
        11 -> "Weibo"
        12 -> "KakaoTalk"
        13 -> "Telegram"
        14 -> "Viber"
        else -> "Message"
    }
}

fun spinner(id: Int): Int{
    return if (id < 3){
        id
    } else {
        id-1
    }
}

fun parseFilter(list: MutableSet<String>): ArrayList<FilterList>{
    val ch = sp
    val array = ArrayList<FilterList>()
    if (list.isNotEmpty()){
        list.forEach {
            val arr = ArrayList<String>()
            var txt = it
            val oc = txt.count{ x -> x == ch}
            if (oc != 0){
                for (x in 0 until oc){
                    val i = txt.indexOf(ch)
                    arr.add(txt.substring(0, i))
                    txt = txt.substring(i + 1, txt.length)
                    if (x == oc-1){
                        arr.add(txt)
                    }
                }
                val app = arr[0]
                arr.removeAt(0)
                array.add(FilterList(app, arr))
            }
        }
    }

    return array
}

fun appFilters(text: String, new: Boolean): ArrayList<String>{
    val ch = if(new) {
        sp
    } else{
        sp_old
    }

    val arr = ArrayList<String>()
    var txt = text
    val oc = txt.count{ x -> x == ch}
    if (oc != 0){
        for (x in 0 until oc){
            val i = txt.indexOf(ch)
            arr.add(txt.substring(0, i))
            txt = txt.substring(i + 1, txt.length)
            if (x == oc-1){
                arr.add(txt)
            }
        }
        arr.removeAt(0)
    }

    return arr
}

//TODO (apps filter migration)
fun parseApps(list: MutableSet<String>, new: Boolean): ArrayList<Channel>{
    val array = ArrayList<Channel>()
    if (list.isNotEmpty()){
        list.forEach {
            val start = if (new){
                it.indexOf(sc)
            } else {
                it.indexOf(sc_old)
            }
            val id = if (start != -1){
                it.substring(0, start)
            } else {
                "20"
            }

            val filter = appFilters(it, new)
            val ind = if (new){
                sp
            } else {
                sp_old
            }
            val name = if (filter.isNotEmpty()){
                it.substring(start + 1, it.indexOf(ind))
            } else {
                it.substring(start + 1, it.length)
            }

            val no = try {
                id.toInt()
            } catch (e: NumberFormatException){
                20
            } catch (e: IllegalArgumentException){
                20
            }
            array.add(Channel(no, name, filter))
        }
    }
    return array
}

fun battery(percentage: Int, icon: Int): Int{
    return when (icon){
        1 -> battery1(percentage)
        2 -> battery2(percentage)
        3 -> battery3(percentage)
        else -> battery1(percentage)
    }
}

fun battery1(percentage: Int): Int{
    return when {
        percentage >= 90 -> R.drawable.ic_bat100w
        percentage >= 70 -> R.drawable.ic_bat80w
        percentage >= 50 -> R.drawable.ic_bat60w
        percentage >= 30 -> R.drawable.ic_bat40w
        percentage >= 20 -> R.drawable.ic_bat20w
        percentage >= 0 -> R.drawable.ic_bat00w
        percentage == -10 -> R.drawable.ic_disc
        else -> R.drawable.ic_watch
    }
}

fun battery2(percentage: Int): Int {
    return when {
        percentage >= 90 -> R.drawable.ic_per100
        percentage >= 70 -> R.drawable.ic_per80
        percentage >= 50 -> R.drawable.ic_per60
        percentage >= 30 -> R.drawable.ic_per40
        percentage >= 10 -> R.drawable.ic_per20
        percentage >= 0 -> R.drawable.ic_per0
        percentage == -10 -> R.drawable.ic_disc
        else -> R.drawable.ic_watch
    }
//    return when (percentage){
//        100 -> R.drawable.ic_per100
//        80  -> R.drawable.ic_per80
//        60 -> R.drawable.ic_per60
//        40 -> R.drawable.ic_per40
//        20 -> R.drawable.ic_per20
//        0 -> R.drawable.ic_per0
//        -10 -> R.drawable.ic_disc
//        else -> R.drawable.ic_watch
//    }
}

fun battery3(percentage: Int): Int{
    return when {
        percentage >= 90 -> R.drawable.ic_bat100c
        percentage >= 70 -> R.drawable.ic_bat80c
        percentage >= 50 -> R.drawable.ic_bat60c
        percentage >= 30 -> R.drawable.ic_bat40c
        percentage >= 20 -> R.drawable.ic_bat20c
        percentage >= 0 -> R.drawable.ic_bat0c
        percentage == -10 -> R.drawable.ic_disc
        else -> R.drawable.ic_watch
    }
}

fun priority(pr: Int): Int{
    return when (pr){
        0 -> NotificationCompat.PRIORITY_MIN
        1 -> NotificationCompat.PRIORITY_LOW
        2 -> NotificationCompat.PRIORITY_DEFAULT
        3 -> NotificationCompat.PRIORITY_HIGH
        4 -> NotificationCompat.PRIORITY_MAX
        else -> NotificationCompat.PRIORITY_DEFAULT
    }
}

fun color(percentage: Int, context: Context): Int{
    return when {
        percentage >= 90 -> Color.parseColor("#008000")
        percentage >= 70 -> Color.parseColor("#308000")
        percentage >= 50 -> Color.parseColor("#508000")
        percentage >= 30 -> Color.parseColor("#F08000")
        percentage >= 10 -> Color.parseColor("#F04000")
        percentage >= 0 -> Color.parseColor("#F00000")
        else -> context.getColorFromAttr(R.attr.colorAccent)
    }
}

fun hasBat(bat: Int): Boolean{
    return when (bat){
        100, 80, 60, 40, 20, 0 -> true
        else -> false
    }
}

fun time(millis: Long): String{
    val sec = millis/1000
    return if (sec >= 60){
        "${sec/60}m ${sec%60}s"
    } else {
        "${sec}s"
    }
}
fun checkPackage(packageName: String, supported: ArrayList<Int>): Int{
    var id = when (packageName) {
        "com.whatsapp" -> 1
        "com.whatsapp.w4b" -> 1
        "com.twitter.android" -> 2
        "com.instagram.android" -> 4
        "com.facebook.katana" -> 5
        "com.facebook.lite" -> 5
        "com.facebook.orca" -> 6
        "com.facebook.mlite" -> 6
        "com.skype.raider" -> 7
        "com.skype.m2" -> 7
        "com.tencent.mobileqq" -> 8
        "com.tencent.mm" -> 9
        "jp.naver.line.android" -> 10
        "com.linecorp.linelite" -> 10
        "com.weico.international" -> 11
        "com.sina.weibo" -> 11
        "com.kakao.talk" -> 12
        "org.telegram.messenger" -> 13
        "com.viber.voip" -> 14
        else -> 0
    }
    if (!supported.contains(id)){
        id = 0
    }
    return id
}


fun alarmRepeat(x: Int, context: Context): String{
    return when (x){
        128 -> {
            context.resources.getString(R.string.once)
        }
        127 -> {
            context.resources.getString(R.string.everyday)
        }
        31 -> {
            context.resources.getString(R.string.mon_fri)
        }
        else -> {
            var out = ""
            var y = x
            if (y/64 == 1){
                out = " "+context.resources.getString(R.string.sun)+out
            }
            y %= 64
            if (y/32 == 1){
                out = " "+context.resources.getString(R.string.sat)+out
            }
            y %= 32
            if (y/16 == 1){
                out = " "+context.resources.getString(R.string.fri)+out
            }
            y %= 16
            if (y/8 == 1){
                out = " "+context.resources.getString(R.string.thur)+out
            }
            y %= 8
            if (y/4 == 1){
                out = " "+context.resources.getString(R.string.wed)+out
            }
            y %= 4
            if (y/2 == 1){
                out = " "+context.resources.getString(R.string.tue)+out
            }
            y %= 2
            if (y/1 == 1){
                out = " "+context.resources.getString(R.string.mon)+out
            }
            out
        }
    }


}

fun isFiltered(text: String, list: ArrayList<String>): Boolean{
    if (list.isNotEmpty()){
        list.forEach {
            if (text.contains(it)){
                return true
            }
        }
    }
    return false
}

fun isQuiet(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int, state: Boolean): Boolean {
    val current = Calendar.getInstance()
    val start = startHour*100 + startMinute
    val end = endHour*100 + endMinute
    val now = current[Calendar.HOUR_OF_DAY]*100 + current[Calendar.MINUTE]

    return when {
        start > end -> {
            (now > start || now < end) && state
        }
        start < end -> {
            (now in (start + 1) until end) && state
        }
        else -> {
            false
        }
    }

}

fun isNotQuiet(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int, state: Boolean):Boolean{
    return !isQuiet(startHour, startMinute, endHour, endMinute, state)
}

fun isQuietA(array: ArrayList<Int>): Boolean{
    return isQuiet(array[1], array[2], array[3], array[4], array[5] != 0)
}

fun stepsWeekly(stepsData: ArrayList<StepsData>): ArrayList<StepsData>{

    return stepsData
}

fun dayOfWeek(day: Int, context: Context): String{
    return when (day){
        2 -> context.resources.getString(R.string.monday)
        3 -> context.resources.getString(R.string.tuesday)
        4 -> context.resources.getString(R.string.wednesday)
        5 -> context.resources.getString(R.string.thursday)
        6 -> context.resources.getString(R.string.friday)
        7 -> context.resources.getString(R.string.saturday)
        1 -> context.resources.getString(R.string.sunday)
        else -> ""
    }
}

fun month(mn: Int, context: Context): String{
    return when (mn){
        1 -> context.resources.getString(R.string.january)
        2 -> context.resources.getString(R.string.february)
        3 -> context.resources.getString(R.string.march)
        4 -> context.resources.getString(R.string.april)
        5 -> context.resources.getString(R.string.may)
        6 -> context.resources.getString(R.string.june)
        7 -> context.resources.getString(R.string.july)
        8 -> context.resources.getString(R.string.august)
        9 -> context.resources.getString(R.string.september)
        10 -> context.resources.getString(R.string.october)
        11 -> context.resources.getString(R.string.november)
        12 -> context.resources.getString(R.string.december)
        else -> ""
    }
}

fun dayOfWk(day: Int, context: Context): String{
    return when (day){
        1 -> context.resources.getString(R.string.sun)
        2 -> context.resources.getString(R.string.mon)
        3 -> context.resources.getString(R.string.tue)
        4 -> context.resources.getString(R.string.wed)
        5 -> context.resources.getString(R.string.thur)
        6 -> context.resources.getString(R.string.fri)
        7 -> context.resources.getString(R.string.sat)
        else -> ""
    }
}

fun time(min: Int): String{
    return when {
        min < 60 -> "${min}m"
        min%60 == 0 -> "${min/60}h"
        else -> "${min/60}h ${min%60}m"
    }
}

fun type(type: Int, context: Context): String{
    return when (type) {
        1 -> context.getString(R.string.light_sleep)
        2 -> context.getString(R.string.deep_sleep)
        else -> "Awake"
    }
}

fun isUnlocked(context: Context): Boolean{
    val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
        !keyguardManager.isDeviceLocked
    } else {
        !keyguardManager.isKeyguardLocked
    }
}

fun isScreenLockSet(context: Context): Boolean{
    val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        keyguardManager.isDeviceSecure
    } else {
        keyguardManager.isKeyguardSecure
    }
}

fun isScreenOn(context: Context): Boolean {
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    return powerManager.isInteractive
}