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