package com.example.mymoneymanager.ui.util

import com.example.mymoneymanager.ui.model.UiCurrencyOption

object CurrencyCatalog {

    val options: List<UiCurrencyOption> = listOf(
        UiCurrencyOption(code = "IDR", symbol = "Rp", label = "Rupiah"),
        UiCurrencyOption(code = "USD", symbol = "$", label = "US Dollar")
    )

    fun symbolFor(code: String): String =
        options.firstOrNull { it.code == code }?.symbol ?: "Rp"

    fun displayNameForSymbol(symbol: String): String {
        val option = options.firstOrNull { it.symbol == symbol }
        return if (option != null) {
            "${option.label} (${option.symbol})"
        } else {
            "Rupiah ($symbol)"
        }
    }
}
