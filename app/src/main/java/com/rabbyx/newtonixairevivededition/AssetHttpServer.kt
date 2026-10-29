package com.rabbyx.newtonixairevivededition

import android.content.Context
import fi.iki.elonen.NanoHTTPD
import java.io.BufferedInputStream

class AssetNanoHttpServer(
    private val context: Context,
    port: Int = 8000
) : NanoHTTPD("127.0.0.3", port) {

    private val basePath = "eee/site"

    override fun serve(session: IHTTPSession): Response {
        val uri = session.uri.removePrefix("/")
        val filePath = if (uri.isEmpty()) "circuitjs.html" else uri
        val assetPath = "$basePath/$filePath"

        var response: Response

        try {
            val inputStream = BufferedInputStream(context.assets.open(assetPath))
            val bytes = inputStream.readBytes()
            val mimeType = getMimeType(filePath)
            response = newFixedLengthResponse(Response.Status.OK, mimeType, bytes.inputStream(), bytes.size.toLong())

            response.addHeader(
                "Content-Security-Policy",
                "default-src 'self'; " +
                        "script-src 'self' 'unsafe-inline' 'unsafe-eval' https://cdnjs.cloudflare.com https://www.dropbox.com https://www.falstad.com http://hyperphysics.phy-astr.gsu.edu http://en.wikipedia.org https://web.archive.org http://schemas.microsoft.com/ http://www.gnu.org/ https://github.com/ http://127.0.0.3:8000 https://lushprojects.com; " +
                        "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com https://fonts.gstatic.com https://www.falstad.com https://www.dropbox.com; " +
                        "img-src 'self' data: https://example.com http://hyperphysics.phy-astr.gsu.edu http://en.wikipedia.org https://web.archive.org http://schemas.microsoft.com/ http://s05.flagcounter.com https://www.falstad.com http://www.memphis.edu http://www.gnu.org/ https://github.com/ https://lushprojects.com https://www.dropbox.com/; " +
                        "font-src 'self' https://fonts.gstatic.com; " +
                        "frame-src 'self' http://hyperphysics.phy-astr.gsu.edu http://en.wikipedia.org https://web.archive.org http://schemas.microsoft.com/ https://www.falstad.com http://www.gnu.org/ https://github.com/ http://127.0.0.3:8000 https://lushprojects.com https://www.dropbox.com/; " +
                        "connect-src 'self' https://github.com/ http://127.0.0.3:8000 https://lushprojects.com https://www.dropbox.com/; " +
                        "manifest-src 'self';"
            )


        } catch (e: Exception) {
            response = newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "404 Not Found")
        }

        return response
    }

    private fun getMimeType(fileName: String): String {
        return when {
            fileName.endsWith(".html") -> "text/html"
            fileName.endsWith(".js") -> "application/javascript"
            fileName.endsWith(".css") -> "text/css"
            fileName.endsWith(".json") -> "application/json"
            fileName.endsWith(".png") -> "image/png"
            fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") -> "image/jpeg"
            fileName.endsWith(".svg") -> "image/svg+xml"
            fileName.endsWith(".wasm") -> "application/wasm"
            else -> "application/octet-stream"
        }
    }
}
