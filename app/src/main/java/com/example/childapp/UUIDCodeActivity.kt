package com.example.childapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.childapp.Models.Responses.ChildLinkResponse
import com.example.childapp.databinding.ActivityQrcodeBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.google.gson.Gson

class UUIDCodeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQrcodeBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var childService: ChildService

    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrcodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        childService = ChildService(applicationContext)

        // Уникальный идентификатор устройства
        val guid = childService.getSavedChildGuid().toString()

        // Создание ребенка на сервере
        childService.createChildLink(guid) { link, error ->
            runOnUiThread {
                if (link != null) {
                    binding.textViewFirstPart.text = "Первый код: ${link.code1}"
                    binding.textViewLastPart.text = "Второй код: ${link.code2}"
                }
                else {
                    Toast.makeText(
                        this@UUIDCodeActivity,
                        "Ошибка загрузки данных: ${error.orEmpty()}",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.textViewFirstPart.text = "Ошибка парсинга данных"
                    binding.textViewLastPart.text = "Неизвестная ошибка"
                }
            }
        }


        // Настройка для получения координат
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        displayLastKnownLocation()
    }

    @SuppressLint("MissingPermission")
    private fun displayLastKnownLocation() {
        if (hasLocationPermission()) {
            fusedLocationClient.lastLocation.addOnCompleteListener { task: Task<android.location.Location> ->
                if (task.isSuccessful && task.result != null) {
                    val location = task.result
                    val latitude = location.latitude
                    val longitude = location.longitude
                    val formattedLocation = "Широта: $latitude, Долгота: $longitude"

                    // Сохранение последнего местоположения
                    val sharedPref = getSharedPreferences("location_prefs", Context.MODE_PRIVATE)
                    sharedPref.edit().putString("last_location", formattedLocation).apply()

                    // Обновление UI
                    binding.textViewCoordinates.text = "Последние координаты: $formattedLocation"
                } else {
                    binding.textViewCoordinates.text = "Координаты отсутствуют"
                }
            }
        } else {
            binding.textViewCoordinates.text = "Нет разрешений на доступ к координатам"
        }
    }

    private fun hasLocationPermission(): Boolean {
        val fineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        return fineLocationPermission == PackageManager.PERMISSION_GRANTED && coarseLocationPermission == PackageManager.PERMISSION_GRANTED
    }
}
