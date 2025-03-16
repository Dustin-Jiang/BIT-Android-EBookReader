package top.tsukino.ebookreader.model.ebook

class EBook(
    var id: Long = 0,
    var title: String,
    val chapters: List<Chapter>,
    var lastReadChapterId: Long = 0
) {
    companion object {
        fun parse(lines: List<String>): EBook {
            val title = lines[0]
            val chapters = ArrayList<Chapter>()
            var content = ArrayList<String>()
            var line = 1
            var chapter = Chapter("Chapter 0", 0)
            val chapterContents = ArrayList<ChapterContent>()

            while (line < lines.size) {
                if (lines[line].isEmpty()) { line++ }
                else if (lines[line].length > 30) {
                    content += lines[line]
                    line++
                }
                else {
                    if (Chapter.isTitle(lines[line])) {
                        // 创建章节内容
                        val chapterContent = ChapterContent(content.joinToString("\n"))
                        chapterContents.add(chapterContent)
                        
                        // 更新章节的contentId（临时使用索引，实际保存时会更新为真实ID）
                        chapter.contentId = chapterContents.size.toLong()
                        chapters.add(chapter)
                        
                        content.clear()
                        chapter = Chapter.parse(lines[line]) ?: Chapter("Chapter ${chapters.size}", chapters.size)
                        line++
                    }
                    else {
                        content += lines[line]
                        line++
                    }
                }
            }

            // 处理最后一章
            val chapterContent = ChapterContent(content.joinToString("\n"))
            chapterContents.add(chapterContent)
            chapter.contentId = chapterContents.size.toLong()
            chapters.add(chapter)
            
            val book = EBook(title = title, chapters = chapters)
            book.chapterContents = chapterContents
            return book
        }
    }

    // 临时存储章节内容，用于保存到数据库
    var chapterContents: List<ChapterContent> = emptyList()

    fun getChapterCount(): Int {
        return chapters.size
    }

    fun getLastReadChapter(): Chapter? {
        return chapters.find { it.id == lastReadChapterId }
    }

    fun updateLastReadChapter(chapterId: Long) {
        lastReadChapterId = chapterId
    }

    fun isChapterRead(chapterId: Long): Boolean {
        val chapter = chapters.find { it.id == chapterId }
        val lastReadChapter = chapters.find { it.id == lastReadChapterId }
        return if (chapter != null && lastReadChapter != null) {
            chapter.index <= lastReadChapter.index
        } else {
            false
        }
    }

    fun copy(
        id: Long = this.id,
        title: String = this.title,
        chapters: List<Chapter> = this.chapters,
        lastReadChapterId: Long = this.lastReadChapterId
    ): EBook {
        val newBook = EBook(id, title, chapters, lastReadChapterId)
        newBook.chapterContents = this.chapterContents
        return newBook
    }
}
