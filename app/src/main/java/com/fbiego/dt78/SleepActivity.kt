package com.fbiego.dt78

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.db.williamchart.data.Scale
import com.fbiego.dt78.app.SettingsActivity
import com.fbiego.dt78.data.*
import kotlinx.android.synthetic.main.activity_sleep.*
import kotlinx.android.synthetic.main.activity_sleep.donutChart
import kotlinx.android.synthetic.main.activity_sleep.targetSteps
import java.util.*
import kotlin.collections.ArrayList

class SleepActivity : AppCompatActivity() {

    private var sleepList = ArrayList<SleepData>()
    private var sleep = ArrayList<SleepData>()
    private var daysList = ArrayList<String>()
    private var current = 0
    private var maxDay = 0
    private var start = 0
    private var end = 0

    companion object {
        lateinit var sleepRecycler: RecyclerView


    }
    override fun onCreate(savedInstanceState: Bundle?) {
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        setTheme(myTheme(pref.getInt(SettingsActivity.PREF_ACCENT, 0)))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sleep)

        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)

        sleepRecycler = findViewById<View>(R.id.recyclerViewSleep) as RecyclerView
        sleepRecycler.layoutManager =
            LinearLayoutManager(this)

        sleepRecycler.isNestedScrollingEnabled = false

        buttonNextSleep.setOnClickListener {
            current--
            if (current <= 0){
                buttonNextSleep.isEnabled = false
            }
            if (current < maxDay-1){
                buttonPrevSleep.isEnabled = true
            }
            plotData(maxDay - (1 + current))
        }

        buttonPrevSleep.setOnClickListener {
            current++
            if (current >= maxDay-1) {
                buttonPrevSleep.isEnabled = false
            }
            if (current > 0){
                buttonNextSleep.isEnabled = true
            }
            plotData(maxDay - (1 + current))
        }

        //val dbHandler = MyDBHandler(this, null, null, 1)




    }

    override fun onResume() {
        super.onResume()

        loadData()
    }

    private fun plotData(pos: Int){
        sleepList.clear()
        sleepList = getSleepDay(daysList[pos])

        val lightS = sleepList.sumBy { if (it.type == 1) it.duration else 0 }
        val deepS = sleepList.sumBy { if (it.type == 2) it.duration else 0 }

        //val lgt = if (lightS != 0) lightS - deepS else 0
//        light.text = this.getString(R.string.light_sleep)+"\n"+time(lightS) //"${time(lightS)}\nLight"
//        deep.text = this.getString(R.string.deep_sleep)+"\n"+time(deepS) //"${time(deepS)}\nDeep"
//        total.text = this.getString(R.string.total_sleep)+"\n"+time(lightS+deepS)
        textDateSleep.text = daysList[pos]
        val data = ArrayList<Pair<String, Float>>()
        data.add(Pair(this.getString(R.string.light_sleep)+"\n"+time(lightS), lightS.toFloat()))
        data.add(Pair(this.getString(R.string.deep_sleep)+"\n"+time(deepS), deepS.toFloat()))

//        sleepChart.labelsColor = this.getColorFromAttr(R.attr.colorIcons)
//        sleepChart.scale = Scale(0f, (lightS+deepS).toFloat())
//        sleepChart.animate(data)

        updateDonut(this, lightS, deepS)
        val sleepAdapter = SleepAdapter(sleepList)
        sleepRecycler.apply {
            layoutManager =
                LinearLayoutManager(this@SleepActivity)
            adapter = sleepAdapter
        }
    }

    private fun updateDonut(context: Context, light: Int, deep: Int){

        val plot1 = ((light.toFloat()/(light+deep))*100)
        val plot2 = ((deep.toFloat()/(light+deep))*100)

        val donutData = arrayListOf(plot1, plot2)

        donutChartLight.donutColors = intArrayOf(context.getColorFromAttr(R.attr.colorIcons))
        donutChartLight.animate(arrayListOf(plot1))
        targetStepsLight.text = context.getString(R.string.light_sleep)+"\n"+time(light)

        donutChartDeep.donutColors = intArrayOf(context.getColorFromAttr(R.attr.colorIcons))
        donutChartDeep.animate(arrayListOf(plot2))
        targetStepsDeep.text = context.getString(R.string.deep_sleep)+"\n"+time(deep)

        //donutChart.
        targetSteps.text = context.getString(R.string.total_sleep)+"\n"+time(light+deep)
        donutChart.donutColors = intArrayOf(context.getColorFromAttr(R.attr.colorButtonEnabled), context.getColorFromAttr(R.attr.colorIcons))
        donutChart.animate(donutData)
    }

    private fun loadData(){
        dailySleep()
        maxDay = daysList.size
        if (daysList.isNotEmpty()){

            plotData(maxDay - (1 + 0))
        } else {
            textDateSleep.text = this.getString(R.string.no_data)
            updateDonut(this, 0, 0)
        }

        buttonNextSleep.isEnabled = false
        buttonPrevSleep.isEnabled = maxDay > 1
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
                        cal.set(Calendar.MONTH, it.month-1)
                        cal.set(Calendar.YEAR, it.year+2000)
                        cal.add(Calendar.DATE, 1)
                        val el = String.format("%02d-%02d-20%02d", cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH)+1, cal.get(Calendar.YEAR)-2000)
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
                            cal.set(Calendar.MONTH, it.month-1)
                            cal.set(Calendar.YEAR, it.year+2000)
                            cal.add(Calendar.DATE, 1)
                            val date = String.format("%02d-%02d-20%02d", cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH)+1, cal.get(Calendar.YEAR)-2000)
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
        return parseData(today)
    }


    fun parseData(input: ArrayList<SleepData>): ArrayList<SleepData>{
        val outData = ArrayList<SleepData>()
        var start = 0L
        var end = 0L
        var last = 0L
        var type = 0

        for (x in 0 until input.size){
            if (x == 0){
                last = timeMillis(input[x], false)
            }
            if (input[x].type == 1){		// save start & end if light
                start = timeMillis(input[x], true)
                end = timeMillis(input[x], false)

            }

            if (x < input.size-1){
                if (input[x].type == 1){	//light
                    if (x != 0 && type != 1){
                        outData.add(millis2Sleep(last, timeMillis(input[x], true)-60000, 1))
                    }
                    outData.add(millis2Sleep(timeMillis(input[x], true), timeMillis(input[x+1], true)-60000, input[x].type))
                } else {	// deep
                    if (last+60000 < timeMillis(input[x], true) && timeMillis(input[x], true) >= start && timeMillis(input[x], false) <= end){
                        outData.add(millis2Sleep(last, timeMillis(input[x], true)-60000, 1))	// insert light between deep
                    } else if (x != 0 && type != 1){
                        outData.add(millis2Sleep(last, timeMillis(input[x], true)-60000, 1))
                    }
                    outData.add(input[x])
                }
                if (input[x+1].type == 1){
                    if (end > timeMillis(input[x], false)){
                        //outData.add(millis2Sleep(timeMillis(input[x], false), end, 1))	// fill light end
                    }
                }

            } else {
                // last element
                if (last+60000 < timeMillis(input[x], true)  && last+60000 >= start && timeMillis(input[x], true)-60000 <= end){
                    outData.add(millis2Sleep(last, timeMillis(input[x], true)-60000, 1))	// insert light between deep
                }  else if (x != 0 && type != 1 ) {
                    outData.add(millis2Sleep(last, timeMillis(input[x], true)-60000, 1))
                }
                outData.add(input[x])	// last element
                if (end > timeMillis(input[x], false)){
                    outData.add(millis2Sleep(timeMillis(input[x], false), end, 1)) // fill light end
                }

            }
            last = timeMillis(input[x], false)
            type = input[x].type
        }

        return contData(outData)
    }


    private fun contData(input: ArrayList<SleepData>): ArrayList<SleepData>{
        val outData = ArrayList<SleepData>()
        var start = 0L
        var end = 0L
        var type = 0

        for (x in 0 until input.size){

            if (input.size == 1){	// one item only
                outData.add(input[x])
            } else {
                if (x == 0){	// first
                    start = timeMillis(input[x], true)
                    end = timeMillis(input[x], false)
                    type = input[x].type
                } else if (x < input.size-1){
                    if (input[x].type == type){
                        if (end == timeMillis(input[x], true)){
                            end = timeMillis(input[x], false)
                        }

                    } else {
                        outData.add(millis2Sleep(start, end-60000, type))
                        start = timeMillis(input[x], true)
                        end = timeMillis(input[x], false)
                        type = input[x].type
                    }

                } else {		// last
                    if (input[x].type == type && end == timeMillis(input[x], true)){
                        end = timeMillis(input[x], false)
                        outData.add(millis2Sleep(start, end-60000, type))
                    } else {
                        outData.add(millis2Sleep(start, end-60000, type))
                        outData.add(input[x])
                    }
                }
            }

        }
        return outData
    }

    private fun timeMillis(sleepData: SleepData, start: Boolean): Long{
        val cal = Calendar.getInstance(Locale.getDefault())
        cal.set(Calendar.YEAR, sleepData.year+2000)
        cal.set(Calendar.MONTH, sleepData.month-1)
        cal.set(Calendar.DAY_OF_MONTH, sleepData.day)
        cal.set(Calendar.HOUR_OF_DAY, sleepData.hour)
        cal.set(Calendar.MINUTE, sleepData.minute)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND , 0)

        return if (start){
            cal.timeInMillis
        } else{
            cal.add(Calendar.MINUTE, sleepData.duration)
            cal.timeInMillis
        }

    }

    private fun millis2Sleep(start: Long, end: Long, type: Int): SleepData{
        val cal = Calendar.getInstance(Locale.getDefault())
        cal.timeInMillis = start

        return SleepData(
            cal.get(Calendar.YEAR)-2000,
            cal.get(Calendar.MONTH)+1,
            cal.get(Calendar.DAY_OF_MONTH),
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            type, ((end+60000-start)/60000).toInt()
        )
    }



}