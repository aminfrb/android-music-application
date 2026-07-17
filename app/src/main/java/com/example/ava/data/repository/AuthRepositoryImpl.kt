package com.example.ava.data.repository

import com.example.ava.core.util.Outcome
import com.example.ava.core.util.runCatchingOutcome
import com.example.ava.data.local.prefs.SettingsStore
import com.example.ava.data.local.prefs.TokenStore
import com.example.ava.data.remote.api.AvaApi
import com.example.ava.data.remote.dto.AuthRequest
import com.example.ava.data.remote.dto.ProfileUpdateDto
import com.example.ava.data.remote.dto.toDomain
import com.example.ava.di.IoDispatcher
import com.example.ava.domain.model.User
import com.example.ava.domain.repository.AuthRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: AvaApi,
    private val tokenStore: TokenStore,
    private val settingsStore: SettingsStore,
    @IoDispatcher private val io: CoroutineDispatcher,
) : AuthRepository {

    private val userState = MutableStateFlow<User?>(null)
    override val currentUser: Flow<User?> = userState.asStateFlow()
    override val isLoggedIn: Flow<Boolean> = tokenStore.token.map { !it.isNullOrBlank() }

    override suspend fun login(username: String, password: String) = withContext(io) {
        runCatchingOutcome {
            val res = api.login(AuthRequest(username, password))
            tokenStore.save(res.token, res.user.id)
            settingsStore.setPremium(res.user.isPremium)
            res.user.toDomain().also { userState.value = it }
        }
    }

    override suspend fun register(username: String, password: String, displayName: String) = withContext(io) {
        runCatchingOutcome {
            val res = api.register(AuthRequest(username, password, displayName))
            tokenStore.save(res.token, res.user.id)
            settingsStore.setPremium(res.user.isPremium)
            res.user.toDomain().also { userState.value = it }
        }
    }

    override suspend fun refreshMe(): Outcome<User> = withContext(io) {
        runCatchingOutcome {
            api.me().toDomain().also {
                userState.value = it
                settingsStore.setPremium(it.isPremium)
            }
        }
    }

    /** Mock purchase: the server flips the flag, DataStore mirrors it for offline reads. */
    override suspend fun buyPremium(): Outcome<User> = withContext(io) {
        runCatchingOutcome {
            api.buyPremium().toDomain().also {
                userState.value = it
                settingsStore.setPremium(true)
            }
        }
    }

    override suspend fun updateAvatar(url: String): Outcome<User> = withContext(io) {
        runCatchingOutcome {
            api.updateProfile(ProfileUpdateDto(avatarUrl = url)).toDomain().also { userState.value = it }
        }
    }

    override suspend fun logout() = withContext(io) {
        tokenStore.clear()
        userState.value = null
    }
}
