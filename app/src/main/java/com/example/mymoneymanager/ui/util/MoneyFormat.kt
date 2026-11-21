package com.example.mymoneymanager.ui.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs

object MoneyFormat {
    private val indonesianLocale: Locale = Locale.Builder()
        .setLanguage("id")
        .setRegion("ID")
        .build()

    private val symbols: DecimalFormatSymbols = DecimalFormatSymbols(indonesianLocale).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }

    private fun createIntegerFormatter(): DecimalFormat = DecimalFormat("#,##0", symbols)

    fun format(
        amountMinor: Long,
        symbol: String = "Rp",
        showSign: Boolean = false,
        isIncome: Boolean = true,
        isMasked: Boolean = false
    ): String {
        if (isMasked) {
            return "$symbol ••••••"
        }
        val cleanAmount = abs(amountMinor)
        val formattedNumber = createIntegerFormatter().format(cleanAmount)
        val isNegativeValue = amountMinor < 0L || (showSign && !isIncome && cleanAmount > 0L)
        val sign = when {
            isNegativeValue -> "-"
            showSign && isIncome && cleanAmount > 0L -> "+"
            else -> ""
        }
        return "$sign$symbol $formattedNumber"
    }

    fun formatCompact(
        amountMinor: Long,
        symbol: String = "Rp",
        isMasked: Boolean = false
    ): String {
        if (isMasked) {
            return "$symbol •••"
        }
        val value = abs(amountMinor)
        val sign = if (amountMinor < 0L) "-" else ""
        val body = when {
            value >= 1_000_000_000L -> "${trimDecimal(value / 1_000_000_000.0)}M"
            value >= 1_000_000L -> "${trimDecimal(value / 1_000_000.0)}jt"
            value >= 1_000L -> "${value / 1_000L}rb"
            else -> value.toString()
        }
        return "$sign$symbol $body"
    }

    fun formatDigitsOnly(digits: String): String {
        val clean = digits.filter { it.isDigit() }
        if (clean.isEmpty()) return ""
        val value = clean.toLongOrNull() ?: return clean
        return createIntegerFormatter().format(value)
    }

    private fun trimDecimal(value: Double): String {
        val formatted = String.format(indonesianLocale, "%.1f", value)
        return formatted.removeSuffix(",0")
    }
}

class ThousandSeparatorVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val original = text.text.filter { it.isDigit() }
        if (original.isEmpty()) {
            return TransformedText(AnnotatedString(""), OffsetMapping.Identity)
        }
        val formatted = MoneyFormat.formatDigitsOnly(original)
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val clamped = offset.coerceIn(0, original.length)
                if (clamped == 0) return 0
                var digitsSeen = 0
                for (i in formatted.indices) {
                    if (formatted[i].isDigit()) {
                        digitsSeen++
                        if (digitsSeen == clamped) {
                            return i + 1
                        }
                    }
                }
                return formatted.length
            }

            override fun transformedToOriginal(offset: Int): Int {
                val clamped = offset.coerceIn(0, formatted.length)
                var digitsBefore = 0
                for (i in 0 until clamped) {
                    if (formatted[i].isDigit()) {
                        digitsBefore++
                    }
                }
                return digitsBefore.coerceIn(0, original.length)
            }
        }
        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}

fun formatAmount(
    amountMinor: Long,
    currencySymbol: String,
    showSign: Boolean = false,
    isNegative: Boolean = false
): String = MoneyFormat.format(
    amountMinor = amountMinor,
    symbol = currencySymbol,
    showSign = showSign,
    isIncome = !isNegative
)

fun formatCompactAmount(
    amountMinor: Long,
    currencySymbol: String
): String = MoneyFormat.formatCompact(
    amountMinor = amountMinor,
    symbol = currencySymbol
)
