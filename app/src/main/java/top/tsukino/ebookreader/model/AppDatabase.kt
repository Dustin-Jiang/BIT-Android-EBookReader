package top.tsukino.ebookreader.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import top.tsukino.ebookreader.model.ebook.ChapterEntity
import top.tsukino.ebookreader.model.ebook.EBookEntity
import top.tsukino.ebookreader.model.ebook.EBookDao
import top.tsukino.ebookreader.model.ebook.ChapterContent
import top.tsukino.ebookreader.model.ebook.ChapterContentDao

@Database(
    entities = [
        ChapterEntity::class,
        EBookEntity::class,
        ChapterContent::class
    ],
    version = 3
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ebookDao() : EBookDao
    abstract fun chapterContentDao() : ChapterContentDao

    companion object {
        private var db: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 创建新的章节表
                database.execSQL(
                    "CREATE TABLE chapters_new " +
                            "(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "title TEXT NOT NULL, " +
                            "`index` INTEGER NOT NULL, " +
                            "contentId INTEGER NOT NULL, " +
                            "ebookId INTEGER NOT NULL, " +
                            "FOREIGN KEY(ebookId) REFERENCES ebooks(id) ON DELETE CASCADE)"
                )
                
                database.execSQL(
                    "INSERT INTO chapters_new (id, title, `index`, contentId, ebookId) " +
                            "SELECT id, title, `index`, contentId, ebookId FROM chapters"
                )
                
                database.execSQL("DROP TABLE chapters")
                database.execSQL("ALTER TABLE chapters_new RENAME TO chapters")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 添加 lastReadChapterId 列
                database.execSQL("ALTER TABLE ebooks ADD COLUMN lastReadChapterId INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return db ?: run {
                val db = Room.databaseBuilder(
                        context,
                        AppDatabase::class.java,
                        "ebook_reader.db"
                    )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                this.db = db
                return db
            }
        }
        
        fun getInstance(context: Context): AppDatabase {
            return getDatabase(context)
        }
    }
}
