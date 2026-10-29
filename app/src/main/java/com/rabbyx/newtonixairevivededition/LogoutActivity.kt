package com.rabbyx.newtonixairevivededition

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

class LogoutActivity : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logout)

        mAuth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val logoutButton = findViewById<Button>(R.id.logoutButton)
        val cancelButton = findViewById<Button>(R.id.cancelButton)

        logoutButton.setOnClickListener {
            performLogout()
        }

        cancelButton.setOnClickListener {
            finish()
        }
    }

    private fun performLogout() {
        if (mAuth == null) {
            android.util.Log.e("LogoutActivity", "FirebaseAuth is null")
            redirectToLogin()
            return
        }

        val currentUser = mAuth?.currentUser
        android.util.Log.d("LogoutActivity", "Before logout - User: ${currentUser?.email ?: "None"}, UID: ${currentUser?.uid ?: "None"}")

        mAuth?.signOut()
        android.util.Log.d("LogoutActivity", "After Firebase signOut - User: ${mAuth?.currentUser?.email ?: "Null"}")

        googleSignInClient.signOut().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                android.util.Log.d("LogoutActivity", "Google Sign-In signed out successfully")
            } else {
                android.util.Log.e("LogoutActivity", "Google Sign-In sign-out failed: ${task.exception?.message}")
            }

            googleSignInClient.revokeAccess().addOnCompleteListener { revokeTask ->
                if (revokeTask.isSuccessful) {
                    android.util.Log.d("LogoutActivity", "Google Sign-In access revoked successfully")
                } else {
                    android.util.Log.e("LogoutActivity", "Google Sign-In revoke failed: ${revokeTask.exception?.message}")
                }
                redirectToLogin()
            }
        }.addOnFailureListener { e ->
            android.util.Log.e("LogoutActivity", "Google Sign-In sign-out failed: ${e.message}", e)
            redirectToLogin()
        }
    }

    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.putExtra("FROM_LOGOUT", true)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finishAffinity()
        android.util.Log.d("LogoutActivity", "Redirected to LoginActivity and finished affinity")
    }
}
