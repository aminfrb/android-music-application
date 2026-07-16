package com.example.ava.di

import com.example.ava.BuildConfig
import com.example.ava.data.remote.api.MusicApiService
import com.example.ava.data.remote.api.ChatWebSocketService
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import org.koin.dsl.module
import java.util.concurrent.TimeUnit

val networkModule = module {
    single {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    single {
        Retrofit.Builder()
            .baseUrl("https://your-api-base-url.com/")
            .client(get())
            .addConverterFactory(GsonConverterFactory.create(
                GsonBuilder().setLenient().create()
            ))
            .build()
    }

    single { get<Retrofit>().create(MusicApiService::class.java) }

    // WebSocket client (OkHttp)
    single { get<OkHttpClient>().newBuilder().build() }
    single { ChatWebSocketService(get()) }
}
