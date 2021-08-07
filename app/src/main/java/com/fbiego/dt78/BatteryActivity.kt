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

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.db.williamchart.data.Scale
import com.fbiego.dt78.app.CrashLogger
import com.fbiego.dt78.app.SettingsActivity
import com.fbiego.dt78.data.*
import kotlinx.android.synthetic.main.activity_battery.*
import kotlinx.android.synthetic.main.activity_battery.barChart
import kotlinx.android.synthetic.main.activity_battery.buttonNext
import kotlinx.android.synthetic.main.activity_battery.buttonPrev
import kotlinx.android.synthetic.main.activity_battery.textDate
import kotlinx.android.synthetic.main.activity_steps.*
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList

class BatteryActivity : AppCompatActivity() {

    private var batteryList = ArrayList<BatteryData>()
    private var weekList = ArrayList<BatteryData>()
    var current = 0
    var maxWeeks = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        setTheme(myTheme(pref.getInt(SettingsActivity.PREF_ACCENT, 0)))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_battery)

        Thread.setDefaultUncaughtExceptionHandler(CrashLogger(this))

        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)

        recyclerViewBattery.layoutManager = LinearLayoutManager(this)
        recyclerViewBattery.isNestedScrollingEnabled = false

        buttonNext.setOnClickListener {
            current--
            if (current <= 0){
                buttonNext.isEnabled = false
            }
            if (current < maxWeeks-1){
                buttonPrev.isEnabled = true
            }
            //loadDaySteps(currentView)
            loadBatteryWeek(current)

        }
        buttonPrev.setOnClickListener {
            current++
            if (current >= maxWeeks-1){
                buttonPrev.isEnabled = false
            }
            if (current > 0){
                buttonNext.isEnabled = true
            }
            //loadDaySteps(currentView)
            loadBatteryWeek(current)

        }

    }

    override fun onResume() {
        super.onResume()

        batteryList.clear()
        batteryList = MyDBHandler(this, null, null, 1).getBattery()

        weekList = ArrayList(batteryList.distinctBy { listOf(it.week(), it.year())  })
        current = 0
        maxWeeks = weekList.size
        buttonNext.isEnabled = false
        buttonPrev.isEnabled = maxWeeks > 1
        loadBatteryWeek(0)


    }

    private fun loadBatteryWeek(x: Int){
        if (weekList.size > x){
            val wk = weekList[x]
            textDate.text = getString(R.string.week) + String.format(" %02d - %04d", wk.week(), wk.year())

            val thisWk = ArrayList(batteryList.filter { it.week()==wk.week() && it.year()==wk.year() })

            val graph = sortWeekGraph(thisWk, wk.week(), wk.year())

            val data = ArrayList<Pair<String, Float>>()
            graph.forEach {
                val txt = if (it.hour == 12 ){
                    dayOfWk(it.day, this)
                } else if (it.hour == 0 && it.day > 1){
                    "|"
                } else {
                    ""
                }
                data.add(Pair(txt, it.level.toFloat()))
                Timber.w("Battery Graph: ${it.day}, ${it.hour}, ${it.level}")
            }

            barChart.fillColor = this.getColorFromAttr(R.attr.colorIcons)
            barChart.scale = Scale(0f, 100f)
            barChart.labelsColor = this.getColorFromAttr(R.attr.colorIcons)
            barChart.animate(data)

            val list = sortWeekList(thisWk)

            recyclerViewBattery.apply {
                layoutManager =
                    LinearLayoutManager(this@BatteryActivity)
                adapter = BatteryAdapter(list)
            }

        } else {

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

    private fun sortWeekList(data: ArrayList<BatteryData>): ArrayList<BatteryData>{
        val sorted = ArrayList<BatteryData>()
        var level = 0
        var state = 0
        var hour = 0
        var day = 0
        //data.sortByDescending { it.time }
        data.forEach {
            if (it.level==level && it.type==state && it.hour()==hour && it.weekDay()==day){

            } else {
                if (level == 100 && state == 2 && it.type != 0){

                } else {
                    sorted.add(it)
                    level = it.level
                    state = it.type
                    hour = it.hour()
                    day = it.weekDay()
                }
            }
        }
        sorted.sortByDescending { it.time }
        return sorted
    }

    private fun sortWeekGraph(data: ArrayList<BatteryData>, week: Int, year: Int): ArrayList<GraphData>{

        val cal = Calendar.getInstance(Locale.getDefault())
        val wk = cal.get(Calendar.WEEK_OF_YEAR)
        val yr = cal.get(Calendar.YEAR)
        val hr = cal.get(Calendar.HOUR_OF_DAY)
        val dy = cal.get(Calendar.DAY_OF_WEEK)
        val sorted = ArrayList<GraphData>()
        var level = 0
        var state = 0
        for (x in 1..7){
            for (y in 0..23){
                val r = data.filter { it.weekDay()==x && it.hour()==y }
                r.sortedByDescending { it.level }
                if (r.isNotEmpty()){
                    level = r[0].level
                    state = r[0].type
                    sorted.add(GraphData(x, y, level, state))
                } else {
                    sorted.add(GraphData(x, y, level, state))
                }
                if (x == dy && y > hr && wk == week && yr == year){
                    level = 0
                    state = 0
                }
            }
        }

        return sorted
    }

    class GraphData(
        var day: Int,
        var hour: Int,
        var level: Int,
        var type: Int
    )
}