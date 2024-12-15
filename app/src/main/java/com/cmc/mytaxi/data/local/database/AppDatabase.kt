package com.cmc.mytaxi.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cmc.mytaxi.data.local.dao.DriverDao
import com.cmc.mytaxi.data.local.models.Driver


@Database(entities = [Driver::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun driverDao(): DriverDao
}