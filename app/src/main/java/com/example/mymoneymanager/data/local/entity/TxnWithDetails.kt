package com.example.mymoneymanager.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class TxnWithDetails(
    @Embedded val txn: TxnEntity,
    @Relation(parentColumn = "accountId", entityColumn = "id")
    val account: AccountEntity?,
    @Relation(parentColumn = "categoryId", entityColumn = "id")
    val category: CategoryEntity?
)
