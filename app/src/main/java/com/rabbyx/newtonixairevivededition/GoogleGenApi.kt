package com.rabbyx.newtonixairevivededition

import retrofit2.http.Body
import retrofit2.http.POST

interface GoogleGenApi {
    @POST("v1beta/models/gemini-1.5-pro:generateContent") // Multimodal model for images
    suspend fun generateImageContent(@Body request: GenerateContentRequest): GenerateContentResponse

    @POST("v1beta/models/gemini-2.0-flash:generateContent") // Hypothetical text model
    suspend fun generateTextContent(@Body request: GenerateContentRequest): GenerateContentResponse
}

// Data classes
data class GenerateContentRequest(
    val contents: List<Content>
)

data class Content(
    val parts: List<Part>
)

data class Part(
    val text: String? = null,           // For text input
    val inlineData: InlineData? = null  // For image input
)

data class InlineData(
    val mimeType: String,  // e.g., "image/png"
    val data: String       // Base64-encoded image
)

data class GenerateContentResponse(
    val candidates: List<Candidate>
)

data class Candidate(
    val content: Content
)