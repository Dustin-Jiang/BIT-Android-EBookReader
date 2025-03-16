package top.tsukino.ebookreader.model.ebook

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

/**
 * 章节内容数据访问对象
 */
@Dao
interface ChapterContentDao {
    @Insert
    suspend fun insert(content: ChapterContent): Long
    
    @Update
    suspend fun update(content: ChapterContent): Int
    
    @Query("SELECT * FROM chapter_contents WHERE id = :id")
    suspend fun getChapterContent(id: Long): ChapterContent?
    
    @Query("DELETE FROM chapter_contents WHERE id = :id")
    suspend fun deleteChapterContent(id: Long): Int
} 