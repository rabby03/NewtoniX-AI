package com.rabbyx.newtonixairevivededition

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.rabbyx.newtonixairevivededition.interfaces.AvatarLoader
import com.rabbyx.newtonixairevivededition.interfaces.HTML
import com.rabbyx.newtonixairevivededition.interfaces.HelperUtil
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.UCropActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

open class MainActivity : AppCompatActivity() {
    private lateinit var latextKeyboardView: WebView
    private lateinit var topBarCard: CardView
    private lateinit var bottomCard: CardView
    private lateinit var fabButton: FloatingActionButton
    companion object {
        @JvmStatic
        lateinit var messageEditText: EditText
        var molDoodleCode: String = ""
        var isMole: Boolean = false
        var organicData: String = ""

    }
    fun getMolDoodleCode(): String {
        Log.d("MainActivity", "Fetching molDoodleCode: $molDoodleCode")
        return molDoodleCode
    }

    private lateinit var paymentMenu: MaterialButton
    private lateinit var sendButton: MaterialButton
    private lateinit var selectImageButton: MaterialButton
    private lateinit var takePhotoButton: MaterialButton
    private lateinit var clearTextButton: MaterialButton
    private lateinit var selectedImageView: ImageView
    private lateinit var imageContainer: FrameLayout
    private lateinit var removeImageButton: ImageButton
    private lateinit var languageSpinner: Spinner
    private lateinit var bookMarkButton: MaterialButton
    private lateinit var tutorButton: MaterialButton
    private lateinit var avatarImage: ImageView
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private var selectedLanguage: String = "Bangla"
    private var selectedImageUri: Uri? = null
    private var imageUri: Uri? = null
    private var lastFullReply: String? = null
    private var isLoading = false
    private var processJob: Job? = null
    private var loadingAnimator: ObjectAnimator? = null
    private val mutex = Mutex()

    internal var mAuth: FirebaseAuth? = null
    internal var db: FirebaseFirestore? = null
    private val geminiApiKey = BuildConfig.GEMINI_API_KEY
    private val sketchFabApi = BuildConfig.SKETCHFAB_API_KEY
    private val generativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = geminiApiKey
        )
    }

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { startCrop(it) }
        }

    private val takePhotoLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            if (success && imageUri != null) {
                startCrop(imageUri!!)
            } else {
                Toast.makeText(this, "Failed to capture photo", Toast.LENGTH_SHORT).show()
            }
        }

    private val uCropResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                RESULT_OK -> {
                    val resultUri = UCrop.getOutput(result.data!!)
                    if (resultUri != null) {
                        selectedImageUri = resultUri
                        selectedImageView.setImageURI(resultUri)

                        imageContainer.visibility = View.VISIBLE
                        removeImageButton.visibility = View.VISIBLE
                        if (viewPager.currentItem == 1) {
                            saveLastScreenshotUri(resultUri)
                        }
                    }
                }
                UCrop.RESULT_ERROR -> {
                    val cropError = UCrop.getError(result.data!!)
                    Toast.makeText(this, "Crop failed: ${cropError?.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        topBarCard = findViewById(R.id.topBarCard)
        bottomCard = findViewById(R.id.bottomCard)
        messageEditText = findViewById(R.id.messageEditText)
        sendButton = findViewById(R.id.sendButton)
        selectImageButton = findViewById(R.id.selectImageButton)
        takePhotoButton = findViewById(R.id.selectCameraButton)
        selectedImageView = findViewById(R.id.selectedImageView)
        imageContainer = findViewById(R.id.imageContainer)
        removeImageButton = findViewById(R.id.removeImageButton)
        languageSpinner = findViewById(R.id.languageSpinner)
        bookMarkButton = findViewById(R.id.bookmarkButton)
        avatarImage = findViewById(R.id.avatarImage)
        clearTextButton = findViewById(R.id.clearText)
        viewPager = findViewById(R.id.view_pager)
        tabLayout = findViewById(R.id.tabs)
        fabButton = findViewById(R.id.fabSubmit)
        tutorButton = findViewById(R.id.tutorButton)
        paymentMenu = findViewById(R.id.paymentButton)

        setupViewPagerAndTabs()

        paymentMenu.setOnClickListener {
            startActivity(Intent(this, Payment::class.java))
        }

        val server = AssetNanoHttpServer(applicationContext)
        server.start()
        Log.d("HTTPServer", "Serving at http://127.0.0.3:8000")

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                lifecycleScope.launch {
                    val isPaid = checkUserPaidStatus()
                    when (position) {
                        0 -> {
                            fabButton.visibility = View.VISIBLE
                            fabButton.setImageResource(R.drawable.ic_sum)
                            val fragment = supportFragmentManager.findFragmentByTag("f0") as? ChatFragment
                            fabButton.setOnClickListener {
                                fragment?.chatContainer?.let { container ->
                                    fragment.chatContainer.removeAllViews()
                                    HelperUtil(container).latexKeyboardMath()
                                }
                            }
                        }
                        1 -> {
                            fabButton.visibility = View.VISIBLE
                            fabButton.setImageResource(R.drawable.ic_chemistry)
                            fabButton.setOnClickListener {
                                val fragment = supportFragmentManager.findFragmentByTag("f1") as? ReactionFragment
                                fragment?.chatContainer?.let { container ->
                                    fragment.chatContainer.removeAllViews()
                                    HelperUtil(container).chemistryDoodle()
                                }
                            }
                        }
                        2 -> {
                            if (!isPaid) {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Anatomy tab is available for paid users only. Please upgrade.",
                                    Toast.LENGTH_LONG
                                ).show()
                                viewPager.setCurrentItem(0, true)
                                fabButton.visibility = View.GONE
                            } else {
                                fabButton.visibility = View.GONE
                            }
                        }
                        3 -> {
                            if (!isPaid) {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Electrical tab is available for paid users only. Please upgrade.",
                                    Toast.LENGTH_LONG
                                ).show()
                                viewPager.setCurrentItem(0, true)
                                fabButton.visibility = View.GONE
                            } else {
                                fabButton.visibility = View.VISIBLE
                                fabButton.setImageResource(R.drawable.icon_ai)
                                val fragment = supportFragmentManager.findFragmentByTag("f3") as? ElectricalFragment
                                hideTopAndBottomBars()
                                fabButton.setOnClickListener {
                                    fragment?.chatContainer?.let { container ->
                                        showTopAndBottomBars()
                                    }
                                }
                            }
                        }
                    }
                    Log.d("ViewPager", "Selected tab: $position, FAB visibility: ${fabButton.visibility}")
                    showTopAndBottomBars()
                }
            }
        })

        viewPager.setOnTouchListener { _, event ->
            Log.d("ViewPagerTouch", "Action: ${event.action}")
            false
        }

        viewPager.post {
            val fragment = supportFragmentManager.findFragmentByTag("f0") as? ChatFragment
            fragment?.chatContainer?.let { container ->
                HelperUtil(container).welcomeText()
            }
        }

        tutorButton.setOnClickListener {
        }

        AvatarLoader(avatarImage).loadUserAvatar()
        avatarImage.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }
        clearTextButton.setOnClickListener {
            when (tabLayout.selectedTabPosition) {
                0 -> {
                    val fragment = supportFragmentManager.findFragmentByTag("f0") as? ChatFragment
                    fragment?.chatContainer?.removeAllViews()
                    HelperUtil(fragment?.chatContainer ?: return@setOnClickListener).welcomeText()
                }
                1 -> {
                    val fragment = supportFragmentManager.findFragmentByTag("f1") as? ReactionFragment
                    fragment?.chatContainer?.removeAllViews()
                    HelperUtil(fragment?.chatContainer ?: return@setOnClickListener).chemistryDoodle()
                    saveLastScreenshotUri(null)
                    selectedImageUri = null
                    selectedImageView.setImageURI(null)
                    selectedImageView.visibility = View.GONE
                    imageContainer.visibility = View.GONE
                    removeImageButton.visibility = View.GONE
                }
                2 -> {
                    val fragment = supportFragmentManager.findFragmentByTag("f2") as? BiologyFragment
                    fragment?.chatContainer?.removeAllViews()
                    HelperUtil(fragment?.chatContainer ?: return@setOnClickListener).welcomeTextBiology()
                }
                3 -> {
                    val fragment = supportFragmentManager.findFragmentByTag("f3") as? ElectricalFragment
                    fragment?.chatContainer?.removeAllViews()
                    HelperUtil(fragment?.chatContainer ?: return@setOnClickListener).electricalDoodle(baseContext)
                    hideTopAndBottomBars()
                }
            }
        }

        languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                selectedLanguage = parent.getItemAtPosition(position).toString()
                Toast.makeText(this@MainActivity, "Language: $selectedLanguage", Toast.LENGTH_SHORT).show()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedLanguage = "Bangla"
            }
        }

        selectImageButton.setOnClickListener {
            lifecycleScope.launch {
                val canUploadImage = checkImageUploadLimit()
                if (canUploadImage) {
                    pickImageLauncher.launch("image/*")
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Non-paid users are limited to 5 image uploads per day. Please upgrade.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        takePhotoButton.setOnClickListener {
            lifecycleScope.launch {
                val canUploadImage = checkImageUploadLimit()
                if (canUploadImage) {
                    imageUri = createImageUri()
                    imageUri?.let { takePhotoLauncher.launch(it) }
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Non-paid users are limited to 5 image uploads per day. Please upgrade.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        removeImageButton.setOnClickListener {
            selectedImageUri = null
            selectedImageView.setImageURI(null)
            selectedImageView.visibility = View.GONE
            imageContainer.visibility = View.GONE
            removeImageButton.visibility = View.GONE
            if (viewPager.currentItem == 1) {
                saveLastScreenshotUri(null)
            }
        }

        bookMarkButton.setOnClickListener {
            startActivity(Intent(this, BookmarksActivity::class.java))
        }

        sendButton.setOnClickListener {
            lifecycleScope.launch {
                mutex.withLock {
                    Log.d("SendButton", "Clicked, isLoading: $isLoading, message: '${messageEditText.text}', imageUri: $selectedImageUri, isMole: $isMole, molDoodleCode: $molDoodleCode")
                    if (isLoading) {
                        Log.d("SendButton", "Canceling process")
                        processJob?.cancel()
                        isLoading = false
                        stopLoadingAnimation()
                        sendButton.text = "Send"
                        sendButton.isEnabled = true
                        addChatMessage("Process canceled by user", isLatex = false)
                        return@withLock
                    }

                    val message = messageEditText.text.toString().trim()
                    val currentTab = viewPager.currentItem

                    if(currentTab == 1){
                            val fragment = supportFragmentManager.findFragmentByTag("f1") as? ReactionFragment
                            fragment?.let {
                                selectedImageUri = captureChemDoodleScreenshot(it)
                                if (selectedImageUri == null) {
                                    addChatMessage("Failed to capture chemical structure screenshot.", isLatex = false)
                                    return@withLock
                                }
                            }
                        }




                    if (currentTab == 3) {
                        val fragment = supportFragmentManager.findFragmentByTag("f3") as? ElectricalFragment
                        fragment?.chatContainer?.let { chatContainer ->
                            selectedImageUri = captureViewScreenshot(chatContainer)
                            selectedImageUri?.let { uri ->
                                selectedImageView.setImageURI(uri)
                                selectedImageView.visibility = View.VISIBLE
                                imageContainer.visibility = View.VISIBLE
                                removeImageButton.visibility = View.VISIBLE
                            } ?: run {
                                Toast.makeText(this@MainActivity, "Failed to capture screenshot", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    if (message.isNotEmpty() || selectedImageUri != null || (isMole && molDoodleCode.isNotEmpty())) {
                        val canProceed = checkAndUpdateAttempts(selectedImageUri != null)
                        Log.d("SendButton", "Can proceed: $canProceed")
                        if (!canProceed) {
                            addChatMessage(
                                "Non-paid users are limited to 20 attempts per day. Please upgrade.",
                                isLatex = false
                            )
                            return@withLock
                        }

                        if (message.isNotEmpty()) {
                            addChatMessage(message, isLatex = false)
                        }
                        isLoading = true
                        sendButton.text = "Cancel"
                        startLoadingAnimation()
                        sendButton.isEnabled = true
                        if (isMole && molDoodleCode.isNotEmpty()) {
                            Log.d("SendButton", "Processing molDoodleCode: $molDoodleCode")
                            processInput(molDoodleCode)
                        } else {
                            Log.d("SendButton", "Processing message: $message")
                            processInput(message)
                        }
                        messageEditText.text.clear()
                    } else {
                        Log.d("SendButton", "Empty input in tab $currentTab")
                        addChatMessage("Please enter text, select an image, or provide a doodle.", isLatex = false)
                    }
                }
            }
        }
    }

    private suspend fun checkUserPaidStatus(): Boolean = withContext(Dispatchers.IO) {
        val userId = mAuth?.currentUser?.uid ?: return@withContext false
        val userDocRef = db?.collection("users")?.document(userId) ?: return@withContext false
        try {
            val document = userDocRef.get().await()
            return@withContext document.getBoolean("isPaid") ?: false
        } catch (e: Exception) {
            Log.e("Firestore", "Error checking paid status: ${e.message}")
            return@withContext false
        }
    }

    private suspend fun checkAndUpdateAttempts(isImageAttempt: Boolean): Boolean = withContext(Dispatchers.IO) {
        val userId = mAuth?.currentUser?.uid ?: return@withContext false
        val userDocRef = db?.collection("users")?.document(userId) ?: return@withContext false

        try {
            val document = userDocRef.get().await()
            if (document.exists()) {
                val isPaid = document.getBoolean("isPaid") ?: false
                val messageCount = document.getLong("messageCount")?.toInt() ?: 0
                val imageCount = document.getLong("imageCount")?.toInt() ?: 0
                val lastReset = document.getLong("lastReset") ?: 0L
                val currentTime = System.currentTimeMillis()
                val oneDayInMillis = TimeUnit.DAYS.toMillis(1)

                if (currentTime - lastReset >= oneDayInMillis) {
                    userDocRef.update(
                        mapOf(
                            "messageCount" to 0,
                            "imageCount" to 0,
                            "lastReset" to currentTime
                        )
                    ).await()
                    return@withContext true
                } else if (isPaid || (messageCount < 20 && (!isImageAttempt || imageCount < 5))) {
                    val updates = mutableMapOf<String, Any>("messageCount" to messageCount + 1)
                    if (isImageAttempt) {
                        updates["imageCount"] = imageCount + 1
                    }
                    userDocRef.update(updates).await()
                    return@withContext true
                } else {
                    return@withContext false
                }
            } else {
                val userData = hashMapOf(
                    "uid" to userId,
                    "email" to mAuth?.currentUser?.email,
                    "isPaid" to false,
                    "messageCount" to 0,
                    "imageCount" to 0,
                    "lastReset" to System.currentTimeMillis()
                )
                userDocRef.set(userData).await()
                return@withContext true
            }
        } catch (e: Exception) {
            Log.e("Firestore", "Error in checkAndUpdateAttempts: ${e.message}")
            return@withContext false
        }
    }

    private suspend fun checkImageUploadLimit(): Boolean = withContext(Dispatchers.IO) {
        val userId = mAuth?.currentUser?.uid ?: return@withContext false
        val userDocRef = db?.collection("users")?.document(userId) ?: return@withContext false

        try {
            val document = userDocRef.get().await()
            if (document.exists()) {
                val isPaid = document.getBoolean("isPaid") ?: false
                val imageCount = document.getLong("imageCount")?.toInt() ?: 0
                val lastReset = document.getLong("lastReset") ?: 0L
                val currentTime = System.currentTimeMillis()
                val oneDayInMillis = TimeUnit.DAYS.toMillis(1)

                if (currentTime - lastReset >= oneDayInMillis) {
                    userDocRef.update(
                        mapOf(
                            "messageCount" to 0,
                            "imageCount" to 0,
                            "lastReset" to currentTime
                        )
                    ).await()
                    return@withContext true
                } else if (isPaid || imageCount < 5) {
                    return@withContext true
                } else {
                    return@withContext false
                }
            } else {
                val userData = hashMapOf(
                    "uid" to userId,
                    "email" to mAuth?.currentUser?.email,
                    "isPaid" to false,
                    "messageCount" to 0,
                    "imageCount" to 0,
                    "lastReset" to System.currentTimeMillis()
                )
                userDocRef.set(userData).await()
                return@withContext true
            }
        } catch (e: Exception) {
            Log.e("Firestore", "Error in checkImageUploadLimit: ${e.message}")
            return@withContext false
        }
    }

    private fun setupViewPagerAndTabs() {
        val adapter = ViewPagerAdapter(this)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Math"
                1 -> "Reactive"
                2 -> "Anatomy"
                3 -> "Electrical"
                else -> "Tab $position"
            }
            Log.d("TabLayoutMediator", "Setting tab $position: ${tab.text}")
        }.attach()

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                Log.d("ViewPager", "Selected tab: $position")
            }
        })

        viewPager.setOnTouchListener { _, event ->
            Log.d("ViewPagerTouch", "Action: ${event.action}")
            false
        }
    }

    private fun startLoadingAnimation() {
        Log.d("Animation", "Starting loading animation")
        loadingAnimator?.cancel()
        loadingAnimator = ObjectAnimator.ofFloat(sendButton, "translationX", -15f, 15f).apply {
            duration = 500
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }
        val scaleAnimator = ObjectAnimator.ofFloat(sendButton, "scaleX", 1f, 1.1f, 1f).apply {
            duration = 500
            repeatCount = ObjectAnimator.INFINITE
        }
        AnimatorSet().apply {
            playTogether(loadingAnimator, scaleAnimator)
            start()
        }
        messageEditText.isEnabled = false
        selectImageButton.isEnabled = false
        takePhotoButton.isEnabled = false
    }

    private fun stopLoadingAnimation() {
        Log.d("Animation", "Stopping loading animation")
        loadingAnimator?.cancel()
        loadingAnimator?.removeAllListeners()
        sendButton.translationX = 0f
        sendButton.scaleX = 1f
        loadingAnimator = null
        messageEditText.isEnabled = true
        selectImageButton.isEnabled = true
        takePhotoButton.isEnabled = true
    }

    private fun createImageUri(): Uri? {
        return try {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.TITLE, "new_image_${System.currentTimeMillis()}")
                put(MediaStore.Images.Media.DESCRIPTION, "Taken with Camera")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            }
            contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        } catch (e: IOException) {
            Toast.makeText(this, "Error creating image URI", Toast.LENGTH_SHORT).show()
            null
        }
    }

    private fun startCrop(uri: Uri) {
        val destinationFile = File(cacheDir, "cropped_image_${System.currentTimeMillis()}.jpg")
        val destinationUri = Uri.fromFile(destinationFile)
        val options = UCrop.Options().apply {
            setFreeStyleCropEnabled(true)
            setShowCropGrid(true)
            setToolbarTitle("Crop Image")
            setCompressionFormat(Bitmap.CompressFormat.JPEG)
            setCompressionQuality(90)
            setHideBottomControls(false)
            setAllowedGestures(UCropActivity.ALL, UCropActivity.ALL, UCropActivity.ALL)
        }
        val uCropIntent = UCrop.of(uri, destinationUri)
            .withOptions(options)
            .withMaxResultSize(1000, 1000)
            .getIntent(this@MainActivity)
        uCropResultLauncher.launch(uCropIntent)
    }

    private fun addChatMessage(
        text: String,
        isLatex: Boolean,
        inputText: String = "",
        imageText: String = "",
        embedUrl: String? = null
    ) {
        val currentTab = viewPager.currentItem
        val fragmentTag = "f$currentTab"
        val fragment = supportFragmentManager.findFragmentByTag(fragmentTag) as? Fragment
        val chatContainer = when (fragment) {
            is ChatFragment -> fragment.chatContainer
            is ReactionFragment -> fragment.chatContainer
            is BiologyFragment -> fragment.chatContainer
            is ElectricalFragment -> fragment.chatContainer
            else -> null
        } ?: return
        val scrollView = when (fragment) {
            is ChatFragment -> fragment.scrollView
            is ReactionFragment -> fragment.scrollView
            is BiologyFragment -> fragment.scrollView
            is ElectricalFragment -> fragment.scrollView
            else -> null
        } ?: return

        val messageWrapper = RelativeLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(16, 16, 16, 16) }
        }

        val messageContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
        }

        if (isLatex && text.trim().isNotEmpty()) {
            val latexContainer = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            }
            val webView = CustomWebView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                settings.javaScriptEnabled = true
                loadDataWithBaseURL(null, HTML().latexViewer(text), "text/html", "UTF-8", null)
            }
            latexContainer.addView(webView)
            messageContainer.addView(latexContainer)
        } else {
            val webView = CustomWebView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = true
                settings.allowContentAccess = true
                if (embedUrl != null) {
                    webChromeClient = object : WebChromeClient() {
                        override fun onShowCustomView(view: View, callback: CustomViewCallback) {
                            val fragment = FullScreenModelFragment.newInstance(embedUrl)
                            supportFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, fragment)
                                .addToBackStack(null)
                                .commit()
                        }
                    }
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                            val url = request?.url.toString()
                            if (url.contains("sketchfab.com")) {
                                view?.loadUrl(url)
                                return true
                            }
                            return false
                        }
                    }
                    loadUrl(embedUrl)
                } else if (text.contains("sketchfab.com/models") || text.contains("youtube.com")) {
                    loadDataWithBaseURL(null, text, "text/html", "UTF-8", null)
                } else {
                    loadDataWithBaseURL(null, HTML().latexViewerMarkdown(text), "text/html", "UTF-8", null)
                }
            }
            messageContainer.addView(webView)
        }

        messageWrapper.addView(messageContainer)
        chatContainer.addView(messageWrapper)

        if (text.isNotEmpty()) {
            val buttonContainer = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(16, 8, 16, 16)
                    gravity = Gravity.END
                }
            }

            val starButton = MaterialButton(this).apply {
                setIconResource(android.R.drawable.btn_star)
                iconTint = ColorStateList.valueOf(Color.parseColor("#FFA500"))
                setBackgroundColor(Color.TRANSPARENT)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(8, 0, 0, 0) }
                setOnClickListener {
                    saveBookmark(inputText, imageText, lastFullReply ?: text)
                    setIconResource(android.R.drawable.btn_star_big_on)
                    iconTint = ColorStateList.valueOf(Color.parseColor("#FF8C00"))
                    Toast.makeText(context, "Bookmarked!", Toast.LENGTH_SHORT).show()
                }
            }
            buttonContainer.addView(starButton)

            val flagButton = MaterialButton(this).apply {
                setIconResource(android.R.drawable.ic_dialog_alert)
                iconTint = ColorStateList.valueOf(Color.parseColor("#FFA500"))
                setBackgroundColor(Color.TRANSPARENT)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(8, 0, 0, 0) }
                setOnClickListener {
                    reportContent(inputText, imageText, lastFullReply ?: text)
                    iconTint = ColorStateList.valueOf(Color.parseColor("#800000"))
                    Toast.makeText(context, "Content reported!", Toast.LENGTH_SHORT).show()
                }
            }
            buttonContainer.addView(flagButton)

            chatContainer.addView(buttonContainer)
        }

        scrollView.post { scrollView.fullScroll(ScrollView.FOCUS_DOWN) }
    }

    private fun reportContent(inputText: String, imageText: String, responseText: String) {
        val userId = mAuth?.currentUser?.uid ?: "anonymous"
        val reportData = hashMapOf(
            "userId" to userId,
            "inputText" to inputText,
            "imageText" to imageText,
            "responseText" to responseText,
            "timestamp" to System.currentTimeMillis(),
            "status" to "pending"
        )

        db?.collection("reports")?.add(reportData)
            ?.addOnSuccessListener {
            }
            ?.addOnFailureListener { e ->
                Toast.makeText(this, "Failed to report content: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun saveBookmark(inputText: String, imageText: String, responseText: String) {
        val prefs = getSharedPreferences("Bookmarks", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val gson = Gson()
        val bookmarkList = getBookmarks().toMutableList()
        val bookmark = Bookmark(inputText, imageText, responseText, System.currentTimeMillis())
        bookmarkList.add(bookmark)
        val json = gson.toJson(bookmarkList)
        editor.putString("bookmark_list", json)
        editor.apply()
    }

    private fun getBookmarks(): List<Bookmark> {
        val prefs = getSharedPreferences("Bookmarks", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = prefs.getString("bookmark_list", null)
        return if (json != null) {
            gson.fromJson(json, Array<Bookmark>::class.java).toList()
        } else {
            emptyList()
        }
    }

    data class Bookmark(
        val inputText: String,
        val imageText: String,
        val responseText: String,
        val timestamp: Long
    )

    private fun processInput(inputText: String) {
        var extractedImageText = ""
        processJob = lifecycleScope.launch(Dispatchers.Main) {
            try {
                val currentTab = viewPager.currentItem
                val fixedPrompt = when (currentTab) {
                    0 -> {
                        "If data contains math, physics, or mathematical chemistry, solve step by step and give result in LaTeX for MathJax if math exists, using this style \$\\frac{a}{b}\$. " +
                                "Answer every step in $selectedLanguage language."
                    }
                    1 -> {
                        "If data contains chemical reactions, structures, or reactive processes, analyze the chemical structure or reaction in the provided image or text. Explain the reaction mechanism, balance equations if applicable, and provide results in LaTeX for MathJax for chemical equations (e.g., \$H_2 + O_2 \\rightarrow H_2O\$). " +
                                "If the data contains a chemical structure and $isMole = true, identify the molecule and provide a brief student-friendly explanation in $selectedLanguage. Use MathJax for equations or generate a PNG of the reaction if needed. " +
                                "For non-equation content, provide clear, step-by-step explanations in $selectedLanguage language, similar to how electrical circuits are explained."
                    }
                    2 -> {
                        "If data contains biology or anatomy topics, provide detailed explanations of biological processes, anatomical structures, or concepts in clear, non-technical language suitable for students, in $selectedLanguage. " +
                                "Avoid LaTeX unless specifically requested for diagrams or formulas."
                    }
                    3 -> {
                        "If data contains electrical engineering topics, such as circuits, electronics, or electromagnetism, explain the concepts, solve problems step by step, and provide results in LaTeX for MathJax for equations (e.g., \$V = IR\$). " +
                                "If data contains circuit diagrams or schematics, describe the circuit and its components in $selectedLanguage language. For non-equation content, provide clear explanations in $selectedLanguage language."
                    }
                    else -> {
                        "Answer in $selectedLanguage language."
                    }
                }
                val organismName = ""
                if (currentTab == 1) {
                    val contentParts = mutableListOf<Any>()
                    contentParts.add(fixedPrompt)
                    if (inputText.isNotEmpty()) {
                        contentParts.add(inputText)
                    }

                    if (selectedImageUri != null) {
                        val imagePrompt = "Analyze the chemical structure or reaction in the image. Explain the reaction mechanism, balance equations if applicable, and provide results in LaTeX for MathJax for chemical equations (e.g., \$H_2 + O_2 \\rightarrow H_2O\$). Provide a clear, step-by-step explanation in $selectedLanguage language."
                        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri)
                        contentParts.add(imagePrompt)
                        contentParts.add(bitmap)

                        val imageView = ImageView(this@MainActivity).apply {
                            setImageURI(selectedImageUri)
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply { setMargins(16, 16, 16, 16) }
                        }
                        val fragment = supportFragmentManager.findFragmentByTag("f1") as? ReactionFragment
                        fragment?.chatContainer?.addView(imageView)
                    }

                    val content = content {
                        contentParts.forEach { part ->
                            if (part is String) text(part) else if (part is Bitmap) image(part)
                        }
                    }
                    val response = generativeModel.generateContent(content)
                    val reply = response.text ?: "No response from model"
                    lastFullReply = reply

                    val latexRegex = Regex("""\$(.*?)\$""")
                    var lastIndex = 0
                    for (match in latexRegex.findAll(reply)) {
                        val nonLatexText = reply.substring(lastIndex, match.range.first).trim()
                        if (nonLatexText.isNotEmpty()) {
                            addChatMessage(nonLatexText, isLatex = false, inputText, extractedImageText)
                        }
                        val latexContent = match.groupValues[1].trim()
                        if (latexContent.isNotEmpty()) {
                            addChatMessage(latexContent, isLatex = true, inputText, extractedImageText)
                        }
                        lastIndex = match.range.last + 1
                    }
                    val remainingText = reply.substring(lastIndex).trim()
                    if (remainingText.isNotEmpty()) {
                        addChatMessage(remainingText, isLatex = false, inputText, extractedImageText)
                    } else if (reply.isBlank()) {
                        addChatMessage("No valid content provided", isLatex = false, inputText, extractedImageText)
                    }

                    val videos = fetchYouTubeVideos(inputText, selectedLanguage, "chemistry")
                    if (videos.isNotEmpty()) {
                        val videoHtml = buildVideoLinksHtml(videos)
                        addChatMessage(videoHtml, isLatex = false, inputText, extractedImageText)
                    }
                } else if (currentTab == 2) {
                    val modelName = if (inputText.isEmpty()) {
                        val organismPrompt = "Identify the biological organism or structure in the image. $organismName = ${inputText.trim()}"
                        val organismContent = content { text(organismName) }
                        val organismResponse = generativeModel.generateContent(organismContent)
                        organismResponse.text?.trim() ?: "heart"
                    } else {
                        inputText.trim()
                    }

                    val explanationPrompt = "Provide a brief, student-friendly scientific explanation of the biological organism or structure '$modelName' in $selectedLanguage."
                    val explanationContent = content { text(explanationPrompt) }
                    val explanationResponse = generativeModel.generateContent(explanationContent)
                    val explanation = explanationResponse.text ?: "No explanation available for '$modelName'."

                    val videos = fetchYouTubeVideos(modelName, selectedLanguage)

                    val modelId = fetchSketchfabModelId(modelName)
                    if (modelId != null) {
                        addChatMessage("Explanation: $explanation", isLatex = false, inputText, extractedImageText)

                        val embedUrl = "https://sketchfab.com/models/$modelId/embed"
                        val htmlContent = """
                            <iframe 
                                title="Sketchfab Model: $modelName" 
                                frameborder="0" 
                                allowfullscreen 
                                mozallowfullscreen="true" 
                                webkitallowfullscreen="true" 
                                allow="autoplay; fullscreen; vr" 
                                xr-spatial-tracking 
                                execution-while-out-of-viewport 
                                execution-while-not-rendered 
                                src="$embedUrl" 
                                style="width:100%;height:400px;">
                            </iframe>
                        """.trimIndent()
                        addChatMessage(htmlContent, isLatex = false, inputText, extractedImageText)

                        if (videos.isNotEmpty()) {
                            val videoHtml = buildVideoLinksHtml(videos)
                            addChatMessage(videoHtml, isLatex = false, inputText, extractedImageText)
                        } else {
                            addChatMessage("No related YouTube videos found for '$modelName'.", isLatex = false, inputText, extractedImageText)
                        }
                    } else {
                        addChatMessage("No 3D model found for '$modelName'. Try a different name, e.g., 'heart', 'DNA', or 'animal cell'.", isLatex = false, inputText, extractedImageText)
                    }
                } else {
                    val contentParts = mutableListOf<Any>()
                    contentParts.add(fixedPrompt)
                    if (inputText.isNotEmpty()) {
                        contentParts.add(inputText)
                    }

                    if (selectedImageUri != null) {
                        val imagePrompt = when (currentTab) {
                            0 -> {
                                "Extract all text and content from this image and translate all text to $selectedLanguage. " +
                                        "If it contains math, physics, or mathematical chemistry, solve step by step in $selectedLanguage and write expressions in MathJax."
                            }
                            2 -> {
                                "Extract all text and content from this image and translate to $selectedLanguage. " +
                                        "If it contains biological or anatomical content, describe the structures or processes in clear, student-friendly $selectedLanguage."
                            }
                            3 -> {
                                "Extract all text and content from this image and translate to $selectedLanguage. " +
                                        "If it contains electrical engineering content, such as circuit diagrams or equations, describe the circuit or solve problems step by step in $selectedLanguage, using LaTeX for equations."
                            }
                            else -> {
                                "Extract all text and content from this image and translate and answer to $selectedLanguage."
                            }
                        }

                        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri)
                        contentParts.add(imagePrompt)
                        contentParts.add(bitmap)

                        val imageView = ImageView(this@MainActivity).apply {
                            setImageURI(selectedImageUri)
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply { setMargins(16, 16, 16, 16) }
                        }
                        val fragment = supportFragmentManager.findFragmentByTag("f$currentTab") as? Fragment

                        when (fragment) {
                            is ChatFragment -> fragment.chatContainer?.addView(imageView)
                            is ReactionFragment -> fragment.chatContainer?.addView(imageView)
                            is BiologyFragment -> fragment.chatContainer?.addView(imageView)
                            is ElectricalFragment -> fragment.chatContainer?.addView(imageView)
                        }
                    }

                    val content = content {
                        contentParts.forEach { part ->
                            if (part is String) text(part) else if (part is Bitmap) image(part)
                        }
                    }
                    val response = generativeModel.generateContent(content)
                    val reply = response.text ?: "No response from model"
                    lastFullReply = reply

                    val latexRegex = Regex("""\$(.*?)\$""")
                    var lastIndex = 0
                    for (match in latexRegex.findAll(reply)) {
                        val nonLatexText = reply.substring(lastIndex, match.range.first).trim()
                        if (nonLatexText.isNotEmpty()) {
                            addChatMessage(nonLatexText, isLatex = false, inputText, extractedImageText)
                        }
                        val latexContent = match.groupValues[1].trim()
                        if (latexContent.isNotEmpty()) {
                            addChatMessage(latexContent, isLatex = true, inputText, extractedImageText)
                        }
                        lastIndex = match.range.last + 1
                    }
                    val remainingText = reply.substring(lastIndex).trim()
                    if (remainingText.isNotEmpty()) {
                        addChatMessage(remainingText, isLatex = false, inputText, extractedImageText)
                    } else if (reply.isBlank()) {
                        addChatMessage("No valid content provided", isLatex = false, inputText, extractedImageText)
                    }

                    if (currentTab == 3) {
                        val videos = fetchYouTubeVideos(inputText, selectedLanguage, "electrical engineering")
                        if (videos.isNotEmpty()) {
                            val videoHtml = buildVideoLinksHtml(videos)
                            addChatMessage(videoHtml, isLatex = false, inputText, extractedImageText)
                        }
                    }
                }
            } catch (e: Exception) {
                addChatMessage("Error: ${e.message}", isLatex = false, inputText, extractedImageText)
            } finally {
                isLoading = false
                stopLoadingAnimation()
                sendButton.text = "Send"
                sendButton.isEnabled = true
                processJob = null
                isMole = false
                molDoodleCode = ""
                selectedImageUri = null
                selectedImageView.setImageURI(null)
                selectedImageView.visibility = View.GONE
                imageContainer.visibility = View.GONE
                removeImageButton.visibility = View.GONE
                Log.d("MainActivity", "Reset: isMole=$isMole, molDoodleCode=$molDoodleCode, selectedImageUri=$selectedImageUri")
            }
        }
    }

    private suspend fun fetchYouTubeVideos(query: String, language: String, topic: String = "biology"): List<Triple<String, String, String>> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.YOUTUBE_API_KEY
        val encodedQuery = URLEncoder.encode("$query $topic", "UTF-8")
        val langCode = when (language.lowercase()) {
            "bangla" -> "bn"
            "english" -> "en"
            else -> "bn"
        }
        val urlString = "https://www.googleapis.com/youtube/v3/search?part=snippet&q=$encodedQuery&type=video&maxResults=3&relevanceLanguage=$langCode&key=$apiKey"

        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection

        try {
            connection.apply {
                requestMethod = "GET"
                connectTimeout = 5000
                readTimeout = 5000
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(response)
                val items = jsonResponse.optJSONArray("items") ?: return@withContext emptyList()
                val videos = mutableListOf<Triple<String, String, String>>()

                for (i in 0 until items.length()) {
                    val item = items.getJSONObject(i)
                    val snippet = item.getJSONObject("snippet")
                    val title = snippet.optString("title", "Untitled Video")
                    val videoId = item.getJSONObject("id").optString("videoId")
                    val thumbnailUrl = snippet.getJSONObject("thumbnails").getJSONObject("default").optString("url", "")
                    if (videoId.isNotEmpty()) {
                        val videoUrl = "https://www.youtube.com/watch?v=$videoId"
                        videos.add(Triple(title, videoUrl, thumbnailUrl))
                    }
                }
                return@withContext videos
            } else {
                Log.e("YouTubeAPI", "Failed to fetch videos, response code: $responseCode")
                return@withContext emptyList()
            }
        } catch (e: Exception) {
            Log.e("YouTubeAPI", "Error fetching YouTube videos: ${e.message}")
            return@withContext emptyList()
        } finally {
            connection.disconnect()
        }
    }

    private fun buildVideoLinksHtml(videos: List<Triple<String, String, String>>): String {
        val htmlBuilder = StringBuilder("<h3>Related Videos:</h3><ul style=\"list-style: none; padding: 0;\">")
        videos.forEach { (title, url, thumbnailUrl) ->
            htmlBuilder.append("""
            <li style="margin-bottom: 10px;">
                <a href="$url" style="text-decoration: none; color: #0000EE; display: flex; align-items: center;">
                    <img src="$thumbnailUrl" style="width: 120px; height: 90px; margin-right: 10px;" alt="$title thumbnail"/>
                    <span>$title</span>
                </a>
            </li>
        """.trimIndent())
        }
        htmlBuilder.append("</ul>")
        return htmlBuilder.toString()
    }

    class FullScreenModelFragment : Fragment() {
        companion object {
            private const val ARG_URL = "embed_url"

            fun newInstance(embedUrl: String): FullScreenModelFragment {
                return FullScreenModelFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_URL, embedUrl)
                    }
                }
            }
        }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            val view = inflater.inflate(R.layout.fragment_full_screen_model, container, false)
            val webView: CustomWebView = view.findViewById(R.id.full_screen_webview)
            val closeButton: Button = view.findViewById(R.id.close_button)

            val embedUrl = arguments?.getString(ARG_URL) ?: ""
            webView.apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = true
                settings.allowContentAccess = true
                loadUrl(embedUrl)
            }

            closeButton.setOnClickListener {
                parentFragmentManager.popBackStack()
            }

            return view
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    inner class ViewPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 4

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> ChatFragment()
                1 -> ReactionFragment()
                2 -> BiologyFragment()
                3 -> ElectricalFragment()
                else -> ChatFragment()
            }
        }
    }

    fun hideTopAndBottomBars() {
        if (topBarCard.translationY == 0f) {
            topBarCard.animate()
                .translationY(-topBarCard.height.toFloat())
                .setDuration(200)
                .start()
        }
        if (bottomCard.translationY == 0f) {
            bottomCard.animate()
                .translationY(bottomCard.height.toFloat())
                .setDuration(200)
                .start()
        }
        if (tabLayout.translationY == 0f) {
            tabLayout.animate()
                .translationY(-tabLayout.height.toFloat())
                .setDuration(200)
                .start()
        }
    }

    fun showTopAndBottomBars() {
        if (topBarCard.translationY != 0f) {
            topBarCard.animate()
                .translationY(0f)
                .setDuration(200)
                .start()
        }
        if (bottomCard.translationY != 0f) {
            bottomCard.animate()
                .translationY(0f)
                .setDuration(200)
                .start()
        }
        if (tabLayout.translationY != 0f) {
            tabLayout.animate()
                .translationY(0f)
                .setDuration(200)
                .start()
        }
    }

    private suspend fun fetchSketchfabModelId(name: String): String? = withContext(Dispatchers.IO) {
        val encodedName = URLEncoder.encode(name, "UTF-8")
        val urlString = "https://api.sketchfab.com/v3/search?type=models&q=$encodedName&tags=biology&count=10"

        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection

        try {
            connection.apply {
                requestMethod = "GET"
                setRequestProperty("Authorization", "Token $sketchFabApi")
                connectTimeout = 5000
                readTimeout = 5000
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(response)

                val results = jsonResponse.optJSONArray("results")
                if (results != null && results.length() > 0) {
                    for (i in 0 until results.length()) {
                        val result = results.getJSONObject(i)
                        val modelName = result.optString("name", "").lowercase()
                        val modelId = result.optString("uid")
                        if (modelId.isNotEmpty() && modelName.contains(name.lowercase())) {
                            return@withContext modelId
                        }
                    }
                    val firstResult = results.getJSONObject(0)
                    val modelId = firstResult.optString("uid")
                    if (modelId.isNotEmpty()) {
                        return@withContext modelId
                    }
                }
                Log.d("SketchfabAPI", "No models found for query: $name")
                return@withContext null
            } else {
                Log.e("SketchfabAPI", "Failed to fetch model, response code: $responseCode")
                return@withContext null
            }
        } catch (e: Exception) {
            Log.e("SketchfabAPI", "Error fetching Sketchfab model ID: ${e.message}")
            return@withContext null
        } finally {
            connection.disconnect()
        }
    }

    private fun captureViewScreenshot(view: View): Uri? {
        try {
            // Enable drawing cache
            view.isDrawingCacheEnabled = true
            view.buildDrawingCache(true)
            val bitmap = Bitmap.createBitmap(view.drawingCache)
            view.isDrawingCacheEnabled = false

            // Save bitmap to a temporary file
            val file = File(cacheDir, "screenshot_${System.currentTimeMillis()}.jpg")
            file.outputStream().use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                out.flush()
            }

            // Get Uri from file
            return FileProvider.getUriForFile(
                this,
                "${packageName}.provider",
                file
            )
        } catch (e: Exception) {
            Log.e("Screenshot", "Error capturing screenshot: ${e.message}")
            Toast.makeText(this, "Failed to capture screenshot", Toast.LENGTH_SHORT).show()
            return null
        }
    }


    fun base64ToUri(base64String: String): Uri? {
        return try {
            val cleanBase64 = base64String.replace("data:image/jpeg;base64,", "")
            val decodedBytes = android.util.Base64.decode(cleanBase64, android.util.Base64.DEFAULT)
            val file = File(cacheDir, "chem_sketcher_screenshot_${System.currentTimeMillis()}.jpg")
            file.writeBytes(decodedBytes)
            val uri = FileProvider.getUriForFile(this, "${packageName}.provider", file)
            Log.d("base64ToUri", "Screenshot saved to: ${file.absolutePath}, Uri: $uri")
            saveLastScreenshotUri(uri)
            uri
        } catch (e: Exception) {
            Log.e("base64ToUri", "Error converting base64 to Uri: ${e.message}")
            Toast.makeText(this, "Error processing screenshot", Toast.LENGTH_SHORT).show()
            null
        }
    }

    fun setSelectedImageUri(uri: Uri) {
        selectedImageUri = uri
        Log.d("setSelectedImageUri", "Selected image Uri set: $uri")
    }

    fun displayScreenshot(uri: Uri) {
        selectedImageView.setImageURI(uri)
        selectedImageView.visibility = View.VISIBLE
        imageContainer.visibility = View.VISIBLE
        removeImageButton.visibility = View.VISIBLE
        Log.d("displayScreenshot", "Screenshot displayed in ImageView")
    }

    internal fun saveLastScreenshotUri(uri: Uri?) {
        val prefs = getSharedPreferences("ChemistryCache", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString("last_screenshot_uri", uri?.toString())
        editor.apply()
        Log.d("ChemistryCache", "Saved last screenshot URI: $uri")
    }

    fun getLastScreenshotUri(): Uri? {
        val prefs = getSharedPreferences("ChemistryCache", Context.MODE_PRIVATE)
        val uriString = prefs.getString("last_screenshot_uri", null)
        return uriString?.let {
            try {
                Uri.parse(it)
            } catch (e: Exception) {
                Log.e("ChemistryCache", "Error parsing cached URI: ${e.message}")
                null
            }
        }
    }

    private fun captureChemDoodleScreenshot(fragment: ReactionFragment): Uri? {
        var screenshotUri: Uri? = null
        fragment.chatContainer?.let { container ->
            val webView = findChemDoodleWebView(container)
            webView?.let {
                it.addJavascriptInterface(ChemDoodleJsInterface(this), "Android")
                val jsCode = """
                    (function() {
                        try {
                            var canvas = ChemDoodle.getCanvasById('ChemDoodleCanvas');
                            if (canvas) {
                                var base64Image = canvas.toDataURL('image/jpeg');
                                window.Android.onScreenshotCaptured(base64Image);
                            } else {
                                window.Android.onScreenshotCaptured('');
                            }
                        } catch (e) {
                            window.Android.onScreenshotCaptured('');
                        }
                    })();
                """.trimIndent()
                runOnUiThread {
                    it.evaluateJavascript(jsCode) { result ->
                        Log.d("ChemDoodleScreenshot", "JS result: $result")
                        if (result == "\"\"") {
                            Toast.makeText(this, "Failed to capture ChemDoodle screenshot", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                Thread.sleep(500)
                screenshotUri = getLastScreenshotUri()
            } ?: run {
                Log.e("ChemDoodleScreenshot", "ChemDoodle WebView not found")
                Toast.makeText(this, "ChemDoodle WebView not found", Toast.LENGTH_SHORT).show()
            }
        }
        Log.d("ChemDoodleScreenshot", "Screenshot URI: $screenshotUri")
        return screenshotUri
    }

    private fun findChemDoodleWebView(container: ViewGroup): WebView? {
        for (i in 0 until container.childCount) {
            val child = container.getChildAt(i)
            if (child is CustomWebView) {
                return child
            } else if (child is ViewGroup) {
                val result = findChemDoodleWebView(child)
                if (result != null) return result
            }
        }
        return null
    }

    private fun captureElectricalScreenshot(fragment: ElectricalFragment): Uri? {
        var screenshotUri: Uri? = null
        fragment.chatContainer?.let { container ->
            val webView = findElectricalWebView(container)
            if (webView != null) {
                Log.d("ElectricalScreenshot", "WebView found, attempting canvas capture")
                webView.addJavascriptInterface(ElectricalJsInterface(this), "Android")
                val jsCode = """
                    (function() {
                        try {
                            var canvas = document.querySelector('canvas, #ElectricalCanvas, .electrical-canvas');
                            if (canvas) {
                                var base64Image = canvas.toDataURL('image/jpeg');
                                window.Android.onScreenshotCaptured(base64Image);
                            } else {
                                window.Android.onScreenshotCaptured('');
                            }
                        } catch (e) {
                            window.Android.onScreenshotCaptured('');
                        }
                    })();
                """.trimIndent()
                runOnUiThread {
                    webView.evaluateJavascript(jsCode) { result ->
                        Log.d("ElectricalScreenshot", "JS result: $result")
                        if (result == "\"\"") {
                            Log.w("ElectricalScreenshot", "Canvas not found, falling back to WebView capture")
                            screenshotUri = captureViewScreenshot(webView)
                            if (screenshotUri != null) {
                                setSelectedImageUri(screenshotUri!!)
                                displayScreenshot(screenshotUri!!)
                                saveLastScreenshotUri(screenshotUri)
                            } else {
                                Toast.makeText(this, "Failed to capture electrical screenshot", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                Thread.sleep(500)
                screenshotUri = getLastScreenshotUri()
            } else {
                Log.e("ElectricalScreenshot", "Electrical WebView not found")
                Toast.makeText(this, "Electrical WebView not found", Toast.LENGTH_SHORT).show()
            }
        }
        Log.d("ElectricalScreenshot", "Screenshot URI: $screenshotUri")
        return screenshotUri
    }

    private fun findElectricalWebView(container: ViewGroup): CustomWebView? {
        for (i in 0 until container.childCount) {
            val child = container.getChildAt(i)
            if (child is CustomWebView) {
                return child
            } else if (child is ViewGroup) {
                val result = findElectricalWebView(child)
                if (result != null) return result
            }
        }
        return null
    }

    class ChemDoodleJsInterface(private val activity: MainActivity) {
        @JavascriptInterface
        fun onScreenshotCaptured(base64Image: String) {
            activity.runOnUiThread {
                Log.d("ChemDoodleJsInterface", "Received base64: ${base64Image.take(50)}...")
                val uri = activity.base64ToUri(base64Image)
                uri?.let {
                    activity.setSelectedImageUri(it)
                    activity.displayScreenshot(it)
                    activity.saveLastScreenshotUri(it)
                }
            }
        }
    }

    class ElectricalJsInterface(private val activity: MainActivity) {
        @JavascriptInterface
        fun onScreenshotCaptured(base64Image: String) {
            activity.runOnUiThread {
                Log.d("ElectricalJsInterface", "Received base64: ${base64Image.take(50)}...")
                val uri = activity.base64ToUri(base64Image)
                uri?.let {
                    activity.setSelectedImageUri(it)
                    activity.displayScreenshot(it)
                    activity.saveLastScreenshotUri(it)
                }
            }
        }
    }
}