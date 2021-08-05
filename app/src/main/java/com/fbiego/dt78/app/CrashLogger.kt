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
import java.io.FileOutputStream
import java.io.IOException


class CrashLogger(val app: Activity): Thread.UncaughtExceptionHandler {
    private var defaultUEH: Thread.UncaughtExceptionHandler? = null

    init {
        defaultUEH = Thread.getDefaultUncaughtExceptionHandler()
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        var arr = e.stackTrace
        var report = "$e\n"
        report += "--------- Stack trace ---------\n\n"
        for (i in arr.indices) {
            report += "${arr[i]}"
        }
        report += "-------------------------------\n\n"

        // If the exception was thrown in a background thread inside
        // AsyncTask, then the actual exception can be found with getCause
        report += "--------- Cause ---------\n\n"
        val cause = e.cause
        if (cause != null) {
            report += "$cause\n"
            arr = cause.stackTrace
            for (i in arr.indices) {
                report += "${arr[i]}"
            }
        }
        report += "-------------------------------\n\n"
        try {
            val trace: FileOutputStream = app.openFileOutput(
                "stack.trace",
                Context.MODE_PRIVATE
            )
            trace.write(report.toByteArray())
            trace.close()
        } catch (ioe: IOException) {
            // ...
        }
        defaultUEH!!.uncaughtException(t, e)
    }
}