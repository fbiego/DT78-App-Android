package com.fbiego.dt78

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.db.williamchart.data.Scale
import com.fbiego.dt78.app.ForegroundService
import com.fbiego.dt78.app.SettingsActivity
import com.fbiego.dt78.data.*
import com.hadiidbouk.charts.BarData
import com.hadiidbouk.charts.ChartProgressBar
import kotlinx.android.synthetic.main.activity_steps.*
import kotlinx.android.synthetic.main.activity_steps.barChart
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList
import com.fbiego.dt78.app.ForegroundService as FG

class StepsActivity : AppCompatActivity() {

    private var stepList = ArrayList<StepsData>()
    private var dayList = ArrayList<StepsData>()
    private var current = 0
    private var currentView = DAY
    private var maxDay = 0
    private var stepSize = 70
    private lateinit var menu: Menu
    var target = 5000
    var viewTarget = 5000


    companion object {
        lateinit var stepRecycler: RecyclerView

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        setTheme(myTheme(pref.getInt(SettingsActivity.PREF_ACCENT, 0)))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_steps)

        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)

        stepRecycler = findViewById<View>(R.id.recyclerView) as RecyclerView
        stepRecycler.layoutManager =
            LinearLayoutManager(this)

        stepRecycler.isNestedScrollingEnabled = false

        buttonNext.setOnClickListener {
            current--
            if (current <= 0){
                buttonNext.isEnabled = false
            }
            if (current < maxDay-1){
                buttonPrev.isEnabled = true
            }
            loadDaySteps(currentView)

        }
        buttonPrev.setOnClickListener {
            current++
            if (current >= maxDay-1){
                buttonPrev.isEnabled = false
            }
            if (current > 0){
                buttonNext.isEnabled = true
            }
            loadDaySteps(currentView)

        }


    }

    @SuppressLint("DefaultLocale")
    override fun onResume() {
        super.onResume()

        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        ForegroundService.lst_sync = pref.getLong(SettingsActivity.PREF_SYNC,System.currentTimeMillis() - 604800000)
        if (System.currentTimeMillis() > ForegroundService.lst_sync + (3600000 * 1)){
            if (ForegroundService().syncData()){
                Toast.makeText(this, R.string.sync_watch, Toast.LENGTH_SHORT).show()
                val editor: SharedPreferences.Editor = pref.edit()
                val time = System.currentTimeMillis()
                editor.putLong(SettingsActivity.PREF_SYNC, time)
                editor.apply()
                editor.commit()
            }

        }

        loadData()

    }

    private fun loadData(){
        dayList.clear()
        val dbHandler = MyDBHandler(this, null, null, 1)
        dayList = when (currentView) {
            DAY -> dbHandler.getDaysWithSteps()
            WEEK -> dbHandler.getWeeksWithSteps()
            MONTH -> dbHandler.getMonthsWithSteps()
            else -> dbHandler.getDaysWithSteps()
        }
        current = 0
        stepSize = dbHandler.getUser().step
        maxDay = dayList.size
        buttonNext.isEnabled = false
        buttonPrev.isEnabled = maxDay > 1
        target = dbHandler.getUser().target

        loadDaySteps(currentView)
    }

    @SuppressLint("SetTextI18n")
    private fun loadDaySteps(view: Int){
        if (dayList.isNotEmpty()){

            stepList.clear()
            stepList = barChart()

            if (stepList.isNotEmpty()) {
                if (currentView == DAY) {
                    stepList.sortBy {
                        it.id
                    }
                }
                var steps = 0
                var cal = 0

                stepList.forEach {
                    steps += it.steps
                    cal += it.calories
                }
                stepsTextA.text = "$steps " + getString(R.string.steps)
                caloriesTextA.text = "$cal " + getString(R.string.kcal)
                distanceTextA.text = distance(steps * stepSize, FG.unit != 0, this)
                val date = when (currentView) {
                    DAY -> today(stepList[0])
                    WEEK -> getString(R.string.week) + String.format(
                        " %02d - 20%02d",
                        stepList[0].month,
                        stepList[0].year
                    )
                    MONTH -> month(stepList[0].month, this) + String.format(
                        " - 20%02d",
                        stepList[0].year
                    )
                    else -> String.format(
                        "%02d-%02d-20%02d",
                        stepList[0].day,
                        stepList[0].month,
                        stepList[0].year
                    )
                }
                textDate.text = date

                Timber.w("Target steps: $viewTarget, View: $currentView ")
                updateDonut(this, steps, viewTarget)

                val stepAdapter = StepsAdapter(stepList, stepSize, view)

                stepRecycler.apply {
                    layoutManager =
                        LinearLayoutManager(this@StepsActivity)
                    adapter = stepAdapter
                }
            } else {
                updateDonut(this, 0, viewTarget)
            }

        } else {
            updateDonut(this, 0, viewTarget)
        }

    }

    private fun barChart(): ArrayList<StepsData>{
        val dbHandler = MyDBHandler(this, null, null, 1)

        val todaySteps = when (currentView) {
            DAY -> dbHandler.getStepsDay(dayList[current].year, dayList[current].month, dayList[current].day)
            WEEK -> dbHandler.getStepsWeek(dayList[current].year, dayList[current].month)
            MONTH -> dbHandler.getStepsMonth(dayList[current].year, dayList[current].month)
            else -> dbHandler.getStepsDay(dayList[current].year, dayList[current].month, dayList[current].day)
        }

        viewTarget = when (currentView){
            DAY -> target
            WEEK -> target * 7
            MONTH -> target * todaySteps.size
            else -> target
        }

//        val date = String.format("%02d-%02d-20%02d", dayList[current].day, dayList[current].month, dayList[current].year)
//        stepsTextA.text = "${dayList[current].steps} "+getString(R.string.steps)
//        caloriesTextA.text = "${dayList[current].calories} "+getString(R.string.kcal)
//        distanceTextA.text = distance(dayList[current].steps*stepSize, FG.unit!=0, this)
//        textDate.text = date
        var max = 2000

        val data = ArrayList<Pair<String, Float>>()


        todaySteps.forEach {
            val lbl = when (currentView) {
                DAY -> it.hour.toString()
                WEEK -> dayOfWk(it.day, this)
                MONTH -> it.day.toString()
                else -> it.hour.toString()
            }
            data.add(Pair("",
                (it.steps).toFloat(),))
            if (it.steps > max){
                max = it.steps
            }
        }
//        val mChart = findViewById<ChartProgressBar>(R.id.ChartProgressBarDay)
//        mChart.setMaxValue(max.toFloat())
//        mChart.setDataList(dataList)
//        mChart.build()



        barChart.fillColor = this.getColorFromAttr(R.attr.colorIcons)
        //barChart.gradientFillColors = intArrayOf(this.getColorFromAttr(R.attr.colorIcons), ContextCompat.getColor(this, R.color.colorTransparent))
        barChart.scale = Scale((max * -0.03).toFloat(), max.toFloat()+500)
        //barChart.labelsFormatter = { "${it.roundToInt()}" }
        barChart.animate(data)

        val stepListToday = ArrayList<StepsData>()
        todaySteps.forEach {
            if (it.steps > 0 || currentView == WEEK ){
                stepListToday.add(it)
            }
        }
        Timber.w("TodaySteps = ${todaySteps.size}, SteplistToday = ${stepListToday.size}")
        return stepListToday
    }

    private fun updateDonut(context: Context, current: Int, target: Int){

        val plot = ((current.toFloat()/target)*100)

        val donutData = arrayListOf(plot)

        //donutChart.
        targetSteps.text = "${plot.toInt()}%"
        donutChart.donutColors = intArrayOf(context.getColorFromAttr(R.attr.colorIcons))
        donutChart.animate(donutData)
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


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.view_switcher, menu)
        this.menu = menu
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.day_view -> {
                menu.findItem(R.id.day_view).isVisible = false
                menu.findItem(R.id.week_view).isVisible = true
                menu.findItem(R.id.month_view).isVisible = true
                currentView = DAY
                loadData()
                true
            }
            R.id.week_view -> {
                menu.findItem(R.id.day_view).isVisible = true
                menu.findItem(R.id.week_view).isVisible = false
                menu.findItem(R.id.month_view).isVisible = true
                currentView = WEEK
                loadData()
                true
            }
            R.id.month_view -> {
                menu.findItem(R.id.day_view).isVisible = true
                menu.findItem(R.id.week_view).isVisible = true
                menu.findItem(R.id.month_view).isVisible = false
                currentView = MONTH
                loadData()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }

    fun today(stepsData: StepsData): String{
        val cal = Calendar.getInstance(Locale.getDefault())
        return if (cal.get(Calendar.DAY_OF_MONTH) == stepsData.day && cal.get(Calendar.MONTH)+1 == stepsData.month && cal.get(Calendar.YEAR) == stepsData.year+2000){
            this.getString(R.string.today)
        } else {
            String.format("%02d-%02d-20%02d", stepsData.day, stepsData.month, stepsData.year)
        }
    }
}