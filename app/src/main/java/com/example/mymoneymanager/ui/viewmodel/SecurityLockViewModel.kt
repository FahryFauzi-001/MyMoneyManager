package com.example.mymoneymanager.ui.viewmodel

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mymoneymanager.data.repository.MoneyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SecurityLockUiState(
    val errorMessage: String? = null
)

class SecurityLockViewModel(
    private val repository: MoneyRepository,
    private val session: SessionState
) : ViewModel() {

    private val _uiState = MutableStateFlow(SecurityLockUiState())
    val uiState: StateFlow<SecurityLockUiState> = _uiState.asStateFlow()

    private var failedAttempts = 0
    private var lockedUntilElapsedMillis = 0L

    fun onPinCompleted(pin: String) {
        val now = SystemClock.elapsedRealtime()
        if (now < lockedUntilElapsedMillis) {
            val secondsLeft = (lockedUntilElapsedMillis - now + 999L) / 1000L
            _uiState.update { it.copy(errorMessage = "Terlalu banyak percobaan. Coba lagi dalam ${secondsLeft} detik.") }
            return
        }

        viewModelScope.launch {
            if (repository.verifyPin(pin)) {
                failedAttempts = 0
                _uiState.update { it.copy(errorMessage = null) }
                session.markUnlocked()
            } else {
                failedAttempts += 1
                if (failedAttempts >= MAX_ATTEMPTS) {
                    failedAttempts = 0
                    lockedUntilElapsedMillis = SystemClock.elapsedRealtime() + LOCKOUT_MILLIS
                    _uiState.update { it.copy(errorMessage = "Terlalu banyak percobaan. Coba lagi dalam 30 detik.") }
                } else {
                    _uiState.update { it.copy(errorMessage = "PIN salah. Silakan coba lagi.") }
                }
            }
        }
    }

    fun onBiometricSuccess() {
        failedAttempts = 0
        _uiState.update { it.copy(errorMessage = null) }
        session.markUnlocked()
    }

    fun onBiometricError(message: String) {
        _uiState.update { it.copy(errorMessage = message) }
    }

    private companion object {
        const val MAX_ATTEMPTS = 5
        const val LOCKOUT_MILLIS = 30_000L
    }
}
