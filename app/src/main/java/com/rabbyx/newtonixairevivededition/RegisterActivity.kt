package com.rabbyx.newtonixairevivededition

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Html
import android.util.Log
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.security.MessageDigest

class RegisterActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var db: FirebaseFirestore

    private val cloudinaryCloudName = "dnsnnxlof"
    private val cloudinaryApiKey = "945461542136233"
    private val cloudinaryApiSecret = "oJMiVggpt6BHmMWpTpO_-rdB0hA"

    private var selectedAvatarUri: Uri? = null
    private lateinit var rootLayout: ScrollView
    private lateinit var welcomeTextView: TextView
    private lateinit var avatarImageView: ImageView
    private lateinit var usernameEditText: EditText
    private lateinit var fullNameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var retypePasswordEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var googleSignUpButton: Button

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            selectedAvatarUri = result.data?.data
            avatarImageView.setImageURI(selectedAvatarUri)
        }
    }

    data class CloudinaryResponse(
        @SerializedName("public_id") val publicId: String,
        @SerializedName("secure_url") val secureUrl: String
    )

    interface CloudinaryService {
        @Multipart
        @POST("image/upload")
        suspend fun uploadImage(
            @Part file: MultipartBody.Part,
            @Part apiKey: MultipartBody.Part,
            @Part timestamp: MultipartBody.Part,
            @Part signature: MultipartBody.Part
        ): Response<CloudinaryResponse>
    }

    private val cloudinaryService: CloudinaryService by lazy {
        val client = OkHttpClient.Builder().build()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.cloudinary.com/v1_1/$cloudinaryCloudName/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(CloudinaryService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        rootLayout = findViewById(R.id.rootLayout)
        welcomeTextView = findViewById(R.id.welcomeTextView)
        avatarImageView = findViewById(R.id.avatarImageView)
        usernameEditText = findViewById(R.id.usernameEditText)
        fullNameEditText = findViewById(R.id.fullNameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        retypePasswordEditText = findViewById(R.id.retypePasswordEditText)
        registerButton = findViewById(R.id.registerButton)
        googleSignUpButton = findViewById(R.id.googleSignUpButton)

        welcomeTextView.text = Html.fromHtml("Create Your Account<sup>TM</sup>", Html.FROM_HTML_MODE_LEGACY)
        setupGradientAnimation()
        setupGoogleSignIn()

        avatarImageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        }

        registerButton.setOnClickListener {
            registerWithEmail()
        }

        googleSignUpButton.setOnClickListener {
            googleSignUp()
        }
    }

    private fun setupGradientAnimation() {
        val gradient = GradientDrawable(
            GradientDrawable.Orientation.BR_TL,
            intArrayOf(Color.parseColor("#4285F4"), Color.parseColor("#34A853"), Color.parseColor("#008080"))
        )

        rootLayout.post {
            gradient.setSize(rootLayout.width * 2, rootLayout.height * 2)
            rootLayout.background = gradient

            val animator = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 6000
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.REVERSE
                interpolator = AccelerateDecelerateInterpolator()
                addUpdateListener {
                    val value = it.animatedValue as Float
                    val shiftX = ((1f - value) * rootLayout.width).toInt()
                    val shiftY = ((1f - value) * rootLayout.height).toInt()
                    gradient.setBounds(-shiftX, -shiftY, rootLayout.width * 2 - shiftX, rootLayout.height * 2 - shiftY)
                }
            }
            animator.start()
        }
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun googleSignUp() {
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
            return
        }

        val resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
        if (resultCode != ConnectionResult.SUCCESS) {
            Toast.makeText(this, "Google Play Services unavailable", Toast.LENGTH_SHORT).show()
            return
        }

        startActivityForResult(googleSignInClient.signInIntent, 1001)
    }

    private fun registerWithEmail() {
        val username = usernameEditText.text.toString().trim()
        val fullName = fullNameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString()
        val retypePassword = retypePasswordEditText.text.toString()

        if (username.isEmpty() || fullName.isEmpty() || email.isEmpty() || password.isEmpty() || password != retypePassword || password.length < 6) {
            Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
            return
        }

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = mAuth.currentUser
                    lifecycleScope.launch {
                        val avatarUrl = selectedAvatarUri?.let { uploadAvatarToCloudinary(it) }
                        user?.updateProfile(
                            UserProfileChangeRequest.Builder()
                                .setDisplayName(fullName)
                                .setPhotoUri(Uri.parse(avatarUrl ?: ""))
                                .build()
                        )

                        db.collection("users").document(user!!.uid).set(
                            mapOf(
                                "uid" to user.uid,
                                "username" to username,
                                "fullName" to fullName,
                                "email" to email,
                                "avatarUrl" to avatarUrl,
                                "createdAt" to System.currentTimeMillis(),
                                "isPaid" to false,
                                "messageCount" to 0,
                                "lastReset" to 0L
                            )
                        ).addOnSuccessListener {
                            Toast.makeText(this@RegisterActivity, "Registration successful!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                            finish()
                        }.addOnFailureListener {
                            Toast.makeText(this@RegisterActivity, "Failed to save user data", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null && cm.activeNetworkInfo!!.isConnected
    }

    private fun String.sha1(): String {
        val bytes = MessageDigest.getInstance("SHA-1").digest(toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private suspend fun uploadAvatarToCloudinary(uri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                val bytes = contentResolver.openInputStream(uri)?.readBytes() ?: return@withContext null
                val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
                val ext = mimeType.substringAfter("/")
                val timestamp = (System.currentTimeMillis() / 1000).toString()
                val signature = "timestamp=$timestamp$cloudinaryApiSecret".sha1()

                val imagePart = MultipartBody.Part.createFormData("file", "avatar.$ext", bytes.toRequestBody(mimeType.toMediaType()))
                val apiKeyPart = MultipartBody.Part.createFormData("api_key", cloudinaryApiKey)
                val timestampPart = MultipartBody.Part.createFormData("timestamp", timestamp)
                val signaturePart = MultipartBody.Part.createFormData("signature", signature)

                val response = cloudinaryService.uploadImage(imagePart, apiKeyPart, timestampPart, signaturePart)
                if (response.isSuccessful) response.body()?.secureUrl else null
            } catch (e: Exception) {
                Log.e("RegisterActivity", "Upload failed", e)
                null
            }
        }
    }

    companion object {
        private const val TAG = "RegisterActivity"
    }
}