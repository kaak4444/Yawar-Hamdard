package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        DoctorEntity::class,
        FacilityEntity::class,
        AppointmentEntity::class,
        ClaimEntity::class,
        CoordinationCaseEntity::class,
        MessageEntity::class,
        UserProfileEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(RoomTypeConverters::class)
abstract class YawarDatabase : RoomDatabase() {
    abstract fun yawarDao(): YawarDao

    companion object {
        @Volatile
        private var INSTANCE: YawarDatabase? = null

        fun getDatabase(context: Context): YawarDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    YawarDatabase::class.java,
                    "yawar_hamdard_health.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
