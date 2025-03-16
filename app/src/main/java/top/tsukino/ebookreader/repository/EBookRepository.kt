package top.tsukino.ebookreader.repository

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import top.tsukino.ebookreader.model.EBookReaderDatabase
import top.tsukino.ebookreader.model.ebook.EBook
import top.tsukino.ebookreader.model.ebook.ChapterContent
import top.tsukino.ebookreader.model.ebook.toChapterEntity
import top.tsukino.ebookreader.model.ebook.toEBookEntity

/**
 * 电子书仓库类
 * 负责处理电子书相关的数据访问逻辑
 */
class EBookRepository(application: Application) {
    private val database = EBookReaderDatabase.getInstance(application)
    private val eBookDao = database.ebookDao()
    private val chapterContentDao = database.chapterContentDao()
    
    // 获取所有书籍
    val allBooks = eBookDao.getAllEBooksWithChapters()
    
    // 根据ID获取书籍
    fun getBook(id: Long): LiveData<EBook> {
        return allBooks.map { books ->
            books.find { it.toEBook().id == id }?.toEBook()
                ?: throw IllegalArgumentException("Book with id $id not found")
        }
    }
    
    // 保存书籍（包括章节和章节内容）
    suspend fun saveBook(book: EBook) = withContext(Dispatchers.IO) {
        // 1. 保存书籍基本信息
        val bookId = eBookDao.insertEBook(book.toEBookEntity())
        
        // 2. 保存章节内容
        val contentIds = book.chapterContents.map { content ->
            chapterContentDao.insert(content)
        }
        
        // 3. 保存章节信息，并关联到对应的内容
        book.chapters.forEachIndexed { index, chapter ->
            // 设置正确的contentId
            chapter.contentId = contentIds[index]
            // 保存章节
            eBookDao.insertChapter(chapter.toChapterEntity(bookId))
        }
    }
    
    // 更新书籍
    suspend fun updateBook(book: EBook) = withContext(Dispatchers.IO) {
        eBookDao.updateEBook(book.toEBookEntity())
    }
    
    // 删除书籍
    suspend fun deleteBook(book: EBook) = withContext(Dispatchers.IO) {
        eBookDao.deleteEBook(book.toEBookEntity())
    }
    
    // 根据章节ID获取章节内容
    suspend fun getChapterContent(chapterId: Long): ChapterContent {
        return chapterContentDao.getChapterContent(chapterId)
            ?: throw IllegalArgumentException("Chapter content with id $chapterId not found")
    }
    
    // 保存章节内容
    suspend fun saveChapterContent(content: ChapterContent): Long {
        return if (content.id == 0L) {
            // 新增
            chapterContentDao.insert(content)
        } else {
            // 更新
            chapterContentDao.update(content)
            content.id
        }
    }
} 