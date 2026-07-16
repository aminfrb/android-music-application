package com.example.ava.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Song(
    val id: String,
    val title: String,
    val artistName: String,
    val coverImageUrl: String,
    val audioUrl: String,
    val durationMs: Long = 0,
    val isLocal: Boolean = false,
    val localFilePath: String? = null
) : Parcelable