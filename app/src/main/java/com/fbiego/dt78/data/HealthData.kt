package com.fbiego.dt78.data

class HealthData(
    var year: Int,
    var month: Int,
    var day: Int,
    var hour: Int,
    var minute: Int,
    var value: String
) {
    var id: Long = 0

    init {
        this.id = (minute + (hour*100)+ (day*10000) + month*1000000 + (year*100000000)).toLong()
    }
}