package com.rabbyx.newtonixairevivededition

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson

class BookmarksActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookmarks)

        recyclerView = findViewById(R.id.bookmarksRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        loadBookmarks()
    }

    private fun loadBookmarks() {
        val bookmarks = getBookmarks()
        if (bookmarks.isEmpty()) {
            recyclerView.adapter = BookmarksAdapter(emptyList()) { /* empty */ }
        } else {
            recyclerView.adapter = BookmarksAdapter(bookmarks) { bookmark ->
                val intent = Intent(this, WebViewActivity::class.java).apply {
                    putExtra("content", bookmark.responseText)
                }
                startActivity(intent)
            }
        }
    }

    private fun getBookmarks(): List<MainActivity.Bookmark> {
        val prefs = getSharedPreferences("Bookmarks", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = prefs.getString("bookmark_list", null)
        return if (json != null) {
            gson.fromJson(json, Array<MainActivity.Bookmark>::class.java).toList()
        } else {
            emptyList()
        }
    }
}