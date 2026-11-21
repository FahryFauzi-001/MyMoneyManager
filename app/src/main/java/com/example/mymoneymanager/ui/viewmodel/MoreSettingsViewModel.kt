package com.example.mymoneymanager.ui.viewmodel

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mymoneymanager.data.repository.MoneyRepository
import com.example.mymoneymanager.ui.model.UiCategory
import com.example.mymoneymanager.ui.model.UiCurrencyOption
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.YearMonth

data class MoreSettingsUiState(
    val isAppLockEnabled: Boolean = false,
    val currencySymbol: String = "Rp",
    val categories: List<UiCategory> = emptyList(),
    val budgets: Map<Long, Long> = emptyMap()
)

class MoreSettingsViewModel(
    private val repository: MoneyRepository
) : ViewModel() {

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages: Flow<String> = _messages.receiveAsFlow()

    val uiState: StateFlow<MoreSettingsUiState> = combine(
        repository.isAppLockEnabled,
        repository.hasPin,
        repository.currencySymbol,
        repository.observeCategories(),
        repository.observeBudgets(YearMonth.now())
    ) { lockEnabled, hasPin, symbol, categories, budgets ->
        MoreSettingsUiState(
            isAppLockEnabled = lockEnabled && hasPin,
            currencySymbol = symbol,
            categories = categories,
            budgets = budgets.associate { it.category.id to it.budgetMinor }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MoreSettingsUiState()
    )

    fun setCurrency(option: UiCurrencyOption) {
        launchWithMessage("Mata uang diubah ke ${option.label} (${option.symbol})") {
            repository.setCurrency(option.code, option.symbol)
        }
    }

    fun saveBudgets(inputs: Map<Long, String>) {
        launchWithMessage("Anggaran bulanan disimpan") {
            inputs.forEach { (categoryId, text) ->
                val amount = text.filter { it.isDigit() }.toLongOrNull() ?: 0L
                repository.setBudget(categoryId, amount)
            }
        }
    }

    fun enableAppLock(pin: String) {
        launchWithMessage("Kunci aplikasi dengan PIN berhasil diaktifkan") {
            repository.setPin(pin)
        }
    }

    fun disableAppLock() {
        launchWithMessage("Kunci aplikasi dinonaktifkan") {
            repository.clearPin()
        }
    }

    fun resetDemoData() {
        launchWithMessage("Data demo berhasil direset!") {
            repository.resetDemoData()
        }
    }

    fun exportCsv(uri: Uri, contentResolver: ContentResolver) {
        viewModelScope.launch {
            try {
                val csv = repository.exportTransactionsCsv()
                withContext(Dispatchers.IO) {
                    val stream = contentResolver.openOutputStream(uri, "wt")
                        ?: error("Tidak dapat membuka file yang dipilih.")
                    stream.use { it.write(csv.toByteArray(Charsets.UTF_8)) }
                }
                _messages.send("Data CSV berhasil diekspor")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _messages.send("Ekspor gagal: ${e.message ?: "terjadi kesalahan"}")
            }
        }
    }

    private fun launchWithMessage(successMessage: String, block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
                _messages.send(successMessage)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _messages.send(e.message ?: "Terjadi kesalahan")
            }
        }
    }
}
