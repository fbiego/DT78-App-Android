package com.fbiego.dt78.app

import android.content.Context
import android.util.Log
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class FileLog(private val context: Context, private val file: String, private val level: Int): Timber.Tree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {

        if (priority == level) {
            try {
                val directory = context.cacheDir

                if (!directory.exists())
                    directory.mkdirs()


                val file = File(directory, file)

                file.createNewFile()

                if (file.exists()) {
                    val fos = FileOutputStream(file, true)

                    fos.write("$message\n".toByteArray(Charsets.UTF_8))
                    fos.close()
                }

            } catch (e: IOException){
                Log.println(Log.ERROR,"FileLogTree", "Error while logging into file: $e")
            }
        }


    }
}