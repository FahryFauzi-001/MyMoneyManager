package com.example.mymoneymanager.data.mapper

import com.example.mymoneymanager.data.local.entity.AccountEntity
import com.example.mymoneymanager.data.local.entity.CategoryEntity
import com.example.mymoneymanager.data.local.entity.TxnWithDetails
import com.example.mymoneymanager.data.util.toLocalDateUtc
import com.example.mymoneymanager.ui.model.UiAccount
import com.example.mymoneymanager.ui.model.UiCategory
import com.example.mymoneymanager.ui.model.UiTransaction

fun AccountEntity.toUi() = UiAccount(
    id = id,
    name = name,
    type = type,
    balanceMinor = balanceMinor,
    colorHex = colorHex
)

fun CategoryEntity.toUi() = UiCategory(
    id = id,
    name = name,
    colorHex = colorHex,
    iconEmoji = iconEmoji
)

fun TxnWithDetails.toUi() = UiTransaction(
    id = txn.id,
    title = txn.title,
    accountName = account?.name ?: txn.accountName.ifBlank { "Akun Diarsipkan" },
    note = txn.note,
    amountMinor = txn.amountMinor,
    type = txn.type,
    date = txn.dateMillis.toLocalDateUtc(),
    category = category?.toUi()
)
