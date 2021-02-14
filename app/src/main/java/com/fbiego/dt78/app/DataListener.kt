package com.fbiego.dt78.app

import no.nordicsemi.android.ble.data.Data

interface DataListener {
    fun onDataReceived(data: Data)
}