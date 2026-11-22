package com.example.mymoneymanager.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mymoneymanager.R
import com.example.mymoneymanager.ui.components.LinkBankModal
import com.example.mymoneymanager.ui.model.UiTransaction
import com.example.mymoneymanager.ui.model.UiTransactionType
import com.example.mymoneymanager.ui.util.MoneyFormat
import com.example.mymoneymanager.ui.viewmodel.TransactionsUiState
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ForestGradientStart = Color(0xFF124328)
private val ForestGradientEnd = Color(0xFF1E5E3A)

@Composable
fun TransactionsScreen(
    state: TransactionsUiState,
    onToggleBalanceVisibility: () -> Unit,
    onOpenLinkBankDialog: () -> Unit,
    onCloseLinkBankDialog: () -> Unit,
    onSelectBank: (String) -> Unit,
    onNavigateToAddIncome: () -> Unit,
    onNavigateToAddExpense: () -> Unit,
    onLoadSampleData: () -> Unit,
    onTransactionClick: (UiTransaction) -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddExpense,
                containerColor = ForestGradientEnd,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tambah Transaksi"
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            HeaderBalanceCard(
                state = state,
                onToggleVisibility = onToggleBalanceVisibility,
                onLinkBankClick = onOpenLinkBankDialog,
                onAddIncomeClick = onNavigateToAddIncome,
                onAddExpenseClick = onNavigateToAddExpense
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Riwayat Transaksi",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                if (state.transactions.isEmpty()) {
                    TextButton(onClick = onLoadSampleData) {
                        Text(
                            text = "Muat Data Demo",
                            color = ForestGradientEnd,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            if (state.transactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 72.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Non-interactive decorative vector background overlay
                    Image(
                        painter = painterResource(id = R.drawable.ic_decorative_empty_state),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(200.dp)
                            .graphicsLayer(alpha = 0.12f)
                            .clearAndSetSemantics { }
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Belum Ada Transaksi",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tekan tombol + untuk mencatat pengeluaran atau pemasukan baru",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline,
                            lineHeight = 16.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = state.transactions,
                        key = { it.id }
                    ) { item ->
                        TransactionRowItem(
                            transaction = item,
                            currencySymbol = state.currencySymbol,
                            isMasked = !state.isBalanceVisible,
                            onClick = { onTransactionClick(item) }
                        )
                    }
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

@Composable
private fun HeaderBalanceCard(
    state: TransactionsUiState,
    onToggleVisibility: () -> Unit,
    onLinkBankClick: () -> Unit,
    onAddIncomeClick: () -> Unit,
    onAddExpenseClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(ForestGradientStart, ForestGradientEnd)
                    )
                )
        ) {
            // Non-interactive decorative micro-vector accents overlay (low opacity ~ 0.12)
            Image(
                painter = painterResource(id = R.drawable.ic_decorative_card_accents),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer(alpha = 0.12f)
                    .clearAndSetSemantics { }
            )

            // Foreground interactive content layer
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Total Saldo",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    IconButton(
                        onClick = onToggleVisibility,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (state.isBalanceVisible) {
                                Icons.Default.Visibility
                            } else {
                                Icons.Default.VisibilityOff
                            },
                            contentDescription = if (state.isBalanceVisible) {
                                "Sembunyikan Saldo"
                            } else {
                                "Tampilkan Saldo"
                            },
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = MoneyFormat.format(
                        amountMinor = state.totalBalanceMinor,
                        symbol = state.currencySymbol,
                        isMasked = !state.isBalanceVisible
                    ),
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickActionPill(
                        icon = Icons.Default.AccountBalance,
                        title = "+ Bank",
                        onClick = onLinkBankClick,
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionPill(
                        icon = Icons.Default.ArrowDownward,
                        title = "+ Pemasukan",
                        onClick = onAddIncomeClick,
                        modifier = Modifier.weight(1.2f)
                    )
                    QuickActionPill(
                        icon = Icons.Default.ArrowUpward,
                        title = "- Pengeluaran",
                        onClick = onAddExpenseClick,
                        modifier = Modifier.weight(1.25f)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionPill(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = 0.16f),
        contentColor = Color.White,
        modifier = modifier.height(38.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TransactionRowItem(
    transaction: UiTransaction,
    currencySymbol: String,
    isMasked: Boolean,
    onClick: () -> Unit
) {
    val dateFormatter = remember {
        DateTimeFormatter.ofPattern(
            "d MMM yyyy",
            Locale.Builder().setLanguage("id").setRegion("ID").build()
        )
    }
    val isIncome = transaction.type == UiTransactionType.INCOME

    val badgeBg = when (transaction.type) {
        UiTransactionType.INCOME -> Color(0xFFE8F5E9)
        UiTransactionType.EXPENSE -> Color(0xFFFFEBEE)
    }
    val amountColor = when (transaction.type) {
        UiTransactionType.INCOME -> Color(0xFF2E7D32)
        UiTransactionType.EXPENSE -> Color(0xFFC62828)
    }
    val emoji = transaction.category?.iconEmoji ?: when (transaction.type) {
        UiTransactionType.INCOME -> "💰"
        UiTransactionType.EXPENSE -> "🛒"
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(badgeBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emoji,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${transaction.accountName} • ${transaction.date.format(dateFormatter)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = MoneyFormat.format(
                    amountMinor = transaction.amountMinor,
                    symbol = currencySymbol,
                    showSign = true,
                    isIncome = isIncome,
                    isMasked = isMasked
                ),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = amountColor,
                maxLines = 1
            )
        }
    }
}
