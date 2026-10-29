package com.rabbyx.newtonixairevivededition

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient

class CustomWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {
    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setWebContentsDebuggingEnabled(false);
        }
    }
    private var startX = 0f
    private var startY = 0f
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop

    // List of allowed domains (both http and https)
    private val allowedDomains = setOf(
        "youtrack.jetbrains.com",
        "www.googleapis.com",
        "facebook.com",
        "x.com",
        "microsoft.com",
        "google.com",
        "www.unoosa.org",
        "cldr.unicode.org",
        "www.unicode.org",
        "unitsofmeasure.org",
        "openid.net",
        "library-example.googleapis.com",
        "example.cloudfunctions.net",
        "example.appspot.com",
        "www.iana.org",
        "kubernetes.io",
        "www.rfc-editor.org",
        "pubsub.googleapis.com",
        "docs.python.org",
        "semver.org",
        "jqueryui.com",
        "web.chemdoodle.com",
        "www.chemdoodle.com",
        "www.ichemlabs.com",
        "cdnjs.cloudflare.com",
        "mathquill.com",
        "unixpapa.com",
        "jsbin.com",
        "reference.sitepoint.com",
        "latex.wikia.com",
        "www.ams.org",
        "msdn.microsoft.com",
        "users.teilar.gr",
        "tinyurl.com",
        "www.falstad.com",
        "lushprojects.com",
        "www.opensource.org",
        "html.spec.whatwg.org",
        "www.whatwg.org",
        "www.dropbox.com",
        "polymer.github.io",
        "freesvg.org",
        "hexi.wokwi.com",
        "ww1.microchip.com",
        "fontello.com",
        "scripts.sil.org",
        "www.entypo.com",
        "thedesignoffice.org",
        "aristeides.com",
        "www.flaticon.com",
        "www.freepikcompany.com",
        "fsf.org",
        "wwwchem.uwimona.edu.jm",
        "iza-structure.org",
        "mmcif.pdb.org",
        "jquery.com",
        "sizzlejs.com",
        "jsperf.com",
        "bugs.jquery.com",
        "web.archive.org",
        "fluidproject.org",
        "example.com",
        "stackoverflow.com",
        "bimixual.org",
        "local.wasp.uwa.edu.au",
        "paulirish.com",
        "goo.gl",
        "app-measurement.com",
        "sketchfab.com",
        "cdn.jsdelivr.net",
        "threejs.org",
        "api.cloudinary.com",
        "www.slf4j.org",
        "firebaseinstallations.googleapis.com",
        "generativelanguage.googleapis.com",
        "www.recaptcha.net",
        "polyfill.io",
        "mathjax.org",
        "cdnjs.cloudflare.com",
        "cdn.jsdelivr.net",
        "marked.js.org"
    )

    // List of allowed IP addresses
    private val allowedIps = setOf(
        "127.0.0.3"
    )

    init {
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        settings.javaScriptEnabled = true

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            settings.allowContentAccess = true
        }

        webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url ?: return true
                return !isUrlAllowed(url.toString())
            }

            @Deprecated("Deprecated in API level 24")
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                return url == null || !isUrlAllowed(url)
            }
        }
    }

    private fun isUrlAllowed(url: String): Boolean {
        return try {
            val uri = Uri.parse(url)
            val host = uri.host ?: return false

            // Check if the host is an allowed IP address
            if (allowedIps.contains(host)) {
                return true
            }

            // Check if the domain is in our allowed list or is a subdomain of an allowed domain
            allowedDomains.any { allowedDomain ->
                host == allowedDomain || host.endsWith(".$allowedDomain")
            }
        } catch (e: Exception) {
            false
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y
                parent?.requestDisallowInterceptTouchEvent(true)
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaX = Math.abs(event.x - startX)
                val deltaY = Math.abs(event.y - startY)
                if (deltaX > deltaY && deltaX > touchSlop) {
                    parent?.requestDisallowInterceptTouchEvent(true)
                } else if (deltaY > deltaX && deltaY > touchSlop) {
                    parent?.requestDisallowInterceptTouchEvent(false)
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                parent?.requestDisallowInterceptTouchEvent(false)
            }
        }
        return super.onTouchEvent(event)
    }
}


