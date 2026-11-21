package com.example.mymoneymanager.ui.model

import java.time.LocalDate

enum class UiAccountType {
    CASH,
    BANK,
    CREDIT_CARD,
    SAVINGS
}

data class UiAccount(
    val id: Long,
    val name: String,
    val type: UiAccountType,
    val balanceMinor: Long,
    val colorHex: String
)

data class UiCategory(
    val id: Long,
    val name: String,
    val colorHex: String,
    val iconEmoji: String
)

enum class UiTransactionType {
    INCOME,
    EXPENSE,
    TRANSFER
}

data class UiTransaction(
    val id: Long,
    val title: String,
    val accountName: String,
    val note: String,
    val amountMinor: Long,
    val type: UiTransactionType,
    val date: LocalDate,
    val category: UiCategory?
)

data class UiBudget(
    val category: UiCategory,
    val budgetMinor: Long,
    val spentMinor: Long
)

data class UiMonthlySummary(
    val incomeMinor: Long = 0L,
    val expenseMinor: Long = 0L,
    val netMinor: Long = 0L,
    val currencySymbol: String = "Rp"
)

data class UiCategoryStatistic(
    val category: UiCategory,
    val amountMinor: Long,
    val percentage: Float
)

data class UiMonthlyTrend(
    val monthLabel: String,
    val amountMinor: Long,
    val isCurrentMonth: Boolean
)

data class UiStatistics(
    val monthLabel: String,
    val totalExpenseMinor: Long = 0L,
    val netMinor: Long = 0L,
    val comparisonText: String,
    val isPositiveComparison: Boolean,
    val categoryStatistics: List<UiCategoryStatistic>,
    val monthlyTrend: List<UiMonthlyTrend>
)

data class UiCurrencyOption(
    val code: String,
    val symbol: String,
    val label: String
)

data class UiWeeklyDay(
    val date: LocalDate,
    val shortLabel: String,
    val isSelected: Boolean,
    val isToday: Boolean
)
