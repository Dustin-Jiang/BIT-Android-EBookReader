package top.tsukino.ebookreader.view

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.tsukino.ebookreader.MainActivity
import top.tsukino.ebookreader.R
import top.tsukino.ebookreader.databinding.FragmentBookListBinding
import top.tsukino.ebookreader.databinding.BookBottomSheetBinding
import top.tsukino.ebookreader.model.ebook.EBook
import top.tsukino.ebookreader.model.ebook.EBookAdapter
import top.tsukino.ebookreader.viewmodel.MainViewModel
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * A simple [androidx.fragment.app.Fragment] subclass.
 * Use the [BookListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BookListFragment : Fragment() {
    private var _binding: FragmentBookListBinding? = null
    private val binding get() = _binding!!
    private var _bottomSheetBinding: BookBottomSheetBinding? = null
    private val bottomSheetBinding get() = _bottomSheetBinding!!

    private lateinit var getContentLauncher: ActivityResultLauncher<String>
    private val vm: MainViewModel by viewModels()
    private lateinit var adapter: EBookAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupBookGrid()
        setupContentLauncher()
        setupClickListeners()
        observeBooks()
    }

    private fun setupToolbar() {
        // 设置Toolbar
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = getString(R.string.app_name)
            setDisplayHomeAsUpEnabled(false)
        }
    }

    private fun setupBookGrid() {
        adapter = EBookAdapter(requireContext(), vm.getBooks())
        binding.bookGrid.apply {
            adapter = this@BookListFragment.adapter
            setOnItemClickListener { _, _, position, _ ->
                navigateToBookDetail(adapter.getItem(position) as EBook)
            }
            setOnItemLongClickListener { _, _, position, _ ->
                showBottomSheet(adapter.getItem(position) as EBook)
                true
            }
        }
    }

    private fun setupContentLauncher() {
        getContentLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { fileUri ->
                Log.d("MainActivity", "Selected file: $fileUri")
                readBookContent(fileUri)
            } ?: run {
                Toast.makeText(requireContext(), "未选择文件", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupClickListeners() {
        binding.importBook.setOnClickListener {
            onAddBookClick()
        }
    }

    private fun observeBooks() {
        vm.allBooks.observe(viewLifecycleOwner) { _ ->
            adapter.updateBooks(vm.getBooks())
        }
    }

    private fun navigateToBookDetail(book: EBook) {
        val args = Bundle().apply {
            putLong(BOOK_ID, book.id)
        }

        val bookDetailFragment = BookDetailFragment().apply {
            arguments = args
        }

        (requireActivity() as MainActivity).navigateToFragment(bookDetailFragment)
    }

    private fun showBottomSheet(book: EBook) {
        val bottomSheet = BottomSheetDialog(requireContext())
        _bottomSheetBinding = BookBottomSheetBinding.inflate(layoutInflater)

        bottomSheetBinding.apply {
            bookTitle.text = book.title
            deleteButton.setOnClickListener {
                deleteBook(book)
                bottomSheet.dismiss()
            }
        }

        bottomSheet.setContentView(bottomSheetBinding.root)
        bottomSheet.show()
    }

    private fun deleteBook(book: EBook) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    vm.removeBook(book)
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "已删除 ${book.title}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "删除失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun onAddBookClick() {
        getContentLauncher.launch("text/plain")
    }

    private fun readBookContent(uri: Uri) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val content = withContext(Dispatchers.IO) {
                    requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
                        BufferedReader(InputStreamReader(inputStream)).useLines { lines ->
                            lines.toList()
                        }
                    } ?: throw Exception("无法读取文件")
                }

                withContext(Dispatchers.IO) {
                    vm.parseBook(content)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "读取文件失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                e.printStackTrace()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _bottomSheetBinding = null
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment BookListFragment.
         */
        @JvmStatic
        fun newInstance() = BookListFragment()
    }
}