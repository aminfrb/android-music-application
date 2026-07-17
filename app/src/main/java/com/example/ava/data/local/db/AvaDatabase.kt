package com.example.ava.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.ava.data.local.dao.ChatDao
import com.example.ava.data.local.dao.SearchHistoryDao
import com.example.ava.data.local.dao.SongDao
import com.example.ava.data.local.entity.*

@Database(
    entities = [SongEntity::class, SearchHistoryEntity::class, MessageEntity::class, ConversationEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class AvaDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun chatDao(): ChatDao

    companion object { const val NAME = "ava.db" }
}
