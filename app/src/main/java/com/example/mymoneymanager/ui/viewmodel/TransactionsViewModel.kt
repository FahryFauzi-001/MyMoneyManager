package com.example.mymoneymanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mymoneymanager.data.repository.MoneyRepository
import com.example.mymoneymanager.ui.model.UiAccount
import com.example.mymoneymanager.ui.model.UiAccountType
import com.example.mymoneymanager.ui.model.UiMonthlySummary
import com.example.mymoneymanager.ui.model.UiTransaction
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
import java.time.LocalDate

data class TransactionsUiState(
    val accounts: List<UiAccount> = emptyList(),
    val transactions: List<UiTransaction> = emptyList(),
    val summary: UiMonthlySummary = UiMonthlySummary(),
    val currencySymbol: String = "Rp",
    val isBalanceVisible: Boolean = true,
    val showLinkBankDialog: Boolean = false,
    val isConnectingBank: Boolean = false
) {
    val totalBalanceMinor: Long
        get() = accounts.sumOf { it.balanceMinor }
}

class TransactionsViewModel(
    private val repository: MoneyRepository
) : ViewModel() {

    private val _isBalanceVisible = MutableStateFlow(true)
    private val _showLinkBankDialog = MutableStateFlow(false)
    private val _isConnectingBank = MutableStateFlow(false)

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages: Flow<String> = _messages.receiveAsFlow()

    private val today = LocalDate.now()

    val uiState: StateFlow<TransactionsUiState> = combine(
        repository.observeAccounts(),
        repository.observeAllTransactions(),
        repository.observeSummary(today.withDayOfMonth(1), today.withDayOfMonth(today.lengthOfMonth())),
        repository.currencySymbol
    ) { accounts, transactions, summary, symbol ->
        TransactionsUiState(
            accounts = accounts,
            transactions = transactions,
            summary = summary,
            currencySymbol = symbol
        )
    }.combine(
        combine(_isBalanceVisible, _showLinkBankDialog, _isConnectingBank) { visible, showDialog, connecting ->
            Triple(visible, showDialog, connecting)
        }
    ) { state, uiFlags ->
        state.copy(
            isBalanceVisible = uiFlags.first,
            showLinkBankDialog = uiFlags.second,
            isConnectingBank = uiFlags.third
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TransactionsUiState()
    )

    fun toggleBalanceVisibility() {
        _isBalanceVisible.value = !_isBalanceVisible.value
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
                _messages.send("Berhasil terhubung ke $bankName! Saldo otomatis terbarui.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _messages.send(e.message ?: "Gagal menghubungkan rekening bank.")
            } finally {
                _isConnectingBank.value = false
                _showLinkBankDialog.value = false
            }
        }
    }

    fun loadSampleData() {
        viewModelScope.launch {
            try {
                repository.loadSampleData()
                _messages.send("Data demo berhasil dimuat!")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _messages.send(e.message ?: "Gagal memuat data demo.")
            }
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            try {
                repository.deleteTransaction(id)
                _messages.send("Transaksi berhasil dihapus.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _messages.send(e.message ?: "Gagal menghapus transaksi.")
            }
        }
    }
}
