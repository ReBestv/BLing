package com.standbyus.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [StatusEntity::class, CheckinRecordEntity::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun statusDao(): StatusDao
    abstract fun checkinDao(): CheckinDao
}
