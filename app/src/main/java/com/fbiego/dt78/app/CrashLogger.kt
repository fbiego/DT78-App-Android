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

import android.app.Activity
import android.content.Context
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


class CrashLogger(val app: Activity): Thread.UncaughtExceptionHandler {
    private var defaultUEH: Thread.UncaughtExceptionHandler? = null

    init {
        defaultUEH = Thread.getDefaultUncaughtExceptionHandler()
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        val cal = Calendar.getInstance(Locale.getDefault())
        var arr = e.stackTrace
        var report = "-------------------------------\n" +
                "Crash at ${cal.time}\n" +
                "-------------------------------\n" +
                "$e\n" +
                "--------- Stack trace ---------\n"
        for (i in arr.indices) {
            report += "${arr[i]}\n"
        }
        report += "-------------------------------\n"

        // If the exception was thrown in a background thread inside
        // AsyncTask, then the actual exception can be found with getCause
        report += "--------- Cause ---------\n"
        val cause = e.cause
        if (cause != null) {
            report += "$cause\n"
            arr = cause.stackTrace
            for (i in arr.indices) {
                report += "${arr[i]}\n"
            }
        }
        report += "-------------------------------\n"
        try {
            val dir = File(Environment.getExternalStoragePublicDirectory("DT78/crash").path)
            if (!dir.exists()){
                dir.mkdirs()
            }
            val crashFile = File(dir, "logs.txt")
            crashFile.createNewFile()
            crashFile.appendText(report)
//            val trace: FileOutputStream = app.openFileOutput(
//                "stack.trace",
//                Context.MODE_PRIVATE
//            )
//            trace.write(report.toByteArray())
//            trace.close()
        } catch (ioe: IOException) {
            // ...
        }
        defaultUEH!!.uncaughtException(t, e)
    }
}