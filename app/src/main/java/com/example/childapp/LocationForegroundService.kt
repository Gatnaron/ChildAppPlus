package com.example.childapp

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices

class LocationForegroundService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var service: ChildService
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        service = ChildService(applicationContext)
        sharedPreferences = applicationContext.getSharedPreferences("ChildAppPrefs", Context.MODE_PRIVATE)
    }

    private fun createNotificationChannel(context: Context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "LocationChannel",
                "Отслеживание местоположения",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Канал уведомлений для отслеживания местоположения"
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    private fun getNotification(context: Context): Notification {
        return NotificationCompat.Builder(context, "LocationChannel")
            .setContentTitle("Отслеживание местоположения")
            .setContentText("Приложение работает в фоновом режиме")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Используйте свой значок
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
    }

    private fun getBatteryLevel(): Int {
        val batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }


    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel(this) // Создание канала уведомлений
        val notification = getNotification(this)

        startForeground(1, notification) // Запуск Foreground Service

        val locationRequest = LocationRequest.create().apply {
            interval = 5000 // 5 секунд
            fastestInterval = 2000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    result.lastLocation?.let { location ->
                        Log.d("LocationService", "Местоположение: ${location.latitude}, ${location.longitude}")
                        val locationStr = "${location.latitude},${location.longitude}"
                        val batteryLevel = getBatteryLevel()

                        sharedPreferences.edit().putString("location", locationStr).apply()

                        service.updateLocation(
                            childId = service.getSavedChildGuid() ?: return,
                            parentId = "", // Укажите реальный идентификатор родителя
                            position = locationStr,
                            batteryLevel = batteryLevel
                        ) { result ->
                            Log.d("LocationService", "Результат отправки: $result")
                        }
                    }
                }

            },
            mainLooper
        )
        return START_STICKY
    }

    override fun onBind(intent: Intent?) = null
}
