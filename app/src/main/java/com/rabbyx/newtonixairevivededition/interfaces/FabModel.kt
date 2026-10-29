package com.rabbyx.newtonixairevivededition.interfaces

class FabModel(val sketchfabModelId: String, val component: String) {
    val iframeHtml = """
                                <html>
                                <body style="margin: 0; padding: 0;">
                                    <p style="font-family: Arial, sans-serif; font-size: 16px; color: #333;">3D Model for $component:</p>
                                    <iframe 
                                        src="https://sketchfab.com/models/$sketchfabModelId/embed?autostart=1&preload=1" 
                                        width="100%" 
                                        height="480" 
                                        frameborder="0" 
                                        allowfullscreen 
                                        mozallowfullscreen="true" 
                                        webkitallowfullscreen="true">
                                    </iframe>
                                </body>
                                </html>
                            """.trimIndent()
}