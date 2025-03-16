package top.tsukino.ebookreader.model.ebook

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 章节内容实体类
 * 用于存储章节的正文内容
 */
@Entity(tableName = "chapter_contents")
class ChapterContent(
    var content: String = "",
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
) {
    override fun toString(): String {
        return "ChapterContent(id=$id, contentLength=${content.length})"
    }
} 