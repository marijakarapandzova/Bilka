package com.plantpulse.plantservice.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import java.security.cert.X509Certificate

@Configuration
class HttpClientConfig {

    @Bean
    fun restClient(): RestClient = RestClient.create()

    companion object {
        init {
            // Global SSL bypass for development
            try {
                // Disable SSL verification for all connections
                System.setProperty("jdk.internal.httpclient.disableHostnameVerification", "true")
                System.setProperty("com.sun.security.enableCRLDP", "false")
                System.setProperty("com.sun.net.ssl.checkRevocation", "false")

                val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                    override fun getAcceptedIssuers(): Array<X509Certificate>? = null
                    override fun checkClientTrusted(certs: Array<X509Certificate>, authType: String) {}
                    override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String) {}
                })

                val sslContext = SSLContext.getInstance("TLSv1.2")
                sslContext.init(null, trustAllCerts, java.security.SecureRandom())

                // Set for HttpsURLConnection (legacy)
                HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.socketFactory)
                HttpsURLConnection.setDefaultHostnameVerifier { _, _ -> true }

                // Also set as default for Java HttpClient
                SSLContext.setDefault(sslContext)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }
}
