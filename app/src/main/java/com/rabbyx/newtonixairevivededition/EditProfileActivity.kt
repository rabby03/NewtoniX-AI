package com.rabbyx.newtonixairevivededition

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.IOException
import java.security.MessageDigest

class EditProfileActivity : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    private var db: FirebaseFirestore? = null
    private var usernameEditText: EditText? = null
    private var fullNameEditText: EditText? = null
    private var emailEditText: EditText? = null
    private var avatarImageView: ImageView? = null
    private var selectedAvatarUri: Uri? = null
    private lateinit var changePasswordButton: MaterialButton
    private lateinit var logoutButton: MaterialButton

    // Cloudinary credentials (same as RegisterActivity)
    private val cloudinaryCloudName = "dnsnnxlof"
    private val cloudinaryApiKey = "945461542136233"
    private val cloudinaryApiSecret = "oJMiVggpt6BHmMWpTpO_-rdB0hA"

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            selectedAvatarUri = result.data?.data
            avatarImageView?.setImageURI(selectedAvatarUri)
        }
    }

    // Cloudinary API Data Classes
    data class CloudinaryResponse(
        @SerializedName("public_id") val publicId: String,
        @SerializedName("secure_url") val secureUrl: String
    )

    // Retrofit Interface for Cloudinary API
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

    // Retrofit setup for Cloudinary
    private val cloudinaryService: CloudinaryService by lazy {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val newRequest = originalRequest.newBuilder()
                    .header("Accept", "application/json")
                    .build()
                chain.proceed(newRequest)
            }
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.cloudinary.com/v1_1/$cloudinaryCloudName/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(CloudinaryService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        usernameEditText = findViewById(R.id.usernameEditText)
        fullNameEditText = findViewById(R.id.fullNameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        avatarImageView = findViewById(R.id.avatarImageView)
        val saveButton = findViewById<Button>(R.id.saveButton)
        changePasswordButton = findViewById(R.id.changePasswordButton)
        logoutButton = findViewById(R.id.logoutButton)

        // Load current user data
        loadUserData()

        logoutButton.setOnClickListener {
            startActivity(Intent(this, LogoutActivity::class.java))
        }

        changePasswordButton.setOnClickListener {
            startActivity(Intent(this, ChangePasswordActivity::class.java))
        }

        // Avatar upload click listener
        avatarImageView?.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        }

        saveButton.setOnClickListener { saveProfileChanges() }
    }

    private fun loadUserData() {
        val user = mAuth!!.currentUser
        if (user != null) {
            emailEditText?.setText(user.email)
            db!!.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        usernameEditText?.setText(document.getString("username"))
                        fullNameEditText?.setText(document.getString("fullName"))
                        val avatarUrl = document.getString("avatarUrl")
                        if (!avatarUrl.isNullOrEmpty()) {
                            Glide.with(this)
                                .load(avatarUrl)
                                .placeholder(R.drawable.animal)
                                .into(avatarImageView!!)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to load profile: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveProfileChanges() {
        val username = usernameEditText?.text.toString().trim()
        val fullName = fullNameEditText?.text.toString().trim()

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(fullName)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val user = mAuth!!.currentUser
        if (user != null) {
            val userData = hashMapOf<String, Any>(
                "username" to username,
                "fullName" to fullName
            )

            // If a new avatar is selected, upload it to Cloudinary
            if (selectedAvatarUri != null) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val avatarUrl = uploadAvatarToCloudinary(selectedAvatarUri!!)
                    withContext(Dispatchers.Main) {
                        if (avatarUrl != null) {
                            userData["avatarUrl"] = avatarUrl
                        } else {
                            Toast.makeText(this@EditProfileActivity, "Avatar upload failed, saving without avatar", Toast.LENGTH_SHORT).show()
                        }
                        updateUserData(user.uid, userData)
                    }
                }
            } else {
                updateUserData(user.uid, userData)
            }
        }
    }

    // SHA-1 utility for Cloudinary signature
    private fun String.sha1(): String {
        val bytes = MessageDigest.getInstance("SHA-1").digest(this.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private suspend fun uploadAvatarToCloudinary(uri: Uri): String? {
        return try {
            Log.d(TAG, "Starting avatar upload to Cloudinary with URI: $uri")

            // Validate credentials
            if (cloudinaryApiKey.isBlank() || cloudinaryApiSecret.isBlank() || cloudinaryCloudName.isBlank()) {
                Log.e(TAG, "Invalid Cloudinary credentials")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditProfileActivity, "Error: Invalid Cloudinary setup", Toast.LENGTH_SHORT).show()
                }
                return null
            }

            // Open input stream
            val inputStream = contentResolver.openInputStream(uri) ?: run {
                Log.e(TAG, "Failed to open input stream for URI: $uri")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditProfileActivity, "Error: Could not access avatar file", Toast.LENGTH_SHORT).show()
                }
                return null
            }

            val imageBytes = inputStream.readBytes()
            inputStream.close()

            if (imageBytes.isEmpty()) {
                Log.e(TAG, "Avatar data is empty for URI: $uri")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditProfileActivity, "Error: Avatar data is empty", Toast.LENGTH_SHORT).show()
                }
                return null
            }

            // File size check
            val maxSize = 10 * 1024 * 1024 // 10MB
            if (imageBytes.size > maxSize) {
                Log.e(TAG, "Avatar size (${imageBytes.size} bytes) exceeds 10MB limit")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditProfileActivity, "Error: Avatar exceeds 10MB limit", Toast.LENGTH_SHORT).show()
                }
                return null
            }

            // Get MIME type and determine extension
            val mimeType = contentResolver.getType(uri) ?: "image/jpeg" // Default to JPEG if unknown
            val fileExtension = when (mimeType.lowercase()) {
                "image/jpeg", "image/jpg" -> "jpg"
                "image/png" -> "png"
                "image/gif" -> "gif"
                "image/bmp" -> "bmp"
                "image/webp" -> "webp"
                "image/tiff" -> "tiff"
                "image/svg+xml" -> "svg"
                else -> {
                    Log.e(TAG, "Unsupported MIME type: $mimeType")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@EditProfileActivity, "Error: Unsupported format ($mimeType)", Toast.LENGTH_SHORT).show()
                    }
                    return null
                }
            }
            Log.d(TAG, "Detected MIME type: $mimeType, extension: $fileExtension")

            // Prepare request
            val timestamp = (System.currentTimeMillis() / 1000).toString()
            val signature = "timestamp=$timestamp$cloudinaryApiSecret".sha1()

            val imagePart = MultipartBody.Part.createFormData(
                "file",
                "avatar.$fileExtension",
                imageBytes.toRequestBody(mimeType.toMediaType())
            )
            val apiKeyPart = MultipartBody.Part.createFormData("api_key", cloudinaryApiKey)
            val timestampPart = MultipartBody.Part.createFormData("timestamp", timestamp)
            val signaturePart = MultipartBody.Part.createFormData("signature", signature)

            Log.d(TAG, "Sending Cloudinary upload request: key=${cloudinaryApiKey.take(4)}..., size=${imageBytes.size} bytes, timestamp=$timestamp")

            // Send request
            val response = cloudinaryService.uploadImage(imagePart, apiKeyPart, timestampPart, signaturePart)

            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody?.secureUrl?.isNotEmpty() == true) {
                    Log.d(TAG, "Avatar uploaded successfully: ${responseBody.secureUrl}")
                    return responseBody.secureUrl
                } else {
                    Log.e(TAG, "Cloudinary response missing secure_url: ${response.body()}")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@EditProfileActivity, "Error: Invalid Cloudinary response", Toast.LENGTH_SHORT).show()
                    }
                    return null
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "No details"
                Log.e(TAG, "Cloudinary upload failed: ${response.code()} - ${response.message()} - $errorBody")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditProfileActivity, "Error: Upload failed (${response.code()})", Toast.LENGTH_SHORT).show()
                }
                return null
            }
        } catch (e: IOException) {
            Log.e(TAG, "IO error during Cloudinary upload: ${e.message}", e)
            withContext(Dispatchers.Main) {
                Toast.makeText(this@EditProfileActivity, "Error: IO error while uploading avatar", Toast.LENGTH_SHORT).show()
            }
            return null
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during Cloudinary upload: ${e.message}", e)
            withContext(Dispatchers.Main) {
                Toast.makeText(this@EditProfileActivity, "Error uploading avatar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            return null
        }
    }

    private fun updateUserData(userId: String, userData: HashMap<String, Any>) {
        db!!.collection("users").document(userId)
            .update(userData)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                finish() // Return to previous activity
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    companion object {
        private const val TAG = "EditProfileActivity"
    }
}