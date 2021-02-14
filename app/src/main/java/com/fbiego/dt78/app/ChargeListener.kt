package com.fbiego.dt78.app

import android.content.Context

interface ChargeListener {
    fun chargerChanged(context: Context, state: Boolean)
    fun batteryChanged(context: Context)
}