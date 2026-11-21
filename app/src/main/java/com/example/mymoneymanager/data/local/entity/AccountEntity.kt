package com.example.mymoneymanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.mymoneymanager.ui.model.UiAccountType

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val type: UiAccountType,
    val balanceMinor: Long,
    val colorHex: String
)
