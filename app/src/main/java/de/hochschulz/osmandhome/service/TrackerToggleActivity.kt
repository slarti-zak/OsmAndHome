package de.hochschulz.osmandhome.service

import android.app.Activity
import android.content.Intent
import android.os.Bundle

class TrackerToggleActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // No setContentView — never draw anything

        when (intent?.action) {
            TrackerToggleActivity.ACTION_START ->
                startForegroundService(Intent(this, HaLocationService::class.java))
            TrackerToggleActivity.ACTION_STOP ->
                stopService(Intent(this, HaLocationService::class.java))
        }

        // Return to OsmAnd immediately — don't let this task surface
        moveTaskToBack(true)
        finish()
    }

    override fun onResume() {
        super.onResume()
        finish()
    }

    companion object {
        const val ACTION_START = "de.hochschulz.osmandhome.START_TRACKER"
        const val ACTION_STOP  = "de.hochschulz.osmandhome.STOP_TRACKER"
    }
}