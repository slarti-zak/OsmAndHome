package de.hochschulz.osmandhome.util

import android.content.Context

object AppPrefs {
    fun getServerUrl(ctx: Context)             = SecurePrefs.get(ctx, "server_url") ?: ""
    fun setServerUrl(ctx: Context, v: String)  = SecurePrefs.put(ctx, "server_url", v)
    fun getToken(ctx: Context)                 = SecurePrefs.get(ctx, "token") ?: ""
    fun setToken(ctx: Context, v: String)      = SecurePrefs.put(ctx, "token", v)
    fun getIntervalMs(ctx: Context)            = SecurePrefs.getLong(ctx, "interval_ms", 60_000L)
    fun setIntervalMs(ctx: Context, v: Long)   = SecurePrefs.putLong(ctx, "interval_ms", v)
    fun getAutostart(ctx: Context)             = SecurePrefs.getBool(ctx, "autostart")
    fun setAutostart(ctx: Context, v: Boolean) = SecurePrefs.putBool(ctx, "autostart", v)
    fun getTrustSsl(ctx: Context)              = SecurePrefs.getBool(ctx, "trust_ssl")
    fun setTrustSsl(ctx: Context, v: Boolean)  = SecurePrefs.putBool(ctx, "trust_ssl", v)
    fun getRemoveOnStop(ctx: Context)          = SecurePrefs.getBool(ctx, "remove_on_stop", true)
    fun setRemoveOnStop(ctx: Context, v: Boolean) = SecurePrefs.putBool(ctx, "remove_on_stop", v)
    fun getOsmAndPackage(ctx: Context)         = SecurePrefs.get(ctx, "osmand_pkg")
    fun setOsmAndPackage(ctx: Context, v: String) = SecurePrefs.put(ctx, "osmand_pkg", v)
}