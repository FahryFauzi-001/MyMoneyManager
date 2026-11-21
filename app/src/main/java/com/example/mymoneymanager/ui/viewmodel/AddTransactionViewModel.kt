package com.example.mymoneymanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mymoneymanager.data.repository.MoneyRepository
import com.example.mymoneymanager.ui.model.UiAccount
import com.example.mymoneymanager.ui.model.UiCategory
import com.example.mymoneymanager.ui.model.UiTransactionType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class AddTransactionUiState(
    val accounts: List<UiAccount> = emptyList(),
    val categories: List<UiCategory> = emptyList(),
    val currencySymbol: String = "Rp",
    val isSaving: Boolean = false
)

sealed interface AddTransactionEvent {
    object Saved : AddTransactionEvent
    data class Error(val message: String) : AddTransactionEvent
}

class AddTransactionViewModel(
    private val repository: MoneyRepository
) : ViewModel() {

    private val isSaving = MutableStateFlow(false)

    private val _events = Channel<AddTransactionEvent>(Channel.BUFFERED)
    val events: Flow<AddTransactionEvent> = _events.receiveAsFlow()

    val uiState: StateFlow<AddTransactionUiState> = combine(
        repository.observeAccounts(),
        repository.observeCategories(),
        repository.currencySymbol,
        isSaving
    ) { accounts, categories, symbol, saving ->
        AddTransactionUiState(
            accounts = accounts,
            categories = categories,
            currencySymbol = symbol,
            isSaving = saving
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AddTransactionUiState()
    )

    fun saveTransaction(
        type: UiTransactionType,
        amountMinor: Long,
        date: LocalDate,
        sourceAccountId: Long?,
        destinationAccountId: Long?,
        categoryId: Long?,
        note: String
    ) {
        if (isSaving.value) return

        val validationError = validate(type, amountMinor, sourceAccountId, destinationAccountId, categoryId)
        if (validationError != null) {
            _events.trySend(AddTransactionEvent.Error(validationError))
            return
        }

        viewModelScope.launch {
            isSaving.value = true
            try {
                repository.saveTransaction(
                    type = type,
                    amountMinor = amountMinor,
                    date = date,
                    sourceAccountId = sourceAccountId,
                    destinationAccountId = destinationAccountId,
                    categoryId = categoryId,
                    note = note
                )
                _events.send(AddTransactionEvent.Saved)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _events.send(AddTransactionEvent.Error(e.message ?: "Gagal menyimpan transaksi."))
            } finally {
                isSaving.value = false
            }
        }
    }

    private fun validate(
        type: UiTransactionType,
        amountMinor: Long,
        sourceAccountId: Long?,
        destinationAccountId: Long?,
        categoryId: Long?
    ): String? = when {
        amountMinor <= 0L -> "Masukkan nominal lebih dari 0."
        sourceAccountId == null -> "Pilih rekening sumber."
        type == UiTransactionType.TRANSFER && destinationAccountId == null ->
            "Pilih rekening tujuan."
        type == UiTransactionType.TRANSFER && destinationAccountId == sourceAccountId ->
            "Rekening sumber dan tujuan harus berbeda."
        type != UiTransactionType.TRANSFER && categoryId == null -> "Pilih kategori transaksi."
        else -> null
    }
}
