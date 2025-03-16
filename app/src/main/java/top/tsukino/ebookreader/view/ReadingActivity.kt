package top.tsukino.ebookreader.view

import android.os.Bundle
import android.view.WindowManager
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.color.DynamicColors
import top.tsukino.ebookreader.R
import top.tsukino.ebookreader.model.ebook.Chapter
import android.graphics.Color
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import com.google.android.material.color.MaterialColors
import top.tsukino.ebookreader.model.ebook.EBook
import top.tsukino.ebookreader.viewmodel.BookDetailViewModel
import com.google.android.material.R as MaterialR

class ReadingActivity : AppCompatActivity() {
    private lateinit var contentView: TextView
    private lateinit var nextChapterButton: Button
    private lateinit var scrollView: NestedScrollView
    private lateinit var vm: BookDetailViewModel
    private var currentChapterIndex: Int = 0
    private var bookId: Long = 0
    private var currentChapterId: Long = 0
    private lateinit var book: EBook

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 应用动态颜色
        DynamicColors.applyToActivityIfAvailable(this)

        enableEdgeToEdge()
        setContentView(R.layout.activity_reading)

        // 设置Toolbar
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 设置系统栏内边距
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            v.setPadding(v.paddingLeft, statusBarInsets.top, v.paddingRight, v.paddingBottom)
            insets
        }

        contentView = findViewById(R.id.content)
        nextChapterButton = findViewById(R.id.next_chapter)
        scrollView = findViewById(R.id.scroll_view)
        vm = BookDetailViewModel(application)

        // 获取章节数据
        bookId = intent.getLongExtra("book_id", -1)
        currentChapterIndex = intent.getIntExtra("chapter_index", 0)
        currentChapterId = intent.getLongExtra("chapter_id", -1)
        val chapterTitle = intent.getStringExtra("chapter_title") ?: "未知章节"

        // 设置标题
        supportActionBar?.title = chapterTitle

        // 加载书籍和章节内容
        vm.getBook(bookId).observe(this) { loadedBook ->
            if (loadedBook != null) {
                book = loadedBook
                // 更新阅读进度
                book.updateLastReadChapter(currentChapterId)
                vm.updateBook(book)
                // 加载章节内容
                loadChapterContent()
            }
        }

        // 设置下一章按钮点击事件
        nextChapterButton.setOnClickListener {
            loadNextChapter()
        }
    }

    private fun loadChapterContent() {
        vm.getChapterContent(currentChapterId).observe(this) { content ->
            if (content != null) {
                contentView.text = content.content
            } else {
                Toast.makeText(this, "无法加载章节内容", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadNextChapter() {
        if (::book.isInitialized) {
            // 更新阅读进度
            book.updateLastReadChapter(currentChapterId)
            vm.updateBook(book)
            val nextChapterIndex = currentChapterIndex + 1
            if (nextChapterIndex < book.chapters.size) {
                val nextChapter = book.chapters[nextChapterIndex]
                currentChapterIndex = nextChapterIndex
                currentChapterId = nextChapter.id
                supportActionBar?.title = nextChapter.title
                
                // 加载下一章内容
                loadChapterContent()
                
                // 滚动到顶部
                scrollView.smoothScrollTo(0, 0)

                // 如果是最后一章，隐藏下一章按钮
                nextChapterButton.visibility = if (nextChapterIndex >= book.chapters.size - 1) View.GONE else View.VISIBLE
            } else {
                Toast.makeText(this, "已经是最后一章了", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 