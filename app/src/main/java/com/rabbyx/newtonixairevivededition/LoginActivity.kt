package com.rabbyx.newtonixairevivededition

import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.animation.AccelerateDecelerateInterpolator
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.*

class LoginActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager

    private lateinit var signupButton: Button
    private lateinit var loginButton: Button
    private lateinit var googleSignInButton: Button
    private lateinit var facebookSignInButton: Button
    private lateinit var twitterSignInButton: Button
    private lateinit var microsoftSignInButton: Button
    private lateinit var rootLayout: ScrollView
    private lateinit var welcomeTextView: TextView
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance()

        // Initialize UI elements
        signupButton = findViewById(R.id.signupButton)
        loginButton = findViewById(R.id.loginButton)
        googleSignInButton = findViewById(R.id.googleSignInButton)
        facebookSignInButton = findViewById(R.id.facebookSignInButton)
        twitterSignInButton = findViewById(R.id.twitterSignInButton)
        microsoftSignInButton = findViewById(R.id.microsoftSignInButton)
        rootLayout = findViewById(R.id.rootLayout)
        welcomeTextView = findViewById(R.id.welcomeTextView)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)

        // Welcome text
        welcomeTextView.text = Html.fromHtml(getString(R.string.welcome_text), Html.FROM_HTML_MODE_LEGACY)
        setupGradientAnimation()

        // Google Sign-In config
        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            googleSignInClient = GoogleSignIn.getClient(this, gso)
        } catch (e: Exception) {
            Log.e(TAG, "Google Sign-In not initialized: ${e.message}")
        }

        // Facebook config
        try {
            callbackManager = CallbackManager.Factory.create()
        } catch (e: Exception) {
            Log.e(TAG, "Facebook CallbackManager not initialized: ${e.message}")
        }

        // Click listeners
        signupButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        loginButton.setOnClickListener {
            loginWithEmail()
        }

        googleSignInButton.setOnClickListener {
            try {
                if (::googleSignInClient.isInitialized) {
                    googleSignIn()
                } else {
                    throw IllegalStateException("Google client not initialized")
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Google Sign-In not available", Toast.LENGTH_SHORT).show()
            }
        }

        facebookSignInButton.setOnClickListener {
            try {
                if (::callbackManager.isInitialized) {
                    facebookSignIn()
                } else {
                    throw IllegalStateException("Facebook callback manager not initialized")
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Facebook Sign-In not available", Toast.LENGTH_SHORT).show()
            }
        }

        twitterSignInButton.setOnClickListener {
            try {
                twitterSignIn()
            } catch (e: Exception) {
                Toast.makeText(this, "Twitter Sign-In not available", Toast.LENGTH_SHORT).show()
            }
        }

        microsoftSignInButton.setOnClickListener {
            Toast.makeText(this, "Microsoft Sign-In not available", Toast.LENGTH_SHORT).show()
        }

        // Already logged in?
        if (intent.getBooleanExtra("FROM_LOGOUT", false)) {
            Log.d(TAG, "Arrived from logout, staying on LoginActivity")
        } else if (mAuth.currentUser != null) {
            Log.d(TAG, "User already logged in: ${mAuth.currentUser?.email}, going to MainActivity")
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun loginWithEmail() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        if (email.isEmpty()) {
            emailEditText.error = "Email is required"
            return
        }
        if (password.isEmpty()) {
            passwordEditText.error = "Password is required"
            return
        }

        loginButton.isEnabled = false
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                loginButton.isEnabled = true
                if (task.isSuccessful) {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun setupGradientAnimation() {
        val gradientDrawable = GradientDrawable(
            GradientDrawable.Orientation.BR_TL,
            intArrayOf(
                Color.parseColor("#4285F4"),
                Color.parseColor("#34A853"),
                Color.parseColor("#008080"),
                Color.parseColor("#004D4D"),
                Color.parseColor("#4A6366"),
                Color.parseColor("#2E4057"),
                Color.parseColor("#5D737E"),
                Color.parseColor("#87B6A7"),
                Color.parseColor("#4682B4")
            )
        ).apply {
            rootLayout.post {
                setSize(rootLayout.width * 2, rootLayout.height * 2)
                rootLayout.background = this
            }
        }

        val animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 6000
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animation ->
                val value = animation.animatedValue as Float
                val shiftX = ((1f - value) * rootLayout.width).toInt()
                val shiftY = ((1f - value) * rootLayout.height).toInt()
                gradientDrawable.setBounds(
                    -shiftX,
                    -shiftY,
                    rootLayout.width * 2 - shiftX,
                    rootLayout.height * 2 - shiftY
                )
            }
        }

        rootLayout.post {
            if (rootLayout.width > 0 && rootLayout.height > 0) {
                animator.start()
            }
        }
    }

    private fun googleSignIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN)
    }

    private fun facebookSignIn() {
        LoginManager.getInstance().logInWithReadPermissions(this, listOf("email", "public_profile"))
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                firebaseAuthWithFacebook(result.accessToken.token)
            }

            override fun onCancel() {
                Toast.makeText(this@LoginActivity, "Facebook Sign-In canceled", Toast.LENGTH_SHORT).show()
            }

            override fun onError(error: FacebookException) {
                Toast.makeText(this@LoginActivity, "Facebook Sign-In failed: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun twitterSignIn() {
        val webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    if (url.startsWith("https://your-app.firebaseapp.com/__/auth/handler")) {
                        Toast.makeText(this@LoginActivity, "Twitter Sign-In requires backend setup", Toast.LENGTH_LONG).show()
                        return true
                    }
                    return false
                }
            }
        }
        webView.loadUrl("https://api.twitter.com/oauth/authorize?consumer_key=${getString(R.string.twitter_consumer_key)}&redirect_uri=https://your-app.firebaseapp.com/__/auth/handler")
        setContentView(webView)
    }

    private fun firebaseAuthWithFacebook(token: String) {
        val credential = FacebookAuthProvider.getCredential(token)
        facebookSignInButton.isEnabled = false
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                facebookSignInButton.isEnabled = true
                if (task.isSuccessful) {
                    val user = mAuth.currentUser
                    val isNewUser = task.result?.additionalUserInfo?.isNewUser == true
                    val message = if (isNewUser) "Welcome! Account created" else "Welcome back!"
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (::callbackManager.isInitialized) {
                callbackManager.onActivityResult(requestCode, resultCode, data)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Facebook onActivityResult failed: ${e.message}")
        }

        if (requestCode == GOOGLE_SIGN_IN) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    firebaseAuthWithGoogle(account)
                } else {
                    Toast.makeText(this, "Google Sign-In failed: No account", Toast.LENGTH_LONG).show()
                }
            } catch (e: ApiException) {
                val msg = when (e.statusCode) {
                    10 -> "Developer error: Check google-services.json and SHA-1"
                    12500 -> "Google Play Services is missing or outdated"
                    12501 -> "Sign-in canceled"
                    7 -> "Network error"
                    else -> "Error code: ${e.statusCode}"
                }
                Toast.makeText(this, "Google Sign-In failed: $msg", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        googleSignInButton.isEnabled = false
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                googleSignInButton.isEnabled = true
                if (task.isSuccessful) {
                    val user = mAuth.currentUser
                    val isNewUser = task.result?.additionalUserInfo?.isNewUser == true
                    val message = if (isNewUser) "Welcome! Account created" else "Welcome back!"
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    companion object {
        private const val TAG = "LoginActivity"
        private const val GOOGLE_SIGN_IN = 9001
    }
}
