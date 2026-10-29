package com.rabbyx.newtonixairevivededition.interfaces

import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.rabbyx.newtonixairevivededition.MainActivity
import com.rabbyx.newtonixairevivededition.R

class AvatarLoader(private val avatarImage: ImageView): MainActivity() {
    internal fun loadUserAvatar() {
        mAuth?.currentUser?.let { user ->
            val userId = user.uid
            db?.collection("users")?.document(userId)?.get()
                ?.addOnSuccessListener { document ->
                    if (document.exists()) {
                        val avatarUrl = document.getString("avatarUrl")
                        if (!avatarUrl.isNullOrEmpty()) {
                            Glide.with(this).load(avatarUrl).placeholder(R.drawable.animal).into(avatarImage)
                        }
                    }
                }
                ?.addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to load avatar: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
