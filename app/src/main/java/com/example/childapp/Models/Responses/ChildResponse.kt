package com.example.childapp.Models.Responses

data class ChildResponse(
    val id: String,
    val deviceId: String,
    val location: String,
    val parentId: String
)
