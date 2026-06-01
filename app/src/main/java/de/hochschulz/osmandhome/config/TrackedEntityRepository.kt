package de.hochschulz.osmandhome.config

import android.content.Context
import de.hochschulz.osmandhome.util.SecurePrefs
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object TrackedEntityRepository {
    private const val KEY = "tracked_entities"
    private val gson = Gson()

    fun getAll(ctx: Context): List<TrackedEntityConfig> {
        val json = SecurePrefs.get(ctx, KEY) ?: return emptyList()
        return try {
            gson.fromJson(json, object : TypeToken<List<TrackedEntityConfig>>() {}.type) ?: emptyList()
        } catch (_: Exception) { emptyList() }
    }

    fun getEnabledEntityIds(ctx: Context): Set<String> =
        getAll(ctx).filter { it.enabled }.map { it.entityId }.toSet()

    fun save(ctx: Context, configs: List<TrackedEntityConfig>) =
        SecurePrefs.put(ctx, KEY, gson.toJson(configs))

    fun upsert(ctx: Context, config: TrackedEntityConfig) {
        val list = getAll(ctx).toMutableList()
        val i = list.indexOfFirst { it.entityId == config.entityId }
        if (i >= 0) list[i] = config else list.add(config)
        save(ctx, list)
    }

    fun setEnabled(ctx: Context, entityId: String, enabled: Boolean) {
        val list = getAll(ctx).toMutableList()
        val i = list.indexOfFirst { it.entityId == entityId }
        if (i >= 0) list[i] = list[i].copy(enabled = enabled)
        else list.add(TrackedEntityConfig(entityId = entityId, enabled = enabled))
        save(ctx, list)
    }

    fun enableAll(ctx: Context, ids: List<String>) {
        val existing = getAll(ctx).associateBy { it.entityId }
        save(ctx, ids.map { existing[it]?.copy(enabled = true) ?: TrackedEntityConfig(it) })
    }

    fun disableAll(ctx: Context) =
        save(ctx, getAll(ctx).map { it.copy(enabled = false) })

    fun resetToDefaults(ctx: Context, ids: List<String>) =
        save(ctx, ids.map { TrackedEntityConfig(entityId = it) })
}