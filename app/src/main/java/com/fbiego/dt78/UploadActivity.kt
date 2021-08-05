/*
 *
 * MIT License
 *
 * Copyright (c) 2021 Felix Biego
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.fbiego.dt78

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.ImageView.ScaleType
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.fbiego.dt78.app.CrashLogger
import com.fbiego.dt78.app.ProgressListener
import com.fbiego.dt78.app.ProgressReceiver
import com.fbiego.dt78.app.SettingsActivity
import com.fbiego.dt78.data.CRC16Modbus
import com.fbiego.dt78.data.byteArrayOfInts
import com.fbiego.dt78.data.myTheme
import kotlinx.android.synthetic.main.activity_upload.*
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import com.fbiego.dt78.app.ForegroundService as FG


class UploadActivity : AppCompatActivity(), ProgressListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        setTheme(myTheme(pref.getInt(SettingsActivity.PREF_ACCENT, 0)))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)

        Thread.setDefaultUncaughtExceptionHandler(CrashLogger(this))

        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)

        ProgressReceiver.bindListener(this)

        //ForegroundService().sendData(byteArrayOfInts(0xAB, 0x00, 0x04, 0xFF, 0x87, 0x80, 0x01))
        //ForegroundService().sendImage(0, this)

        //createFile(this, "out")
    }

    override fun onResume() {
        super.onResume()
        val directory = this.cacheDir
        //val upload = File(directory, "upload")

        val img = File(directory, "crop.png")
        if (img.exists()){
            image.setImageBitmap(BitmapFactory.decodeFile(img.path))
            buttonUpload.visibility = View.VISIBLE
        }


    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    private fun createFile(context: Context, byteArray: ByteArray, pos: Int, isChecksum: Boolean){
        try {
            val directory = context.cacheDir
            if (!directory.exists()) {
                directory.mkdirs()
            }

            val upload = File(directory, "upload")
            if (!upload.exists()){
                upload.mkdirs()
            }
            if (isChecksum){
                val check = File(upload, "check$pos.hex")
                if (check.exists()){
                    check.delete()
                }
                val fos = FileOutputStream(check, true)
                fos.write(byteArray)
                fos.close()
            } else {
                val file = File(upload, "out$pos.hex")

                if (file.exists()){
                    file.delete()
                }

                val fos = FileOutputStream(file, true)
                fos.write(byteArray)
                fos.close()
            }
        } catch (e: IOException){
            Timber.e("Create file error: $e")
        }

    }

    private fun loadCrop(){
        val directory = this.cacheDir
        val file = File(directory, "image.png")
        if (file.exists()){

            val uri = Uri.fromFile(file)
            performCrop(uri.path!!)
        }
    }

    private fun saveImage(context: Context, bitmap: Bitmap, crop: Boolean){
        try {
            val directory = context.cacheDir
            if (!directory.exists()) {
                directory.mkdirs()
            }

            val file = if (crop){
                File(directory, "image.png")
            } else {
                File(directory, "crop.png")
            }
            if (file.exists()){
                file.delete()

            }
            val fos = FileOutputStream(file, true)
            bitmap.compress(Bitmap.CompressFormat.PNG, 85, fos)
            fos.flush()
            fos.close()
        } catch (e: IOException){
            Timber.e("Create file error: $e")
        }

        if (crop) {
            loadCrop()
        }
    }

    private fun createBitmap(){
        val opts = BitmapFactory.Options()
        opts.inPreferredConfig = Bitmap.Config.RGB_565
        val out = BitmapFactory.decodeFile(File(this.cacheDir, "crop.png").path, opts)
        val buffer = ByteBuffer.allocate(115200)
        out.copyPixelsToBuffer(buffer)


        for(x in 0 until 112){
            val byteArray = ByteArray(1024)

            for (b in 0 until 1024){
                val y = if (b % 2 == 0){ b+1 } else { b-1 }
                byteArray[b] = buffer[(x*1024)+y]
            }
            saveDataFile(this, byteArray, x)
            percentProgress.text = "${(x.toFloat()/112)*100}%"
        }
        val btr = ByteArray(512)
        for (b in 0 until 512){
            val y = if (b % 2 == 0){ b+1 } else { b-1 }
            btr[b] = buffer[114688+y]
        }
        saveDataFile(this, btr, 112)



        FG().uploadFile(0, this)
        textProgress.text = getString(R.string.upload_image)
        //FG().createBitmap(0 , this)

    }

    private fun saveDataFile(context: Context, byteArray: ByteArray, pos: Int ){
        val checkSum = byteArrayOfInts(0xAD, 0x04, 0x00, 0x00, 0x00, 0x37, 0x9D, 0x00)
        val data = byteArrayOfInts(0xAE, 0x12, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
        val crc = CRC16Modbus()
        crc.update(byteArray)
        val sum = crc.crcBytes
        checkSum[3] = (pos / 256).toByte()
        checkSum[4] = (pos % 256).toByte()
        checkSum[5] = sum[1]
        checkSum[6] = sum[0]
        if (pos == 112){
            checkSum[1] = 0x02
            checkSum[7] = 0x01
        }
        createFile(context, checkSum, pos, true)
        if (pos == 112){
            val array = ByteArray(640)
            for (b in 0 until  32) {
                for (x in 0 until 16) {
                    data[x + 4] = byteArray[x + (b * 16)]
                }
                data[2] = ((b + 0x0E00)/ 256).toByte()
                data[3] = ((b + 0x0E00) % 256).toByte()
                for (y in 0 until 20){
                    array[(b*20)+y] = data[y]
                }
            }
            createFile(context, array, pos, false)
        } else {
            val array = ByteArray(1280)
            for (b in 0 until  64) {
                for (x in 0 until 16) {
                    data[x + 4] = byteArray[x + (b * 16)]
                }
                data[2] = ((b + pos) / 256).toByte()
                data[3] = ((b + pos) % 256).toByte()

                for (y in 0 until 20){
                    array[(b*20)+y] = data[y]
                }
            }
            createFile(context, array, pos, false)
        }
    }

    fun clicked(view: View){
        when (view.id){
            R.id.buttonUpload -> {

                if (FG().sendData(byteArrayOfInts(0xAB, 0x00, 0x04, 0xFF, 0x87, 0x80, 0x01))){
                    textProgress.text = getString(R.string.create_upload)
                    progressUpload.isIndeterminate = true
                    createBitmap()
                } else {
                    Toast.makeText(this, R.string.not_connect, Toast.LENGTH_SHORT).show()
                }


            }
            R.id.choose -> {
                if (checkExternal()){
                    val pickPhoto = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(pickPhoto, 31)
                } else {
                    requestExternal()
                }

            }
        }

    }
    private fun checkExternal(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ActivityCompat.checkSelfPermission( this@UploadActivity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            return false
        }
        return true
    }

    private fun requestExternal(){
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 56 )
    }

    private fun performCrop(picUri: String) {
        try {
            //Start Crop Activity
            val cropIntent = Intent("com.android.camera.action.CROP")
            // indicate image type and Uri
            val f = File(picUri)
            //val contentUri: Uri = Uri.fromFile(f)
            val contentUri: Uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", f)
            cropIntent.data = contentUri
            // set crop properties
            cropIntent.putExtra("crop", "true")
            // indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 1)
            cropIntent.putExtra("aspectY", 1)
            // indicate output X and Y
            cropIntent.putExtra("outputX", 240)
            cropIntent.putExtra("outputY", 240)

            // retrieve data on return
            cropIntent.putExtra("return-data", true)
            cropIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            cropIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri)
            // start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, 32)
        } // respond to users whose devices do not support the crop action
        catch (anfe: ActivityNotFoundException) {
            // display an error message
            val errorMessage = "your device doesn't support the crop action!"
            val toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT)
            toast.show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Timber.w("RequestCode= $requestCode, ResultCode= $resultCode, Data= ${data!=null}")
        if (resultCode == Activity.RESULT_OK){

            if (requestCode == 56){
                val pickPhoto = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(pickPhoto, 31)
            }
            if (data != null && requestCode == 31) {
                val selectedImage = data.data
                val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                if (selectedImage != null) {
                    val cursor = contentResolver.query(selectedImage,
                        filePathColumn, null, null, null)
                    if (cursor != null) {
                        cursor.moveToFirst()
                        val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                        val picturePath = cursor.getString(columnIndex)

                        val img = BitmapFactory.decodeFile(picturePath)
                        cursor.close()
                        saveImage(this, img, true)
                    }
                }
            }
            if (data != null && requestCode == 32){
                val uri = data.data
                if (uri != null){
                    val bmp = BitmapFactory.decodeFile(File(this.cacheDir, "image.png").path)
                    val out = Bitmap.createScaledBitmap(bmp, 240, 240, true)
                    image.setImageBitmap(out)
                    image.scaleType = ScaleType.FIT_XY
                    textProgress.text = getString(R.string.image_cropped)
                    buttonUpload.visibility = View.VISIBLE
                    saveImage(this, out, false)

                } else if (data.extras != null){
                    val selectedBitmap = data.extras?.getParcelable<Bitmap>("data")

                    if (selectedBitmap != null) {
                        image.setImageBitmap(selectedBitmap)
                        image.scaleType = ScaleType.FIT_XY
                        textProgress.text = getString(R.string.image_cropped)
                        buttonUpload.visibility = View.VISIBLE
                        saveImage(this, selectedBitmap, false)
                    }
                }


            }
        }
    }

    override fun onProgress(progress: Int, text: String) {
        runOnUiThread {
            progressUpload.isIndeterminate = false
            textProgress.text = text
            progressUpload.progress = progress
            percentProgress.text = "$progress%"
        }
    }
}