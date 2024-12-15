package com.cmc.mytaxi.utils

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import com.cmc.mytaxi.R

class NotificationHelper(private val context: Context) {

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val CHANNEL_ID = "fare_channel"
        const val CHANNEL_NAME = "Ride Fare Notifications"
        const val NOTIFICATION_ID = 1
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications related to ride fares"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Vérifiez la permission sur Android 13 et versions ultérieures
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!isNotificationPermissionGranted()) {
                requestNotificationPermission()
            }
        }
    }

    // Vérifie si la permission de notification est accordée
    private fun isNotificationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Demande la permission POST_NOTIFICATIONS
    private fun requestNotificationPermission() {
        ActivityCompat.requestPermissions(
            context as Activity,
            arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
            1
        )
    }

    fun sendFareNotification(fare: Double, distance: Double, time: Long) {
        // Format the values with two decimal places
        val formattedFare = String.format("%.2f", fare)
        val formattedDistance = String.format("%.2f", distance)
        val formattedTime = "$time min"  // Make sure time is formatted correctly (e.g., minutes)

        // Ensure you have a valid icon
        val icon = R.drawable.taxi  // Replace with a valid icon, or use android.R.drawable.ic_menu_report_image

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Ride Completed")
            .setContentText("Fare: $formattedFare DH | Distance: $formattedDistance km | Time: $formattedTime")
            .setSmallIcon(icon)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // High priority for immediate notification
            .setAutoCancel(true) // Dismiss the notification when tapped
            .build()

        // Show the notification
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
