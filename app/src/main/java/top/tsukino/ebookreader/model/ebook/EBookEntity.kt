package top.tsukino.ebookreader.model.ebook

import androidx.room.Entity
import androidx.room.PrimaryKey
import top.tsukino.ebookreader.model.ebook.EBook

@Entity(tableName = "ebooks")
data class EBookEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    var lastReadChapterId: Long = 0
) {
    // Optional: Extension function to convert to EBook data class
    fun toEBook(): EBook {
        return EBook(id, title, emptyList(), lastReadChapterId) // Chapters will be loaded separately
    }
}

// Extension function to convert EBook data class to EBookEntity
fun EBook.toEBookEntity(): EBookEntity {
    return EBookEntity(id, title, lastReadChapterId)
}