package de.hochschulz.osmandhome.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import de.hochschulz.osmandhome.util.AppPrefs

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED && AppPrefs.getAutostart(ctx))
            ctx.startForegroundService(Intent(ctx, HaLocationService::class.java))
    }
}