package com.fbiego.dt78

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import android.view.View
import androidx.annotation.RequiresApi
import androidx.preference.PreferenceManager
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.fbiego.dt78.data.getColorFromAttr
import com.fbiego.dt78.data.myTheme
import kotlinx.android.synthetic.main.activity_about.*
import com.fbiego.dt78.app.SettingsActivity as ST

class AboutActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        setTheme(myTheme(pref.getInt(ST.PREF_ACCENT, 0)))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)


        val actionbar = supportActionBar
        actionbar!!.setDisplayHomeAsUpEnabled(true)


        webView.loadUrl("file:///android_asset/terms.html")
        termsCard.backgroundTintList = ColorStateList.valueOf(this.getColorFromAttr(R.attr.colorCardBackgroundLight))
        title = "Terms & Conditions"

        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)){
            WebSettingsCompat.setForceDark(webView.settings, WebSettingsCompat.FORCE_DARK_AUTO)
        }

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun change(view: View){
        when (view.id){
            R.id.policyCard -> {
                webView.loadUrl("file:///android_asset/policy.html")
                title = "Privacy Policy"
                policyCard.backgroundTintList = ColorStateList.valueOf(this.getColorFromAttr(R.attr.colorCardBackgroundLight))
                termsCard.backgroundTintList = ColorStateList.valueOf(this.getColorFromAttr(R.attr.colorCardBackgroundDark))
            }

            R.id.termsCard -> {
                webView.loadUrl("file:///android_asset/terms.html")
                title = "Terms & Conditions"
                termsCard.backgroundTintList = ColorStateList.valueOf(this.getColorFromAttr(R.attr.colorCardBackgroundLight))
                policyCard.backgroundTintList = ColorStateList.valueOf(this.getColorFromAttr(R.attr.colorCardBackgroundDark))
            }
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
}