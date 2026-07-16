package com.example.ava.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.example.ava.data.local.AppDatabase
import com.example.ava.data.local.dao.*
import com.example.ava.data.repository.*
import com.example.ava.domain.repository.*
import com.example.ava.presentation.screens.home.HomeViewModel
import com.example.ava.presentation.screens.player.PlayerViewModel
import com.example.ava.presentation.screens.search.SearchViewModel
import com.example.ava.presentation.screens.profile.ProfileViewModel
import com.example.ava.presentation.screens.chat.ChatViewModel
import com.example.ava.service.MusicPlayerManager
import com.example.ava.utils.PreferenceManager
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { PreferenceManager(get(), get()) }

    // DataStore
    single<DataStore<Preferences>> {
        PreferenceDataStoreFactory.create(
            produceFile = { get<Context>().preferencesDataStoreFile("user_prefs") }
        )
    }

    // Repositories
    single<SongRepository> { SongRepositoryImpl(get(), get(), get()) }
    single<PlaylistRepository> { PlaylistRepositoryImpl(get(), get()) }
    single<UserRepository> { UserRepositoryImpl(get(), get()) }
    single<ChatRepository> { ChatRepositoryImpl(get(), get(), get()) }

    // Music Player Manager
    single { MusicPlayerManager(get(), get(), get()) }

    // ViewModels
    viewModel { HomeViewModel(get(), get(), get()) }
    viewModel { SearchViewModel(get(), get()) }
    viewModel { PlayerViewModel(get(), get()) }
    viewModel { ProfileViewModel(get(), get()) }
    viewModel { ChatViewModel(get(), get()) }
}