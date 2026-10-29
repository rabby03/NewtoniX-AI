package com.rabbyx.newtonixairevivededition

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.rabbyx.newtonixairevivededition.interfaces.HelperUtil

class ElectricalFragment : Fragment() {

    internal lateinit var chatContainer: LinearLayout
    internal lateinit var scrollView: ScrollView
    private var isBarsVisible = true // Track visibility state

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_electrical, container, false)
        chatContainer = view.findViewById(R.id.chatMessagesContainer)
        scrollView = view.findViewById(R.id.chatScrollView)

        // Optional: do your layout logic
        chatContainer.let { HelperUtil(it).electricalDoodle(requireContext()) }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val webView = view.findViewById<CustomWebView>(R.id.customWebView)

        // Set up full-screen immersive mode initially
        setupFullScreenMode(view)

        // Set touch listener on the root view to capture all touch events
        view.setOnTouchListener { _, event ->
            handleTouchEvent(event)
            false // Allow other views to process the touch event
        }

        // Optional: Handle ViewPager touch interception
        scrollView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_MOVE) {
                requireActivity().findViewById<ViewPager2>(R.id.view_pager)
                    .requestDisallowInterceptTouchEvent(false)
            }
            false
        }
    }

    private fun setupFullScreenMode(view: View) {
        // Use WindowInsetsControllerCompat for immersive mode
        val windowInsetsController = ViewCompat.getWindowInsetsController(view)
        windowInsetsController?.let {
            // Hide both status and navigation bars
            it.hide(WindowInsetsCompat.Type.systemBars())
            // Allow content to draw behind system bars
            it.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        // Ensure the fragment's view draws behind system bars
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            v.setPadding(0, 0, 0, 0) // Remove padding to use full screen
            WindowInsetsCompat.CONSUMED
        }
    }

    private fun handleTouchEvent(event: MotionEvent) {
        val windowInsetsController = ViewCompat.getWindowInsetsController(view ?: return)
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                if (!isBarsVisible) {
                    // Show system bars
                    windowInsetsController?.show(WindowInsetsCompat.Type.systemBars())
                    isBarsVisible = true
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isBarsVisible) {
                    // Hide system bars for immersive mode
                    windowInsetsController?.hide(WindowInsetsCompat.Type.systemBars())
                    isBarsVisible = false
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-apply full-screen mode when fragment resumes
        view?.let { setupFullScreenMode(it) }
    }

    override fun onPause() {
        super.onPause()
        // Restore system bars when leaving the fragment
        val windowInsetsController = ViewCompat.getWindowInsetsController(view ?: return)
        windowInsetsController?.show(WindowInsetsCompat.Type.systemBars())
    }
}