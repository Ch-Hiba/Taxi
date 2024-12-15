package com.cmc.mytaxi.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.cmc.mytaxi.R
import com.cmc.mytaxi.data.viewmodel.CalculatTraficViewModel
import com.cmc.mytaxi.data.viewmodel.CalculatTraficViewModelFactory
import com.cmc.mytaxi.databinding.ActivityHomePageBinding
import com.cmc.mytaxi.utils.NotificationHelper
import com.cmc.mytaxi.utils.PermissionsHelper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.Locale

class HomePage : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityHomePageBinding
    private lateinit var viewModel: CalculatTraficViewModel
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var isRideActive: Boolean = false
    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval: Long = 5000

    private companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    private val locationPermissionResult = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            enableLocation()
        } else {
            Toast.makeText(this, "Permission denied. Cannot start the ride.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val factory = CalculatTraficViewModelFactory(this)
        viewModel = ViewModelProvider(this, factory)[CalculatTraficViewModel::class.java]
        notificationHelper = NotificationHelper(this)

        setupLocationServices()

        setupRideToggleButton()

        observeRideData()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setupMap()

        setupProfileImageClick()
    }

    private fun setupLocationServices() {
        PermissionsHelper.checkAndPromptLocationServices(
            this,
            onLocationSettingsSatisfied = {
                Toast.makeText(this, "Location services are already enabled.", Toast.LENGTH_SHORT).show()
            },
            onResolutionRequired = { exception ->
                try {
                    exception.startResolutionForResult(
                        this,
                        PermissionsHelper.LOCATION_REQUEST_CODE
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Toast.makeText(
                        this,
                        "Unable to start resolution: ${sendEx.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            onFailure = { exception ->
                Toast.makeText(
                    this,
                    "Failed to check location settings: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    private fun setupRideToggleButton() {
        binding.btnToggleRide.setOnClickListener {
            if (!PermissionsHelper.hasLocationPermission(this)) {
                PermissionsHelper.requestLocationPermission(this)
                return@setOnClickListener
            }

            if (!isRideActive) {
                startRide()
                binding.btnToggleRide.text = getString(R.string.end_ride)
                isRideActive = true
            } else {
                endRide()
                binding.btnToggleRide.text = getString(R.string.start_ride)
                isRideActive = false
            }
        }
    }

    private fun observeRideData() {
        viewModel.rideData.observe(this) { ride ->
            val distance = String.format(Locale.ROOT, "%.2f", ride.distance)
            val fare = String.format(Locale.ROOT, "%.2f", ride.totalFare)

            binding.tvDistance.text = getString(R.string.distance_format, distance)
            binding.tvTimeElapsed.text = getString(R.string.time_elapsed_format, ride.timeElapsed)
            binding.tvTotalFare.text = getString(R.string.total_fare_format, fare)
        }
    }

    private fun setupMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun setupProfileImageClick() {
        binding.profileImage.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("MainActivity", "editProfile")
            }
            startActivity(intent)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        enableLocation()
    }

    private fun enableLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            map.isMyLocationEnabled = true
            getCurrentLocation()
        } else {
            locationPermissionResult.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let { updateMapLocation(it) }
        }

        fusedLocationClient.requestLocationUpdates(
            com.google.android.gms.location.LocationRequest.create().apply {
                interval = 5000
                fastestInterval = 2000
                priority = com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
            },
            object : com.google.android.gms.location.LocationCallback() {
                override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
                    locationResult.lastLocation?.let { updateMapLocation(it) }
                }
            },
            null
        )
    }

    private fun updateMapLocation(location: Location) {
        val currentLatLng = LatLng(location.latitude, location.longitude)
        map.clear()
        map.addMarker(MarkerOptions().position(currentLatLng).title("Ma position actuelle"))
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
    }

    private fun startRide() {
        viewModel.startRide()
        handler.postDelayed(updateRideTask, updateInterval)
    }

    private fun endRide() {
        handler.removeCallbacks(updateRideTask)
        viewModel.rideData.value?.let { ride ->
            notificationHelper.sendFareNotification(
                ride.totalFare, ride.distance, ride.timeElapsed
            )
        }
    }

    private val updateRideTask = object : Runnable {
        override fun run() {
            if (isRideActive) {
                viewModel.updateLocation()
                handler.postDelayed(this, updateInterval)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableLocation()
            }
        }
    }
}
