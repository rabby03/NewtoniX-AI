package com.rabbyx.newtonixairevivededition.interfaces

import android.util.Log
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class HelperSketchFab(private val sketchFabAPi: String) {
    // Helper function to fetch Sketchfab model ID for a single component
    internal suspend fun fetchSketchfabModelId(query: String): String? {
        return try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val url = URL("https://api.sketchfab.com/v3/search?type=models&q=$encodedQuery&count=1")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Authorization", "Token $sketchFabAPi")
            connection.connect()

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = connection.inputStream
                val response = inputStream.bufferedReader().use { it.readText() }
                inputStream.close()

                val jsonResponse = JSONObject(response)
                val results = jsonResponse.getJSONArray("results")
                if (results.length() > 0) {
                    val model = results.getJSONObject(0)
                    model.getString("uid") // Return the model ID
                } else {
                    null
                }
            } else {
                Log.e("Sketchfab", "API request failed for '$query' with code: $responseCode")
                null
            }
        } catch (e: Exception) {
            Log.e("Sketchfab", "Error fetching Sketchfab model for '$query': ${e.message}")
            null
        }
    }
}