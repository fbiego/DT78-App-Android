package com.fbiego.dt78.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.preference.PreferenceManager
import timber.log.Timber

/**
 * Invoked after the system boots up
 */
class OnBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val startAtBoot = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(SettingsActivity.PREF_KEY_START_AT_BOOT, false)
        if (startAtBoot) {
            Timber.w("OnBoot received")
            // test without restarting using adb
            // .\adb shell su -c am broadcast -a android.intent.action.BOOT_COMPLETED -p com.fbiego.dt78
            Thread(Runnable {
                try {
                    // Lets wait for 30 seconds
                    Timber.w("Waiting for delay")
                    Thread.sleep((30 * 1000).toLong())
                } catch (e:InterruptedException) {
                    Timber.e("OnBoot error: $e")
                }
                // Start your application here
                val intentService = Intent(context, ForegroundService::class.java)
                intentService.putExtra("origin", "onBoot")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intentService)
                } else {
                    context.startService(intentService)
                }
                Timber.w("Service started")
            }).start()
        }

    }
}