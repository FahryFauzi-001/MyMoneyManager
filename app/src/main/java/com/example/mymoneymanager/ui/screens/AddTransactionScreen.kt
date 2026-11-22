package com.example.mymoneymanager.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mymoneymanager.data.util.toEpochMillisUtc
import com.example.mymoneymanager.data.util.toLocalDateUtc
import com.example.mymoneymanager.ui.model.UiAccount
import com.example.mymoneymanager.ui.model.UiCategory
import com.example.mymoneymanager.ui.model.UiTransactionType
import com.example.mymoneymanager.ui.util.MoneyFormat
import com.example.mymoneymanager.ui.util.ThousandSeparatorVisualTransformation
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ForestPrimary = Color(0xFF1E5E3A)
private val IndonesianDateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("id-ID"))

@Composable
fun AddTransactionScreen(
    initialType: UiTransactionType = UiTransactionType.EXPENSE,
    accounts: List<UiAccount>,
    categories: List<UiCategory>,
    currencySymbol: String,
    selectedDate: LocalDate,
    onSaveClick: (
        type: UiTransactionType,
        amountMinor: Long,
        date: LocalDate,
        sourceAccountId: Long?,
        categoryId: Long?,
        note: String,
    ) -> Unit,
    onBackClick: () -> Unit
) {
    var selectedType by rememberSaveable(initialType) { mutableStateOf(initialType) }
    var amountText by rememberSaveable { mutableStateOf("") }
    var selectedDateValue by rememberSaveable { mutableStateOf(selectedDate.toString()) }
    var sourceAccountId by rememberSaveable { mutableStateOf<Long?>(null) }
    var selectedCategoryId by rememberSaveable { mutableStateOf<Long?>(null) }
    var note by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(accounts) {
        if ((sourceAccountId == null) && accounts.isNotEmpty()) {
            sourceAccountId = accounts.first().id
        }
    }

    LaunchedEffect(categories, selectedType) {
        if (selectedCategoryId == null && categories.isNotEmpty()) {
            selectedCategoryId = categories.first().id
        }
    }

    val amountMinor = amountText.filter { it.isDigit() }.toLongOrNull() ?: 0L

    val amountError = amountMinor <= 0L
    val sourceError = sourceAccountId == null
    val categoryError = selectedCategoryId == null

    val canSave = !amountError && !sourceError && !categoryError

    val parsedDate = runCatching {
        LocalDate.parse(selectedDateValue)
    }.getOrDefault(LocalDate.now())

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AddTransactionTopBar(
                selectedType = selectedType,
                onBackClick = onBackClick
            )
        },
        bottomBar = {
            SaveButton(
                enabled = canSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, bottom = 16.dp, top = 8.dp),
                onSaveClick = {
                    onSaveClick(
                        selectedType,
                        amountMinor,
                        parsedDate,
                        sourceAccountId,
                        selectedCategoryId,
                        note.trim()
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            TransactionTypeSelector(
                selectedType = selectedType,
                onTypeSelected = { type ->
                    selectedType = type
                    if (selectedCategoryId == null && categories.isNotEmpty()) {
                        selectedCategoryId = categories.first().id
                    }
                }
            )

            AmountInput(
                amountText = amountText,
                currencySymbol = currencySymbol,
                isError = amountError && amountText.isNotBlank(),
                onAmountChanged = { value ->
                    val digits = value.filter { it.isDigit() }
                    if (digits.length <= 13) amountText = digits
                }
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionLabel(text = "Rekening Sumber")
                AccountSelector(
                    accounts = accounts,
                    selectedAccountId = sourceAccountId,
                    currencySymbol = currencySymbol,
                    placeholder = "Pilih rekening sumber",
                    isError = sourceError,
                    onAccountSelected = { account ->
                        sourceAccountId = account?.id
                    }
                )
                if (sourceError) {
                    FieldError(text = "Pilih rekening sumber terlebih dahulu.")
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionLabel(text = "Kategori")
                CategorySelector(
                    categories = categories,
                    selectedCategoryId = selectedCategoryId,
                    onCategorySelected = { category ->
                        selectedCategoryId = category?.id
                    }
                )
                if (categoryError) {
                    FieldError(text = "Pilih kategori transaksi.")
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionLabel(text = "Tanggal Transaksi")
                DateSelector(
                    selectedDate = parsedDate,
                    onDateSelected = { picked -> selectedDateValue = picked.toString() }
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionLabel(text = "Catatan")
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(text = "Contoh: Makan siang, bayar listrik (opsional)") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTransactionTopBar(
    selectedType: UiTransactionType,
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = when (selectedType) {
                    UiTransactionType.EXPENSE -> "Catat Pengeluaran"
                    UiTransactionType.INCOME -> "Catat Pemasukan"
                },
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            navigationIconContentColor = MaterialTheme.colorScheme.onBackground
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionTypeSelector(
    selectedType: UiTransactionType,
    onTypeSelected: (UiTransactionType) -> Unit
) {
    val options = listOf(
        UiTransactionType.EXPENSE to "Pengeluaran",
        UiTransactionType.INCOME to "Pemasukan"
    )

    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, (type, label) ->
            SegmentedButton(
                selected = selectedType == type,
                onClick = { onTypeSelected(type) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = ForestPrimary,
                    activeContentColor = Color.White
                ),
                label = { Text(text = label, maxLines = 1) }
            )
        }
    }
}

@Composable
private fun AmountInput(
    amountText: String,
    currencySymbol: String,
    isError: Boolean,
    onAmountChanged: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionLabel(text = "Nominal")
        OutlinedTextField(
            value = amountText,
            onValueChange = onAmountChanged,
            modifier = Modifier.fillMaxWidth(),
            isError = isError,
            supportingText = if (isError) {
                { Text(text = "Masukkan nominal lebih dari 0.") }
            } else null,
            prefix = {
                Text(
                    text = "$currencySymbol ",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = ForestPrimary
                )
            },
            textStyle = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = ThousandSeparatorVisualTransformation(),
            placeholder = {
                Text(
                    text = "0",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.outline
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateSelector(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }

    OutlinedButton(
        onClick = { showPicker = true },
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.CalendarMonth,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = selectedDate.format(IndonesianDateFormatter),
            fontWeight = FontWeight.Medium
        )
    }

    if (showPicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.toEpochMillisUtc()
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            onDateSelected(millis.toLocalDateUtc())
                        }
                        showPicker = false
                    }
                ) {
                    Text(text = "Pilih")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text(text = "Batal")
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
private fun AccountSelector(
    accounts: List<UiAccount>,
    selectedAccountId: Long?,
    currencySymbol: String,
    placeholder: String,
    isError: Boolean,
    onAccountSelected: (UiAccount?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedAccount = accounts.firstOrNull { it.id == selectedAccountId }

    Box(modifier = Modifier.fillMaxWidth()) {
        Card(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            ),
            border = if (isError) {
                BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.error)
            } else {
                null
            },
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            color = selectedAccount?.colorHex?.toColor() ?: ForestPrimary,
                            shape = CircleShape
                        )
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = selectedAccount?.name ?: placeholder,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    if (selectedAccount != null) {
                        Text(
                            text = "Saldo: ${MoneyFormat.format(selectedAccount.balanceMinor, currencySymbol)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Filled.ExpandMore,
                    contentDescription = null
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            accounts.forEach { account ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(text = account.name, fontWeight = FontWeight.Medium)
                            Text(
                                text = MoneyFormat.format(account.balanceMinor, currencySymbol),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    color = account.colorHex.toColor(),
                                    shape = CircleShape
                                )
                        )
                    },
                    onClick = {
                        onAccountSelected(account)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategorySelector(
    categories: List<UiCategory>,
    selectedCategoryId: Long?,
    onCategorySelected: (UiCategory?) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { category ->
            val isSelected = category.id == selectedCategoryId
            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(category) },
                label = { Text(text = "${category.iconEmoji} ${category.name}") },
                shape = RoundedCornerShape(50),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ForestPrimary,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@Composable
private fun SaveButton(
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onSaveClick: () -> Unit
) {
    Button(
        onClick = onSaveClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = ForestPrimary,
            contentColor = Color.White,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            disabledContentColor = MaterialTheme.colorScheme.outline
        )
    ) {
        Icon(imageVector = Icons.Filled.Check, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Simpan Transaksi",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun FieldError(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error
    )
}

private fun String.toColor(): Color {
    val hex = removePrefix("#")
    val value = hex.toLongOrNull(16) ?: 0x1E5E3AL
    return Color(
        red = ((value shr 16) and 0xFF) / 255f,
        green = ((value shr 8) and 0xFF) / 255f,
        blue = (value and 0xFF) / 255f
    )
}
