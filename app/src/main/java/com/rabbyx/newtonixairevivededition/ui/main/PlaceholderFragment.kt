package com.rabbyx.newtonixairevivededition.ui.main

import android.animation.ObjectAnimator
import android.app.Activity.RESULT_OK
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.AdapterView
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.rabbyx.newtonixairevivededition.BookmarksActivity
import com.rabbyx.newtonixairevivededition.EditProfileActivity
import com.rabbyx.newtonixairevivededition.R
import com.rabbyx.newtonixairevivededition.databinding.FragmentIndexBinding
import com.rabbyx.newtonixairevivededition.interfaces.ANIMATIONS
import com.rabbyx.newtonixairevivededition.interfaces.AvatarLoader
import com.rabbyx.newtonixairevivededition.interfaces.HTML
import com.rabbyx.newtonixairevivededition.interfaces.HelperUtil
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.UCropActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

class PlaceholderFragment : Fragment() {

    private lateinit var chatContainer: LinearLayout
    private lateinit var topBarCard: CardView
    private lateinit var bottomCard: CardView
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: MaterialButton
    private lateinit var selectImageButton: MaterialButton
    private lateinit var takePhotoButton: MaterialButton
    private lateinit var clearTextButton: MaterialButton
    private lateinit var selectedImageView: ImageView
    private lateinit var imageContainer: FrameLayout
    private lateinit var removeImageButton: ImageButton
    private lateinit var scrollView: ScrollView
    private lateinit var languageSpinner: Spinner
    private lateinit var bookMarkButton: MaterialButton
    private lateinit var avatarImage: ImageView
    private var selectedLanguage: String = "Bangla"
    private var selectedImageUri: Uri? = null
    private var imageUri: Uri? = null
    private var lastFullReply: String? = null
    private var isLoading = false
    private var processJob: Job? = null
    private var loadingAnimator: ObjectAnimator? = null

    private var mAuth: FirebaseAuth? = null
    private var db: FirebaseFirestore? = null
    private val geminiApiKey = "AIzaSyA6nlK1FJEZ7i3SnJIY3qnSsVs0YmfER3g" // Move to BuildConfig for security

    private lateinit var pageViewModel: PageViewModel
    private var _binding: FragmentIndexBinding? = null
    private val binding get() = _binding!!

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
                Toast.makeText(context, "Failed to capture photo", Toast.LENGTH_SHORT).show()
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
                        selectedImageView.visibility = View.VISIBLE
                        imageContainer.visibility = View.VISIBLE
                        removeImageButton.visibility = View.VISIBLE
                    }
                }
                UCrop.RESULT_ERROR -> {
                    val cropError = UCrop.getError(result.data!!)
                    Toast.makeText(context, "Crop failed: ${cropError?.message}", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        pageViewModel = ViewModelProvider(this).get(PageViewModel::class.java).apply {
            setIndex(arguments?.getInt(ARG_SECTION_NUMBER) ?: 1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIndexBinding.inflate(inflater, container, false)
        val root = binding.root

        // Initialize views
        topBarCard = root.findViewById(R.id.topBarCard)
        bottomCard = root.findViewById(R.id.bottomCard)
        messageEditText = root.findViewById(R.id.messageEditText)
        sendButton = root.findViewById(R.id.sendButton)
        selectImageButton = root.findViewById(R.id.selectImageButton)
        takePhotoButton = root.findViewById(R.id.selectCameraButton)
        selectedImageView = root.findViewById(R.id.selectedImageView)
        imageContainer = root.findViewById(R.id.imageContainer)
        removeImageButton = root.findViewById(R.id.removeImageButton)
        languageSpinner = root.findViewById(R.id.languageSpinner)
        bookMarkButton = root.findViewById(R.id.bookmarkButton)
        avatarImage = root.findViewById(R.id.avatarImage)
        clearTextButton = root.findViewById(R.id.clearText)

        // Initialize chatContainer
        chatContainer = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Setup ScrollView
        scrollView = ScrollView(requireContext()).apply {
            id = R.id.chatScrollView
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0
            ).apply {
                weight = 1f
            }
            clipToPadding = false
            setPadding(0, 0, 0, 8 * resources.displayMetrics.density.toInt())
        }
        scrollView.addView(chatContainer)

        // Add ScrollView to the section_container
        val sectionContainer: LinearLayout = root.findViewById(R.id.section_container)
        sectionContainer.addView(scrollView)

        // Setup listeners and initial content
        setupScrollListener()
        AvatarLoader(avatarImage).loadUserAvatar()
        HelperUtil(chatContainer).welcomeText()

        avatarImage.setOnClickListener {
            startActivity(Intent(context, EditProfileActivity::class.java))
        }

        languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                selectedLanguage = parent.getItemAtPosition(position).toString()
                Toast.makeText(requireContext(), "Language: $selectedLanguage", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedLanguage = "Bangla"
            }
        }

        selectImageButton.setOnClickListener { pickImageLauncher.launch("image/*") }
        takePhotoButton.setOnClickListener {
            imageUri = createImageUri()
            imageUri?.let { takePhotoLauncher.launch(it) }
        }
        removeImageButton.setOnClickListener {
            selectedImageUri = null
            selectedImageView.setImageURI(null)
            selectedImageView.visibility = View.GONE
            imageContainer.visibility = View.GONE
            removeImageButton.visibility = View.GONE
        }
        bookMarkButton.setOnClickListener {
            startActivity(Intent(requireContext(), BookmarksActivity::class.java))
        }

        sendButton.setOnClickListener {
            if (isLoading) {
                processJob?.cancel()
                isLoading = false
                stopLoadingAnimation()
                sendButton.text = "Send"
                sendButton.isEnabled = true
                addChatMessage("Process canceled by user", isLatex = false)
                return@setOnClickListener
            }

            val message = messageEditText.text.toString().trim()
            if (message.isNotEmpty() || selectedImageUri != null) {
                if (message.isNotEmpty()) {
                    addChatMessage("$message", isLatex = false)
                }
                isLoading = true
                sendButton.text = "Cancel"
                startLoadingAnimation()
                sendButton.isEnabled = true
                processInput(message)
                messageEditText.text.clear()
            } else {
                addChatMessage("Please enter text or select an image.", isLatex = false)
            }
        }

        clearTextButton.setOnClickListener {
            chatContainer.removeAllViews()
            HelperUtil(chatContainer).welcomeText()
        }

        // Observe ViewModel for section-specific content
        pageViewModel.text.observe(viewLifecycleOwner) { sectionText ->
            when (arguments?.getInt(ARG_SECTION_NUMBER)) {
                1 -> {
                    // Chat interface is already set up
                }
                2 -> {
                    sectionContainer.removeAllViews()
                    val textView = android.widget.TextView(requireContext()).apply {
                        text = "Custom content for Tab 2: $sectionText"
                        textSize = 18f
                        setPadding(0, 16, 0, 16)
                    }
                    sectionContainer.addView(textView)
                    val editText = android.widget.EditText(requireContext()).apply {
                        hint = "Type something"
                    }
                    sectionContainer.addView(editText)
                }
                3 -> {
                    sectionContainer.removeAllViews()
                    val textView = android.widget.TextView(requireContext()).apply {
                        text = "Custom content for Tab 3: $sectionText"
                        textSize = 18f
                        setPadding(0, 16, 0, 16)
                    }
                    sectionContainer.addView(textView)
                    val editText = android.widget.EditText(requireContext()).apply {
                        hint = "Type something"
                    }
                    sectionContainer.addView(editText)
                }
                else -> {
                    sectionContainer.removeAllViews()
                    val textView = android.widget.TextView(requireContext()).apply {
                        text = sectionText
                        textSize = 18f
                    }
                    sectionContainer.addView(textView)
                }
            }
        }

        return root
    }

    private fun createImageUri(): Uri? {
        return try {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.TITLE, "new_image_${System.currentTimeMillis()}")
                put(MediaStore.Images.Media.DESCRIPTION, "Taken with Camera")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            }
            context?.contentResolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        } catch (e: IOException) {
            Toast.makeText(context, "Error creating image URI", Toast.LENGTH_SHORT).show()
            null
        }
    }

    private fun startCrop(uri: Uri) {
        val destinationFile = File(requireContext().cacheDir, "cropped_image_${System.currentTimeMillis()}.jpg")
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
            .getIntent(requireContext())

        uCropResultLauncher.launch(uCropIntent)
    }

    private fun setupScrollListener() {
        var isScrolling = false
        var areBarsHidden = false

        scrollView.viewTreeObserver.addOnScrollChangedListener {
            val scrollY = scrollView.scrollY
            val maxScroll = scrollView.getChildAt(0).height - scrollView.height

            if (scrollY > 0 && scrollY < maxScroll) {
                if (!isScrolling) {
                    isScrolling = true
                    areBarsHidden = true
                    ANIMATIONS(topBarCard, bottomCard).hideCardsWithAnimation()
                }
            } else if ((scrollY == 0 || scrollY >= maxScroll) && isScrolling) {
                isScrolling = false
                areBarsHidden = false
                ANIMATIONS(topBarCard, bottomCard).showCardsWithAnimation()
            }
        }

        scrollView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (areBarsHidden && !isScrolling) {
                        areBarsHidden = false
                        ANIMATIONS(topBarCard, bottomCard).showCardsWithAnimation()
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    scrollView.postDelayed({
                        val scrollY = scrollView.scrollY
                        val maxScroll = scrollView.getChildAt(0).height - scrollView.height
                        if (scrollY == 0 || scrollY >= maxScroll) {
                            isScrolling = false
                            areBarsHidden = false
                            ANIMATIONS(topBarCard, bottomCard).showCardsWithAnimation()
                        }
                    }, 200)
                }
            }
            false
        }
    }

    private fun startLoadingAnimation() {
        loadingAnimator?.cancel()
        loadingAnimator = ObjectAnimator.ofFloat(sendButton, "translationX", -30f, 30f).apply {
            duration = 1000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            start()
        }
    }

    private fun stopLoadingAnimation() {
        loadingAnimator?.cancel()
        sendButton.translationX = 0f
        loadingAnimator = null
    }

    private fun addChatMessage(
        text: String,
        isLatex: Boolean,
        inputText: String = "",
        imageText: String = ""
    ) {
        val messageWrapper = RelativeLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(16, 16, 16, 16) }
        }

        val messageContainer = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
        }

        if (isLatex && text.trim().isNotEmpty()) {
            val latexContainer = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            }
            val webView = WebView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                settings.javaScriptEnabled = true
                val htmlContent = HTML().latexViewer(text).trimIndent()
                loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
            }
            latexContainer.addView(webView)
            messageContainer.addView(latexContainer)
        } else {
            val webView = WebView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
                settings.javaScriptEnabled = true
                val htmlContent = HTML().latexViewerMarkdown(text).toString()
                loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
            }
            messageContainer.addView(webView)
        }

        messageWrapper.addView(messageContainer)
        chatContainer.addView(messageWrapper)

        if (text.isNotEmpty()) {
            val buttonContainer = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(16, 8, 16, 16)
                    gravity = Gravity.END
                }
            }

            val starButton = MaterialButton(requireContext()).apply {
                setIconResource(android.R.drawable.btn_star)
                iconTint = android.content.res.ColorStateList.valueOf(Color.parseColor("#FFA500"))
                backgroundTintList = null
                setBackgroundColor(Color.TRANSPARENT)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(8, 0, 0, 0) }
                setOnClickListener {
                    saveBookmark(inputText, imageText, lastFullReply ?: text)
                    setIconResource(android.R.drawable.btn_star_big_on)
                    iconTint = android.content.res.ColorStateList.valueOf(Color.parseColor("#FF8C00"))
                    Toast.makeText(context, "Bookmarked!", Toast.LENGTH_SHORT).show()
                }
            }
            buttonContainer.addView(starButton)

            val flagButton = MaterialButton(requireContext()).apply {
                setIconResource(android.R.drawable.ic_dialog_alert)
                iconTint = android.content.res.ColorStateList.valueOf(Color.parseColor("#FFA500"))
                backgroundTintList = null
                setBackgroundColor(Color.TRANSPARENT)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(8, 0, 0, 0) }
                setOnClickListener {
                    reportContent(inputText, imageText, lastFullReply ?: text)
                    iconTint = android.content.res.ColorStateList.valueOf(Color.parseColor("#800000"))
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
                Log.d("PlaceholderFragment", "Report successfully stored in Firestore")
            }
            ?.addOnFailureListener { e ->
                Toast.makeText(context, "Failed to report content: ${e.message}", Toast.LENGTH_LONG)
                    .show()
            }
    }

    private fun saveBookmark(inputText: String, imageText: String, responseText: String) {
        val prefs = requireContext().getSharedPreferences("Bookmarks", Context.MODE_PRIVATE)
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
        val prefs = requireContext().getSharedPreferences("Bookmarks", Context.MODE_PRIVATE)
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
        processJob = lifecycleScope.launch(Dispatchers.Main) {
            try {
                val fixedPrompt =
                    "If data contains math, physics, or mathematical chemistry, solve step by step and give result in LaTeX for MathJax if math exists, using this style \$\\frac{a}{b}\$. " +
                            "Answer every step in $selectedLanguage language."

                val contentParts = mutableListOf<Any>()
                contentParts.add(fixedPrompt)
                if (inputText.isNotEmpty()) {
                    contentParts.add(inputText)
                }

                var extractedImageText = ""
                if (selectedImageUri != null) {
                    val imagePrompt =
                        "Extract all text and content from this image and translate all text to $selectedLanguage. " +
                                "If it contains math, physics, or mathematical chemistry, solve step by step in $selectedLanguage and write expressions in MathJax."
                    val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, selectedImageUri)
                    contentParts.add(imagePrompt)
                    contentParts.add(bitmap)

                    val imageView = ImageView(requireContext()).apply {
                        setImageURI(selectedImageUri)
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply { setMargins(16, 16, 16, 16) }
                    }
                    chatContainer.addView(imageView)
                }

                val content = content {
                    contentParts.forEach { part ->
                        when (part) {
                            is String -> text(part)
                            is Bitmap -> image(part)
                        }
                    }
                }
                val response = generativeModel.generateContent(content)
                val reply = response.text ?: "No response from model"
                lastFullReply = reply

                Log.d("PlaceholderFragment", "Raw reply: '$reply'")

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
                    addChatMessage(
                        "No valid content provided",
                        isLatex = false,
                        inputText,
                        extractedImageText
                    )
                }
            } catch (e: Exception) {
                Log.e("PlaceholderFragment", "Error processing input: ${e.message}")
                addChatMessage("Error: ${e.message}", isLatex = false, inputText, "")
            } finally {
                isLoading = false
                stopLoadingAnimation()
                sendButton.text = "Send"
                sendButton.isEnabled = true
                processJob = null

                selectedImageUri = null
                selectedImageView.setImageURI(null)
                selectedImageView.visibility = View.GONE
                imageContainer.visibility = View.GONE
                removeImageButton.visibility = View.GONE
            }
        }
    }

    companion object {
        private const val ARG_SECTION_NUMBER = "section_number"

        @JvmStatic
        fun newInstance(sectionNumber: Int): PlaceholderFragment {
            return PlaceholderFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}