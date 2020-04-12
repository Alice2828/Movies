package com.example.movie.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.movie.model.Movie

@Database(entities = [Movie::class], version = 1,exportSchema = false)
abstract class MovieDatabase : RoomDatabase() {

    abstract fun movieDao(): MovieDao

    companion object {

        var INSTANCE: MovieDatabase? = null

        fun getDatabase(context: Context): MovieDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                    context.applicationContext,
                    MovieDatabase::class.java,
                    "movies_database.db"
                ).build()
            }
            return INSTANCE!!
        }
    }
}