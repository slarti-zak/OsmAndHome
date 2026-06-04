package de.hochschulz.osmandhome.ui

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.hochschulz.osmandhome.api.HaState
import de.hochschulz.osmandhome.api.RetrofitClient
import de.hochschulz.osmandhome.config.TrackedEntityConfig
import de.hochschulz.osmandhome.config.TrackedEntityRepository
import de.hochschulz.osmandhome.service.HaLocationService
import de.hochschulz.osmandhome.util.AppPrefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsState(
    val serverUrl: String = "",
    val token: String = "",
    val intervalMs: Long = 60_000L,
    val autostart: Boolean = false,
    val trustSsl: Boolean = false,
    val removeOnStop: Boolean = true
)

data class DiscoveryUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val allStates: List<HaState> = emptyList(),
    val configs: Map<String, TrackedEntityConfig> = emptyMap(),
    val query: String = ""
) {
    val filtered: List<HaState>
        get() = allStates.filter { it.attributes.hasLocation }.let { list ->
            if (query.isBlank()) list
            else list.filter {
                it.attributes.friendlyName?.contains(query, true) == true || it.entityId.contains(
                    query,
                    true
                )
            }
        }
}

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val ctx get() = getApplication<Application>()

    // Service
    private val _running = MutableStateFlow(false)
    val serviceRunning: StateFlow<Boolean> = _running

    fun startService() {
        val url = AppPrefs.getServerUrl(ctx)
        val token = AppPrefs.getToken(ctx)
        if (url.isBlank() || token.isBlank()) {
            _error.value = "Configure server URL and token first"; return
        }
        ctx.startForegroundService(Intent(ctx, HaLocationService::class.java))
        _running.value = true; _error.value = null
    }

    fun stopService() {
        ctx.stopService(Intent(ctx, HaLocationService::class.java))
        _running.value = false
    }

    // Error snackbar
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    fun clearError() {
        _error.value = null
    }

    // Settings
    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<SettingsState> = _settings

    private fun loadSettings() = SettingsState(
        serverUrl = AppPrefs.getServerUrl(ctx),
        token = AppPrefs.getToken(ctx),
        intervalMs = AppPrefs.getIntervalMs(ctx),
        autostart = AppPrefs.getAutostart(ctx),
        trustSsl = AppPrefs.getTrustSsl(ctx),
        removeOnStop = AppPrefs.getRemoveOnStop(ctx)
    )

    fun saveSettings(s: SettingsState) {
        AppPrefs.setServerUrl(ctx, s.serverUrl)
        AppPrefs.setToken(ctx, s.token)
        AppPrefs.setIntervalMs(ctx, s.intervalMs)
        AppPrefs.setAutostart(ctx, s.autostart)
        AppPrefs.setTrustSsl(ctx, s.trustSsl)
        AppPrefs.setRemoveOnStop(ctx, s.removeOnStop)
        _settings.value = s
    }

    // Discovery
    private val _discovery = MutableStateFlow(DiscoveryUiState())
    val discovery: StateFlow<DiscoveryUiState> = _discovery

    fun setQuery(q: String) {
        _discovery.update { it.copy(query = q) }
    }

    fun loadEntities() {
        val url = AppPrefs.getServerUrl(ctx)
        val token = "Bearer ${AppPrefs.getToken(ctx)}"
        val trust = AppPrefs.getTrustSsl(ctx)
        if (url.isBlank()) {
            _discovery.update { it.copy(error = "Server URL not configured") }
            return
        }
        _discovery.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                val states = RetrofitClient.get(url, trust).getAllStates(token)
                val configs = TrackedEntityRepository.getAll(ctx).associateBy { it.entityId }
                _discovery.update {
                    it.copy(
                        loading = false,
                        allStates = states,
                        configs = configs
                    )
                }
            } catch (e: Exception) {
                _discovery.update { it.copy(loading = false, error = e.message) }
            }
        }
    }

    private fun refreshConfigs() {
        _discovery.update {
            it.copy(
                configs = TrackedEntityRepository.getAll(ctx).associateBy { c -> c.entityId })
        }
    }

    fun setEntityEnabled(entityId: String, enabled: Boolean) {
        TrackedEntityRepository.setEnabled(ctx, entityId, enabled)
        refreshConfigs()
    }

    fun upsertEntityConfig(cfg: TrackedEntityConfig) {
        TrackedEntityRepository.upsert(ctx, cfg)
        refreshConfigs()
    }

    fun enableAll() {
        TrackedEntityRepository.enableAll(
            ctx, _discovery.value.allStates
                .filter { it.attributes.hasLocation }.map { it.entityId })
        refreshConfigs()
    }

    fun disableAll() {
        TrackedEntityRepository.disableAll(ctx)
        refreshConfigs()
    }

    fun resetToDefaults() {
        TrackedEntityRepository.resetToDefaults(
            ctx, _discovery.value.allStates
                .filter { it.attributes.hasLocation }.map { it.entityId })
        refreshConfigs()
    }
}