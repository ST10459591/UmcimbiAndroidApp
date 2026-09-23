package com.example.umcimbisa.model

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "customer",
    val language: String = "English",
    val phone: String = "",
    val notifications: Boolean = true,
    val darkMode: Boolean = false
)
