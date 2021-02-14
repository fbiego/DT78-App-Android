package com.fbiego.dt78.camera

import android.graphics.Bitmap
import android.os.Environment
import android.util.Log
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object Converters {


    // This subscription needs to be disposed off to release the system resources primarily held for purpose.

    @JvmStatic   // this annotation is required for caller class written in Java to recognize this method as static
    fun convertBitmapToFile(bitmap: Bitmap, onBitmapConverted: (File) -> Unit): Disposable {
        return Single.fromCallable {
            compressBitmap(bitmap)
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it != null) {
                    Timber.i(it.path)
                    onBitmapConverted(it)
                }
            }, { it.printStackTrace() })
    }

    private fun compressBitmap(bitmap: Bitmap): File? {
        //create a file to write bitmap data

        try {
            val myStuff =
                File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "DT78"
                )
            if (!myStuff.exists())
                myStuff.mkdirs()
            val picture = File(myStuff, "IMG-" + System.currentTimeMillis() + ".png")

            //Convert bitmap to byte array
            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100 /*ignored for PNG*/, bos)
            val bitmapData = bos.toByteArray()

            //write the bytes in file
            val fos = FileOutputStream(picture)
            fos.write(bitmapData)
            fos.flush()
            fos.close()
            return picture
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }


}
