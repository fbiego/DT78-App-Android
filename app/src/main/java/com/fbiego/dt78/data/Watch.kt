package com.fbiego.dt78.data


class Watch(var id: Int) {
    var name = when(id){
        DT78, DT78_2 -> "DT78"
        DT92 -> "DT92"
        DT66 -> "DT66"
        T03 -> "T03"
        ESP32 -> "ESP32"
        MI_AIR -> "MIBRO AIR"
        L_11 -> "L11"
        else -> "UNKNOWN"
    }

    var rtw = when(id){
        DT92, DT66, T03, DT78_2, MI_AIR, L_11 -> true
        else -> false
    }
    var line25 = when(id){
        DT78, DT66, DT78_2, T03, MI_AIR, L_11 -> true
        else -> false
    }

    var contact = when(id) {
        DT92, L_11 -> true
        else -> false
    }

    var lang = when(id){
        DT78 -> IntString(
            arrayListOf(0, 1, 3, 5, 6),
            arrayListOf("中文", "English", "Español", "русский", "日本人")
        )
        DT92, DT66, DT78_2, T03, MI_AIR, L_11 -> IntString(
            arrayListOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 10),
            arrayListOf("中文", "English", "Italiano", "Español", "Português", "русский", "日本人", "中文", "Deutsche", "ไทย")
        )
        ESP32 -> IntString(
            arrayListOf(5, 10, 20, 30), arrayListOf("5 sec", "10 sec", "20 sec", "30 sec")
        )
        else -> IntString(
            arrayListOf(1), arrayListOf("English")
        )
    }

    var iconSet = when(id){
        DT78 -> arrayListOf(0, 1, 2, 4, 5, 6, 7, 8, 9, 10, 11, 12)
        DT78_2, DT92, DT66, MI_AIR, L_11 -> arrayListOf(0, 1, 2, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14)
        T03 -> arrayListOf(0, 1, 2, 5, 6, 8, 9, 10, 11, 12)
        else -> arrayListOf(0)
    }

}