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

import android.animation.Animator
import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fbiego.dt78.app.DataListener
import com.fbiego.dt78.app.DataReceiver
import com.fbiego.dt78.app.SettingsActivity
import com.fbiego.dt78.data.*
import kotlinx.android.synthetic.main.activity_health.*
import no.nordicsemi.android.ble.data.Data
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList
import com.fbiego.dt78.app.ForegroundService as FG


class HealthActivity : AppCompatActivity(), DataListener {

    private var healthList = ArrayList<HealthData>()
    private var healthAdapter = HealthAdapter(healthList)

    var rate = 0L
    var measuring = false

    companion object {
        lateinit var healthRecycler: RecyclerView

        var viewH = 0
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        setTheme(myTheme(pref.getInt(SettingsActivity.PREF_ACCENT, 0)))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_health)

        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)
        DataReceiver.bindListener(this)

        healthRecycler = findViewById<View>(R.id.recyclerHealthList) as RecyclerView
        healthRecycler.layoutManager =
            LinearLayoutManager(this)

        healthRecycler.isNestedScrollingEnabled = false

        healthRecycler.apply {
            layoutManager =
                LinearLayoutManager(this@HealthActivity)
            adapter = healthAdapter
        }



    }

    override fun onResume() {
        super.onResume()

        FG.healthRun = true

        healthList.clear()
        val dbHandler = MyDBHandler(this, null, null, 1)
        healthList = when (viewH) {
            0 -> {
                hrmCard.backgroundTintList = ColorStateList.valueOf(this.getColorFromAttr(R.attr.colorCardBackgroundLight))
                dbHandler.getHeart()
            }
            1 -> {
                bpCard.backgroundTintList = ColorStateList.valueOf(this.getColorFromAttr(R.attr.colorCardBackgroundLight))
                dbHandler.getBp()
            }
            2 -> {
                sp02Card.backgroundTintList = ColorStateList.valueOf(this.getColorFromAttr(R.attr.colorCardBackgroundLight))
                dbHandler.getSp02()
            }
            3 -> {
                allCard.backgroundTintList = ColorStateList.valueOf(this.getColorFromAttr(R.attr.colorCardBackgroundLight))
                dbHandler.getHeart()
            }
            else -> {
                hrmCard.backgroundTintList = ColorStateList.valueOf(this.getColorFromAttr(R.attr.colorCardBackgroundLight))
                dbHandler.getHeart()
            }
        }

        healthAdapter.update(healthList)



    }

    fun measure(view: View) {

        if (measuring){
            stopMeasure()
            progressBar.visibility = View.GONE
            heart_beat.visibility = View.GONE
            tapInfo.text = getString(R.string.start_measure)
            measuring = false
        } else {
            if (startMeasure()){
                measuring = true
                tapInfo.text = getString(R.string.stop_measure)
                progressBar.visibility = View.VISIBLE
                if (viewH == 3){
                    val duration : Long = 60 * 1000
                    progressBar.isIndeterminate = false
                    progressBar.max = 100
                    progressBar.progress = 100
                    valueHealth.text = "-- --/-- --"
                    val anim = ProgressBarAnimation(progressBar, 100f, 0f)
                    anim.duration = duration
                    progressBar.startAnimation(anim)
                    Handler(Looper.getMainLooper()).postDelayed({
                        stopMeasure()
                        progressBar.visibility = View.GONE
                        heart_beat.visibility = View.GONE
                        progressBar.isIndeterminate = true
                        tapInfo.text = getString(R.string.start_measure)
                        measuring = false
                    }, duration)
                } else {
                    progressBar.isIndeterminate = true
                    heart_beat.visibility = View.VISIBLE
                    rate = 0
                    valueHealth.text = "--"
                }
            } else{
                Toast.makeText(this, R.string.not_connect, Toast.LENGTH_LONG).show()
            }

        }
    }

    private fun startMeasure(): Boolean{
        return when (viewH){
            0 -> {
                heart_beat.setImageResource(R.drawable.ic_heart_beat)
                FG().sendData(byteArrayOfInts(0xAB, 0x00, 0x04, 0xFF, 0x31, 0x0A, 0x01))
            }
            1 -> {
                heart_beat.setImageResource(R.drawable.ic_b_pressure)
                FG().sendData(byteArrayOfInts(0xAB, 0x00, 0x04, 0xFF, 0x31, 0x22, 0x01))
            }
            2 -> {
                heart_beat.setImageResource(R.drawable.ic_b_oxygen)
                FG().sendData(byteArrayOfInts(0xAB, 0x00, 0x04, 0xFF, 0x31, 0x12, 0x01))
            }
            3 -> {
                heart_beat.setImageResource(R.drawable.ic_b_oxygen)
                FG().sendData(byteArrayOfInts(0xAB, 0x00, 0x04, 0xFF, 0x32, 0x80, 0x01))
            }
            else -> false
        }
    }

    private fun stopMeasure(){

        if (measuring){
            when (viewH){
                0 -> {
                    FG().sendData(byteArrayOfInts(0xAB, 0x00, 0x04, 0xFF, 0x31, 0x0A, 0x00))
                }
                1 -> {
                    FG().sendData(byteArrayOfInts(0xAB, 0x00, 0x04, 0xFF, 0x31, 0x22, 0x00))
                }
                2 -> {
                    FG().sendData(byteArrayOfInts(0xAB, 0x00, 0x04, 0xFF, 0x31, 0x12, 0x00))
                }
                3 -> {
                    FG().sendData(byteArrayOfInts(0xAB, 0x00, 0x04, 0xFF, 0x32, 0x80, 0x00))
                }
            }
            rate = 0L
            progressBar.visibility = View.GONE
            heart_beat.visibility = View.GONE
        }
    }

    private val scaleUp : Animator.AnimatorListener = object : Animator.AnimatorListener{
        override fun onAnimationStart(p0: Animator?) {

        }

        override fun onAnimationEnd(p0: Animator?) {
            heart_beat.animate().scaleXBy(-0.2f).scaleYBy(-0.2f).setDuration(rate)
                .setListener(scaleDown)
        }

        override fun onAnimationCancel(p0: Animator?) {

        }

        override fun onAnimationRepeat(p0: Animator?) {

        }

    }

    private val scaleDown : Animator.AnimatorListener = object : Animator.AnimatorListener{
        override fun onAnimationStart(p0: Animator?) {

        }

        override fun onAnimationEnd(p0: Animator?) {
            if (measuring) {
                heart_beat.animate().scaleXBy(0.2f).scaleYBy(0.2f).setDuration(rate)
                    .setListener(scaleUp)
            } else {
                heart_beat.animate().cancel()
            }
        }

        override fun onAnimationCancel(p0: Animator?) {

        }

        override fun onAnimationRepeat(p0: Animator?) {

        }

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun change(view: View){

        stopMeasure()

        hrmCard.backgroundTintList = ColorStateList.valueOf(this.getColorFromAttr(R.attr.colorCardBackgroundDark))
        bpCard.backgroundTintList = ColorStateList.valueOf(this.getColorFromAttr(R.attr.colorCardBackgroundDark))
        sp02Card.backgroundTintList = ColorStateList.valueOf(this.getColorFromAttr(R.attr.colorCardBackgroundDark))
        allCard.backgroundTintList = ColorStateList.valueOf(this.getColorFromAttr(R.attr.colorCardBackgroundDark))
        healthList.clear()
        val dbHandler = MyDBHandler(this, null, null, 1)
        healthList = when(view.id){
            R.id.hrmCard -> {
                viewH = 0
                hrmCard.backgroundTintList = ColorStateList.valueOf(this.getColorFromAttr(R.attr.colorCardBackgroundLight))
                dbHandler.getHeart()

            }
            R.id.bpCard -> {
                viewH = 1
                bpCard.backgroundTintList = ColorStateList.valueOf(this.getColorFromAttr(R.attr.colorCardBackgroundLight))
                dbHandler.getBp()
            }
            R.id.sp02Card -> {
                viewH = 2
                sp02Card.backgroundTintList = ColorStateList.valueOf(this.getColorFromAttr(R.attr.colorCardBackgroundLight))
                dbHandler.getSp02()
            }
            R.id.allCard -> {
                viewH = 3
                allCard.backgroundTintList = ColorStateList.valueOf(this.getColorFromAttr(R.attr.colorCardBackgroundLight))

                dbHandler.getHeart()
            }
            else -> {
                viewH = 0
                hrmCard.backgroundTintList = ColorStateList.valueOf(this.getColorFromAttr(R.attr.colorCardBackgroundLight))
                
                dbHandler.getHeart()
            }
        }

        measuring = false

        Timber.d("list size = ${healthList.size}")
        healthAdapter.update(healthList)
    }

    override fun onPause() {
        super.onPause()
        stopMeasure()
        FG.healthRun = false
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

    @SuppressLint("SetTextI18n")
    override fun onDataReceived(data: Data) {

        val dbHandler = MyDBHandler(this, null, null, 1)
        val calendar = Calendar.getInstance(Locale.getDefault())

        if (data.getByte(0) == (0xAB).toByte() && data.getByte(3) == (0xFF).toByte()) {
            if (data.getByte(4) == (0x31).toByte()) {

                Timber.d(
                    "Type = ${data.getByte(5)!!.toPInt()} and value = ${
                        data.getByte(6)!!.toPInt()
                    }"
                )

                if (data.getByte(5) == (0x0A).toByte()) {
                    val bp = data.getByte(6)!!.toPInt()

                    if (bp != 0) {
                        runOnUiThread {
                            if (measuring) {
                                valueHealth.text = "$bp " + getString(R.string.bpm)
                                if (rate == 0L) {
                                    rate = ((60000 / bp) / 2).toLong()
                                    heart_beat.animate().scaleXBy(0.2f).scaleYBy(0.2f)
                                        .setDuration(rate)
                                        .setListener(scaleUp)
                                } else {
                                    rate = ((60000 / bp) / 2).toLong()
                                }
                            }
                        }
                        dbHandler.insertHeart(
                            HeartData(
                                calendar.get(Calendar.YEAR) - 2000,
                                calendar.get(Calendar.MONTH) + 1,
                                calendar.get(Calendar.DAY_OF_MONTH),
                                calendar.get(Calendar.HOUR_OF_DAY),
                                calendar.get(Calendar.MINUTE),
                                bp,
                                H_APP
                            )
                        )
                    }
                }

                if (data.getByte(5) == (0x12).toByte()) {
                    val sp = data.getByte(6)!!.toPInt()

                    if (sp != 0) {
                        runOnUiThread {
                            if (measuring) {
                                valueHealth.text = "$sp %"
                                if (rate == 0L) {
                                    rate = 350L
                                    heart_beat.animate().scaleXBy(0.2f).scaleYBy(0.2f)
                                        .setDuration(rate)
                                        .setListener(scaleUp)
                                }
                            }
                        }
                        dbHandler.insertSp02(
                            OxygenData(
                                calendar.get(Calendar.YEAR) - 2000,
                                calendar.get(Calendar.MONTH) + 1,
                                calendar.get(Calendar.DAY_OF_MONTH),
                                calendar.get(Calendar.HOUR_OF_DAY),
                                calendar.get(Calendar.MINUTE),
                                sp,
                                H_APP
                            )
                        )
                    }
                }

                if (data.getByte(5) == (0x22).toByte()) {
                    val bph = data.getByte(6)!!.toPInt()
                    val bpl = data.getByte(7)!!.toPInt()

                    if (bph != 0) {
                        runOnUiThread {
                            if (measuring) {
                                valueHealth.text = "$bpl/$bph " + getString(R.string.mmHg)
                                if (rate == 0L) {
                                    rate = 350L
                                    heart_beat.animate().scaleXBy(0.2f).scaleYBy(0.2f)
                                        .setDuration(rate)
                                        .setListener(scaleUp)
                                }
                            }
                        }
                        dbHandler.insertBp(
                            PressureData(
                                calendar.get(Calendar.YEAR) - 2000,
                                calendar.get(Calendar.MONTH) + 1,
                                calendar.get(Calendar.DAY_OF_MONTH),
                                calendar.get(Calendar.HOUR_OF_DAY),
                                calendar.get(Calendar.MINUTE),
                                bph,
                                bpl,
                                H_APP
                            )
                        )
                    }
                }
            }
            if (data.getByte(4) == (0x32).toByte()) {
                val bp = data.getByte(6)!!.toPInt()
                val sp = data.getByte(7)!!.toPInt()
                val bph = data.getByte(8)!!.toPInt()
                val bpl = data.getByte(9)!!.toPInt()

                runOnUiThread {
                    valueHealth.text =
                        "$bp " + getString(R.string.bpm) + " | $bpl/$bph " + getString(R.string.mmHg) + " | $sp %"
                }

                if (bp != 0) {
                    dbHandler.insertHeart(
                        HeartData(
                            calendar.get(Calendar.YEAR) - 2000,
                            calendar.get(Calendar.MONTH) + 1,
                            calendar.get(Calendar.DAY_OF_MONTH),
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            bp,
                            H_APP
                        )
                    )
                }
                if (bph != 0) {
                    dbHandler.insertBp(
                        PressureData(
                            calendar.get(Calendar.YEAR) - 2000,
                            calendar.get(Calendar.MONTH) + 1,
                            calendar.get(Calendar.DAY_OF_MONTH),
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            bph,
                            bpl,
                            H_APP
                        )
                    )
                }
                if (sp != 0) {
                    dbHandler.insertSp02(
                        OxygenData(
                            calendar.get(Calendar.YEAR) - 2000,
                            calendar.get(Calendar.MONTH) + 1,
                            calendar.get(Calendar.DAY_OF_MONTH),
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            sp,
                            H_APP
                        )
                    )
                }
            }

            healthList = when (viewH) {
                0 -> {
                    dbHandler.getHeart()
                }
                1 -> {
                    dbHandler.getBp()
                }
                2 -> {
                    dbHandler.getSp02()
                }
                else -> {
                    dbHandler.getHeart()
                }
            }

            healthAdapter.update(healthList)
        }

    }

    class ProgressBarAnimation(
        private val progressBar: ProgressBar,
        private val from: Float,
        private val to: Float
    ) :
        Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
            super.applyTransformation(interpolatedTime, t)
            val value = from + (to - from) * interpolatedTime
            progressBar.progress = value.toInt()
        }
    }
}