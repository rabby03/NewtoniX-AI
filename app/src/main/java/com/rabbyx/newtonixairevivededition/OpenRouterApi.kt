package com.rabbyx.newtonixairevivededition

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenRouterApi {
    @POST("chat/completions")
    suspend fun createChatCompletion(
        @Header("Authorization") apiKey: String,
        @Header("HTTP-Referer") referer: String,
        @Header("X-Title") title: String,
        @Body request: ChatRequest
    ): ChatResponse
}

data class ChatRequest(
    val model: String = "google/gemma-3-27b-it:free",
    val messages: List<ChatMessage>,
    val extra_body: Map<String, Any> = emptyMap()
)

data class ChatMessage(
    val role: String,
    val content: List<ChatMessageContent>
)

data class ChatMessageContent(
    val type: String,           // "text" or "image_url"
    val text: String? = null,   // For text content
    val image_url: ImageUrl? = null // For image content
)

data class ImageUrl(
    val url: String
)

data class ChatResponse(
    val choices: List<ChatChoice>
)

data class ChatChoice(
    val message: ChatMessageResponse
)

data class ChatMessageResponse(
    val content: String
)