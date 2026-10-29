package com.rabbyx.newtonixairevivededition

import android.os.Bundle
import android.view.View
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import com.rabbyx.newtonixairevivededition.databinding.ActivityIndexBinding
import com.rabbyx.newtonixairevivededition.ui.main.SectionsPagerAdapter

class IndexActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIndexBinding
    private var isTabLayoutVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIndexBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = binding.tabs
        tabs.setupWithViewPager(viewPager)
        val fab: FloatingActionButton = binding.fab

        tabs.alpha = 0f
        tabs.visibility = View.VISIBLE
        isTabLayoutVisible = false

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                if (positionOffset > 0.0f && !isTabLayoutVisible) {
                    tabs.animate().alpha(1f).setDuration(300).withStartAction {
                        tabs.visibility = View.VISIBLE
                    }.withEndAction {
                        isTabLayoutVisible = true
                    }.start()
                }
            }

            override fun onPageSelected(position: Int) {
                if (isTabLayoutVisible) {
                    tabs.animate().alpha(0f).setDuration(300).withEndAction {
                        isTabLayoutVisible = false
                    }.start()
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
                // Handled in onPageScrolled and onPageSelected
            }
        })

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .setAnchorView(R.id.fab).show()
        }
    }
}