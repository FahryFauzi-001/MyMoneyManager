package com.example.mymoneymanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mymoneymanager.data.repository.MoneyRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface StartState {
    object Loading : StartState
    object Onboarding : StartState
    object Locked : StartState
    object Main : StartState
}

class MainViewModel(
    repository: MoneyRepository,
    private val session: SessionState
) : ViewModel() {

    val startState: StateFlow<StartState> = combine(
        repository.isOnboardingCompleted,
        repository.isAppLockEnabled,
        repository.hasPin,
        session.isUnlocked
    ) { onboarded, lockEnabled, hasPin, unlocked ->
        when {
            !onboarded -> StartState.Onboarding
            lockEnabled && hasPin && !unlocked -> StartState.Locked
            else -> StartState.Main
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = StartState.Loading
    )

    init {
        viewModelScope.launch {
            val lockRequired = repository.isAppLockEnabled.first() && repository.hasPin.first()
            if (!lockRequired) session.markUnlocked()
        }
    }
}
