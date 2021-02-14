package com.fbiego.dt78.data

import java.util.*

class ErrorData (
    var year: Int,
    var month: Int,
    var day: Int,
    var hour: Int,
    var minute: Int,
    var second: Int,
    var error: Int
) {
    var id: Long = 0

    init {
        val cal = Calendar.getInstance(Locale.getDefault())
        cal.set(Calendar.YEAR, year+2000)
        cal.set(Calendar.MONTH, month-1)
        cal.set(Calendar.DAY_OF_MONTH, day)
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        cal.set(Calendar.SECOND, second)
        this.id = cal.timeInMillis
    }
}