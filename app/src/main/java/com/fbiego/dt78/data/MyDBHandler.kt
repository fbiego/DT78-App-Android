package com.fbiego.dt78.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.fbiego.dt78.R
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList

class MyDBHandler(context: Context, name: String?, factory: SQLiteDatabase.CursorFactory?, version: Int)
    : SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION ){

    private val cnt = context

    companion object {
        private const val DATABASE_NAME = "watch_data"
        private const val DATABASE_VERSION = 6
        const val STEPS_TABLE = "stepsData"
        const val HRM_TABLE = "heartRate"
        const val BP_TABLE = "bloodPressure"
        const val SP02_TABLE = "bloodOxygen"
        const val SLEEP_TABLE = "sleepData"
        const val ALARM_TABLE = "alarmData"
        const val USER_TABLE = "userData"
        const val SET_TABLE = "settingsData"
        const val CONTACTS_TABLE = "contactsData"
        const val ERROR_TABLE = "errorData"

        const val COLUMN_ID = "_id"
        const val COLUMN_YEAR = "year"
        const val COLUMN_MONTH = "month"
        const val COLUMN_DAY = "day"
        const val COLUMN_HOUR = "hour"
        const val COLUMN_MIN = "minute"
        const val COLUMN_SEC = "second"
        const val COLUMN_ERROR = "error"
        const val COLUMN_HOUR2 = "hour2"
        const val COLUMN_MIN2 = "minute2"
        const val COLUMN_INTERVAL = "interval"
        const val COLUMN_STEPS = "steps"
        const val COLUMN_CALORIES = "calories"
        const val COLUMN_BPM = "bpm"
        const val COLUMN_BPH = "bpHigh"
        const val COLUMN_BPL = "bpLow"
        const val COLUMN_SP02 = "sp02"
        const val COLUMN_TYPE = "type"
        const val COLUMN_DURATION = "duration"
        const val COLUMN_NAME = "name"
        const val COLUMN_NUMBER = "number"
        const val COLUMN_STATE = "state"
        const val COLUMN_REPEAT = "repeat"
        const val COLUMN_AGE = "age"
        const val COLUMN_LENGTH = "length"
        const val COLUMN_HEIGHT = "height"
        const val COLUMN_WEIGHT = "weight"
    }
    override fun onCreate(p0: SQLiteDatabase?) {
        val createStepsTable = ("CREATE TABLE " + STEPS_TABLE + "(" + COLUMN_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_YEAR + " INTEGER," + COLUMN_MONTH + " INTEGER," + COLUMN_DAY + " INTEGER," + COLUMN_HOUR + " INTEGER,"
                + COLUMN_STEPS + " INTEGER," + COLUMN_CALORIES + " INTEGER" + ")"  )
        val createHrmTable = ("CREATE TABLE " + HRM_TABLE + "(" + COLUMN_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_YEAR + " INTEGER," + COLUMN_MONTH + " INTEGER," + COLUMN_DAY + " INTEGER," + COLUMN_HOUR + " INTEGER,"
                + COLUMN_MIN + " INTEGER," + COLUMN_BPM + " INTEGER" + ")"  )
        val createBpTable = ("CREATE TABLE " + BP_TABLE + "(" + COLUMN_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_YEAR + " INTEGER," + COLUMN_MONTH + " INTEGER," + COLUMN_DAY + " INTEGER," + COLUMN_HOUR + " INTEGER,"
                + COLUMN_MIN + " INTEGER," + COLUMN_BPH + " INTEGER," + COLUMN_BPL + " INTEGER" + ")"  )
        val createSp02Table = ("CREATE TABLE " + SP02_TABLE + "(" + COLUMN_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_YEAR + " INTEGER," + COLUMN_MONTH + " INTEGER," + COLUMN_DAY + " INTEGER," + COLUMN_HOUR + " INTEGER,"
                + COLUMN_MIN + " INTEGER," + COLUMN_SP02 + " INTEGER" + ")"  )
        val createSleepTable = ("CREATE TABLE " + SLEEP_TABLE + "(" + COLUMN_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_YEAR + " INTEGER," + COLUMN_MONTH + " INTEGER," + COLUMN_DAY + " INTEGER," + COLUMN_HOUR + " INTEGER,"
                + COLUMN_MIN + " INTEGER," + COLUMN_TYPE + " INTEGER," + COLUMN_DURATION + " INTEGER" + ")"  )
        val createAlarmTable = ("CREATE TABLE $ALARM_TABLE ($COLUMN_ID INTEGER PRIMARY KEY, $COLUMN_STATE INTEGER, $COLUMN_HOUR INTEGER," +
                "$COLUMN_MIN INTEGER, $COLUMN_REPEAT INTEGER )")
        val createUserTable = ("CREATE TABLE $USER_TABLE ($COLUMN_ID INTEGER PRIMARY KEY, $COLUMN_AGE INTEGER, $COLUMN_LENGTH INTEGER," +
                "$COLUMN_HEIGHT INTEGER, $COLUMN_WEIGHT INTEGER, $COLUMN_STEPS INTEGER)")
        val createSetTable = ("CREATE TABLE $SET_TABLE ($COLUMN_ID INTEGER PRIMARY KEY, $COLUMN_HOUR INTEGER, $COLUMN_MIN INTEGER," +
                "$COLUMN_HOUR2 INTEGER, $COLUMN_MIN2 INTEGER, $COLUMN_STATE INTEGER, $COLUMN_INTERVAL INTEGER)")
        val createContactsTable = ("CREATE TABLE $CONTACTS_TABLE ($COLUMN_ID INTEGER PRIMARY KEY, $COLUMN_NAME TEXT, $COLUMN_NUMBER TEXT)")
        val createErrorTable = ("CREATE TABLE  $ERROR_TABLE ($COLUMN_ID INTEGER PRIMARY KEY,"
                + "$COLUMN_YEAR INTEGER, $COLUMN_MONTH INTEGER, $COLUMN_DAY  INTEGER, $COLUMN_HOUR INTEGER,"
                + "$COLUMN_MIN  INTEGER, $COLUMN_SEC  INTEGER, $COLUMN_ERROR  INTEGER, $COLUMN_STATE INTEGER)"  )
        p0?.execSQL(createStepsTable)
        p0?.execSQL(createHrmTable)
        p0?.execSQL(createBpTable)
        p0?.execSQL(createSp02Table)
        p0?.execSQL(createSleepTable)
        p0?.execSQL(createAlarmTable)
        p0?.execSQL(createUserTable)
        p0?.execSQL(createSetTable)
        p0?.execSQL(createContactsTable)
        p0?.execSQL(createErrorTable)
        for (al in 0 .. 7){
            p0?.execSQL("INSERT INTO $ALARM_TABLE ($COLUMN_ID, $COLUMN_STATE, $COLUMN_HOUR, $COLUMN_MIN, $COLUMN_REPEAT ) VALUES($al, 0, 0, 0,0)")
        }
        p0?.execSQL("INSERT INTO $USER_TABLE ($COLUMN_ID, $COLUMN_AGE, $COLUMN_LENGTH, $COLUMN_HEIGHT, $COLUMN_WEIGHT, $COLUMN_STEPS ) VALUES(0, 18, 60, 150, 60, 5000)")

    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        val createAlarmTable = ("CREATE TABLE IF NOT EXISTS $ALARM_TABLE ($COLUMN_ID INTEGER PRIMARY KEY, $COLUMN_STATE INTEGER, $COLUMN_HOUR INTEGER," +
                "$COLUMN_MIN INTEGER, $COLUMN_REPEAT INTEGER )")

        val createUserTable = ("CREATE TABLE IF NOT EXISTS $USER_TABLE ($COLUMN_ID INTEGER PRIMARY KEY, $COLUMN_AGE INTEGER, $COLUMN_LENGTH INTEGER," +
                "$COLUMN_HEIGHT INTEGER, $COLUMN_WEIGHT INTEGER, $COLUMN_STEPS INTEGER)")
        val createSetTable = ("CREATE TABLE IF NOT EXISTS $SET_TABLE ($COLUMN_ID INTEGER PRIMARY KEY, $COLUMN_HOUR INTEGER, $COLUMN_MIN INTEGER," +
                "$COLUMN_HOUR2 INTEGER, $COLUMN_MIN2 INTEGER, $COLUMN_STATE INTEGER, $COLUMN_INTERVAL INTEGER)")
        val createContactsTable = ("CREATE TABLE IF NOT EXISTS $CONTACTS_TABLE ($COLUMN_ID INTEGER PRIMARY KEY, $COLUMN_NAME TEXT, $COLUMN_NUMBER TEXT)")
        val createErrorTable = ("CREATE TABLE IF NOT EXISTS $ERROR_TABLE ($COLUMN_ID INTEGER PRIMARY KEY, $COLUMN_YEAR INTEGER, $COLUMN_MONTH INTEGER, " +
                "$COLUMN_DAY  INTEGER, $COLUMN_HOUR INTEGER, $COLUMN_MIN  INTEGER, $COLUMN_SEC  INTEGER, $COLUMN_ERROR  INTEGER, $COLUMN_STATE INTEGER)" )
        p0?.execSQL(createAlarmTable)
        p0?.execSQL(createUserTable)
        p0?.execSQL(createSetTable)
        p0?.execSQL(createContactsTable)
        p0?.execSQL(createErrorTable)
    }

    fun insertSteps(stepsData: StepsData){
        val values = ContentValues()
        values.put(COLUMN_ID, stepsData.id)
        values.put(COLUMN_YEAR, stepsData.year)
        values.put(COLUMN_MONTH, stepsData.month)
        values.put(COLUMN_DAY, stepsData.day)
        values.put(COLUMN_HOUR, stepsData.hour)
        values.put(COLUMN_STEPS, stepsData.steps)
        values.put(COLUMN_CALORIES, stepsData.calories)

        val db = this.writableDatabase
        db.replace(STEPS_TABLE, null, values)
        db.close()
    }

    private fun insertError(errorData: ErrorData){
        val values = ContentValues()
        values.put(COLUMN_ID, errorData.id)
        values.put(COLUMN_YEAR, errorData.year)
        values.put(COLUMN_MONTH, errorData.month)
        values.put(COLUMN_DAY, errorData.day)
        values.put(COLUMN_HOUR, errorData.hour)
        values.put(COLUMN_MIN, errorData.minute)
        values.put(COLUMN_SEC, errorData.second)
        values.put(COLUMN_ERROR, errorData.error)
        values.put(COLUMN_STATE, 0)

        val db = this.writableDatabase
        db.replace(ERROR_TABLE, null, values)
        db.close()
    }

    fun writeError(cal: Calendar, error: Int, name: String){
        val errorData = ErrorData(
            cal.get(Calendar.YEAR)-2000,
            cal.get(Calendar.MONTH)+1,
            cal.get(Calendar.DAY_OF_MONTH),
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            cal.get(Calendar.SECOND),
            error
        )
        Timber.e("${getErrorName(error)} on [$name] at ${cal.time}")
        insertError(errorData)
    }

    fun getErrorData(): ArrayList<ErrorData>{

        val qr = ArrayList<ErrorData>()
        val query = "SELECT * FROM $ERROR_TABLE"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        while (cursor.moveToNext()){
            qr.add(ErrorData(cursor.getString(1).toInt(), cursor.getString(2).toInt(),  cursor.getString(3).toInt(),
                cursor.getString(4).toInt(), cursor.getString(5).toInt(), cursor.getString(6).toInt(), cursor.getString(7).toInt()))
        }
        cursor.close()
        db.close()
        qr.sortByDescending {
            it.id
        }
        return qr
    }

    fun getErrorDay(yr: Int, mt: Int, dy: Int): ArrayList<ErrorData>{
        val qr = ArrayList<ErrorData>()

        val query = "SELECT * FROM $ERROR_TABLE WHERE $COLUMN_DAY = $dy AND $COLUMN_MONTH = $mt AND $COLUMN_YEAR = $yr"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)

        while (cursor.moveToNext()){
            qr.add(
                ErrorData(cursor.getString(1).toInt(), cursor.getString(2).toInt(),  cursor.getString(3).toInt(),
                    cursor.getString(4).toInt(), cursor.getString(5).toInt(), cursor.getString(6).toInt(), cursor.getString(7).toInt())
            )
        }
        cursor.close()
        db.close()


        return qr
    }

    fun clearAllErrors(){
        val db = this.writableDatabase
        db.execSQL("DELETE FROM $ERROR_TABLE")
        db.close()
    }

    fun getDaysWithErrors(): ArrayList<ErrorData>{
        val qr = ArrayList<ErrorData>()

        val query = "SELECT $COLUMN_YEAR, $COLUMN_MONTH, $COLUMN_DAY FROM $ERROR_TABLE GROUP BY $COLUMN_YEAR, $COLUMN_MONTH, $COLUMN_DAY;"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)

        while (cursor.moveToNext()){
            qr.add(ErrorData(cursor.getString(0).toInt(), cursor.getString(1).toInt(), cursor.getString(2).toInt(), 0, 0, 0, 0))
        }
        cursor.close()
        db.close()
        qr.sortBy {
            it.id
        }
        return qr
    }

    fun getSteps(): ArrayList<StepsData>{
        val qr = ArrayList<StepsData>()
        val query = "SELECT * FROM $STEPS_TABLE"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        var dy = 0
        var step = 0
        var step2 = 0
        var cal = 0
        var cal2 = 0

        while (cursor.moveToNext()){

            if (dy != cursor.getString(3).toInt()){
                dy = cursor.getString(3).toInt()
                step2 = 0
                cal2 = 0
            }

            if (cursor.getString(5).toInt() < step2){
                step2 = 0
                cal2 = 0
            }

            step = cursor.getString(5).toInt() - step2
            step2 = cursor.getString(5).toInt()

            if (step > 0){
                cal = cursor.getString(6).toInt() - cal2
                cal2 = cursor.getString(6).toInt()
                if (cal < 0){
                    cal2 -= cal
                    cal *= -1
                }
                qr.add(StepsData(cursor.getString(1).toInt(), cursor.getString(2).toInt(), cursor.getString(3).toInt(),
                    cursor.getString(4).toInt(), step, cal))
            }


        }
        cursor.close()
        db.close()
        return qr
    }



    fun getDaysWithSteps(): ArrayList<StepsData>{
        val qr = ArrayList<StepsData>()

        val query = "SELECT $COLUMN_YEAR, $COLUMN_MONTH, $COLUMN_DAY, $COLUMN_STEPS, $COLUMN_CALORIES, sum($COLUMN_HOUR) FROM $STEPS_TABLE GROUP BY $COLUMN_YEAR, $COLUMN_MONTH, $COLUMN_DAY;"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)

        while (cursor.moveToNext()){
            qr.add(StepsData(cursor.getString(0).toInt(), cursor.getString(1).toInt(), cursor.getString(2).toInt(),
                0, cursor.getString(3).toInt(), cursor.getString(4).toInt()))
        }
        cursor.close()
        db.close()
        qr.sortByDescending {
            it.id
        }
        return qr
    }



    fun getWeeklySteps(): ArrayList<StepsData>{
        val daily = getDaysWithSteps()
        val qr = ArrayList<StepsData>()
        val cal = Calendar.getInstance()
        for (ar in daily){
            cal.set(Calendar.YEAR, ar.year+2000)
            cal.set(Calendar.MONTH, ar.month-1)
            cal.set(Calendar.DAY_OF_MONTH, ar.day)

            qr.add(StepsData(if (ar.month == 12 && cal.get(Calendar.WEEK_OF_YEAR) == 1) ar.year+1 else ar.year,
                cal.get(Calendar.WEEK_OF_YEAR), cal.get(Calendar.DAY_OF_WEEK), 0, ar.steps, ar.calories))
        }
        qr.sortByDescending {
            it.id
        }
        return qr
    }

    fun getMonthlySteps(): ArrayList<StepsData>{
        val daily = getDaysWithSteps()
        val qr = ArrayList<StepsData>()
        for (ar in daily){
            qr.add(StepsData(ar.year, ar.month, ar.day, 0, ar.steps, ar.calories))
        }
        qr.sortByDescending {
            it.id
        }
        return qr
    }

    fun getWeeksWithSteps(): ArrayList<StepsData>{
        val weekly = getWeeklySteps()
        var year = 0
        var week = 0
        var st = 0
        var cl = 0
        var first = false
        val qr = ArrayList<StepsData>()
        for (wk in weekly){
            if (week == wk.month && year == wk.year){
                st =+ wk.steps
                cl =+ wk.calories
            } else {
                if (first){
                    qr.add(StepsData(year, week, 0, 0, st, cl))
                } else {
                    first = true
                }
                year = wk.year
                week = wk.month
                st = wk.steps
                cl = wk.calories
            }
        }
        qr.add(StepsData(year, week, 0, 0, st, cl))
        qr.sortByDescending {
            it.id
        }
        return qr
    }

    fun getMonthsWithSteps(): ArrayList<StepsData>{
        val months = getDaysWithSteps()
        var year = 0
        var month = 0
        var st = 0
        var cl = 0
        var first = false
        val qr = ArrayList<StepsData>()
        for (mt in months){
            if (month == mt.month && year == mt.year){
                st =+ mt.steps
                cl =+ mt.calories
            } else {
                if (first){
                    qr.add(StepsData(year, month, 0, 0, st, cl))
                } else {
                    first = true
                }
                year = mt.year
                month = mt.month
                st = mt.steps
                cl = mt.calories
            }
        }
        qr.add(StepsData(year, month, 0, 0, st, cl))
        qr.sortByDescending {
            it.id
        }
        return qr
    }

    fun getStepsWeek(year: Int, week: Int): ArrayList<StepsData>{
        val weekly = getWeeklySteps()
        val qr = ArrayList<StepsData>()
        for (wk in weekly){
            if (wk.year == year && wk.month == week){
                qr.add(wk)
            }
        }

        var cont: Boolean
        for (x in 1..7){
            cont = false
            if (qr.isNotEmpty()){
                qr.forEach {
                    if (it.day == x){
                        cont = true
                    }
                }
            }
            if (!cont){
                qr.add(StepsData(year, week, x, 0, 0, 0))
            }
        }

        qr.sortBy {
            it.day
        }
        return qr
    }

    fun getStepsMonth(year: Int, month: Int): ArrayList<StepsData>{
        val weekly = getDaysWithSteps()
        val qr = ArrayList<StepsData>()
        for (wk in weekly){
            if (wk.year == year && wk.month == month){
                qr.add(wk)
            }
        }

        var cont: Boolean
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year+2000)
        cal.set(Calendar.MONTH, month-1)

        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (x in 1..maxDays){
            cont = false
            if (qr.isNotEmpty()){
                qr.forEach {
                    if (it.day == x){
                        cont = true
                    }
                }
            }
            if (!cont){
                qr.add(StepsData(year, month, x, 0, 0, 0))
            }
        }

        qr.sortBy {
            it.day
        }
        return qr
    }

    fun getStepCalToday(): StepsData{
        val calendar = Calendar.getInstance(Locale.getDefault())
        val year = calendar.get(Calendar.YEAR)-2000
        val month = calendar.get(Calendar.MONTH)+1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val query = "SELECT * FROM $STEPS_TABLE WHERE $COLUMN_DAY = $day AND $COLUMN_MONTH = $month AND $COLUMN_YEAR = $year"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        var dy = 0
        var step = 0
        var cal = 0

        while (cursor.moveToNext()){
            step = cursor.getInt(5)
            cal = cursor.getInt(6)
        }
        cursor.close()
        db.close()


        return StepsData(year, month, day, 0, step, cal)
    }

    fun getStepsToday(): ArrayList<StepsData>{
        val qr = ArrayList<StepsData>()
        val calendar = Calendar.getInstance(Locale.getDefault())
        val year = calendar.get(Calendar.YEAR)-2000
        val month = calendar.get(Calendar.MONTH)+1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val query = "SELECT * FROM $STEPS_TABLE WHERE $COLUMN_DAY = $day AND $COLUMN_MONTH = $month AND $COLUMN_YEAR = $year"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        var dy = 0
        var step = 0
        var step2 = 0
        var cal = 0
        var cal2 = 0

        while (cursor.moveToNext()){

            if (dy != cursor.getString(3).toInt()){
                dy = cursor.getString(3).toInt()
                step2 = 0
                cal2 = 0
            }

            if (cursor.getString(5).toInt() < step2){
                step2 = 0
                cal2 = 0
            }

            step = cursor.getString(5).toInt() - step2
            step2 = cursor.getString(5).toInt()

            if (step > 0){
                cal = cursor.getString(6).toInt() - cal2
                cal2 = cursor.getString(6).toInt()
                if (cal < 0){
                    cal2 -= cal
                    cal *= -1
                }
                qr.add(StepsData(cursor.getString(1).toInt(), cursor.getString(2).toInt(), cursor.getString(3).toInt(),
                    cursor.getString(4).toInt(), step, cal))
            }


        }
        cursor.close()
        db.close()

        var cont: Boolean
        for (x in 7..21){
            cont = false
            if (qr.isNotEmpty()){
                qr.forEach {
                    if (it.hour == x){
                        cont = true
                    }
                }
            }
            if (!cont){
                qr.add(StepsData(year, month, day, x, 0, 0))
            }
        }

        qr.sortBy {
            it.hour
        }
        val tr = ArrayList<StepsData>()
        for (x in 7..21){
            if (qr.isNotEmpty()){
                qr.forEach {
                    if (it.hour == x){
                        tr.add(it)
                    }
                }
            }
        }
        return tr
    }

    fun getStepsDay(year: Int, month: Int, day: Int): ArrayList<StepsData>{
        val qr = ArrayList<StepsData>()
        val query = "SELECT * FROM $STEPS_TABLE WHERE $COLUMN_DAY = $day AND $COLUMN_MONTH = $month AND $COLUMN_YEAR = $year"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        var dy = 0
        var step = 0
        var step2 = 0
        var cal = 0
        var cal2 = 0

        while (cursor.moveToNext()){

            if (dy != cursor.getString(3).toInt()){
                dy = cursor.getString(3).toInt()
                step2 = 0
                cal2 = 0
            }

            if (cursor.getString(5).toInt() < step2){
                step2 = 0
                cal2 = 0
            }
            step = cursor.getString(5).toInt() - step2
            step2 = cursor.getString(5).toInt()

            if (step > 0){
                cal = cursor.getString(6).toInt() - cal2
                cal2 = cursor.getString(6).toInt()
                if (cal < 0){
                    cal2 -= cal
                    cal *= -1
                }

                qr.add(StepsData(cursor.getString(1).toInt(), cursor.getString(2).toInt(), cursor.getString(3).toInt(),
                    cursor.getString(4).toInt(), step, cal))
            }


        }
        cursor.close()
        db.close()

        var cont: Boolean
        for (x in 0..23){
            cont = false
            if (qr.isNotEmpty()){
                qr.forEach {
                    if (it.hour == x){
                        cont = true
                    }
                }
            }
            if (!cont){
                qr.add(StepsData(year, month, day, x, 0, 0))
            }
        }
        qr.sortBy {
            it.hour
        }

        return qr

    }

    fun insertSleep(data: SleepData){
        val values = ContentValues()
        values.put(COLUMN_ID, data.id)
        values.put(COLUMN_YEAR, data.year)
        values.put(COLUMN_MONTH, data.month)
        values.put(COLUMN_DAY, data.day)
        values.put(COLUMN_HOUR, data.hour)
        values.put(COLUMN_MIN, data.minute)
        values.put(COLUMN_TYPE, data.type)
        values.put(COLUMN_DURATION, data.duration)

        val db = this.writableDatabase
        db.replace(SLEEP_TABLE, null, values)
        db.close()
    }

    fun getSleepData(): ArrayList<SleepData>{

        val qr = ArrayList<SleepData>()
        val query = "SELECT * FROM $SLEEP_TABLE"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        while (cursor.moveToNext()){
            qr.add(SleepData(cursor.getString(1).toInt(), cursor.getString(2).toInt(),  cursor.getString(3).toInt(),
                cursor.getString(4).toInt(), cursor.getString(5).toInt(), cursor.getString(6).toInt(), cursor.getString(7).toInt()))
        }
        cursor.close()
        db.close()
        qr.sortBy {
            it.id
        }
        return qr
    }

    fun insertHeart(data: HeartData){
        val values = ContentValues()
        values.put(COLUMN_ID, data.id)
        values.put(COLUMN_YEAR, data.year)
        values.put(COLUMN_MONTH, data.month)
        values.put(COLUMN_DAY, data.day)
        values.put(COLUMN_HOUR, data.hour)
        values.put(COLUMN_MIN, data.minute)
        values.put(COLUMN_BPM, data.bpm)

        val db = this.writableDatabase
        db.replace(HRM_TABLE, null, values)
        db.close()
    }

    fun getHeartToday(): Int{

        val query = "SELECT $COLUMN_BPM FROM $HRM_TABLE"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)

        var hrm = 0

        while (cursor.moveToNext()){
            hrm = cursor.getInt(0)
        }
        cursor.close()
        db.close()
        return hrm
    }

    fun getHeart(): ArrayList<HealthData>{
        val qr = ArrayList<HealthData>()
        val query = "SELECT * FROM $HRM_TABLE"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)

        while (cursor.moveToNext()){
            val hrm = cursor.getString(6)+" "+cnt.resources.getString(R.string.bpm)
            qr.add(HealthData(cursor.getInt(1), cursor.getInt(2),cursor.getInt(3),cursor.getInt(4),cursor.getInt(5), hrm))

        }
        cursor.close()
        db.close()

        qr.sortByDescending {
            it.id
        }
        return qr
    }

    fun insertBp(data: PressureData){
        val values = ContentValues()
        values.put(COLUMN_ID, data.id)
        values.put(COLUMN_YEAR, data.year)
        values.put(COLUMN_MONTH, data.month)
        values.put(COLUMN_DAY, data.day)
        values.put(COLUMN_HOUR, data.hour)
        values.put(COLUMN_MIN, data.minute)
        values.put(COLUMN_BPH, data.bpHigh)
        values.put(COLUMN_BPL, data.bpLow)

        val db = this.writableDatabase
        db.replace(BP_TABLE, null, values)
        db.close()
    }

    fun getBpToday(): ArrayList<Int>{

        val query = "SELECT $COLUMN_BPL,$COLUMN_BPH FROM $BP_TABLE"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)

        val bp = ArrayList<Int>(2)
        bp.add(0, 0)
        bp.add(1, 0)

        while (cursor.moveToNext()){
            bp[0] = cursor.getInt(0)
            bp[1] = cursor.getInt(1)
        }
        cursor.close()
        db.close()
        return bp
    }

    fun getBp(): ArrayList<HealthData>{
        val qr = ArrayList<HealthData>()
        val query = "SELECT * FROM $BP_TABLE"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)

        while (cursor.moveToNext()){
            val bp = cursor.getString(7)+"/"+cursor.getString(6)+" "+cnt.resources.getString(R.string.mmHg)
            qr.add(HealthData(cursor.getInt(1), cursor.getInt(2),cursor.getInt(3),cursor.getInt(4),cursor.getInt(5), bp))

        }
        cursor.close()
        db.close()
        qr.sortByDescending {
            it.id
        }

        return qr
    }

    fun insertSp02(data: OxygenData){
        val values = ContentValues()
        values.put(COLUMN_ID, data.id)
        values.put(COLUMN_YEAR, data.year)
        values.put(COLUMN_MONTH, data.month)
        values.put(COLUMN_DAY, data.day)
        values.put(COLUMN_HOUR, data.hour)
        values.put(COLUMN_MIN, data.minute)
        values.put(COLUMN_SP02, data.sp02)

        val db = this.writableDatabase
        db.replace(SP02_TABLE, null, values)
        db.close()
    }
    fun getSp02Today(): Int{

        val query = "SELECT $COLUMN_SP02 FROM $SP02_TABLE"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)

        var sp = 0

        while (cursor.moveToNext()){
            sp = cursor.getInt(0)
        }
        cursor.close()
        db.close()
        return sp
    }

    fun getSp02(): ArrayList<HealthData>{
        val qr = ArrayList<HealthData>()
        val query = "SELECT * FROM $SP02_TABLE"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)

        while (cursor.moveToNext()){
            val sp = cursor.getString(6)+" %"
            qr.add(HealthData(cursor.getInt(1), cursor.getInt(2),cursor.getInt(3),cursor.getInt(4),cursor.getInt(5), sp))

        }
        cursor.close()
        db.close()
        qr.sortByDescending {
            it.id
        }
        return qr
    }

    fun createAlarms(){
        for (al in 0 .. 7){
            insertAlarm(AlarmData(al, false, 0, 0, 0))
        }
    }

    fun insertAlarm(alarmData: AlarmData){
        val values = ContentValues()
        values.put(COLUMN_ID, alarmData.id)
        values.put(COLUMN_STATE, if (alarmData.enable) 1 else 0)
        values.put(COLUMN_HOUR, alarmData.hour)
        values.put(COLUMN_MIN, alarmData.minute)
        values.put(COLUMN_REPEAT, alarmData.repeat)

        val db = this.writableDatabase
        db.replace(ALARM_TABLE, null, values)
        db.close()
    }

    fun getAlarms(): ArrayList<AlarmData>{
        val qr = ArrayList<AlarmData>()
        val query = "SELECT * FROM $ALARM_TABLE"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)

        while (cursor.moveToNext()){
            qr.add(AlarmData(cursor.getInt(0),cursor.getInt(1)!=0,cursor.getInt(2),cursor.getInt(3), cursor.getInt(4)))

        }
        cursor.close()
        db.close()
        qr.sortBy {
            it.id
        }
        return qr
    }

    fun insertUser(userData: UserData){
        val values = ContentValues()
        values.put(COLUMN_ID, userData.id)
        values.put(COLUMN_AGE, userData.age)
        values.put(COLUMN_LENGTH, userData.step)
        values.put(COLUMN_HEIGHT, userData.height)
        values.put(COLUMN_WEIGHT, userData.weight)
        values.put(COLUMN_STEPS, userData.target)

        val db = this.writableDatabase
        db.replace(USER_TABLE, null, values)
        db.close()
    }

    fun getUser(): UserData {
        var user = UserData(0, "Name", 60, 18, 150, 60, 5000)
        val query = "SELECT * FROM $USER_TABLE WHERE $COLUMN_ID = 0"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)

        while (cursor.moveToNext()){
            user.id = cursor.getInt(0)
            user.age = cursor.getInt(1)
            user.step = cursor.getInt(2)
            user.height = cursor.getInt(3)
            user.weight = cursor.getInt(4)
            user.target = cursor.getInt(5)
            user.name = "name"
        }
        cursor.close()
        db.close()

        return user
    }

    fun insertSet(set: ArrayList<Int>){
        val values = ContentValues()
        values.put(COLUMN_ID, set[0])
        values.put(COLUMN_HOUR, set[1])
        values.put(COLUMN_MIN, set[2])
        values.put(COLUMN_HOUR2, set[3])
        values.put(COLUMN_MIN2, set[4])
        values.put(COLUMN_STATE, set[5])
        values.put(COLUMN_INTERVAL, set[6])

        val db = this.writableDatabase
        db.replace(SET_TABLE, null, values)
        db.close()
    }

    fun getSet(pos: Int): ArrayList<Int> {
        val user = ArrayList<Int>()
        val query = "SELECT * FROM $SET_TABLE WHERE $COLUMN_ID = $pos"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)

        while (cursor.moveToNext()){
            user.add(cursor.getInt(0))
            user.add(cursor.getInt(1))
            user.add(cursor.getInt(2))
            user.add(cursor.getInt(3))
            user.add(cursor.getInt(4))
            user.add(cursor.getInt(5))
            user.add(cursor.getInt(6))
        }
        cursor.close()
        db.close()

        if (user.isEmpty()){
            user.addAll(arrayListOf(pos, 0, 0, 0, 0, 0, 0))
        }

        return user
    }

    fun insertContact(contactData: ArrayList<ContactData>){

        val size = contactData.size
        //val current = getContacts().size
        val db = this.writableDatabase

        for (c in 1 .. 8){
            if (c <= size){
                val values = ContentValues()
                values.put(COLUMN_ID, c)
                values.put(COLUMN_NAME, contactData[c-1].name)
                values.put(COLUMN_NUMBER, contactData[c-1].number)
                db.replace(CONTACTS_TABLE, null, values)
            } else {
                db.delete(CONTACTS_TABLE, "$COLUMN_ID=?", arrayOf(c.toString()))
            }
        }
        db.close()
    }

    fun getContacts(): ArrayList<ContactData>{
        val qr = ArrayList<ContactData>()
        val query = "SELECT * FROM $CONTACTS_TABLE"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)

        while (cursor.moveToNext()){
            qr.add(ContactData(cursor.getInt(0),cursor.getString(1),cursor.getString(2)))

        }
        cursor.close()
        db.close()
        qr.sortBy {
            it.id
        }
        return qr
    }

}