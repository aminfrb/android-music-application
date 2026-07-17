package com.example.ava.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import com.example.ava.core.util.Outcome
import com.example.ava.domain.model.User
import com.example.ava.domain.repository.AuthRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user: User? = null,
    val isPurchasing: Boolean = false,
)

sealed interface ProfileEffect {
    data object Upgraded : ProfileEffect
    data object Failed : ProfileEffect
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val purchasing = MutableStateFlow(false)

    val uiState: StateFlow<ProfileUiState> =
        combine(authRepository.currentUser, purchasing) { user, busy -> ProfileUiState(user, busy) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

    private val _effects = Channel<ProfileEffect>(Channel.BUFFERED)
    val effects: Flow<ProfileEffect> = _effects.receiveAsFlow()

    init { viewModelScope.launch { authRepository.refreshMe() } }

    /** Mock checkout: a short "processing" beat, then the flag flips server-side. */
    fun buyPremium() = viewModelScope.launch {
        purchasing.value = true
        delay(1_200)
        val result = authRepository.buyPremium()
        purchasing.value = false
        _effects.send(if (result is Outcome.Success) ProfileEffect.Upgraded else ProfileEffect.Failed)
    }

    fun changeAvatar(url: String) = viewModelScope.launch { authRepository.updateAvatar(url) }
}
