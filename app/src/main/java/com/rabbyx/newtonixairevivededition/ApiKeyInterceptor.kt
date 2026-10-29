package com.rabbyx.newtonixairevivededition.network

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger

class ApiKeyInterceptor(private val apiKeys: List<String>) : Interceptor {
    private val keyIndex = AtomicInteger(0)

    @Volatile
    var currentApiKey: String = apiKeys.first()

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request().newBuilder()
            .header("Authorization", "Bearer $currentApiKey")
            .build()

        var response = chain.proceed(request)

        if (response.code == 429) {
            response.close()
            rotateApiKey()
            request = chain.request().newBuilder()
                .header("Authorization", "Bearer $currentApiKey")
                .build()
            response = chain.proceed(request)
        }

        return response
    }

    private fun rotateApiKey() {
        val nextIndex = keyIndex.incrementAndGet() % apiKeys.size
        currentApiKey = apiKeys[nextIndex]
    }
}
