package top.tsukino.ebookreader.model.ebook

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import top.tsukino.ebookreader.R
import com.google.android.material.R as MaterialR

class ChapterAdapter(
    private var chapters: List<Chapter>,
    private val onChapterClick: (Chapter) -> Unit,
    private var bookLiveData: LiveData<EBook>? = null,
    private val lifecycleOwner: LifecycleOwner
) : RecyclerView.Adapter<ChapterAdapter.ViewHolder>() {

    private var book: EBook? = null

    init {
        bookLiveData?.observe(lifecycleOwner) { newBook ->
            book = newBook
            notifyDataSetChanged()
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.chapter_title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.chapter_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chapter = chapters[position]
        holder.title.text = chapter.title
        holder.itemView.setOnClickListener { onChapterClick(chapter) }
        
        // 设置已读章节的颜色
        if (book?.isChapterRead(chapter.id) == true) {
            holder.title.setTextColor(holder.title.context.getColor(MaterialR.color.material_on_background_disabled))
        } else {
            holder.title.setTextColor(holder.title.context.getColor(MaterialR.color.material_on_background_emphasis_high_type))
        }
    }

    override fun getItemCount() = chapters.size

    fun updateChapters(newChapters: List<Chapter>, newBookLiveData: LiveData<EBook>) {
        chapters = newChapters
        // 移除旧的观察者
        bookLiveData?.removeObservers(lifecycleOwner)
        // 设置新的 LiveData
        bookLiveData = newBookLiveData
        // 添加新的观察者
        bookLiveData?.observe(lifecycleOwner) { newBook ->
            book = newBook
            notifyDataSetChanged()
        }
    }
} 