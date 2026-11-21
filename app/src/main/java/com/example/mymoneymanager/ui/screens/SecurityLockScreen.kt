package com.example.mymoneymanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private const val DEFAULT_PIN_LENGTH = 6
private val ForestLockAccent = Color(0xFF1E5E3A)

@Composable
fun SecurityLockScreen(
    pinLength: Int = DEFAULT_PIN_LENGTH,
    isBiometricAvailable: Boolean = true,
    errorMessage: String? = null,
    onPinCompleted: (String) -> Unit,
    onBiometricClick: () -> Unit
) {
    var enteredPin by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(36.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = ForestLockAccent.copy(alpha = 0.14f),
                contentColor = ForestLockAccent
            ) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                    modifier = Modifier.padding(20.dp)
                )
            }

            Text(
                text = "Masukkan PIN Anda",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Masukkan $pinLength digit PIN keamanan Anda untuk membuka MyMoneyManager",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            PinDotsIndicator(
                enteredCount = enteredPin.length,
                totalCount = pinLength,
                isError = errorMessage != null
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }

            OutlinedButton(
                onClick = onBiometricClick,
                shape = RoundedCornerShape(50)
            ) {
                Icon(
                    imageVector = Icons.Filled.Fingerprint,
                    contentDescription = null,
                    tint = ForestLockAccent
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = if (isBiometricAvailable) "Gunakan Sidik Jari" else "Gunakan Sidik Jari (Simulasi)",
                    color = ForestLockAccent,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        NumericKeypad(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            onDigitClick = { digit ->
                if (enteredPin.length < pinLength) {
                    enteredPin += digit
                    if (enteredPin.length == pinLength) {
                        onPinCompleted(enteredPin)
                        enteredPin = ""
                    }
                }
            },
            onBackspaceClick = {
                if (enteredPin.isNotEmpty()) {
                    enteredPin = enteredPin.dropLast(1)
                }
            },
            onBiometricClick = onBiometricClick
        )
    }
}

@Composable
private fun PinDotsIndicator(
    enteredCount: Int,
    totalCount: Int,
    isError: Boolean
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalCount) { index ->
            val isFilled = index < enteredCount
            val fillColor = when {
                isError -> MaterialTheme.colorScheme.error
                isFilled -> ForestLockAccent
                else -> Color.Transparent
            }
            val borderColor = when {
                isError -> MaterialTheme.colorScheme.error
                isFilled -> ForestLockAccent
                else -> MaterialTheme.colorScheme.outline
            }
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(color = fillColor, shape = CircleShape)
                    .border(width = 1.5.dp, color = borderColor, shape = CircleShape)
            )
        }
    }
}

@Composable
private fun NumericKeypad(
    modifier: Modifier = Modifier,
    onDigitClick: (Char) -> Unit,
    onBackspaceClick: () -> Unit,
    onBiometricClick: () -> Unit
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        KeypadRow(labels = listOf("1", "2", "3"), onDigitClick = onDigitClick)
        KeypadRow(labels = listOf("4", "5", "6"), onDigitClick = onDigitClick)
        KeypadRow(labels = listOf("7", "8", "9"), onDigitClick = onDigitClick)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KeypadKey(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.Fingerprint,
                contentDescription = "Gunakan Sidik Jari",
                onClick = onBiometricClick
            )
            KeypadKey(
                modifier = Modifier.weight(1f),
                label = "0",
                onClick = { onDigitClick('0') }
            )
            KeypadKey(
                modifier = Modifier.weight(1f),
                icon = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = "Hapus Digit",
                onClick = onBackspaceClick
            )
        }

        Text(
            text = "Data tersimpan 100% lokal di perangkat Anda. Pastikan Anda mengingat PIN keamanan.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
        )
    }
}

@Composable
private fun KeypadRow(
    labels: List<String>,
    onDigitClick: (Char) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        labels.forEach { label ->
            KeypadKey(
                modifier = Modifier.weight(1f),
                label = label,
                onClick = { onDigitClick(label.first()) }
            )
        }
    }
}

@Composable
private fun KeypadKey(
    modifier: Modifier = Modifier,
    label: String? = null,
    icon: ImageVector? = null,
    contentDescription: String? = null,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription
                )
            } else if (label != null) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
