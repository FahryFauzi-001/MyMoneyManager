package com.example.mymoneymanager.data.local.dao

import com.example.mymoneymanager.ui.model.UiTransactionType

data class TypeTotal(
    val type: UiTransactionType,
    val total: Long
)

data class CategoryTotal(
    val categoryId: Long?,
    val total: Long
)

data class MonthTypeTotal(
    val ym: String,
    val type: UiTransactionType,
    val total: Long
)
