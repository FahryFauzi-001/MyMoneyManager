package com.example.mymoneymanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mymoneymanager.ui.model.UiAccount
import com.example.mymoneymanager.ui.model.UiAccountType
import com.example.mymoneymanager.ui.model.UiCategory
import com.example.mymoneymanager.ui.model.UiCurrencyOption
import com.example.mymoneymanager.ui.model.UiTransaction
import com.example.mymoneymanager.ui.util.MoneyFormat
import com.example.mymoneymanager.ui.util.ThousandSeparatorVisualTransformation

private data class BankOption(
    val name: String,
    val subtitle: String,
    val badgeColor: Color
)

private val NationalBanks = listOf(
    BankOption("BCA", "Bank Central Asia", Color(0xFF005E9C)),
    BankOption("Bank Mandiri", "PT Bank Mandiri (Persero)", Color(0xFF003D79)),
    BankOption("BRI", "Bank Rakyat Indonesia", Color(0xFF00529C)),
    BankOption("BNI", "Bank Negara Indonesia", Color(0xFFF15A22))
)

@Composable
fun LinkBankModal(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSelectBank: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = {
            Text(
                text = "Hubungkan Rekening Bank",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            if (isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = Color(0xFF1E5E3A))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Menghubungkan ke API Bank...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Pilih bank nasional untuk sinkronisasi saldo otomatis (Simulasi Open Banking):",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    NationalBanks.forEach { bank ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectBank(bank.name) },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(bank.badgeColor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalance,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = bank.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = bank.subtitle,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            if (!isLoading) {
                TextButton(onClick = onDismiss) {
                    Text(text = "Batal")
                }
            }
        }
    )
}

@Composable
fun ConfirmDeleteAccountDialog(
    account: UiAccount,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Hapus Rekening?", fontWeight = FontWeight.Bold) },
        text = {
            Text(
                text = "Hapus akun ${account.name}? Transaksi yang sudah tercatat dengan akun ini akan tetap tersimpan sebagai arsip."
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = "Hapus", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Batal")
            }
        }
    )
}

@Composable
fun ConfirmDeleteTransactionDialog(
    transaction: UiTransaction,
    currencySymbol: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Hapus Transaksi?", fontWeight = FontWeight.Bold) },
        text = {
            Text(
                text = "\"${transaction.title}\" (${MoneyFormat.format(transaction.amountMinor, currencySymbol)}) akan dihapus dan saldo rekening akan disesuaikan kembali."
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = "Hapus", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Batal")
            }
        }
    )
}

@Composable
fun ConfirmResetDemoDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Reset Data Demo?", fontWeight = FontWeight.Bold) },
        text = {
            Text(
                text = "Data transaksi dan rekening saat ini akan diganti dengan data demo baru (termasuk riwayat 6 bulan untuk statistik)."
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = "Reset Data", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Batal")
            }
        }
    )
}

@Composable
fun CurrencyPickerDialog(
    options: List<UiCurrencyOption>,
    selectedSymbol: String,
    onSelect: (UiCurrencyOption) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Pilih Mata Uang", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    text = "Hanya simbol tampilan mata uang yang berubah. Nominal tersimpan tidak dikonversi.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(option) }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = option.symbol == selectedSymbol,
                            onClick = { onSelect(option) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "${option.label} (${option.symbol})")
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Tutup")
            }
        }
    )
}

@Composable
fun BudgetEditorDialog(
    categories: List<UiCategory>,
    budgets: Map<Long, Long>,
    currencySymbol: String,
    onSave: (Map<Long, String>) -> Unit,
    onDismiss: () -> Unit
) {
    val inputs = remember {
        mutableStateMapOf<Long, String>().apply {
            categories.forEach { category ->
                put(category.id, budgets[category.id]?.toString().orEmpty())
            }
        }
    }
    val separatorTransformation = remember { ThousandSeparatorVisualTransformation() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Anggaran Bulanan", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 360.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Kosongkan atau isi 0 untuk menghapus batas anggaran kategori.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                categories.forEach { category ->
                    OutlinedTextField(
                        value = inputs[category.id].orEmpty(),
                        onValueChange = { value ->
                            inputs[category.id] = value.filter { it.isDigit() }.take(12)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(text = "${category.iconEmoji} ${category.name}") },
                        prefix = { Text(text = "$currencySymbol ") },
                        singleLine = true,
                        visualTransformation = separatorTransformation,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(inputs.toMap()) }) {
                Text(text = "Simpan", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Batal")
            }
        }
    )
}

@Composable
fun PinSetupDialog(
    pinLength: Int = 6,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var pin by rememberSaveable { mutableStateOf("") }
    var confirmPin by rememberSaveable { mutableStateOf("") }
    var isPinVisible by rememberSaveable { mutableStateOf(false) }
    var isConfirmPinVisible by rememberSaveable { mutableStateOf(false) }

    val isLengthValid = pin.length == pinLength && confirmPin.length == pinLength
    val isMatching = pin == confirmPin
    val showMismatch = confirmPin.isNotEmpty() && !isMatching

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Atur PIN Keamanan", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Buat PIN $pinLength digit untuk melindungi data keuangan Anda. Data disimpan 100% lokal di perangkat tanpa pemulihan online.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it.filter { c -> c.isDigit() }.take(pinLength) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = "PIN Baru ($pinLength Digit)") },
                    singleLine = true,
                    visualTransformation = if (isPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isPinVisible = !isPinVisible }) {
                            Icon(
                                imageVector = if (isPinVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (isPinVisible) "Sembunyikan PIN" else "Tampilkan PIN"
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    shape = RoundedCornerShape(14.dp)
                )
                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = { confirmPin = it.filter { c -> c.isDigit() }.take(pinLength) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = "Konfirmasi PIN") },
                    singleLine = true,
                    isError = showMismatch,
                    supportingText = {
                        if (showMismatch) {
                            Text(text = "PIN tidak cocok")
                        } else if (pin.isNotEmpty() && pin.length < pinLength) {
                            Text(text = "Masukkan tepat $pinLength digit angka")
                        }
                    },
                    visualTransformation = if (isConfirmPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isConfirmPinVisible = !isConfirmPinVisible }) {
                            Icon(
                                imageVector = if (isConfirmPinVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (isConfirmPinVisible) "Sembunyikan Konfirmasi PIN" else "Tampilkan Konfirmasi PIN"
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    shape = RoundedCornerShape(14.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(pin) },
                enabled = isLengthValid && isMatching
            ) {
                Text(text = "Simpan", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Batal")
            }
        }
    )
}

@Composable
fun PrivacyDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Privasi 100% Offline", fontWeight = FontWeight.Bold) },
        text = {
            Text(
                text = "MyMoneyManager bekerja sepenuhnya secara lokal tanpa akun, tanpa server pihak ketiga, dan tanpa pelacakan. Semua catatan transaksi, rekening, dan anggaran hanya tersimpan di perangkat Anda."
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Mengerti", fontWeight = FontWeight.SemiBold)
            }
        }
    )
}

@Composable
fun AddAccountDialog(
    currencySymbol: String,
    onConfirm: (name: String, type: UiAccountType, openingBalanceMinor: Long) -> Unit,
    onDismiss: () -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableStateOf(UiAccountType.BANK) }
    var balanceText by rememberSaveable { mutableStateOf("") }
    val separatorTransformation = remember { ThousandSeparatorVisualTransformation() }

    val amount = balanceText.toLongOrNull() ?: 0L
    val signedBalance = if (type == UiAccountType.CREDIT_CARD) -amount else amount

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Tambah Rekening Manual", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = "Nama rekening") },
                    placeholder = { Text(text = "Contoh: Dompet Harian, Mandiri") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )

                UiAccountType.entries.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { type = option },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = type == option, onClick = { type = option })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = option.dialogLabel())
                    }
                }

                OutlinedTextField(
                    value = balanceText,
                    onValueChange = { balanceText = it.filter { c -> c.isDigit() }.take(13) },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(
                            text = if (type == UiAccountType.CREDIT_CARD) "Tagihan berjalan" else "Saldo awal"
                        )
                    },
                    prefix = { Text(text = "$currencySymbol ") },
                    singleLine = true,
                    visualTransformation = separatorTransformation,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(14.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name.trim(), type, signedBalance) },
                enabled = name.isNotBlank()
            ) {
                Text(text = "Tambah", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Batal")
            }
        }
    )
}

private fun UiAccountType.dialogLabel(): String = when (this) {
    UiAccountType.CASH -> "Kas Tunai"
    UiAccountType.BANK -> "Rekening Bank"
    UiAccountType.CREDIT_CARD -> "Kartu Kredit"
    UiAccountType.SAVINGS -> "Tabungan"
}
