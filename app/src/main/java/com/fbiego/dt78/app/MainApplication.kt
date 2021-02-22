package com.fbiego.dt78.app

import android.content.Context
import android.content.SharedPreferences
import androidx.multidex.MultiDexApplication
import android.util.Log
import com.fbiego.dt78.BuildConfig
import timber.log.Timber

/**
 *
 */
class MainApplication : MultiDexApplication() {

    companion object {
        const val PREFS_KEY_ALLOWED_PACKAGES = "PREFS_KEY_ALLOWED_PACKAGES"
        lateinit var sharedPrefs: SharedPreferences
    }

    override fun onCreate() {
        super.onCreate()

        sharedPrefs = getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            //Timber.plant(FileLog(this, "timber.txt"))
        }
        Timber.plant(FileLog(this, "error.txt", Log.ERROR))
    }


}