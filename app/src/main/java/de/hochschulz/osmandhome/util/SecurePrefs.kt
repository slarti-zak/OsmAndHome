package de.hochschulz.osmandhome.util

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.core.content.edit
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object SecurePrefs {
    private const val FILE = "ha_tracker_secure"
    private const val KEY_ALIAS = "ha_tracker_aes_key"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val GCM_TAG_LENGTH_BITS = 128

    private fun prefs(ctx: Context): SharedPreferences =
        ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val existingKey = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
        if (existingKey != null) return existingKey

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )

        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()

        keyGenerator.init(keyGenParameterSpec)
        return keyGenerator.generateKey()
    }

    private fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())

        val iv = cipher.iv
        val cipherBytes = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))

        val payload = JSONObject().apply {
            put("iv", Base64.encodeToString(iv, Base64.NO_WRAP))
            put("data", Base64.encodeToString(cipherBytes, Base64.NO_WRAP))
        }

        return payload.toString()
    }

    private fun decrypt(payload: String): String? {
        return runCatching {
            val json = JSONObject(payload)
            val iv = Base64.decode(json.getString("iv"), Base64.NO_WRAP)
            val data = Base64.decode(json.getString("data"), Base64.NO_WRAP)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateSecretKey(), spec)

            val plainBytes = cipher.doFinal(data)
            String(plainBytes, StandardCharsets.UTF_8)
        }.getOrNull()
    }

    fun get(ctx: Context, key: String): String? {
        val encrypted = prefs(ctx).getString(key, null) ?: return null
        return decrypt(encrypted)
    }

    fun put(ctx: Context, key: String, value: String) {
        prefs(ctx).edit { putString(key, encrypt(value)) }
    }

    fun remove(ctx: Context, key: String) {
        prefs(ctx).edit { remove(key) }
    }

    fun contains(ctx: Context, key: String): Boolean =
        prefs(ctx).contains(key)

    fun getBool(ctx: Context, key: String, default: Boolean = false): Boolean =
        get(ctx, key)?.toBooleanStrictOrNull() ?: default

    fun putBool(ctx: Context, key: String, value: Boolean) {
        put(ctx, key, value.toString())
    }

    fun getLong(ctx: Context, key: String, default: Long = 0L): Long =
        get(ctx, key)?.toLongOrNull() ?: default

    fun putLong(ctx: Context, key: String, value: Long) {
        put(ctx, key, value.toString())
    }
}