package de.hochschulz.osmandhome.util

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object SecurePrefs {
    private const val FILE = "ha_tracker_secure"

    private fun prefs(ctx: Context): SharedPreferences {
        val mk = MasterKey.Builder(ctx).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
        return EncryptedSharedPreferences.create(
            ctx, FILE, mk,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun get(ctx: Context, key: String): String?           = prefs(ctx).getString(key, null)
    fun put(ctx: Context, key: String, v: String)         = prefs(ctx).edit().putString(key, v).apply()
    fun getBool(ctx: Context, key: String, d: Boolean = false) = prefs(ctx).getBoolean(key, d)
    fun putBool(ctx: Context, key: String, v: Boolean)    = prefs(ctx).edit().putBoolean(key, v).apply()
    fun getLong(ctx: Context, key: String, d: Long = 0L)  = prefs(ctx).getLong(key, d)
    fun putLong(ctx: Context, key: String, v: Long)       = prefs(ctx).edit().putLong(key, v).apply()
}