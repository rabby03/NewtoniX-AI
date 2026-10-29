package com.rabbyx.newtonixairevivededition.interfaces

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import com.rabbyx.newtonixairevivededition.CustomWebView
import com.rabbyx.newtonixairevivededition.MainActivity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.charset.Charset
import android.app.DownloadManager
import android.os.Environment
import android.webkit.URLUtil
import android.widget.Toast

class HelperUtil(private val chatContainer: LinearLayout) {

    internal fun welcomeText() {
        val welcomeHtml = """
            <html>
            <head>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        margin: 0;
                        padding: 8px;
                        background: linear-gradient(45deg, #ffffff, #e0e0e0, #b0c4de, #f0f8ff); /* Light mode animated gradient */
                        background-size: 200% 200%;
                        animation: backgroundShift 10s ease infinite;
                        transition: background 0.5s ease;
                        overflow-x: hidden;
                        display: flex;
                        justify-content: center;
                        align-items: flex-start;
                        min-height: 100vh;
                        fallback-background: #f0f0f0; /* Fallback color */
                    }
                    @media (prefers-color-scheme: dark) {
                        body {
                            background: linear-gradient(45deg, #2f4f4f, #4a4a4a, #708090, #2f4f4f); /* Dark mode animated gradient */
                        }
                        p {
                            color: #ddd; /* Dark mode text color */
                        }
                        .gradient-text {
                            background: linear-gradient(45deg, #64b5f6, #81c784, #ffd54f, #ef5350); /* Adjusted for dark mode */
                            -webkit-background-clip: text;
                            background-clip: text;
                            color: transparent;
                        }
                    }
                    @keyframes backgroundShift {
                        0% { background-position: 0% 50%; }
                        50% { background-position: 100% 50%; }
                        100% { background-position: 0% 50%; }
                    }
                    .container {
                        display: flex;
                        flex-wrap: wrap;
                        max-width: 1200px;
                        width: 100%;
                        gap: 20px;
                        justify-content: space-between;
                        align-items: center;
                        position: relative;
                        z-index: 1; /* Above canvas */
                        padding: 20px;
                    }
                    .text-content {
                        flex: 1;
                        min-width: 300px;
                        text-align: center;
                    }
                    .model-content {
                        flex: 0 0 300px; /* Fixed width for model viewer */
                        min-width: 300px;
                        text-align: center;
                    }
                    .gradient-text {
                        font-size: 30px;
                        font-weight: bold;
                        background: linear-gradient(45deg, #4285f4, #34a853, #fbbc05, #ea4335);
                        -webkit-background-clip: text;
                        background-clip: text;
                        color: transparent;
                        animation: gradientShift 5s ease infinite;
                        display: inline-block;
                        background-size: 200% 200%;
                    }
                    #dynamic-text {
                        font-size: 30px;
                        min-height: 40px; /* Prevent layout shift */
                    }
                    @keyframes gradientShift {
                        0% { background-position: 0% 0%; }
                        25% { background-position: 100% 0%; }
                        50% { background-position: 100% 100%; }
                        75% { background-position: 0% 100%; }
                        100% { background-position: 0% 0%; }
                    }
                    p {
                        font-size: 16px;
                        color: #333;
                    }
                    model-viewer {
                        width: 300px;
                        height: 300px;
                        background-color: transparent;
                        border: 2px solid #4285f4;
                        border-radius: 10px;
                        --poster-color: transparent;
                    }
                    model-viewer:hover {
                        border-color: #fbbc05;
                        box-shadow: 0 0 10px rgba(251, 188, 5, 0.5);
                    }
                    @media (max-width: 768px) {
                        .container {
                            flex-direction: column;
                            align-items: center;
                        }
                        .model-content {
                            flex: 1;
                        }
                    }
                    #heartCanvas {
                        width: 100vw;
                        height: 100vh;
                        position: fixed;
                        top: 0;
                        left: 0;
                        z-index: -1; /* Behind content */
                    }
                </style>
                <script type="module" src="https://unpkg.com/@google/model-viewer/dist/model-viewer.min.js"></script>
            </head>
            <body>
                <canvas id="heartCanvas"></canvas>
                <div class="container">
                    <div class="text-content">
                        <h2 class="gradient-text">Welcome to <br/>NewtoniX AI<sub><small>Revived</small></sub><pre>Powered by RABBYX</pre></h2>
                        <h1 class="gradient-text"><span id="dynamic-text"></span></h1><br/>
                        <p><font color="#F44336">Feel free to ask questions, upload images, or explore mathematical models!</font></p>
                        <p><font color="#F44336">আপনি নির্দ্বিধায় গণিত, গাণিতিক পদার্থবিজ্ঞান, বা গাণিতিক রসায়ন সম্পর্কে প্রশ্ন জিজ্ঞাসা করতে পারেন, ছবি আপলোড করতে পারেন, বা মডেল অন্বেষণ করতে পারেন!</font></p>
                    </div>
                   
                </div>

                <script src="https://cdnjs.cloudflare.com/ajax/libs/three.js/r128/three.min.js"></script>
                <script src="https://cdn.jsdelivr.net/npm/three@0.128.0/examples/js/loaders/GLTFLoader.js"></script>
                <script>
                    // Dynamic typing animation
                    const textElement = document.getElementById('dynamic-text');
                    const texts = ['Mathematics', 'Mathematical Physics', 'Mathematical Chemistry'];
                    let textIndex = 0;
                    let charIndex = 0;
                    let isDeleting = false;

                    function type() {
                        const currentText = texts[textIndex];
                        if (!isDeleting) {
                            textElement.textContent = currentText.substring(0, charIndex);
                            charIndex++;
                            if (charIndex > currentText.length) {
                                isDeleting = true;
                                setTimeout(type, 1500); // Pause before deleting
                            } else {
                                setTimeout(type, 100); // Typing speed
                            }
                        } else {
                            textElement.textContent = currentText.substring(0, charIndex);
                            charIndex--;
                            if (charIndex < 0) {
                                isDeleting = false;
                                textIndex = (textIndex + 1) % texts.length;
                                setTimeout(type, 500); // Pause before next text
                            } else {
                                setTimeout(type, 50); // Deleting speed
                            }
                        }
                    }
                    type();

                    // Three.js setup for background
                    if (window.WebGLRenderingContext) {
                        const canvas = document.getElementById('heartCanvas');
                        const scene = new THREE.Scene();
                        const camera = new THREE.PerspectiveCamera(75, window.innerWidth / window.innerHeight, 0.1, 1000);
                        const renderer = new THREE.WebGLRenderer({ canvas: canvas, alpha: true });
                        renderer.setSize(window.innerWidth, window.innerHeight);

                        window.addEventListener('resize', () => {
                            camera.aspect = window.innerWidth / window.innerHeight;
                            camera.updateProjectionMatrix();
                            renderer.setSize(window.innerWidth, window.innerHeight);
                        });

                        const ambientLight = new THREE.AmbientLight(0xffffff, 0.5);
                        scene.add(ambientLight);
                        const pointLight = new THREE.PointLight(0xffffff, 0.5);
                        pointLight.position.set(5, 5, 5);
                        scene.add(pointLight);

                        const geometry = new THREE.TorusGeometry(5, 2, 16, 100);
                        const material = new THREE.MeshBasicMaterial({ color: 0x4285f4, wireframe: true });
                        const torus = new THREE.Mesh(geometry, material);
                        scene.add(torus);

                        camera.position.z = 15;

                        function animate() {
                            requestAnimationFrame(animate);
                            torus.rotation.y += 0.01;
                            torus.rotation.x += 0.005;
                            renderer.render(scene, camera);
                        }
                        animate();
                    } else {
                        console.error('WebGL is not supported.');
                    }
                </script>
            </body>
            </html>
        
            
        """.trimIndent()

        val welcomeWebView = CustomWebView(chatContainer.context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(16, 16, 16, 16) }
            settings.javaScriptEnabled = true
            loadDataWithBaseURL(null, welcomeHtml, "text/html", "UTF-8", null)
        }
        welcomeWebView.setBackgroundColor(Color.TRANSPARENT)
        chatContainer.addView(welcomeWebView)
    }

    internal fun welcomeTextBiology() {
        val welcomeHtml = """
            <html>
            <head>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        margin: 0;
                        padding: 8px;
                        background: linear-gradient(45deg, #ffffff, #e0e0e0, #b0c4de, #f0f8ff); /* Light mode animated gradient */
                        background-size: 200% 200%;
                        animation: backgroundShift 10s ease infinite;
                        transition: background 0.5s ease;
                        overflow-x: hidden;
                        fallback-background: #f0f0f0; /* Fallback color if gradient fails */
                    }
                    @media (prefers-color-scheme: dark) {
                        body {
                            background: linear-gradient(45deg, #2f4f4f, #4a4a4a, #708090, #2f4f4f); /* Dark mode animated gradient */
                        }
                        p {
                            color: #ddd; /* Dark mode text color */
                        }
                    }
                    @keyframes backgroundShift {
                        0% { background-position: 0% 50%; }
                        50% { background-position: 100% 50%; }
                        100% { background-position: 0% 50%; }
                    }
                    .gradient-text {
                        font-size: 30px;
                        font-weight: bold;
                        background: linear-gradient(45deg, #4285f4, #34a853, #fbbc05, #ea4335);
                        -webkit-background-clip: text;
                        background-clip: text;
                        color: transparent;
                        animation: gradientShift 5s ease infinite;
                        display: inline-block;
                        background-size: 200% 200%;
                    }
                    @keyframes gradientShift {
                        0% { background-position: 0% 0%; }
                        25% { background-position: 100% 0%; }
                        50% { background-position: 100% 100%; }
                        75% { background-position: 0% 100%; }
                        100% { background-position: 0% 0%; }
                    }
                    p {
                        font-size: 16px;
                        color: #333;
                    }
                    #heartCanvas {
                        width: 100vw;
                        height: 100vh;
                        position: fixed;
                        top: 0;
                        left: 0;
                        z-index: -1; /* Place canvas behind content */
                    }
                    .content {
                        position: relative;
                        z-index: 1; /* Ensure content is above canvas */
                        text-align: center;
                        padding: 20px;
                    }
                </style>
            </head>
            <body>
                <canvas id="heartCanvas"></canvas>
                <div class="content">
                    <h2 class="gradient-text">Welcome to <br/>NewtoniX AI<sub><small>Revived</small></sub><pre>Powered by RABBYX</pre></h2>
                    <h1 class="gradient-text">Biology <b>/</b> <br/> Zoology/<br/>Anatomy</h1><br/>
                    <p><font color="#F44336">আপনি জৈবিক নামটি লিখুন অথবা ছবি আপলোড করুন, কিংবা একইসাথে জৈবিক নাম লিখে এবং ছবি আপলোড করুন!</font></p>
                </div>

                <!-- Fallback Sketchfab iframe (uncomment to use instead of Three.js) -->
                <!--
                <div class="sketchfab-embed-wrapper" style="width: 100vw; height: 100vh; position: fixed; top: 0; left: 0; z-index: -1;">
                    <iframe title="Heart" frameborder="0" allowfullscreen mozallowfullscreen="true" webkitallowfullscreen="true" allow="autoplay; fullscreen; xr-spatial-tracking" xr-spatial-tracking execution-while-out-of-viewport execution-while-not-rendered web-share src="https://sketchfab.com/models/6b867dc61b994318b23c12142288d48f/embed"></iframe>
                </div>
                -->

                <script src="https://cdnjs.cloudflare.com/ajax/libs/three.js/r128/three.min.js"></script>
                <script src="https://cdn.jsdelivr.net/npm/three@0.128.0/examples/js/loaders/GLTFLoader.js"></script>
                <script>
                    // Check WebGL support
                    if (!window.WebGLRenderingContext) {
                        console.error('WebGL is not supported on this device.');
                    } else {
                        console.log('WebGL is supported.');
                    }

                    // Three.js setup for 3D heart model
                    const canvas = document.getElementById('heartCanvas');
                    const scene = new THREE.Scene();
                    const camera = new THREE.PerspectiveCamera(75, window.innerWidth / window.innerHeight, 0.1, 1000);
                    const renderer = new THREE.WebGLRenderer({ canvas: canvas, alpha: true });
                    renderer.setSize(window.innerWidth, window.innerHeight);

                    // Adjust canvas size on window resize
                    window.addEventListener('resize', () => {
                        camera.aspect = window.innerWidth / window.innerHeight;
                        camera.updateProjectionMatrix();
                        renderer.setSize(window.innerWidth, window.innerHeight);
                    });

                    // Lighting
                    const ambientLight = new THREE.AmbientLight(0xffffff, 0.5);
                    scene.add(ambientLight);
                    const pointLight = new THREE.PointLight(0xffffff, 0.5);
                    pointLight.position.set(5, 5, 5);
                    scene.add(pointLight);

                    // Load heart model
                    const loader = new THREE.GLTFLoader();
                    loader.load(
                        './bio/human_heart.glb', // Relative path; resolved via base URL file:///android_asset/
                        (gltf) => {
                            console.log('Model loaded successfully.');
                            const heart = gltf.scene;
                            heart.scale.set(3.0, 3.0, 3.0); // Larger scale for full-screen background
                            scene.add(heart);

                            // Animation for spinning
                            function animate() {
                                requestAnimationFrame(animate);
                                heart.rotation.y += 0.01; // Spin around Y-axis
                                renderer.render(scene, camera);
                            }
                            animate();
                        },
                        (xhr) => {
                            console.log((xhr.loaded / xhr.total * 100) + '% loaded');
                        },
                        (error) => {
                            console.error('Error loading model:', error);
                        }
                    );

                    camera.position.z = 10; // Adjusted for larger model and full-screen view
                </script>
            </body>
            </html>
        """.trimIndent()
        val welcomeWebView = CustomWebView(chatContainer.context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(16, 16, 16, 16) }
            settings.javaScriptEnabled = true
            settings.cacheMode = WebSettings.LOAD_NO_CACHE
            settings.domStorageEnabled = true
            settings.allowFileAccess = true
            setBackgroundColor(Color.TRANSPARENT)

            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                allowFileAccess = true
                allowContentAccess = true
                cacheMode = WebSettings.LOAD_NO_CACHE
            }

            // Enable WebView debugging
            loadDataWithBaseURL(
                "file:///android_asset/",
                welcomeHtml,
                "text/html",
                "UTF-8",
                null
            )
        }
        welcomeWebView.setBackgroundColor(Color.TRANSPARENT)
        chatContainer.addView(welcomeWebView)
    }

    internal fun chemistryDoodle() {
        val welcomeWebView = CustomWebView(chatContainer.context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            ).apply {
                setPadding(5, 5, 5, 5)
            }

            settings.javaScriptEnabled = true
            settings.cacheMode = WebSettings.LOAD_NO_CACHE
            settings.domStorageEnabled = true
            settings.allowFileAccess = true
            setBackgroundColor(Color.TRANSPARENT)

            MainActivity.isMole = true
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view as CustomWebView, url)
                    Log.d("ChemWebView", "Page loaded: $url")
                }
            }

            val inputHtml = chatContainer.context.assets.open("chem/samples/full.html").bufferedReader()
                .use { it.readText() }

            loadDataWithBaseURL(
                "file:///android_asset/chem/samples/",
                inputHtml,
                "text/html",
                "UTF-8",
                null
            )
        }

        welcomeWebView.addJavascriptInterface(AndroidBridge(chatContainer.context), "AndroidBridge")
        chatContainer.addView(welcomeWebView)
    }

    internal fun electricalDoodle(baseContext: Context) {
        val welcomeWebView = CustomWebView(chatContainer.context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.FILL_PARENT
            ).apply {
                setPadding(0, 5, 5, 5)
            }

            settings.javaScriptEnabled = true
            settings.cacheMode = WebSettings.LOAD_NO_CACHE
            settings.domStorageEnabled = true
            settings.allowFileAccess = true
            setBackgroundColor(Color.TRANSPARENT)


            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view as CustomWebView, url)
                    view?.evaluateJavascript(
                        """
                    (function() {
                        function sendCircuitData() {
                            try {
                                var htmlData = document.documentElement.outerHTML;
                                var circuitData = window.Circuit && window.Circuit.dump 
                                    ? window.Circuit.dump() 
                                    : '';
                                AndroidBridge.saveCircuitData({
                                    html: htmlData,
                                    circuit: circuitData
                                });
                            } catch (e) {
                                console.error('Error capturing circuit data: ' + e);
                            }
                        }
                        setInterval(sendCircuitData, 1000);
                        if (window.Circuit && window.Circuit.onChange) {
                            window.Circuit.onChange = sendCircuitData;
                        }
                    })();
                    """.trimIndent(),
                        null
                    )
                }
            }

            // Set DownloadListener to handle file downloads
            setDownloadListener { url, userAgent, contentDisposition, mimeType, contentLength ->
                // Extract filename from contentDisposition or URL
                val fileName = contentDisposition?.let {
                    val regex = Regex("filename=\"(.+?)\"")
                    regex.find(it)?.groupValues?.get(1)
                } ?: URLUtil.guessFileName(url, contentDisposition, mimeType)

                val request = DownloadManager.Request(Uri.parse(url)).apply {
                    setMimeType(mimeType)
                    setTitle(fileName)
                    setDescription("Downloading file...")
                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                    allowScanningByMediaScanner()
                }

                val downloadManager = baseContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                downloadManager.enqueue(request)
                Toast.makeText(baseContext, "Downloading $fileName", Toast.LENGTH_SHORT).show()
            }

            loadUrl("http://127.0.0.3:8000/")
        }

        welcomeWebView.addJavascriptInterface(AndroidBridge(chatContainer.context), "AndroidBridge")
        chatContainer.addView(welcomeWebView)
    }


    internal fun latexKeyboardMath() {
        val welcomeWebView = CustomWebView(chatContainer.context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 16, 16, 16)
            }

            settings.javaScriptEnabled = true
            settings.cacheMode = WebSettings.LOAD_NO_CACHE
            setBackgroundColor(Color.TRANSPARENT)

            val inputHtml = chatContainer.context.assets.open("input.html").bufferedReader()
                .use { it.readText() }

            loadDataWithBaseURL(
                "file:///android_asset/",
                inputHtml,
                "text/html",
                "UTF-8",
                null
            )
        }

        welcomeWebView.addJavascriptInterface(AndroidBridge(chatContainer.context), "AndroidBridge")
        chatContainer.addView(welcomeWebView)
    }

    inner class AndroidBridge(private val context: Context) {
        @JavascriptInterface
        fun onLatexReceived(latex: String) {
            if (latex.isNullOrBlank() || latex.length > 1000) {
                Log.w("AndroidBridge", "Invalid LaTeX input")
                return
            }
            val sanitizedLatex = sanitizeInput(latex)

            if (context is MainActivity) {
                context.runOnUiThread {
                    MainActivity.messageEditText.append(sanitizedLatex)
                    Log.d("AndroidBridge", "LaTeX received: $sanitizedLatex")
                }
            }
        }

        @JavascriptInterface
        fun sendMOLData(molData: String) {
            if (molData.isNullOrBlank() || molData.length > 10000) {
                Log.w("AndroidBridge", "Invalid MOL data")
                return
            }
            if (context is MainActivity) {
                context.runOnUiThread {
                    MainActivity.molDoodleCode = molData
                    MainActivity.isMole = true
                    Log.d("AndroidBridge", "MOL data received: $molData")
                }
            }
        }

        @JavascriptInterface
        fun sendScreenshot(screenshotData: String) {
            if (screenshotData.isNullOrBlank() || screenshotData.length > 5000000) {
                Log.w("AndroidBridge", "Invalid screenshot data")
                return
            }
            if (context is MainActivity) {
                context.runOnUiThread {
                    Log.d("AndroidBridge", "Screenshot received, length: ${screenshotData.length}")
                    val uri = context.base64ToUri(screenshotData)
                    if (uri != null) {
                        context.setSelectedImageUri(uri)
                        context.displayScreenshot(uri)
                        Log.d("AndroidBridge", "Screenshot Uri set: $uri")
                    } else {
                        Log.e("AndroidBridge", "Failed to convert screenshot to Uri")
                        (context as MainActivity).runOnUiThread {
                            android.widget.Toast.makeText(context, "Failed to process screenshot", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        @JavascriptInterface
        fun saveCircuitData(data: String) {
            if (data.isNullOrBlank() || data.length > 100000) {
                Log.w("AndroidBridge", "Invalid circuit data")
                return
            }

            Thread {
                try {
                    val file = File(context.filesDir, "circuit_data.txt")
                    FileOutputStream(file).use { fos ->
                        val content = try {
                            val json = org.json.JSONObject(data)
                            json.optString("circuit", json.optString("html", ""))
                        } catch (e: Exception) {
                            Log.w("AndroidBridge", "Invalid JSON format, using sanitized fallback")
                            sanitizeInput(data)
                        }
                        fos.write(content.toByteArray(Charset.forName("UTF-8")))
                    }
                    Log.d("AndroidBridge", "Circuit data saved to ${file.absolutePath}")
                } catch (e: IOException) {
                    Log.e("AndroidBridge", "Error saving circuit data: ${e.message}")
                }
            }.start()
        }

        private fun sanitizeInput(input: String): String {
            return input
        }
    }
}