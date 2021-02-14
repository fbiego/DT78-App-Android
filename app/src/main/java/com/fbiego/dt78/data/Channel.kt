package com.fbiego.dt78.data

class Channel(
    var icon : Int,
    var app: String,
    var filters: ArrayList<String>
)
{
    private fun filterList(): String{
        var txt = ""
        if (filters.isNotEmpty()){
            filters.forEach {
                txt = txt + sp + it
            }
        }
        return txt
    }

    fun formatted(): String {
        return "" + icon + sc + app + filterList()
    }

    fun hasFilters(): Boolean{
        return filters.isNotEmpty()
    }

    fun hideScreen(): Boolean{
        return filters.contains(sON)
    }
}