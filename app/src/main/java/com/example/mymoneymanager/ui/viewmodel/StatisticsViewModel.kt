package com.example.mymoneymanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mymoneymanager.data.repository.MoneyRepository
import com.example.mymoneymanager.ui.model.UiStatistics
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

data class StatisticsUiState(
    val statistics: UiStatistics,
    val currencySymbol: String = "Rp"
)

class StatisticsViewModel(
    repository: MoneyRepository
) : ViewModel() {

    private val indonesianLocale: Locale = Locale.Builder()
        .setLanguage("id")
        .setRegion("ID")
        .build()

    private val month = MutableStateFlow(YearMonth.now())

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<StatisticsUiState> = month
        .flatMapLatest { selectedMonth ->
            combine(
                repository.observeStatistics(selectedMonth),
                repository.currencySymbol
            ) { statistics, symbol ->
                StatisticsUiState(statistics = statistics, currencySymbol = symbol)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = StatisticsUiState(
                statistics = UiStatistics(
                    monthLabel = YearMonth.now().month.getDisplayName(TextStyle.FULL, indonesianLocale),
                    totalExpenseMinor = 0L,
                    netMinor = 0L,
                    comparisonText = "Belum ada data bulan lalu",
                    isPositiveComparison = true,
                    categoryStatistics = emptyList(),
                    monthlyTrend = emptyList()
                )
            )
        )
}
