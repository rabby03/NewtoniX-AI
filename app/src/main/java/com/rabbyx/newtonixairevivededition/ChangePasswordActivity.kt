package com.rabbyx.newtonixairevivededition

import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.rabbyx.newtonixairevivededition.R

class ChangePasswordActivity : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    private var currentPasswordEditText: EditText? = null
    private var newPasswordEditText: EditText? = null
    private var retypeNewPasswordEditText: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        mAuth = FirebaseAuth.getInstance()

        currentPasswordEditText = findViewById(R.id.currentPasswordEditText)
        newPasswordEditText = findViewById(R.id.newPasswordEditText)
        retypeNewPasswordEditText = findViewById(R.id.retypeNewPasswordEditText)
        val changePasswordButton = findViewById<Button>(R.id.changePasswordButton)

        changePasswordButton.setOnClickListener { changePassword() }
    }

    private fun changePassword() {
        val currentPassword = currentPasswordEditText?.text.toString().trim()
        val newPassword = newPasswordEditText?.text.toString().trim()
        val retypeNewPassword = retypeNewPasswordEditText?.text.toString().trim()

        // Validate inputs
        if (TextUtils.isEmpty(currentPassword) || TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(retypeNewPassword)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword != retypeNewPassword) {
            Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword.length < 6) {
            Toast.makeText(this, "New password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }

        val user = mAuth!!.currentUser
        if (user != null && user.email != null) {
            // Reauthenticate user with current password
            val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)
            user.reauthenticate(credential)
                .addOnSuccessListener {
                    // Reauthentication successful, update password
                    user.updatePassword(newPassword)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show()
                            finish() // Close activity
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Failed to update password: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Reauthentication failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}