package com.rabbyx.newtonixairevivededition

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.rabbyx.newtonixairevivededition.R
import com.rabbyx.newtonixairevivededition.interfaces.HelperUtil

class BiologyFragment : Fragment() {

    lateinit var chatContainer: LinearLayout
    lateinit var scrollView: ScrollView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_biology, container, false)
        chatContainer = view.findViewById(R.id.chatMessagesContainer)
        scrollView = view.findViewById(R.id.chatScrollView)
        chatContainer.let { HelperUtil(it).welcomeTextBiology() }
        scrollView.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            val activity = requireActivity() as? MainActivity
            if (scrollY > oldScrollY && scrollY > 0) {
                // Scrolling down
                activity?.hideTopAndBottomBars()
            } else if (scrollY < oldScrollY) {
                // Scrolling up
                activity?.showTopAndBottomBars()
            } else if (scrollY == 0) {
                // At the top
                activity?.showTopAndBottomBars()
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Prevent ScrollView from intercepting horizontal swipes
        scrollView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    // Allow ViewPager2 to handle horizontal swipes
                    requireActivity().findViewById<ViewPager2>(R.id.view_pager)
                        .requestDisallowInterceptTouchEvent(false)
                }
            }
            false
        }
    }
}

private fun MainActivity?.showTopAndBottomBars() {
    TODO("Not yet implemented")
}

private fun MainActivity?.hideTopAndBottomBars() {
    TODO("Not yet implemented")
}
