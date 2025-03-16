package top.tsukino.ebookreader.model.ebook

import androidx.room.Embedded
import androidx.room.Relation
import top.tsukino.ebookreader.model.ebook.EBook

data class EBookWithChapters(
    @Embedded val ebookEntity: EBookEntity, // Embed EBookEntity
    @Relation(
        parentColumn = "id", // Column in EBookEntity
        entityColumn = "ebookId" // Column in ChapterEntity
    )
    val chapters: List<ChapterEntity> // List of related ChapterEntities
) {
    // Optional: Extension function to convert to EBook data class with List<Chapter>
    fun toEBook(): EBook {
        return EBook(
            ebookEntity.id,
            ebookEntity.title,
            chapters.map { it.toChapter() } // Convert ChapterEntities to Chapters
        )
    }
}