package top.tsukino.ebookreader

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.GridView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.activity.enableEdgeToEdge
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.color.DynamicColors
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.runBlocking
import top.tsukino.ebookreader.model.ebook.EBook
import top.tsukino.ebookreader.model.ebook.EBookAdapter
import top.tsukino.ebookreader.view.BookDetailActivity
import top.tsukino.ebookreader.viewmodel.MainViewModel
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {
    private lateinit var getContentLauncher: ActivityResultLauncher<String>
    lateinit var vm: MainViewModel

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // 应用动态颜色
        DynamicColors.applyToActivityIfAvailable(this)
        
        setContentView(R.layout.activity_main)

        // 设置系统栏内边距
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            insets
        }
        
        // 设置ActionBar
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.apply {
            title = getString(R.string.app_name)
            setDisplayHomeAsUpEnabled(false)
        }

        vm = MainViewModel(application)

        getContentLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { fileUri ->
                Log.d("MainActivity", "Selected file: $fileUri")
                readBookContent(fileUri)
            } ?: run {
                Toast.makeText(this, "No file selected.", Toast.LENGTH_SHORT).show()
            }
        }

        val bookGrid = findViewById<GridView>(R.id.bookGrid)
        val adapter = EBookAdapter(this, vm.getBooks())
        bookGrid.adapter = adapter

        vm.allBooks.observe(this) { books ->
            adapter.updateBooks(vm.getBooks())
        }

        bookGrid.setOnItemClickListener { parent, view, position, id ->
            val book = adapter.getItem(position)
            val intent = Intent(this, BookDetailActivity::class.java)
            intent.putExtra("book", book.id)
            startActivity(intent)
        }

        bookGrid.setOnItemLongClickListener { parent, view, position, id ->
            val book = adapter.getItem(position)
            showBottomSheet(book)
            true
        }

        val addBookButton = findViewById<ExtendedFloatingActionButton>(R.id.importBook)
        addBookButton.setOnClickListener {
            onAddBookClick()
        }
    }

    private fun showBottomSheet(book: EBook) {
        val bottomSheet = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.book_bottom_sheet, null)
        
        view.findViewById<TextView>(R.id.bookTitle).text = book.title
        
        view.findViewById<Button>(R.id.deleteButton).setOnClickListener {
            runBlocking { vm.removeBook(book) }
            bottomSheet.dismiss()
            Toast.makeText(this, "已删除 ${book.title}", Toast.LENGTH_SHORT).show()
        }
        
        bottomSheet.setContentView(view)
        bottomSheet.show()
    }

    private fun onAddBookClick() {
        getContentLauncher.launch("text/plain")
    }

    private fun readBookContent(uri: Uri) {
        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val reader = BufferedReader(InputStreamReader(inputStream))
                val content = ArrayList<String>()
                var line: String? = reader.readLine()
                while (line != null) {
                    content.add(line)
                    line = reader.readLine()
                }
                runBlocking { vm.parseBook(content) }
            } ?: run {
                Toast.makeText(this, "Fail to read file as plain text. ", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Fail to read file. ", Toast.LENGTH_SHORT).show()
        }
    }
}