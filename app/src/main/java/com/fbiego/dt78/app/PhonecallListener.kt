package com.fbiego.dt78.app

interface PhonecallListener {
    fun callReceived(caller: String)
    fun callEnded()
}