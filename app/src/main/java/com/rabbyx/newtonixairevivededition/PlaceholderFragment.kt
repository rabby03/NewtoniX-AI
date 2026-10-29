package com.rabbyx.newtonixairevivededition.ui // Move to correct package

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.rabbyx.newtonixairevivededition.R

class PlaceholderFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_placeholder, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Optional: Add log to confirm fragment is loaded
        android.util.Log.d("PlaceholderFragment", "PlaceholderFragment loaded")
    }
}

