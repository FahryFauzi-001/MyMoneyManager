package com.example.mymoneymanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mymoneymanager.ui.components.LinkBankModal
import com.example.mymoneymanager.ui.model.UiAccount
import com.example.mymoneymanager.ui.model.UiAccountType
import com.example.mymoneymanager.ui.theme.ExpenseRust
import com.example.mymoneymanager.ui.util.MoneyFormat
import com.example.mymoneymanager.ui.viewmodel.AccountsUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    state: AccountsUiState,
    onAddManualAccountClick: () -> Unit,
    onOpenLinkBankDialog: () -> Unit,
    onCloseLinkBankDialog: () -> Unit,
    onSelectBank: (String) -> Unit,
    onDeleteAccountClick: (UiAccount) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { AccountsTopBar() },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddManualAccountClick,
                containerColor = Color(0xFF1E5E3A),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Tambah Rekening Manual")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                AccountsSummaryCard(
                    totalBalanceMinor = state.totalBalanceMinor,
                    cashTotalMinor = state.cashTotalMinor,
                    bankTotalMinor = state.bankTotalMinor,
                    currencySymbol = state.currencySymbol
                )
            }

            item {
                Button(
                    onClick = onOpenLinkBankDialog,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E5E3A),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccountBalance,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "+ Hubungkan Bank",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }

            item {
                Text(
                    text = "Daftar Rekening & Dompet",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (state.accounts.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        Text(
                            text = "Belum ada rekening. Hubungkan bank atau tekan tombol + untuk menambahkan rekening.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(items = state.accounts, key = { it.id }) { account ->
                    AccountCard(
                        account = account,
                        currencySymbol = state.currencySymbol,
                        onDeleteClick = { onDeleteAccountClick(account) }
                    )
                }
            }
        }

        if (state.showLinkBankDialog) {
            LinkBankModal(
                isLoading = state.isConnectingBank,
                onDismiss = onCloseLinkBankDialog,
                onSelectBank = onSelectBank
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountsTopBar() {
    TopAppBar(
        title = { Text(text = "Rekening & Bank", fontWeight = FontWeight.Bold) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        )
    )
}

@Composable
private fun AccountsSummaryCard(
    totalBalanceMinor: Long,
    cashTotalMinor: Long,
    bankTotalMinor: Long,
    currencySymbol: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF124328), Color(0xFF1E5E3A))
                    )
                )
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Total Akumulasi Saldo",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.85f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = MoneyFormat.format(totalBalanceMinor, currencySymbol),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AccountSummaryBadge(
                        modifier = Modifier.weight(1f),
                        label = "Kas Tunai",
                        amount = MoneyFormat.format(cashTotalMinor, currencySymbol)
                    )
                    AccountSummaryBadge(
                        modifier = Modifier.weight(1f),
                        label = "Rekening Bank",
                        amount = MoneyFormat.format(bankTotalMinor, currencySymbol)
                    )
                }
            }
        }
    }
}

@Composable
private fun AccountSummaryBadge(
    label: String,
    amount: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.16f),
        contentColor = Color.White
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.85f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = amount,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AccountCard(
    account: UiAccount,
    currencySymbol: String,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 14.dp, bottom = 14.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(account.colorHex.toSafeColor(), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = account.type.toIcon(),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = account.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = account.type.toLabel(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = MoneyFormat.format(account.balanceMinor, currencySymbol),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (account.balanceMinor < 0L) ExpenseRust else MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Hapus akun ${account.name}",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

private fun UiAccountType.toIcon(): ImageVector = when (this) {
    UiAccountType.CASH -> Icons.Outlined.Payments
    UiAccountType.BANK -> Icons.Filled.AccountBalance
    UiAccountType.CREDIT_CARD -> Icons.Filled.CreditCard
    UiAccountType.SAVINGS -> Icons.Filled.Savings
}

private fun UiAccountType.toLabel(): String = when (this) {
    UiAccountType.CASH -> "Kas Tunai"
    UiAccountType.BANK -> "Rekening Bank"
    UiAccountType.CREDIT_CARD -> "Kartu Kredit"
    UiAccountType.SAVINGS -> "Tabungan"
}

private fun String.toSafeColor(): Color = runCatching {
    val hex = removePrefix("#")
    val value = hex.toLong(16)
    Color(
        red = ((value shr 16) and 0xFF) / 255f,
        green = ((value shr 8) and 0xFF) / 255f,
        blue = (value and 0xFF) / 255f
    )
}.getOrDefault(Color(0xFF1E5E3A))
