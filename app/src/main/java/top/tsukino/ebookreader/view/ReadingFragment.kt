package top.tsukino.ebookreader.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import top.tsukino.ebookreader.R
import top.tsukino.ebookreader.databinding.FragmentReadingBinding
import top.tsukino.ebookreader.model.ebook.EBook
import top.tsukino.ebookreader.viewmodel.BookDetailViewModel

const val CHAPTER_INDEX = "chapter_index"
const val CHAPTER_ID = "chapter_id"
const val CHAPTER_TITLE = "chapter_title"

class ReadingFragment : Fragment() {
    private var _binding: FragmentReadingBinding? = null
    private val binding get() = _binding!!
    private val vm: BookDetailViewModel by viewModels()
    private var currentChapterIndex: Int = 0
    private var bookId: Long = 0
    private var currentChapterId: Long = 0
    private var chapterTitle: String? = null
    private lateinit var book: EBook

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )
        arguments?.let {
            bookId = it.getLong(
                top.tsukino.ebookreader.view.BOOK_ID
            )
            currentChapterIndex = it.getInt(
                CHAPTER_INDEX
            )
            currentChapterId = it.getLong(
                CHAPTER_ID
            )
            chapterTitle = it.getString(
                CHAPTER_TITLE
            )
        }
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReadingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 设置Toolbar
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // 设置标题
        binding.toolbar.title = chapterTitle

        // 加载书籍和章节内容
        vm.getBook(bookId).observe(viewLifecycleOwner) { loadedBook ->
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
        binding.nextChapter.setOnClickListener {
            loadNextChapter()
        }
    }

    private fun loadChapterContent() {
        vm.getChapterContent(currentChapterId).observe(viewLifecycleOwner) { content ->
            if (content != null) {
                binding.content.text = content.content
            } else {
                Toast.makeText(requireContext(), "无法加载章节内容", Toast.LENGTH_SHORT).show()
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
                binding.toolbar.title = nextChapter.title
                
                // 加载下一章内容
                loadChapterContent()
                
                // 滚动到顶部
                binding.scrollView.smoothScrollTo(0, 0)

                // 如果是最后一章，隐藏下一章按钮
                binding.nextChapter.visibility = if (nextChapterIndex >= book.chapters.size - 1) View.GONE else View.VISIBLE
            } else {
                Toast.makeText(requireContext(), "已经是最后一章了", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 