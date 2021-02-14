package com.fbiego.dt78.data

import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import com.fbiego.dt78.AppsActivity
import com.fbiego.dt78.app.ForegroundService.Companion.dt78
import com.fbiego.dt78.app.MainApplication
import timber.log.Timber
import java.util.*

class AppsLoader internal constructor (activity: AppsActivity,private val new: Boolean): Runnable {

    private val mActivity: AppsActivity = activity

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun run() {


        val installedApps = mActivity.packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        installedApps.sortWith { a, b ->
            val nameA = mActivity.packageManager.getApplicationLabel(a).toString()
            val nameB = mActivity.packageManager.getApplicationLabel(b).toString()
            nameA.compareTo(nameB)
        }
        val names: Array<String> = installedApps.map { applicationInfo -> mActivity.packageManager.getApplicationLabel(applicationInfo).toString() }.toTypedArray()

        val prefsPackages: MutableSet<String> = MainApplication.sharedPrefs.getStringSet(
            MainApplication.PREFS_KEY_ALLOWED_PACKAGES, mutableSetOf())!!

        val appChannels = parseApps(prefsPackages, new)
        val prefsAllowedPackages = ArrayList<String>()
        if (appChannels.isNotEmpty()){
            appChannels.forEach {
                prefsAllowedPackages.add(it.app)
            }
        }
        //val channels = ArrayList<Int>()
        val fil = ArrayList<String>()
        val filterList = ArrayList<Channel>()
//        if (names.isNotEmpty()){
//            names.forEach { _ ->
//                channels.add(0)
//            }
//        }
        Timber.d("names = ${names.size}, appChannels = ${appChannels.size}, installedApps = ${installedApps.size}")
        val checkedItems = BooleanArray(installedApps.size)

        for (i in names.indices) {

            filterList.add(Channel(0, installedApps[i].packageName, fil))

            if (prefsAllowedPackages.contains(installedApps[i].packageName)){

                filterList[i].icon = appChannels[prefsAllowedPackages.indexOf(installedApps[i].packageName)].icon
                filterList[i].filters = appChannels[prefsAllowedPackages.indexOf(installedApps[i].packageName)].filters


                Timber.w("Icon0 = ${filterList[i].icon}")
                if (!Watch(dt78).iconSet.contains(filterList[i].icon)){
                    filterList[i].icon = 0
                }
                if (filterList[i].icon == 0){
                    filterList[i].icon = checkPackage(installedApps[i].packageName, Watch(dt78).iconSet)
                }
                Timber.w("Icon1 = ${filterList[i].icon}")

                //Timber.d("package: ${installedApps[i].packageName} channel: ${channels[i]}")
            }

            checkedItems[i] = prefsAllowedPackages.contains(installedApps[i].packageName)
        }



        val modifiedList: ArrayList<String> = arrayListOf()
        modifiedList.addAll(prefsAllowedPackages)

        mActivity.appsLoaded(names, installedApps, filterList, checkedItems)

    }
}