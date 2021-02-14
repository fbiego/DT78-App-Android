package com.fbiego.dt78.data

class StepsData(
    var year: Int,
    var month: Int,
    var day: Int,
    var hour: Int,
    var steps: Int,
    var calories: Int
) {
    var id: Long = 0

    init {
        this.id = (hour+ (day*100) + month*10000 + (year*1000000)).toLong()
    }
}