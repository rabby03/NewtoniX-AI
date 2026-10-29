package com.rabbyx.newtonixairevivededition

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat

class BookmarksAdapter(
    private val bookmarks: List<MainActivity.Bookmark>,
    private val onItemClick: (MainActivity.Bookmark) -> Unit
) : RecyclerView.Adapter<BookmarksAdapter.BookmarkViewHolder>() {

    class BookmarkViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bookmarkText: TextView = itemView.findViewById(R.id.bookmarkText)
        val timestampText: TextView = itemView.findViewById(R.id.timestampText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.bookmark_item, parent, false)
        return BookmarkViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookmarkViewHolder, position: Int) {
        val bookmark = bookmarks[position]
        holder.bookmarkText.text = bookmark.inputText+bookmark.imageText.substringBefore("$") // Or whatever you want to show as preview
        holder.timestampText.text = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(bookmark.timestamp)

        holder.itemView.setOnClickListener {
            onItemClick(bookmark)
        }
    }

    override fun getItemCount(): Int = bookmarks.size
}