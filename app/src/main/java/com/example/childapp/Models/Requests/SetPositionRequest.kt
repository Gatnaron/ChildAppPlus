package com.example.childapp.Models.Requests

data class SetPositionRequest(
    val childId: String,
    val parentId: String,
    val position: String,
    val batteryLevel: Int
)