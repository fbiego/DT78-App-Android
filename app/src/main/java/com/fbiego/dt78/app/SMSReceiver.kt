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
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.telephony.SmsMessage
import com.fbiego.dt78.data.Watch
import timber.log.Timber
import com.fbiego.dt78.app.ForegroundService as FG

class SMSReceiver : BroadcastReceiver() {
    companion object {
        private lateinit var mListener:MessageListener
        fun bindListener(listener:MessageListener) {
            mListener = listener
        }
    }

    override fun onReceive(context:Context, intent:Intent) {

        val data = intent.extras
        val pdus = data?.get("pdus") as Array<*>
        for (i in pdus.indices) {
            val smsMessage = SmsMessage.createFromPdu(pdus[i] as ByteArray)
            var name = smsMessage.displayOriginatingAddress

            //Resolving the contact name from the contacts.
            val lookupUri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(smsMessage.displayOriginatingAddress)
            )
            var c: Cursor? = null
            try {
                c = context.contentResolver.query(
                    lookupUri,
                    arrayOf(ContactsContract.Data.DISPLAY_NAME),
                    null,
                    null,
                    null
                )

                if (c?.moveToFirst()!!){
                    val displayName = c.getString(0)
                    name = displayName
                }

            } catch (e: Exception) {
                Timber.e("SMS Error: $e")
            } finally {
                c?.close()
            }

            val message = ("Sender : " + smsMessage.displayOriginatingAddress
                    + "Email From: " + name
                    + "Email Body: " + smsMessage.emailBody
                    + "Display message body: " + smsMessage.displayMessageBody
                    + "Time in millisecond: " + smsMessage.timestampMillis
                    + "Message: " + smsMessage.messageBody)
            val msg = if (name.length < 25 && smsMessage.messageBody.length < 125 - 25 && Watch(FG.dt78).line25){
                for (x in 0 until 25-name.length){
                    name += " "
                }
                name += smsMessage.messageBody
                name
            } else {
                name += ": "+ smsMessage.messageBody
                name
            }
            mListener.messageReceived(msg)
        }

    }


}
