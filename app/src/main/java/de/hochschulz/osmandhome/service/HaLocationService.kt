package de.hochschulz.osmandhome.service

import android.app.*
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import de.hochschulz.osmandhome.api.RetrofitClient
import de.hochschulz.osmandhome.config.TrackedEntityRepository
import de.hochschulz.osmandhome.osmand.OsmAndHelper
import de.hochschulz.osmandhome.MainActivity
import de.hochschulz.osmandhome.util.AppPrefs
import kotlinx.coroutines.*

class HaLocationService : Service() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var osmAnd: OsmAndHelper
    private var pollJob: Job? = null

    companion object {
        const val CHANNEL_ID = "ha_tracker"
        const val NOTIF_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        osmAnd = OsmAndHelper(this).apply {
            onConnected = { startPolling() }
            onDisconnected = { pollJob?.cancel() }
        }
        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID, "HA Tracker", NotificationManager.IMPORTANCE_LOW
                ).apply { description = "Location tracking status" })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIF_ID, buildNotif("Connecting to OsmAnd…"))
        osmAnd.bind()
        osmAnd.registerWidget(true)
        return START_STICKY
    }

    override fun onDestroy() {
        osmAnd.registerWidget(false)

        pollJob?.cancel()
        scope.cancel()
        osmAnd.unbind()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startPolling() {
        pollJob?.cancel()
        pollJob = scope.launch {
            val token = "Bearer ${AppPrefs.getToken(this@HaLocationService)}"
            val url = AppPrefs.getServerUrl(this@HaLocationService)
            val trust = AppPrefs.getTrustSsl(this@HaLocationService)
            val interval = AppPrefs.getIntervalMs(this@HaLocationService)
            val api = RetrofitClient.get(url, trust)
            while (isActive) {
                try {
                    val enabledIds =
                        TrackedEntityRepository.getEnabledEntityIds(this@HaLocationService)
                    val configs = TrackedEntityRepository.getAll(this@HaLocationService)
                        .associateBy { it.entityId }
                    val toShow = api.getAllStates(token).filter {
                        it.attributes.hasLocation && (enabledIds.isEmpty() || it.entityId in enabledIds)
                    }
                    val toShowIds = toShow.map { it.entityId }.toSet()
                    osmAnd.currentIds().filterNot { it in toShowIds }
                        .forEach { osmAnd.removeMarker(it) }
                    toShow.forEach { osmAnd.upsertMarker(it, configs[it.entityId]) }
                    osmAnd.refresh()
                    notify("Tracking ${toShow.size} entities")
                } catch (e: Exception) {
                    notify("Error: ${e.message?.take(50)}")
                }
                delay(interval)
            }
        }
    }

    private fun buildNotif(text: String) = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("HA Tracker").setContentText(text)
        .setSmallIcon(android.R.drawable.ic_menu_mylocation)
        .setOngoing(true).setSilent(true)
        .setContentIntent(
            PendingIntent.getActivity(
                this, 0,
                Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
            )
        )
        .build()

    private fun notify(text: String) =
        getSystemService(NotificationManager::class.java).notify(NOTIF_ID, buildNotif(text))
}