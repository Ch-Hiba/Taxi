package com.cmc.mytaxi.ui.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.cmc.mytaxi.App
import com.cmc.mytaxi.R
import com.cmc.mytaxi.data.repository.DriverRepository
import com.cmc.mytaxi.data.viewmodel.ProfileViewModel
import com.cmc.mytaxi.data.viewmodel.ProfileViewModelFactory
import com.cmc.mytaxi.ui.fragments.profile.EditProfileFragment
import com.cmc.mytaxi.ui.fragments.profile.ProfileFragment
import com.cmc.mytaxi.ui.activity.HomePage

class MainActivity : AppCompatActivity() {




    private lateinit var driverViewModel: ProfileViewModel
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        enableEdgeToEdge()


        setContentView(R.layout.activity_main)

        // Applique un padding pour gérer les fenêtres système (barres d'état et de navigation)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialisation de SharedPreferences pour vérifier le premier lancement
        sharedPreferences = getSharedPreferences("MyTaxiAppPrefs", Context.MODE_PRIVATE)

        // Vérifie si c'est le premier lancement
        val isFirstLaunch = sharedPreferences.getBoolean("isFirstLaunch", true)

        if (isFirstLaunch) {
            // Si c'est le premier lancement, afficher le ProfileFragment
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProfileFragment())
                .commit()

            // Met à jour SharedPreferences pour marquer que l'application a déjà été lancée
            sharedPreferences.edit().putBoolean("isFirstLaunch", false).apply()
        } else {
            // Initialisation du repository et du ViewModel pour récupérer et gérer les données du driver
            val driverRepository = DriverRepository(App.database.driverDao())
            val factory = ProfileViewModelFactory(driverRepository)
            driverViewModel = ViewModelProvider(this, factory)[ProfileViewModel::class.java]

            val targetFragment = intent.getStringExtra("MainActivity")

            if (targetFragment != null) {
                when (targetFragment) {
                    "editProfile" -> {
                        val fragment = EditProfileFragment()
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .commit()
                    }
                    else -> {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, ProfileFragment())
                            .commit()
                    }
                }
            } else {
                // Vérifie si le driver est déjà créé ou non
                driverViewModel.getDriverById(1).observe(this) { driver ->
                    if (driver?.isCreated == true) {
                        // Si le driver existe et est créé, rediriger vers la HomePage
                        val intent = Intent(this, HomePage::class.java)
                        startActivity(intent)
                        finish()  // Terminer l'activité actuelle pour ne pas revenir
                    } else {
                        // Si le driver n'est pas créé, afficher le ProfileFragment
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, ProfileFragment())
                            .commit()
                    }
                }
            }
        }
    }
}
