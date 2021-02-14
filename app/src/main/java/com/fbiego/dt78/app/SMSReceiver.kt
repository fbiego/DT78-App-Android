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
