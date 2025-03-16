package top.tsukino.ebookreader.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import top.tsukino.ebookreader.model.EBookReaderDatabase
import top.tsukino.ebookreader.model.ebook.EBook
import top.tsukino.ebookreader.model.ebook.EBookDao
import top.tsukino.ebookreader.model.ebook.EBookWithChapters
import top.tsukino.ebookreader.model.ebook.toChapterEntity
import top.tsukino.ebookreader.model.ebook.toEBookEntity
import top.tsukino.ebookreader.repository.EBookRepository

class MainViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val repository = EBookRepository(application)
    var allBooks: LiveData<List<EBookWithChapters>> = repository.allBooks

    init {
        allBooks.observeForever { bookList ->
            Log.d("MainViewModel", "Loaded books: ${bookList.map { it.toEBook().title }}")
        }
    }

    fun getBooks(): List<EBook> {
        return allBooks.value?.map { it.toEBook() } ?: emptyList()
    }

    suspend fun addBook(book: EBook) {
        repository.saveBook(book)
    }

    suspend fun removeBook(book: EBook) {
        repository.deleteBook(book)
    }

    fun getBookCount(): Int {
        return allBooks.value?.size ?: 0
    }

    fun getBook(index: Int): EBook? {
        if (index < 0 || index >= getBookCount()) {
            return null
        }
        return allBooks.value?.get(index)?.toEBook()
    }

    suspend fun updateBook(book: EBook) {
        repository.updateBook(book)
    }

    suspend fun parseBook(context: List<String>) {
        val book = EBook.parse(context)
        Log.d("MainViewModel", "Parsed book: $book")
        addBook(book)
    }
}