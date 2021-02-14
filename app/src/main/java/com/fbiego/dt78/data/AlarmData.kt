package com.fbiego.dt78.data

class AlarmData (
    var id: Int,
    var enable: Boolean,
    var hour: Int,
    var minute: Int,
    var repeat: Int
) {
    var name = "Alarm ${id + 1}"
}