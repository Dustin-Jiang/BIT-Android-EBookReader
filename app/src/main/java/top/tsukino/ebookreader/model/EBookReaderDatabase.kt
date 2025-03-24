package top.tsukino.ebookreader.model

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor

class EBookReaderDatabase {
    companion object {
        private var db: AppDatabase? = null

        // 数据库迁移：从版本1到版本2
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 创建章节内容表
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `chapter_contents` " +
                            "(`content` TEXT NOT NULL, " +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL)"
                )
                
                // 从章节表中提取内容到新表
                database.execSQL(
                    "INSERT INTO chapter_contents (content) " +
                            "SELECT content FROM chapters"
                )
                
                // 更新章节表，添加contentId字段
                database.execSQL(
                    "ALTER TABLE chapters ADD COLUMN contentId INTEGER NOT NULL DEFAULT 0"
                )
                
                // 更新章节表中的contentId字段
                database.execSQL(
                    "UPDATE chapters SET contentId = id"
                )
                
                // 删除章节表中的content字段（SQLite不支持直接删除列，需要创建新表）
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

        fun getDatabase(context: Context): AppDatabase {
            return db ?: run {
                val db = Room.databaseBuilder(
                        context,
                        AppDatabase::class.java,
                        "ebook_reader.db"
                    )
                    .addMigrations(MIGRATION_1_2)
                    .setQueryExecutor(Dispatchers.IO.asExecutor())
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
