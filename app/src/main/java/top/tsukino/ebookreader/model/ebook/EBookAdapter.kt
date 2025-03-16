package top.tsukino.ebookreader.model.ebook

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import top.tsukino.ebookreader.R
import top.tsukino.ebookreader.viewmodel.MainViewModel

class EBookAdapter(
    private val context: Context,
    private var books: List<EBook>
) : BaseAdapter() {
    private val inflater = LayoutInflater.from(context)

    override fun getCount(): Int {
        return books.size
    }

    override fun getItem(idx: Int): EBook {
        return books[idx]
    }

    override fun getItemId(pos: Int): Long {
        return pos.toLong()
    }

    override fun getView(pos: Int, currentView: View?, parent: ViewGroup?): View {
        val view: View
        val holder: ViewHolder

        if (currentView == null) {
            view = inflater.inflate(R.layout.book_item, parent, false)
            holder = ViewHolder(view)
            view.tag = holder
        } else {
            view = currentView
            holder = currentView.tag as ViewHolder
        }

        getItem(pos).let { book ->
            holder.title.text = book.title
            holder.chapterCount.text = "${book.getChapterCount()}章"
            
            // 设置最后阅读章节
            val lastReadChapter = book.getLastReadChapter()
            if (lastReadChapter != null) {
                val progress = ((lastReadChapter.index.toFloat() / book.chapters.size.toFloat()) * 100).toInt()
                holder.chapterCount.text = "${book.getChapterCount()}章 · ${progress}%"
            }
        }

        return view
    }

    fun updateBooks(newBooks: List<EBook>) {
        books = newBooks
        notifyDataSetChanged()
    }

    private class ViewHolder(view: View) {
        val title = view.findViewById<TextView>(R.id.book_title)
        val chapterCount = view.findViewById<TextView>(R.id.book_chapter_count)
    }
}