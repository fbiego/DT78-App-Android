package com.fbiego.dt78.app

interface ProgressListener {
    fun onProgress(progress: Int, text: String)
}