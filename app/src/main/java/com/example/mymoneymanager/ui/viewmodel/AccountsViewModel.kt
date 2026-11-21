package com.example.mymoneymanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mymoneymanager.data.repository.MoneyRepository
import com.example.mymoneymanager.ui.model.UiAccount
import com.example.mymoneymanager.ui.model.UiAccountType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AccountsUiState(
    val accounts: List<UiAccount> = emptyList(),
    val totalBalanceMinor: Long = 0L,
    val cashTotalMinor: Long = 0L,
    val bankTotalMinor: Long = 0L,
    val currencySymbol: String = "Rp",
    val showLinkBankDialog: Boolean = false,
    val isConnectingBank: Boolean = false
)

class AccountsViewModel(
    private val repository: MoneyRepository
) : ViewModel() {

    private val _showLinkBankDialog = MutableStateFlow(false)
    private val _isConnectingBank = MutableStateFlow(false)

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages: Flow<String> = _messages.receiveAsFlow()

    val uiState: StateFlow<AccountsUiState> = combine(
        repository.observeAccounts(),
        repository.currencySymbol,
        _showLinkBankDialog,
        _isConnectingBank
    ) { accounts, symbol, showLinkBank, isConnecting ->
        val cashTotal = accounts
            .filter { it.type == UiAccountType.CASH }
            .sumOf { it.balanceMinor }
        val bankTotal = accounts
            .filter { it.type != UiAccountType.CASH }
            .sumOf { it.balanceMinor }

        AccountsUiState(
            accounts = accounts,
            totalBalanceMinor = accounts.sumOf { it.balanceMinor },
            cashTotalMinor = cashTotal,
            bankTotalMinor = bankTotal,
            currencySymbol = symbol,
            showLinkBankDialog = showLinkBank,
            isConnectingBank = isConnecting
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AccountsUiState()
    )

    fun addAccount(name: String, type: UiAccountType, openingBalanceMinor: Long) {
        viewModelScope.launch {
            try {
                val colorHex = if (type == UiAccountType.CREDIT_CARD) {
                    CREDIT_CARD_COLOR
                } else {
                    ACCOUNT_PALETTE[uiState.value.accounts.size % ACCOUNT_PALETTE.size]
                }
                repository.addAccount(
                    name = name,
                    type = type,
                    openingBalanceMinor = openingBalanceMinor,
                    colorHex = colorHex
                )
                _messages.send("Akun $name berhasil ditambahkan.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _messages.send(e.message ?: "Gagal menambahkan akun.")
            }
        }
    }

    fun deleteAccount(account: UiAccount) {
        viewModelScope.launch {
            try {
                repository.deleteAccount(account.id)
                _messages.send("Akun ${account.name} dihapus. Transaksi lama tetap tersimpan sebagai arsip.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _messages.send(e.message ?: "Gagal menghapus akun.")
            }
        }
    }

    fun openLinkBankDialog() {
        _showLinkBankDialog.value = true
    }

    fun closeLinkBankDialog() {
        if (!_isConnectingBank.value) {
            _showLinkBankDialog.value = false
        }
    }

    fun simulateLinkBank(bankName: String, initialBalance: Long = 14_350_000L) {
        if (_isConnectingBank.value) return
        viewModelScope.launch {
            _isConnectingBank.value = true
            try {
                delay(1_500L)
                val colorHex = when (bankName) {
                    "BCA" -> "#005E9C"
                    "Bank Mandiri" -> "#003D79"
                    "BRI" -> "#00529C"
                    "BNI" -> "#F15A22"
                    else -> "#1E5E3A"
                }
                repository.addAccount(
                    name = bankName,
                    type = UiAccountType.BANK,
                    openingBalanceMinor = initialBalance,
                    colorHex = colorHex
                )
                _messages.send("Berhasil terhubung ke $bankName! Saldo otomatis diperbarui.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _messages.send(e.message ?: "Gagal menghubungkan ke API Bank.")
            } finally {
                _isConnectingBank.value = false
                _showLinkBankDialog.value = false
            }
        }
    }

    private companion object {
        const val CREDIT_CARD_COLOR = "#C62828"
        val ACCOUNT_PALETTE = listOf(
            "#1E5E3A", "#005E9C", "#1976D2", "#8E24AA", "#E67E22", "#5D4037"
        )
    }
}
