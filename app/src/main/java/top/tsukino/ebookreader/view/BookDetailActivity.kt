package top.tsukino.ebookreader.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.color.DynamicColors
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.appbar.CollapsingToolbarLayout
import top.tsukino.ebookreader.R
import top.tsukino.ebookreader.model.ebook.Chapter
import top.tsukino.ebookreader.model.ebook.ChapterAdapter
import top.tsukino.ebookreader.model.ebook.EBook
import top.tsukino.ebookreader.viewmodel.BookDetailViewModel
import androidx.lifecycle.LiveData
import kotlin.math.roundToInt

class BookDetailActivity : AppCompatActivity() {
    private lateinit var vm: BookDetailViewModel
    private lateinit var bookLiveData: LiveData<EBook>
    private lateinit var chapterAdapter: ChapterAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 应用动态颜色
        DynamicColors.applyToActivityIfAvailable(this)

        enableEdgeToEdge()
        setContentView(R.layout.activity_book_detail)

        // 设置Toolbar
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 设置系统栏内边距
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            insets
        }

        // 设置状态栏和导航栏颜色
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }

        vm = BookDetailViewModel(application)

        // 初始化 RecyclerView
        val chapterList = findViewById<RecyclerView>(R.id.chapter_list)
        chapterList.layoutManager = LinearLayoutManager(this)
        
        // 获取书籍ID并加载数据
        val bookId = intent.extras?.getLong("book")
        if (bookId != null) {
            bookLiveData = vm.getBook(bookId)
            
            // 初始化适配器
            chapterAdapter = ChapterAdapter(emptyList(), { chapter ->
                onChapterClick(chapter)
            }, bookLiveData, this)
            chapterList.adapter = chapterAdapter

            // 观察书籍数据变化
            bookLiveData.observe(this) { book ->
                if (book != null) {
                    updateInfo(book)
                } else {
                    Toast.makeText(this, "无法加载书籍信息", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }

        // 设置FAB点击事件
        findViewById<ExtendedFloatingActionButton>(R.id.start_reading).setOnClickListener {
            bookLiveData.value?.let { book ->
                if (book.chapters.isNotEmpty()) {
                    // 从上次阅读的章节继续
                    val lastReadChapter = book.getLastReadChapter() ?: book.chapters[0]
                    onChapterClick(lastReadChapter)
                } else {
                    Toast.makeText(this, "没有可阅读的章节", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun updateInfo(book: EBook) {
        // 更新标题
        val collapsingToolbar = findViewById<CollapsingToolbarLayout>(R.id.collapsing_toolbar)
        collapsingToolbar.title = book.title

        // 更新章节数
        findViewById<TextView>(R.id.chapter_count).text = "共 ${book.chapters.size} 章"

        // 更新最后阅读章节
        val lastReadChapter = book.getLastReadChapter()
        findViewById<TextView>(R.id.last_read_chapter).text = if (lastReadChapter != null) {
            "上次读到：${lastReadChapter.title}"
        } else {
            "尚未开始阅读"
        }

        // 更新阅读进度
        val progress = if (lastReadChapter != null && book.chapters.isNotEmpty()) {
            ((lastReadChapter.index.toFloat() / book.chapters.size.toFloat()) * 100).roundToInt()
        } else {
            0
        }
        findViewById<TextView>(R.id.reading_progress).text = "阅读进度：${progress}%"

        // 更新章节列表
        chapterAdapter.updateChapters(book.chapters, bookLiveData)
    }

    private fun onChapterClick(chapter: Chapter) {
        bookLiveData.value?.let { book ->
            // 更新最后阅读的章节
            vm.updateLastReadChapter(book, chapter.id)
            
            val intent = Intent(this, ReadingActivity::class.java)
            intent.putExtra("book_id", book.id)
            intent.putExtra("chapter_index", chapter.index)
            intent.putExtra("chapter_id", chapter.id)
            intent.putExtra("chapter_title", chapter.title)
            startActivity(intent)
        }
    }
}