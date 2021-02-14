package com.fbiego.dt78.data

class UserData(
    var id: Int,
    var name: String,
    var step: Int,
    var age: Int,
    var height: Int,
    var weight: Int,
    var target: Int
)  {
    fun data(): Array<String>{
        return arrayOf("$age yrs", "$step cm", "$height cm", "$weight kg", "$target steps")
    }

}