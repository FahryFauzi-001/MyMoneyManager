package com.example.mymoneymanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mymoneymanager.data.repository.MoneyRepository
import com.example.mymoneymanager.ui.util.CurrencyCatalog
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class QuickOnboardingViewModel(
    private val repository: MoneyRepository
) : ViewModel() {

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages: Flow<String> = _messages.receiveAsFlow()

    fun completeOnboarding(
        currencyCode: String,
        cashOpeningBalanceMinor: Long,
        bankOpeningBalanceMinor: Long
    ) {
        if (_isSaving.value) return
        viewModelScope.launch {
            _isSaving.value = true
            try {
                repository.completeOnboarding(
                    currencyCode = currencyCode,
                    currencySymbol = CurrencyCatalog.symbolFor(currencyCode),
                    cashOpeningBalanceMinor = cashOpeningBalanceMinor,
                    bankOpeningBalanceMinor = bankOpeningBalanceMinor
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _messages.send(e.message ?: "Gagal menyelesaikan pengaturan awal.")
            } finally {
                _isSaving.value = false
            }
        }
    }
}
