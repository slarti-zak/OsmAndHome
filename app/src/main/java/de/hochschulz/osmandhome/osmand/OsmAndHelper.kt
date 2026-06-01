package de.hochschulz.osmandhome.osmand

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.util.Log
import de.hochschulz.osmandhome.api.HaState
import de.hochschulz.osmandhome.config.TrackedEntityConfig
import de.hochschulz.osmandhome.service.TrackerToggleActivity
import de.hochschulz.osmandhome.util.AppPrefs
import de.hochschulz.osmandhome.util.ColorAssigner
import net.osmand.aidlapi.IOsmAndAidlInterface
import net.osmand.aidlapi.map.ALatLon
import net.osmand.aidlapi.maplayer.AMapLayer
import net.osmand.aidlapi.maplayer.AddMapLayerParams
import net.osmand.aidlapi.maplayer.RemoveMapLayerParams
import net.osmand.aidlapi.maplayer.UpdateMapLayerParams
import net.osmand.aidlapi.maplayer.point.AMapPoint
import net.osmand.aidlapi.maplayer.point.AddMapPointParams
import net.osmand.aidlapi.maplayer.point.RemoveMapPointParams
import net.osmand.aidlapi.maplayer.point.UpdateMapPointParams
import net.osmand.aidlapi.mapwidget.AMapWidget
import net.osmand.aidlapi.mapwidget.AddMapWidgetParams
import net.osmand.aidlapi.mapwidget.UpdateMapWidgetParams
import java.text.SimpleDateFormat
import java.util.*

class OsmAndHelper(private val ctx: Context) {

    private var aidl: IOsmAndAidlInterface? = null
    private val activeIds = mutableSetOf<String>()
    var onConnected: (() -> Unit)? = null
    var onDisconnected: (() -> Unit)? = null
    val isConnected get() = aidl != null

    companion object {
        const val LAYER_ID = "ha_people_layer"
        const val LAYER_NAME = "Home Assistant Entities"
        val PACKAGES = listOf("net.osmand.plus", "net.osmand", "net.osmand.dev")
        private const val TAG = "OsmAndHelper"
    }

    private val conn = object : ServiceConnection {
        override fun onServiceConnected(n: ComponentName, b: IBinder) {
            val aidl = IOsmAndAidlInterface.Stub.asInterface(b)
            this@OsmAndHelper.aidl = aidl
            runCatching {
                if (aidl != null) {
                    val layer = AMapLayer(LAYER_ID, LAYER_NAME, 5.5f, null)
                    layer.isImagePoints = true
                    if (!aidl.updateMapLayer(UpdateMapLayerParams(layer))) {
                        aidl.addMapLayer(AddMapLayerParams(layer))
                    }
                    registerWidget(true)
                }
            }
            onConnected?.invoke()
        }

        override fun onServiceDisconnected(n: ComponentName) {
            aidl = null; onDisconnected?.invoke()
        }
    }

    fun bind(): Boolean {
        val pkg = AppPrefs.getOsmAndPackage(ctx) ?: detect() ?: return false
        val flags = if (Build.VERSION.SDK_INT >= 34)
            Context.BIND_AUTO_CREATE or Context.BIND_ALLOW_ACTIVITY_STARTS
        else
            Context.BIND_AUTO_CREATE
        return ctx.bindService(
            Intent("net.osmand.aidl.OsmandAidlServiceV2").apply { `package` = pkg },
            conn, flags
        )
    }

    fun unbind() {
        if (aidl != null) {
            if (AppPrefs.getRemoveOnStop(ctx))
                runCatching { aidl?.removeMapLayer(RemoveMapLayerParams(LAYER_ID)) }
            runCatching { ctx.unbindService(conn) }
            aidl = null; activeIds.clear()
        }
    }

    fun upsertMarker(state: HaState, cfg: TrackedEntityConfig?) {
        val lat = state.attributes.latitude ?: return
        val lon = state.attributes.longitude ?: return
        val iface = aidl ?: return

        val name = cfg?.displayName?.takeIf { it.isNotBlank() }
            ?: state.attributes.friendlyName ?: state.entityId
        val color = cfg?.customColor ?: ColorAssigner.forEntityArgb(state.entityId)
        val initials = name.split(" ").take(2)
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("")
            .ifEmpty { name.take(2).uppercase() }

        // details is List<String> in the real OsmAnd API
        val details = buildList {
            add("State: ${state.state}")
            state.attributes.batteryLevel?.let { add("Battery: $it%") }
            state.attributes.gpsAccuracy?.let { add("Accuracy: ${"%.0f".format(it)} m") }
            add("Updated: ${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())}")
            add("Entity: ${state.entityId}")
        }

        // params is Map<String,String> — use for extra metadata
        val params = mapOf("entity_id" to state.entityId)

        val point = AMapPoint(
            state.entityId,          // pointId
            initials,                // shortName — shown on map bubble
            name,                    // fullName — shown in context menu header
            "Home Assistant",        // typeName — shown in context menu subheader
            LAYER_ID,
            color,
            ALatLon(lat, lon),
            details,
            params
        )

        runCatching {
            if (state.entityId in activeIds) {
                // updateMapPoint keeps the menu open and follows the point
                iface.updateMapPoint(UpdateMapPointParams(LAYER_ID, point, true))
            } else {
                iface.addMapPoint(AddMapPointParams(LAYER_ID, point))
                activeIds.add(state.entityId)
            }
        }.onFailure { Log.e(TAG, "upsert failed for ${state.entityId}", it) }
    }

    fun removeMarker(id: String) {
        runCatching {
            aidl?.removeMapPoint(RemoveMapPointParams(LAYER_ID, id))
            activeIds.remove(id)
        }
    }

    fun refresh() {
        runCatching { aidl?.refreshMap() }
    }

    fun currentIds() = activeIds.toSet()

    fun detect(): String? {
        val byPackage = PACKAGES.firstOrNull {
            runCatching { ctx.packageManager.getPackageInfo(it, 0); true }.getOrDefault(false)
        }
        if (byPackage != null) return byPackage
        val intent = Intent("net.osmand.aidl.OsmandAidlService")
        return PACKAGES.firstOrNull { pkg ->
            intent.`package` = pkg
            ctx.packageManager.resolveService(intent, 0) != null
        }
    }

    fun registerWidget(running: Boolean) {
        val iface = aidl ?: return
        val action = if (running) TrackerToggleActivity.ACTION_STOP
        else TrackerToggleActivity.ACTION_START

        // Fully explicit: component = package + class name — no intent-filter resolution needed
        val intent = Intent().apply {
            component = ComponentName(
                ctx.packageName,
                TrackerToggleActivity::class.java.name   // "de.hochschulz.osmandhome.service.TrackerToggleActivity"
            )
            this.action = action
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_NO_ANIMATION       // ← suppress the task-switch animation
            )
        }
        val widget = AMapWidget(
            "ha_tracker_widget",
            "ic_action_location_color",
            "HA Tracker",
            "ic_action_location_color",
            "ic_action_location_color",
            if (running) "ON" else "OFF",
            "HA Tracker",
            25,
            intent
        )
        runCatching {
            //iface.removeMapWidget(RemoveMapWidgetParams("ha_tracker_widget"))
            if (!iface.updateMapWidget(UpdateMapWidgetParams(widget))) {
                iface.addMapWidget(AddMapWidgetParams(widget))
            }
        }
    }
}