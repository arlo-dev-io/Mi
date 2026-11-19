package com.example.animelibrary

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

enum class Category(val label: String) {
    WATCHING("Watching"),
    COMPLETED("Completed"),
    PLAN("Plan to Watch")
}

@Entity(tableName = "anime_table")
data class Anime(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val posterUrl: String,
    val watchUrl: String,
    val category: Category
)

class Converters {
    @TypeConverter
    fun fromCategory(category: Category): String = category.name

    @TypeConverter
    fun toCategory(value: String): Category = Category.valueOf(value)
}

@Dao
interface AnimeDao {
    @Query("SELECT * FROM anime_table WHERE category = :category ORDER BY id DESC")
    fun observeByCategory(category: Category): Flow<List<Anime>>

    @Insert
    suspend fun insert(anime: Anime)

    @Update
    suspend fun update(anime: Anime)

    @Delete
    suspend fun delete(anime: Anime)
}

@Database(entities = [Anime::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AnimeDatabase : RoomDatabase() {
    abstract fun animeDao(): AnimeDao

    companion object {
        @Volatile
        private var INSTANCE: AnimeDatabase? = null

        fun getDatabase(context: Context): AnimeDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AnimeDatabase::class.java,
                    "anime_library_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
