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

import java.util.Date
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.telephony.TelephonyManager
import android.util.Log
import com.fbiego.dt78.MainActivity
import timber.log.Timber

class PhonecallReceiver: BroadcastReceiver() {

    override fun onReceive(context:Context, intent:Intent) {

        val stateStr = intent.extras?.getString(TelephonyManager.EXTRA_STATE)
        val number = intent.extras?.getString(TelephonyManager.EXTRA_INCOMING_NUMBER)
        var state = 0
        var name = number
        when (stateStr) {
            TelephonyManager.EXTRA_STATE_IDLE -> {
                state = TelephonyManager.CALL_STATE_IDLE
            }
            TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                state = TelephonyManager.CALL_STATE_OFFHOOK
            }
            TelephonyManager.EXTRA_STATE_RINGING -> {
                state = TelephonyManager.CALL_STATE_RINGING
            }
        }
        if (number != null){
            val lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number))
            var c: Cursor? = null
            try {
                c = context.contentResolver.query(lookupUri, arrayOf(ContactsContract.Data.DISPLAY_NAME), null, null, null)
                if (c?.moveToFirst()!!){
                    val displayName = c.getString(0)
                    name = displayName
                }
            } catch (e:Exception) {
                Timber.e("Caller Error: $e")
            } finally {
                c?.close()
            }
        }

        Timber.d( "name is $name")
        if (name != null){
            onCallStateChanged(state, name)
        }




    }
    //Derived classes should override these to respond to specific events of interest

    //Deals with actual events
    //Incoming call- goes from IDLE to RINGING when it rings, to OFFHOOK when it's answered, to IDLE when its hung up
    //Outgoing call- goes from IDLE to OFFHOOK when it dials out, to IDLE when hung up
    private fun onCallStateChanged(state:Int, number:String) {
        if (lastState == state) {
            //No change, debounce extras
            return
        }
        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> {
                isIncoming = true
                callStartTime = Date()
                savedNumber = number
                mListener.callReceived(number)
            }
            TelephonyManager.CALL_STATE_OFFHOOK ->
                //Transition of ringing->offhook are pickups of incoming calls. Nothing done on them
                if (lastState != TelephonyManager.CALL_STATE_RINGING) {
                    isIncoming = false
                    callStartTime = Date()
                    mListener.callEnded()
                } else {
                    isIncoming = true
                    callStartTime = Date()
                    mListener.callEnded()
                }
            TelephonyManager.CALL_STATE_IDLE ->
                //Went to idle- this is the end of a call. What type depends on previous state(s)
                when {
                    lastState == TelephonyManager.CALL_STATE_RINGING -> {
                        //Ring but no pickup- a miss
                        mListener.callEnded()
                    }
                    isIncoming -> {
                        mListener.callEnded()
                    }
                    else -> {
                        mListener.callEnded()
                    }
                }
        }
        lastState = state
    }
    companion object {
        //The receiver will be recreated whenever android feels like it. We need a static variable to remember data between instantiations
        private var lastState = TelephonyManager.CALL_STATE_IDLE
        private lateinit var callStartTime:Date
        private var isIncoming:Boolean = false
        private lateinit var savedNumber:String //because the passed incoming is only valid in ringing

        private lateinit var mListener:PhonecallListener
        fun bindListener(listener: PhonecallListener) {
            mListener = listener
        }
    }
}