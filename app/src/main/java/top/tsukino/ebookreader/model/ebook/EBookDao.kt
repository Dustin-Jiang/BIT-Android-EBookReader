package top.tsukino.ebookreader.model.ebook

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface EBookDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEBook(ebook: EBookEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: ChapterEntity)

    @Transaction // Ensures atomicity of the operation
    @Query("SELECT * FROM ebooks WHERE id = :ebookId")
    suspend fun getEBookWithChapters(ebookId: Long): EBookWithChapters?

    @Transaction
    @Query("SELECT * FROM ebooks ORDER BY title ASC")
    fun getAllEBooksWithChapters(): LiveData<List<EBookWithChapters>>

    @Delete
    suspend fun deleteEBook(ebook: EBookEntity)

    @Update
    suspend fun updateEBook(ebook: EBookEntity)

    // ... other DAO methods ...
}