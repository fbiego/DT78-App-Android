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

import android.content.DialogInterface
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.fbiego.dt78.app.CrashLogger
import com.fbiego.dt78.app.SettingsActivity
import com.fbiego.dt78.data.UserListAdapter
import com.fbiego.dt78.data.myTheme
import kotlinx.android.synthetic.main.activity_functions.*
import com.fbiego.dt78.app.SettingsActivity as ST
import com.fbiego.dt78.app.ForegroundService as FG


class FunctionsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        setTheme(myTheme(pref.getInt(SettingsActivity.PREF_ACCENT, 0)))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_functions)

        Thread.setDefaultUncaughtExceptionHandler(CrashLogger(this))

        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)
    }

    override fun onResume() {
        super.onResume()

        val pref = PreferenceManager.getDefaultSharedPreferences(this)

        val names = arrayListOf(pref.getString(ST.PREF_FUNC_PREV, "None")!!,
            pref.getString(ST.PREF_FUNC_PLAY, "None")!!,
            pref.getString(ST.PREF_FUNC_NEXT, "None")!!)
        val states = arrayListOf<Boolean?>(null, null, null)
        val icons = arrayListOf(R.drawable.ic_func_prev, R.drawable.ic_func_play, R.drawable.ic_func_next)

        val appList = ArrayList<String>()
        val apps: MutableList<ApplicationInfo> = ArrayList()

        apps.add(ApplicationInfo())
        appList.add("None")

        functionsList.visibility = View.GONE
        progressBar3.visibility = View.VISIBLE

        Thread {
            val installedApps =
                this.packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

            installedApps.sortWith { a, b ->
                val nameA = this.packageManager.getApplicationLabel(a).toString()
                val nameB = this.packageManager.getApplicationLabel(b).toString()
                nameA.compareTo(nameB)
            }

            for (i in installedApps.indices) {
                if (this.packageManager.getLaunchIntentForPackage(installedApps[i].packageName) != null) {
                    //If you're here, then this is a launch-able app
                    apps.add(installedApps[i])
                    appList.add("${installedApps[i].loadLabel(packageManager)} (${installedApps[i].packageName})")
                }
            }
            runOnUiThread {
                progressBar3.visibility = View.GONE
                functionsList.visibility = View.VISIBLE
            }
        }.start()


        val list = UserListAdapter(this, icons, names, null, states)
        functionsList.adapter = list
        functionsList.setOnItemClickListener { adapterView, view, i, l ->
            val builder =  AlertDialog.Builder(this)
            builder.setTitle("Make a choice from the list:")
            builder.setCancelable(false)
            builder.setItems(appList.toTypedArray()) { dialog, item ->
                Toast.makeText(
                    applicationContext,
                    "Selection: $item",
                    Toast.LENGTH_SHORT
                ).show()
                val pack = if (item == 0) "None"  else apps[item].packageName
                when(i){
                    0 -> {
                        pref.edit().putString(ST.PREF_FUNC_PREV, pack).apply()
                        FG.f_prev = pack
                    }
                    1 ->  {
                        pref.edit().putString(ST.PREF_FUNC_PLAY,pack).apply()
                        FG.f_play = pack
                    }
                    2 -> {
                        pref.edit().putString(ST.PREF_FUNC_NEXT, pack).apply()
                        FG.f_next = pack
                    }
                }
                names[i] = pack
                list.notifyDataSetChanged()

            }
            val alert: AlertDialog = builder.create()
            alert.show()
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
}