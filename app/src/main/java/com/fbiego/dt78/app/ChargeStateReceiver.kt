package com.fbiego.dt78.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ChargeStateReceiver: BroadcastReceiver() {
    companion object {
        private lateinit var chargeListener: ChargeListener
        var isInit = false
        fun bindListener(listener: ChargeListener){
            chargeListener = listener
            isInit = true
        }
        fun unBindListener(){
            isInit = false
        }
    }

    override fun onReceive(p0: Context, p1: Intent) {

        when (p1.action) {
            Intent.ACTION_POWER_CONNECTED -> {
                if (isInit) {
                    chargeListener.chargerChanged(p0, true)
                }
            }
            Intent.ACTION_POWER_DISCONNECTED -> {
                if (isInit) {
                    chargeListener.chargerChanged(p0, false)
                }
            }
            Intent.ACTION_BATTERY_CHANGED -> {
                if (isInit){
                    chargeListener.batteryChanged(p0)
                }
            }
        }
    }


}