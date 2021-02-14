package com.fbiego.dt78.data

class OxygenData(
    var year: Int,
    var month: Int,
    var day: Int,
    var hour: Int,
    var minute: Int,
    var sp02: Int
) {
    var id: Long = 0

    init {
        this.id = (minute + (hour*100)+ (day*10000) + month*1000000 + (year*100000000)).toLong()
    }
}