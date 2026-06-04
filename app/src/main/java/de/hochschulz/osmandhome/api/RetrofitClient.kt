package de.hochschulz.osmandhome.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object RetrofitClient {
    private var cache: HomeAssistantApi? = null
    private var cachedUrl: String? = null
    private var cachedTrust = false

    fun get(baseUrl: String, trustSelfSigned: Boolean = false): HomeAssistantApi {
        if (cache == null || cachedUrl != baseUrl || cachedTrust != trustSelfSigned) {
            cachedUrl = baseUrl; cachedTrust = trustSelfSigned
            cache = build(baseUrl, trustSelfSigned)
        }
        return cache!!
    }

    private fun build(baseUrl: String, trust: Boolean): HomeAssistantApi {
        val cb = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
        if (trust) {
            val tm = object : X509TrustManager {
                override fun checkClientTrusted(c: Array<X509Certificate>, a: String) {}
                override fun checkServerTrusted(c: Array<X509Certificate>, a: String) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
            }
            val sc = SSLContext.getInstance("TLS").apply {
                init(null, arrayOf<TrustManager>(tm), java.security.SecureRandom())
            }
            cb.sslSocketFactory(sc.socketFactory, tm).hostnameVerifier { _, _ -> true }
        }
        val url = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        return Retrofit.Builder().baseUrl(url).client(cb.build())
            .addConverterFactory(GsonConverterFactory.create()).build()
            .create(HomeAssistantApi::class.java)
    }
}