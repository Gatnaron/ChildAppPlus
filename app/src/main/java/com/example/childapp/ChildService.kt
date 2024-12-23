package com.example.childapp

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.childapp.Models.Requests.SetPositionRequest
import com.example.childapp.Models.Responses.ChildLinkResponse
import okhttp3.*
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.http.HttpMethod
import java.io.IOException

class ChildService(private val context: Context) {
    // Базовый URL API
    private val BASE_URL = "http://10.0.2.2:5059/api/child/"

    private val client = OkHttpClient()
    private val gson = Gson()

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("ChildAppPrefs", Context.MODE_PRIVATE)

    private fun saveGuidToPreferences(guid: String) {
        sharedPreferences.edit().putString("childGuid", guid).apply()
    }
    fun getSavedChildGuid(): String? {
        val id = sharedPreferences.getString("childGuid", null)?.replace("\"", "")
        Log.e("GUID", id.toString())
        return id
    }

    fun createChild(deviceId: String, callback: (String) -> Unit) {
        val url = "${BASE_URL}create/$deviceId"

        val request = Request.Builder()
            .url(url)
            .post(RequestBody.create(null, ByteArray(0)))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val guid = response.body?.string() ?: ""
                    saveGuidToPreferences(guid)
                    callback("Child created successfully: $guid")
                } else {
                    callback("Error: ${response.code}")
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                callback("Server error: ${e.message}")
            }
        })
    }

    fun createChildLink(childId: String, callback: (ChildLinkResponse?, String?) -> Unit) {
        val url = "${BASE_URL}link/$childId"
        val request = Request.Builder()
            .url(url)
            .post(RequestBody.create(null, ByteArray(0)))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let {
                    try {
                        val childLinkResponse = gson.fromJson(it, ChildLinkResponse::class.java)
                        callback(childLinkResponse, null)
                    } catch (e: Exception) {
                        callback(null, "Ошибка парсинга: ${e.message}")
                    }
                } ?: callback(null, "Пустой ответ сервера")
            }

            override fun onFailure(call: Call, e: IOException) {
                callback(null, "Ошибка сервера: ${e.message}")
            }
        })
    }


    fun updateLocation(childId: String, parentId: String, position: String, batteryLevel: Int, callback: (String) -> Unit) {
        val url = "${BASE_URL}location"
        val requestBody = Gson().toJson(SetPositionRequest(childId, parentId, position, batteryLevel))

        Log.d("UpdateLocation", "JSON Request: $requestBody") // Лог JSON для проверки

        val request = Request.Builder()
            .url(url)
            .put(RequestBody.create("application/json".toMediaTypeOrNull(), requestBody))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    callback("Location updated successfully")
                } else {
                    callback("Error: ${response.code}")
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                callback("Server error: ${e.message}")
            }
        })
    }


}
