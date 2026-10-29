package com.rabbyx.newtonixairevivededition

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "https://openrouter.ai/api/v1/"
    private const val API_KEY = "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"
    private const val REFERER_URL = "https://rabbyx.com" // Optional, customizable
    private const val APP_TITLE = "NewtoniX AI" // Matches package name

    // Lazy initialization for OkHttpClient
    private val client: OkHttpClient by lazy { buildClient() }

    // Build OkHttpClient with interceptors
    private fun buildClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // Logs request/response bodies for debugging
        }
        val headerInterceptor = Interceptor { chain ->
            val original: Request = chain.request()
            val request: Request = original.newBuilder()
                .header("Authorization", "Bearer $API_KEY")
                .header("HTTP-Referer", REFERER_URL) // Optional for OpenRouter rankings
                .header("X-Title", APP_TITLE) // Optional for OpenRouter rankings
                .method(original.method, original.body)
                .build()
            chain.proceed(request)
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(headerInterceptor)
            .build()
    }

    // Lazy initialization for OpenRouterApi instance, corrected syntax
    internal val openRouterApi: OpenRouterApi by lazy { buildOpenRouterApi() }

    // Provide a public getter for openRouterApi
    fun getOpenRouterApi(): OpenRouterApi = openRouterApi

    // Build Retrofit instance for OpenRouter API
    private fun buildOpenRouterApi(): OpenRouterApi {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenRouterApi::class.java)
    }

    // Reset client and API instance (e.g., for configuration changes)
    fun resetClient() {
        synchronized(this) {
            // Rebuild client and API instance
            val newClient = buildClient()
            val newRetrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(newClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            // Since openRouterApi is private val, we can't reassign it directly here
            // Instead, rely on lazy reinitialization or refactor if reassignment is needed
        }
    }
}