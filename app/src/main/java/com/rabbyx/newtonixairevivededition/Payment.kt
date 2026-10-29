package com.rabbyx.newtonixairevivededition

import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Payment : AppCompatActivity() {
    private lateinit var rootLayout: ScrollView
    private lateinit var paymentMethod: Spinner
    private lateinit var trxID: EditText
    private lateinit var payBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_payment)

        rootLayout = findViewById(R.id.main)
        paymentMethod = findViewById(R.id.paymentMethod)
        trxID = findViewById(R.id.trxID)
        payBtn = findViewById(R.id.payBtn)

        // Set up payment method spinner
        val paymentMethods = arrayOf("Select Payment Method", "bKash", "Nagad", "Rocket", "Bank Card")
        val adapter = ArrayAdapter(this, R.layout.spinner_item, paymentMethods)
        adapter.setDropDownViewResource(R.layout.spinner_item)
        paymentMethod.adapter = adapter

        setupGradientAnimation()

        // Handle window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        payBtn.setOnClickListener {
            val transactionId = trxID.text.toString().trim()
            val selectedMethod = paymentMethod.selectedItem.toString()

            // Validate payment method
            if (selectedMethod == "Select Payment Method") {
                Toast.makeText(this, "Please select a valid payment method", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validate transaction ID
            if (transactionId.isEmpty()) {
                trxID.error = "Transaction ID is required"
                return@setOnClickListener
            }

            // Validate user authentication
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                Toast.makeText(this, "Please sign in to proceed", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Prepare payment data (aligned with Firestore rules)
            val paymentData = hashMapOf(
                "userId" to currentUser.uid,
                "trxId" to transactionId,
                "amount" to 300, // Hardcoded for simplicity; adjust as needed
                "method" to selectedMethod,
                "timestamp" to System.currentTimeMillis(),
                "status" to "pending"
            )

            // Disable button to prevent multiple submissions
            payBtn.isEnabled = false
            payBtn.text = "Processing..."

            // Submit payment to Firestore
            FirebaseFirestore.getInstance()
                .collection("payments")
                .add(paymentData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Payment submitted successfully! Awaiting verification.", Toast.LENGTH_LONG).show()
                    trxID.text?.clear()
                    paymentMethod.setSelection(0)
                    // Reset button state
                    payBtn.isEnabled = true
                    payBtn.text = "Pay Now"
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to submit payment: ${e.message}", Toast.LENGTH_LONG).show()
                    // Reset button state
                    payBtn.isEnabled = true
                    payBtn.text = "Pay Now"
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
}