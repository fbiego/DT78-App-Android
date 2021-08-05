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
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaActionSound
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.fbiego.dt78.app.*
import com.fbiego.dt78.camera.Camera2
import com.fbiego.dt78.camera.Converters
import com.fbiego.dt78.data.byteArrayOfInts
import com.fbiego.dt78.data.myTheme
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_camera.*
import no.nordicsemi.android.ble.data.Data


class CameraActivity : AppCompatActivity(), DataListener{

    private lateinit var camera2: Camera2
    private var disposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        setTheme(myTheme(pref.getInt(SettingsActivity.PREF_ACCENT, 0)))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        Thread.setDefaultUncaughtExceptionHandler(CrashLogger(this))

        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)

        DataReceiver.bindListener(this)

        init()
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

    private fun init() {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        )

            initCamera2Api()
        else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                requestPermissions(
                    arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ), 3
                )
            else initCamera2Api()

        }

    }

    private fun initCamera2Api() {

        camera2 = Camera2(this, camera_view)

        iv_rotate_camera.setOnClickListener {
            camera2.switchCamera()
        }

        iv_capture_image.setOnClickListener { v ->
            camera2.takePhoto {
                Toast.makeText(v.context, "Saving Picture", Toast.LENGTH_SHORT).show()
                val audio = getSystemService(AUDIO_SERVICE) as AudioManager
                when (audio.ringerMode) {
                    AudioManager.RINGER_MODE_NORMAL -> {
                        val sound = MediaActionSound()
                        sound.play(MediaActionSound.SHUTTER_CLICK)
                    }
                    AudioManager.RINGER_MODE_SILENT -> {
                    }
                    AudioManager.RINGER_MODE_VIBRATE -> {
                    }
                }
                disposable = Converters.convertBitmapToFile(it) { file ->
                    Toast.makeText(v.context, "Saved Picture Path ${file.path}", Toast.LENGTH_SHORT).show()
                }

            }


        }

        iv_camera_flash_on.setOnClickListener {
            camera2.setFlash(Camera2.FLASH.ON)
            it.alpha = 1f
            iv_camera_flash_auto.alpha = 0.4f
            iv_camera_flash_off.alpha = 0.4f
        }


        iv_camera_flash_auto.setOnClickListener {
            iv_camera_flash_off.alpha = 0.4f
            iv_camera_flash_on.alpha = 0.4f
            it.alpha = 1f
            camera2.setFlash(Camera2.FLASH.AUTO)
        }

        iv_camera_flash_off.setOnClickListener {
            camera2.setFlash(Camera2.FLASH.OFF)
            it.alpha = 1f
            iv_camera_flash_on.alpha = 0.4f
            iv_camera_flash_auto.alpha = 0.4f

        }

    }


    override fun onPause() {
        //  cameraPreview.pauseCamera()
        camera2.close()
        super.onPause()
        ForegroundService().sendData(byteArrayOfInts(0xAB, 0x00, 0x04, 0xFF, 0x79, 0x80, 0x00))
    }

    override fun onResume() {
        // cameraPreview.resumeCamera()
        camera2.onResume()
        super.onResume()
        if (!ForegroundService().sendData(byteArrayOfInts(0xAB, 0x00, 0x04, 0xFF, 0x79, 0x80, 0x01))){
            Toast.makeText(this, R.string.not_connect, Toast.LENGTH_SHORT).show()
            //startActivity(Intent(this, MainActivity::class.javaObjectType))
            //overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

    }

    override fun onDestroy() {
        if (disposable != null)
            disposable!!.dispose()
        super.onDestroy()
    }



    override fun onDataReceived(data: Data) {
        if (data.size() == 7 && data.getByte(4) == (0x79).toByte()) {

            Toast.makeText(this, "Data Received", Toast.LENGTH_SHORT).show()

            camera2.takePhoto {
                Toast.makeText(this, "Saving Picture", Toast.LENGTH_SHORT).show()
                val audio = getSystemService(AUDIO_SERVICE) as AudioManager
                when (audio.ringerMode) {
                    AudioManager.RINGER_MODE_NORMAL -> {
                        val sound = MediaActionSound()
                        sound.play(MediaActionSound.SHUTTER_CLICK)
                    }
                    AudioManager.RINGER_MODE_SILENT -> {
                    }
                    AudioManager.RINGER_MODE_VIBRATE -> {
                    }
                }
                disposable = Converters.convertBitmapToFile(it) { file ->
                    Toast.makeText(this, "Saved Picture Path ${file.path}", Toast.LENGTH_SHORT)
                        .show()
                }

            }

        }
    }
}