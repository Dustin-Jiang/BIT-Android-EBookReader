package top.tsukino.ebookreader.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.tsukino.ebookreader.model.EBookReaderDatabase
import top.tsukino.ebookreader.model.ebook.EBook
import top.tsukino.ebookreader.model.ebook.EBookDao
import top.tsukino.ebookreader.model.ebook.EBookWithChapters
import top.tsukino.ebookreader.model.ebook.ChapterContent
import top.tsukino.ebookreader.repository.EBookRepository

class BookDetailViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val db: EBookDao =
        EBookReaderDatabase.getDatabase(
            application
        ).ebookDao()

    val allBooks: LiveData<List<EBookWithChapters>> = db.getAllEBooksWithChapters()

    private val repository = EBookRepository(application)

    fun getBook(id: Long): LiveData<EBook> {
        return repository.getBook(id)
    }
    
    fun getChapterContent(chapterId: Long): LiveData<ChapterContent> {
        val result = MutableLiveData<ChapterContent>()
        viewModelScope.launch {
            val content = withContext(Dispatchers.IO) {
                repository.getChapterContent(chapterId)
            }
            result.postValue(content)
        }
        return result
    }

    fun updateBook(book: EBook) {
        viewModelScope.launch {
            repository.updateBook(book)
        }
    }

    fun updateLastReadChapter(book: EBook, chapterId: Long) {
        viewModelScope.launch {
            // 创建新的 EBook 对象以触发 LiveData 更新
            val updatedBook = book.copy(
                lastReadChapterId = chapterId
            )
            repository.updateBook(updatedBook)
        }
    }
}