package com.cmc.mytaxi.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cmc.mytaxi.data.repository.DriverRepository

class ProfileViewModelFactory(
    private val driverRepository: DriverRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(driverRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
