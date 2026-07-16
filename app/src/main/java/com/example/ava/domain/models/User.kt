package com.example.ava.domain.models

data class User(
    val id: String,
    val name: String,
    val avatarUrl: String?,
    val isPremium: Boolean = false,
    val followers: List<String> = emptyList(),
    val following: List<String> = emptyList()
)
