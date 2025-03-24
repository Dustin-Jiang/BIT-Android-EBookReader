package top.tsukino.ebookreader.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.tsukino.ebookreader.R
import top.tsukino.ebookreader.databinding.FragmentBookDetailBinding
import top.tsukino.ebookreader.model.ebook.Chapter
import top.tsukino.ebookreader.model.ebook.ChapterAdapter
import top.tsukino.ebookreader.model.ebook.EBook
import top.tsukino.ebookreader.viewmodel.BookDetailViewModel
import kotlin.math.roundToInt

const val BOOK_ID = "book_id"

/**
 * A simple [androidx.fragment.app.Fragment] subclass.
 * Use the [BookDetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BookDetailFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var bookId: Long = 0
    private var _binding: FragmentBookDetailBinding? = null
    private val binding get() = _binding!!

    private val vm: BookDetailViewModel by viewModels()
    private lateinit var chapterAdapter: ChapterAdapter
    private lateinit var bookLiveData: LiveData<EBook>

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )
        arguments?.let {
            bookId =
                it.getLong(
                    top.tsukino.ebookreader.view.BOOK_ID
                )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        // 确保 ActionBar 显示
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()
    }

    override fun onStop() {
        super.onStop()
        // 确保 ActionBar 显示
        (requireActivity() as AppCompatActivity).supportActionBar?.show()
    }


    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        setupToolbar()
        loadBookData()
        setupRecyclerView()
        setupClickListeners()
    }

    private fun setupToolbar() {
        // 设置 Toolbar
        binding.toolbar.apply {
            // 设置返回按钮
            setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
            setNavigationOnClickListener {
                requireActivity().onBackPressed()
            }
        }

        // 设置 CollapsingToolbarLayout 的一些属性
        binding.collapsingToolbar.apply {
            setExpandedTitleTextAppearance(R.style.ExpandedTitleStyle)
            setCollapsedTitleTextAppearance(R.style.CollapsedTitleStyle)
            isTitleEnabled = true
        }

        // 设置 AppBarLayout 的状态监听
        binding.appbar.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            val scrollRange = appBarLayout.totalScrollRange
            val fraction = Math.abs(verticalOffset).toFloat() / scrollRange
            
            // 根据滚动位置调整 FAB 的显示状态
            if (fraction > 0.5f) {
                binding.startReading.shrink()
            } else {
                binding.startReading.extend()
            }
        }
    }

    private fun setupRecyclerView() {
        if (!::bookLiveData.isInitialized) {
            bookLiveData = vm.getBook(bookId)
        }
        
        chapterAdapter = ChapterAdapter(
            emptyList(),
            { chapter -> onChapterClick(chapter) },
            bookLiveData,
            this
        )
        
        binding.chapterList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = chapterAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupClickListeners() {
        binding.startReading.setOnClickListener {
            bookLiveData.value?.let { book ->
                if (book.chapters.isNotEmpty()) {
                    val lastReadChapter = book.getLastReadChapter() ?: book.chapters[0]
                    onChapterClick(lastReadChapter)
                } else {
                    Toast.makeText(context, "没有可阅读的章节", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadBookData() {
        if (bookId != 0L) {
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        bookLiveData = vm.getBook(bookId)
                    }
                    
                    bookLiveData.observe(viewLifecycleOwner) { book ->
                        viewLifecycleOwner.lifecycleScope.launch {
                            if (book != null) {
                                updateInfo(book)
                            } else {
                                Toast.makeText(context, "无法加载书籍信息", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "加载书籍信息失败: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private suspend fun updateInfo(book: EBook) = withContext(Dispatchers.Main) {
        binding.apply {
            // 更新标题
            collapsingToolbar.title = book.title

            // 更新章节数
            chapterCount.text = "共 ${book.chapters.size} 章"

            // 更新最后阅读章节
            val lastReadChapter = book.getLastReadChapter()
            this.lastReadChapter.text = if (lastReadChapter != null) {
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
            readingProgress.text = "阅读进度：${progress}%"
        }

        // 更新章节列表 - 确保在主线程中执行
        chapterAdapter.updateChapters(book.chapters, bookLiveData)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onChapterClick(chapter: Chapter) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                bookLiveData.value?.let { book ->
                    // 更新最后阅读的章节
                    withContext(Dispatchers.IO) {
                        vm.updateLastReadChapter(book, chapter.id)
                    }

                    withContext(Dispatchers.Main) {
                        val args = Bundle().apply {
                            putLong(BOOK_ID, book.id)
                            putLong(CHAPTER_ID, chapter.id)
                            putInt(CHAPTER_INDEX, chapter.index)
                            putString(CHAPTER_TITLE, chapter.title)
                        }

                        val readingFragment = ReadingFragment().apply {
                            arguments = args
                        }

                        requireActivity().supportFragmentManager.beginTransaction()
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_MATCH_ACTIVITY_OPEN)
                            .replace(R.id.container, readingFragment)
                            .addToBackStack(null)
                            .commit()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "更新阅读进度失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param bookId
         * @return A new instance of fragment BookDetailFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(
            bookId: Long
        ) =
            BookDetailFragment().apply {
                arguments =
                    Bundle().apply {
                        putLong(
                            top.tsukino.ebookreader.view.BOOK_ID,
                            bookId
                        )
                    }
            }
    }
}