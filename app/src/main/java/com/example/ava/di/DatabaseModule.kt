package com.example.ava.di

import android.content.Context
import androidx.room.Room
import com.example.mymusicapp.data.local.AppDatabase
import com.example.mymusicapp.data.local.dao.*
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(
            get<Context>(),
            AppDatabase::class.java,
            "music_app_db"
        ).fallbackToDestructiveMigration().build()
    }

    single { get<AppDatabase>().songDao() }
    single { get<AppDatabase>().playlistDao() }
    single { get<AppDatabase>().searchHistoryDao() }
    single { get<AppDatabase>().chatMessageDao() }
}
