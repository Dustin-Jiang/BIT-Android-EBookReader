package top.tsukino.ebookreader.model.ebook

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import top.tsukino.ebookreader.model.ebook.Chapter

@Entity(
    tableName = "chapters",
    foreignKeys = [
        ForeignKey(
            entity = EBookEntity::class, // Reference EBookEntity
            parentColumns = ["id"],       // Parent column in EBookEntity
            childColumns = ["ebookId"],    // Child column in ChapterEntity
            onDelete = ForeignKey.CASCADE // Optional: Delete chapters when ebook is deleted
        )
    ]
)
data class ChapterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ebookId: Long, // Foreign key linking to EBookEntity
    val index: Int, // You can add chapter number for ordering
    val title: String,
    val contentId: Long = 0 // 关联到章节内容的ID
) {
    // Optional: Extension function to convert to Chapter data class (if you have one)
    fun toChapter(): Chapter {
        return Chapter(title, index, contentId, id) // 使用contentId替代content
    }
}

// Extension function to convert
fun Chapter.toChapterEntity(ebookId: Long): ChapterEntity {
    return ChapterEntity(id, ebookId, index, title, contentId)
}