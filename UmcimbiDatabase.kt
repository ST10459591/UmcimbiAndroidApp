package com.example.umcimbisa

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ProviderEntity::class],
    version = 2,
    exportSchema = false
)
abstract class UmcimbiDatabase : RoomDatabase() {

    abstract fun providerDao(): ProviderDao

    companion object {

        @Volatile
        private var INSTANCE: UmcimbiDatabase? = null

        fun getDatabase(context: Context): UmcimbiDatabase {

            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UmcimbiDatabase::class.java,
                    "umcimbi_database"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}