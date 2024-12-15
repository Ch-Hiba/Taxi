package com.cmc.mytaxi.data.repository

import com.cmc.mytaxi.data.local.dao.DriverDao
import com.cmc.mytaxi.data.local.models.Driver

class DriverRepository(private val driverDao: DriverDao) {

    suspend fun upsertDriver(driver: Driver) {
        driverDao.upsertDriver(driver)
    }

    suspend fun getDriverById(driverId: Int): Driver? {
        return driverDao.getDriverById(driverId)
    }
}
