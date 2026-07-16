package com.shortsblocker.app

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        val enableButton = findViewById<Button>(R.id.enableButton)

        enableButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }

    override fun onResume() {
        super.onResume()
        statusText.text = if (isServiceEnabled()) {
            "Status: ACTIVE — Shorts will be auto-blocked"
        } else {
            "Status: NOT ENABLED — tap below to turn it on"
        }
    }

    private fun isServiceEnabled(): Boolean {
        val expectedComponent = "$packageName/${ShortsBlockerService::class.java.canonicalName}"
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val splitter = TextUtils.SimpleStringSplitter(':')
        splitter.setString(enabledServices)
        while (splitter.hasNext()) {
            if (splitter.next().equals(expectedComponent, ignoreCase = true)) {
                return true
            }
        }
        return false
    }
}
