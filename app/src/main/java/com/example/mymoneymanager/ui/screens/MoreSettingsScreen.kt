package com.example.mymoneymanager.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CurrencyExchange
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Paid
import androidx.compose.material.icons.outlined.Pin
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mymoneymanager.ui.util.CurrencyCatalog

private val ForestAccent = Color(0xFF1E5E3A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreSettingsScreen(
    isAppLockEnabled: Boolean,
    currencySymbol: String,
    appVersion: String,
    onExportCsvClick: () -> Unit,
    onResetDemoClick: () -> Unit,
    onBudgetsClick: () -> Unit,
    onAppLockChanged: (Boolean) -> Unit,
    onChangePinClick: () -> Unit,
    onCurrencyClick: () -> Unit,
    onPrivacyClick: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { MoreSettingsTopBar() }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SettingsGroupCard(title = "Preferensi Mata Uang") {
                    ClickableSettingsItem(
                        headline = "Mata Uang Utama",
                        supporting = "Mata uang aktif: ${CurrencyCatalog.displayNameForSymbol(currencySymbol)}",
                        leading = Icons.Outlined.CurrencyExchange,
                        onClick = onCurrencyClick
                    )
                }
            }

            item {
                SettingsGroupCard(title = "Keamanan Aplikasi") {
                    ListItem(
                        headlineContent = {
                            Text(
                                text = "Kunci Aplikasi (PIN / Biometrik)",
                                fontWeight = FontWeight.Medium
                            )
                        },
                        supportingContent = {
                            Text(
                                text = if (isAppLockEnabled) {
                                    "Aktif • Meminta PIN 6 digit atau sidik jari saat aplikasi dibuka"
                                } else {
                                    "Aktifkan kunci PIN 6 digit untuk melindungi data keuangan Anda"
                                }
                            )
                        },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = null,
                                tint = ForestAccent
                            )
                        },
                        trailingContent = {
                            Switch(
                                checked = isAppLockEnabled,
                                onCheckedChange = onAppLockChanged,
                                colors = SwitchDefaults.colors(
                                    checkedTrackColor = ForestAccent,
                                    checkedThumbColor = Color.White
                                )
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )

                    if (isAppLockEnabled) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                        ClickableSettingsItem(
                            headline = "Atur Ulang PIN Keamanan",
                            supporting = "Ubah PIN 6 digit yang digunakan untuk membuka aplikasi",
                            leading = Icons.Outlined.Pin,
                            onClick = onChangePinClick
                        )
                    }
                }
            }

            item {
                SettingsGroupCard(title = "Anggaran Bulanan") {
                    ClickableSettingsItem(
                        headline = "Atur Batas Anggaran Kategori",
                        supporting = "Pantau batas pengeluaran Makanan, Transportasi, Belanja, dan lainnya",
                        leading = Icons.Outlined.Savings,
                        onClick = onBudgetsClick
                    )
                }
            }

            item {
                SettingsGroupCard(title = "Manajemen Data") {
                    ClickableSettingsItem(
                        headline = "Ekspor Data CSV",
                        supporting = "Simpan seluruh riwayat transaksi ke file CSV lokal",
                        leading = Icons.Outlined.Description,
                        onClick = onExportCsvClick
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    ClickableSettingsItem(
                        headline = "Reset Data Demo",
                        supporting = "Muat ulang contoh rekening dan transaksi 6 bulan terakhir",
                        leading = Icons.Outlined.RestartAlt,
                        onClick = onResetDemoClick
                    )
                }
            }

            item {
                SettingsGroupCard(title = "Tentang Aplikasi") {
                    ClickableSettingsItem(
                        headline = "Privasi Offline 100%",
                        supporting = "Seluruh data keuangan disimpan lokal tanpa server pihak ketiga",
                        leading = Icons.Outlined.Info,
                        onClick = onPrivacyClick
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    ListItem(
                        headlineContent = {
                            Text(text = "Versi Aplikasi", fontWeight = FontWeight.Medium)
                        },
                        supportingContent = { Text(text = "v$appVersion") },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Outlined.Paid,
                                contentDescription = null,
                                tint = ForestAccent
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoreSettingsTopBar() {
    TopAppBar(
        title = {
            Text(
                text = "Pengaturan",
                fontWeight = FontWeight.Bold
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        )
    )
}

@Composable
private fun SettingsGroupCard(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            modifier = Modifier.padding(horizontal = 6.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = ForestAccent
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }
    }
}

@Composable
private fun ClickableSettingsItem(
    headline: String,
    supporting: String,
    leading: ImageVector,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(text = headline, fontWeight = FontWeight.Medium) },
        supportingContent = { Text(text = supporting) },
        leadingContent = {
            Icon(
                imageVector = leading,
                contentDescription = null,
                tint = ForestAccent
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    )
}
