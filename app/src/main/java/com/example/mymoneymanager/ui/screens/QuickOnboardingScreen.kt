package com.example.mymoneymanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mymoneymanager.ui.model.UiCurrencyOption
import com.example.mymoneymanager.ui.util.ThousandSeparatorVisualTransformation

@Composable
fun QuickOnboardingScreen(
    currencyOptions: List<UiCurrencyOption>,
    onStartTrackingClick: (
        currencyCode: String,
        cashOpeningBalanceMinor: Long,
        bankOpeningBalanceMinor: Long
    ) -> Unit
) {
    var selectedCurrency by rememberSaveable {
        mutableStateOf(currencyOptions.firstOrNull()?.code ?: "IDR")
    }
    var cashBalanceText by rememberSaveable { mutableStateOf("") }
    var bankBalanceText by rememberSaveable { mutableStateOf("") }

    val selectedCurrencyOption = currencyOptions.firstOrNull { it.code == selectedCurrency }
        ?: currencyOptions.first()
    val cashBalanceMinor = cashBalanceText.filter { it.isDigit() }.toLongOrNull() ?: 0L
    val bankBalanceMinor = bankBalanceText.filter { it.isDigit() }.toLongOrNull() ?: 0L

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(36.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier.size(96.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF124328), Color(0xFF1E5E3A))
                            ),
                            shape = RoundedCornerShape(28.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccountBalanceWallet,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(42.dp)
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFE8F5E9),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.VerifiedUser,
                            contentDescription = null,
                            tint = Color(0xFF1E5E3A),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Text(
                text = "Kelola Keuangan Pribadi Tanpa Was-was",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Data transaksi tersimpan aman 100% di perangkat Anda tanpa server pihak ketiga.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Mata uang utama",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    currencyOptions.forEach { option ->
                        FilterChip(
                            selected = selectedCurrency == option.code,
                            onClick = { selectedCurrency = option.code },
                            label = { Text(text = "${option.symbol} ${option.code}") },
                            shape = RoundedCornerShape(50)
                        )
                    }
                }
            }

            OpeningBalanceField(
                label = "Saldo Awal Kas Tunai",
                currencySymbol = selectedCurrencyOption.symbol,
                value = cashBalanceText,
                onValueChanged = { value ->
                    cashBalanceText = value.filter { it.isDigit() }.take(13)
                }
            )

            OpeningBalanceField(
                label = "Saldo Awal Bank Utama",
                currencySymbol = selectedCurrencyOption.symbol,
                value = bankBalanceText,
                onValueChanged = { value ->
                    bankBalanceText = value.filter { it.isDigit() }.take(13)
                }
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = "Saldo awal bersifat opsional. Anda tetap bisa mengubah atau menambahkan rekening kapan saja.",
                    modifier = Modifier.padding(14.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = {
                    onStartTrackingClick(selectedCurrency, cashBalanceMinor, bankBalanceMinor)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1E5E3A),
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Mulai Catat Keuangan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun OpeningBalanceField(
    label: String,
    currencySymbol: String,
    value: String,
    onValueChanged: (String) -> Unit
) {
    val separatorTransformation = remember { ThousandSeparatorVisualTransformation() }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChanged,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(text = label) },
        placeholder = { Text(text = "0") },
        prefix = {
            Text(
                text = "$currencySymbol ",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        visualTransformation = separatorTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        shape = RoundedCornerShape(16.dp)
    )
}
