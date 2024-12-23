package com.example.childapp.Models.Responses

data class ChildLinkResponse(
    val id: String,
    val code1: String,
    val code2: String,
    val childId: String?,
    val lastsIn: String,
    val cryptKey: String
)