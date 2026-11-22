package com.example.mymoneymanager.data.repository

import androidx.room.withTransaction
import com.example.mymoneymanager.data.local.AppDatabase
import com.example.mymoneymanager.data.local.dao.CategoryTotal
import com.example.mymoneymanager.data.local.dao.MonthTypeTotal
import com.example.mymoneymanager.data.local.dao.TypeTotal
import com.example.mymoneymanager.data.local.entity.AccountEntity
import com.example.mymoneymanager.data.local.entity.BudgetEntity
import com.example.mymoneymanager.data.local.entity.CategoryEntity
import com.example.mymoneymanager.data.local.entity.TxnEntity
import com.example.mymoneymanager.data.local.preferences.UserPreferences
import com.example.mymoneymanager.data.mapper.toUi
import com.example.mymoneymanager.data.util.endMillis
import com.example.mymoneymanager.data.util.startMillis
import com.example.mymoneymanager.data.util.toEpochMillisUtc
import com.example.mymoneymanager.data.util.toLocalDateUtc
import com.example.mymoneymanager.ui.model.UiAccount
import com.example.mymoneymanager.ui.model.UiAccountType
import com.example.mymoneymanager.ui.model.UiBudget
import com.example.mymoneymanager.ui.model.UiCategory
import com.example.mymoneymanager.ui.model.UiCategoryStatistic
import com.example.mymoneymanager.ui.model.UiMonthlySummary
import com.example.mymoneymanager.ui.model.UiMonthlyTrend
import com.example.mymoneymanager.ui.model.UiStatistics
import com.example.mymoneymanager.ui.model.UiTransaction
import com.example.mymoneymanager.ui.model.UiTransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

class MoneyRepository(
    private val db: AppDatabase,
    private val preferences: UserPreferences
) {
    private val accountDao = db.accountDao()
    private val categoryDao = db.categoryDao()
    private val txnDao = db.txnDao()
    private val budgetDao = db.budgetDao()

    private val indonesianLocale: Locale = Locale.Builder()
        .setLanguage("id")
        .setRegion("ID")
        .build()

    val currencySymbol: Flow<String> = preferences.currencySymbol
    val isAppLockEnabled: Flow<Boolean> = preferences.isAppLockEnabled
    val isOnboardingCompleted: Flow<Boolean> = preferences.isOnboardingCompleted
    val hasPin: Flow<Boolean> = preferences.hasPin

    suspend fun clearPin() = preferences.clearPin()

    fun observeAccounts(): Flow<List<UiAccount>> =
        accountDao.observeAll().map { list -> list.map { it.toUi() } }

    fun observeCategories(): Flow<List<UiCategory>> =
        categoryDao.observeAll().map { list -> list.map { it.toUi() } }

    suspend fun addAccount(
        name: String,
        type: UiAccountType,
        openingBalanceMinor: Long,
        colorHex: String
    ): Long {
        require(name.isNotBlank()) { "Nama rekening tidak boleh kosong." }
        return accountDao.insert(
            AccountEntity(
                name = name.trim(),
                type = type,
                balanceMinor = openingBalanceMinor,
                colorHex = colorHex
            )
        )
    }

    suspend fun deleteAccount(accountId: Long) {
        db.withTransaction {
            val existing = accountDao.getById(accountId) ?: return@withTransaction
            txnDao.snapshotAccountName(accountId, existing.name)
            accountDao.deleteById(accountId)
        }
    }

    suspend fun addCategory(name: String, colorHex: String, iconEmoji: String): Long {
        require(name.isNotBlank()) { "Nama kategori tidak boleh kosong." }
        return categoryDao.insert(
            CategoryEntity(name = name.trim(), colorHex = colorHex, iconEmoji = iconEmoji)
        )
    }

    fun observeAllTransactions(): Flow<List<UiTransaction>> =
        txnDao.observeAll().map { list -> list.map { it.toUi() } }

    fun observeTransactions(from: LocalDate, to: LocalDate = from): Flow<List<UiTransaction>> =
        txnDao.observeBetween(from.toEpochMillisUtc(), to.toEpochMillisUtc())
            .map { list -> list.map { it.toUi() } }

    fun observeSummary(from: LocalDate, to: LocalDate = from): Flow<UiMonthlySummary> =
        combine(
            txnDao.observeTotalsByType(from.toEpochMillisUtc(), to.toEpochMillisUtc()),
            preferences.currencySymbol
        ) { totals, symbol ->
            val income = totals.totalOf(UiTransactionType.INCOME)
            val expense = totals.totalOf(UiTransactionType.EXPENSE)
            UiMonthlySummary(
                incomeMinor = income,
                expenseMinor = expense,
                netMinor = income - expense,
                currencySymbol = symbol
            )
        }

    suspend fun preload() {
        db.withTransaction {
            accountDao.getAllOnce()
            categoryDao.getAllOnce()
        }
    }

    suspend fun saveTransaction(
        type: UiTransactionType,
        amountMinor: Long,
        date: LocalDate,
        sourceAccountId: Long?,
        categoryId: Long?,
        note: String
    ): Long {
        require(type in listOf(UiTransactionType.INCOME, UiTransactionType.EXPENSE)) {
            "Tipe transaksi hanya boleh Pemasukan atau Pengeluaran."
        }
        require(amountMinor > 0L) { "Nominal harus lebih dari 0." }
        val sourceId = requireNotNull(sourceAccountId) { "Pilih rekening sumber." }
        val category = categoryId?.let { categoryDao.getById(it) }
            ?: error("Kategori transaksi harus dipilih.")

        return db.withTransaction {
            val source = accountDao.getById(sourceId) ?: error("Rekening sumber tidak ditemukan.")

            val title = when (type) {
                UiTransactionType.INCOME -> category.name.ifBlank { "Pemasukan" }
                UiTransactionType.EXPENSE -> category.name.ifBlank { "Pengeluaran" }
            }

            val entity = TxnEntity(
                title = title,
                amountMinor = amountMinor,
                type = type,
                dateMillis = date.toEpochMillisUtc(),
                accountId = sourceId,
                accountName = source.name,
                categoryId = category.id,
                note = note.trim()
            )
            val id = txnDao.insert(entity)
            applyBalanceEffect(entity, direction = +1)
            id
        }
    }

    suspend fun deleteTransaction(id: Long) {
        db.withTransaction {
            val old = txnDao.getById(id) ?: return@withTransaction
            applyBalanceEffect(old, direction = -1)
            txnDao.deleteById(id)
        }
    }

    private suspend fun applyBalanceEffect(txn: TxnEntity, direction: Int) {
        val delta = txn.amountMinor * direction
        val srcId = txn.accountId ?: return
        when (txn.type) {
            UiTransactionType.INCOME -> accountDao.adjustBalance(srcId, delta)
            UiTransactionType.EXPENSE -> accountDao.adjustBalance(srcId, -delta)
        }
    }

    fun observeStatistics(month: YearMonth): Flow<UiStatistics> =
        combine(
            txnDao.observeExpenseByCategory(month.startMillis(), month.endMillis()),
            txnDao.observeMonthlyTotals(month.minusMonths(5).startMillis(), month.endMillis()),
            categoryDao.observeAll()
        ) { categoryTotals, monthlyTotals, categories ->
            buildStatistics(month, categoryTotals, monthlyTotals, categories)
        }

    private fun buildStatistics(
        month: YearMonth,
        categoryTotals: List<CategoryTotal>,
        monthlyTotals: List<MonthTypeTotal>,
        categories: List<CategoryEntity>
    ): UiStatistics {
        val categoryById = categories.associateBy { it.id }

        val expenseByMonth: Map<String, Long> = monthlyTotals
            .filter { it.type == UiTransactionType.EXPENSE }
            .groupBy { it.ym }
            .mapValues { (_, rows) -> rows.sumOf { it.total } }

        val netByMonth: Map<String, Long> = monthlyTotals
            .groupBy { it.ym }
            .mapValues { (_, rows) ->
                rows.sumOf { if (it.type == UiTransactionType.INCOME) it.total else -it.total }
            }

        fun expenseOf(m: YearMonth): Long = expenseByMonth[m.toString()] ?: 0L
        fun netOf(m: YearMonth): Long = netByMonth[m.toString()] ?: 0L

        val resolved = categoryTotals.mapNotNull { row ->
            row.categoryId?.let { categoryById[it] }?.let { it to row.total }
        }
        val totalExpense = resolved.sumOf { it.second }
        val denominator = totalExpense.coerceAtLeast(1L).toFloat()
        val categoryStatistics = resolved.map { (category, total) ->
            UiCategoryStatistic(
                category = category.toUi(),
                amountMinor = total,
                percentage = total.toFloat() / denominator
            )
        }

        val currentExpense = expenseOf(month)
        val previousExpense = expenseOf(month.minusMonths(1))
        val (comparisonText, isPositive) = if (previousExpense == 0L) {
            "Belum ada data bulan lalu" to true
        } else {
            val pct = ((currentExpense - previousExpense) * 100.0 / abs(previousExpense)).roundToInt()
            val sign = if (pct > 0) "+" else ""
            "$sign$pct% dibanding bulan lalu" to (pct <= 0)
        }

        val trend = (5 downTo 0).map { back ->
            val m = month.minusMonths(back.toLong())
            UiMonthlyTrend(
                monthLabel = m.month.getDisplayName(TextStyle.SHORT, indonesianLocale),
                amountMinor = expenseOf(m).coerceAtLeast(0L),
                isCurrentMonth = back == 0
            )
        }

        return UiStatistics(
            monthLabel = month.month.getDisplayName(TextStyle.FULL, indonesianLocale),
            totalExpenseMinor = currentExpense,
            netMinor = netOf(month),
            comparisonText = comparisonText,
            isPositiveComparison = isPositive,
            categoryStatistics = categoryStatistics,
            monthlyTrend = trend
        )
    }

    fun observeBudgets(month: YearMonth): Flow<List<UiBudget>> =
        combine(
            budgetDao.observeAll(),
            categoryDao.observeAll(),
            txnDao.observeExpenseByCategory(month.startMillis(), month.endMillis())
        ) { budgets, categories, spent ->
            val categoryById = categories.associateBy { it.id }
            val spentByCategory = spent.associate { it.categoryId to it.total }
            budgets.mapNotNull { budget ->
                categoryById[budget.categoryId]?.let { category ->
                    UiBudget(
                        category = category.toUi(),
                        budgetMinor = budget.monthlyBudgetMinor,
                        spentMinor = spentByCategory[budget.categoryId] ?: 0L
                    )
                }
            }
        }

    suspend fun setBudget(categoryId: Long, monthlyBudgetMinor: Long) {
        if (monthlyBudgetMinor <= 0L) {
            budgetDao.deleteByCategory(categoryId)
        } else {
            budgetDao.upsert(BudgetEntity(categoryId, monthlyBudgetMinor))
        }
    }

    suspend fun completeOnboarding(
        currencyCode: String,
        currencySymbol: String,
        cashOpeningBalanceMinor: Long,
        bankOpeningBalanceMinor: Long
    ) {
        db.withTransaction {
            val cash = accountDao.getFirstByType(UiAccountType.CASH)
            if (cash != null) {
                accountDao.setBalance(cash.id, cashOpeningBalanceMinor)
            } else {
                accountDao.insert(
                    AccountEntity(
                        name = "Kas Tunai",
                        type = UiAccountType.CASH,
                        balanceMinor = cashOpeningBalanceMinor,
                        colorHex = "#1E5E3A"
                    )
                )
            }
            val bank = accountDao.getFirstByType(UiAccountType.BANK)
            if (bank != null) {
                accountDao.setBalance(bank.id, bankOpeningBalanceMinor)
            } else {
                accountDao.insert(
                    AccountEntity(
                        name = "BCA",
                        type = UiAccountType.BANK,
                        balanceMinor = bankOpeningBalanceMinor,
                        colorHex = "#005E9C"
                    )
                )
            }
        }
        preferences.setCurrency(currencyCode, currencySymbol)
        preferences.setOnboardingCompleted()
    }

    suspend fun loadSampleData() {
        db.withTransaction {
            val existingAccounts = accountDao.getAllOnce()
            val cashId = existingAccounts.firstOrNull { it.type == UiAccountType.CASH }?.id
                ?: accountDao.insert(AccountEntity(name = "Kas Tunai", type = UiAccountType.CASH, balanceMinor = 1_500_000L, colorHex = "#1E5E3A"))
            val bankId = existingAccounts.firstOrNull { it.type == UiAccountType.BANK }?.id
                ?: accountDao.insert(AccountEntity(name = "BCA", type = UiAccountType.BANK, balanceMinor = 10_000_000L, colorHex = "#005E9C"))

            val existingCategories = categoryDao.getAllOnce()
            suspend fun ensureCategory(name: String, colorHex: String, emoji: String): Long =
                existingCategories.firstOrNull {
                    it.name.equals(name, ignoreCase = true)
                }?.id ?: categoryDao.insert(CategoryEntity(name = name, colorHex = colorHex, iconEmoji = emoji))

            val catFood = ensureCategory("Makanan", "#E67E22", "🍜")
            val catTransport = ensureCategory("Transportasi", "#1976D2", "🚕")
            val catShop = ensureCategory("Belanja", "#8E24AA", "🛍️")
            val catBills = ensureCategory("Tagihan", "#D84315", "🧾")
            val catSalary = ensureCategory("Gaji", "#2E7D32", "💼")

            insertDemoTransactions(cashId, bankId, catFood, catTransport, catShop, catBills, catSalary)
        }
    }

    suspend fun resetDemoData() {
        db.withTransaction {
            txnDao.deleteAll()
            budgetDao.deleteAll()
            accountDao.deleteAll()
            categoryDao.deleteAll()

            val cashId = accountDao.insert(
                AccountEntity(name = "Kas Tunai", type = UiAccountType.CASH, balanceMinor = 1_250_000L, colorHex = "#1E5E3A")
            )
            val bankId = accountDao.insert(
                AccountEntity(name = "BCA", type = UiAccountType.BANK, balanceMinor = 12_500_000L, colorHex = "#005E9C")
            )

            val catFood = categoryDao.insert(CategoryEntity(name = "Makanan", colorHex = "#E67E22", iconEmoji = "🍜"))
            val catTransport = categoryDao.insert(CategoryEntity(name = "Transportasi", colorHex = "#1976D2", iconEmoji = "🚕"))
            val catShop = categoryDao.insert(CategoryEntity(name = "Belanja", colorHex = "#8E24AA", iconEmoji = "🛍️"))
            val catBills = categoryDao.insert(CategoryEntity(name = "Tagihan", colorHex = "#D84315", iconEmoji = "🧾"))
            val catSalary = categoryDao.insert(CategoryEntity(name = "Gaji", colorHex = "#2E7D32", iconEmoji = "💼"))

            insertDemoTransactions(cashId, bankId, catFood, catTransport, catShop, catBills, catSalary)
        }
    }

    private suspend fun insertDemoTransactions(
        cashId: Long,
        bankId: Long,
        catFood: Long,
        catTransport: Long,
        catShop: Long,
        catBills: Long,
        catSalary: Long
    ) {
        val today = LocalDate.now()
        val demoItems = listOf(
            DemoTxn(UiTransactionType.INCOME, "Gaji Bulanan", 8_500_000L, today.withDayOfMonth(1), bankId, "BCA", catSalary, "Gaji pokok bulan ini"),
            DemoTxn(UiTransactionType.EXPENSE, "Makan Siang & Kopi", 85_000L, today, cashId, "Kas Tunai", catFood, "Waroeng & kopi susu"),
            DemoTxn(UiTransactionType.EXPENSE, "Transportasi Harian", 120_000L, today.minusDays(1), cashId, "Kas Tunai", catTransport, "Ojek online & tol"),
            DemoTxn(UiTransactionType.EXPENSE, "Belanja Kebutuhan Rumah", 460_000L, today.minusDays(2), bankId, "BCA", catShop, "Supermarket bulanan"),
            DemoTxn(UiTransactionType.EXPENSE, "Tagihan Listrik & Internet", 680_000L, today.minusDays(3), bankId, "BCA", catBills, "PLN + WiFi rumah"),
            DemoTxn(UiTransactionType.EXPENSE, "Makan Malam Keluarga", 245_000L, today.minusDays(4), bankId, "BCA", catFood, "Resto nusantara"),
            // Historical 5 months for 6-month Bar Chart trend
            DemoTxn(UiTransactionType.EXPENSE, "Pengeluaran Bulanan", 1_850_000L, today.minusMonths(1).withDayOfMonth(12), bankId, "BCA", catFood, "Arsip bulan lalu"),
            DemoTxn(UiTransactionType.EXPENSE, "Pengeluaran Bulanan", 1_420_000L, today.minusMonths(2).withDayOfMonth(14), bankId, "BCA", catShop, "Arsip 2 bulan lalu"),
            DemoTxn(UiTransactionType.EXPENSE, "Pengeluaran Bulanan", 2_100_000L, today.minusMonths(3).withDayOfMonth(10), bankId, "BCA", catBills, "Arsip 3 bulan lalu"),
            DemoTxn(UiTransactionType.EXPENSE, "Pengeluaran Bulanan", 1_280_000L, today.minusMonths(4).withDayOfMonth(15), cashId, "Kas Tunai", catTransport, "Arsip 4 bulan lalu"),
            DemoTxn(UiTransactionType.EXPENSE, "Pengeluaran Bulanan", 1_650_000L, today.minusMonths(5).withDayOfMonth(11), bankId, "BCA", catFood, "Arsip 5 bulan lalu")
        )

        demoItems.forEach { item ->
            val entity = TxnEntity(
                title = item.title,
                amountMinor = item.amountMinor,
                type = item.type,
                dateMillis = item.date.toEpochMillisUtc(),
                accountId = item.accountId,
                accountName = item.accountName,
                categoryId = item.categoryId,
                note = item.note
            )
            txnDao.insert(entity)
            if (item.date.month == today.month && item.date.year == today.year) {
                applyBalanceEffect(entity, direction = +1)
            }
        }
    }

    private data class DemoTxn(
        val type: UiTransactionType,
        val title: String,
        val amountMinor: Long,
        val date: LocalDate,
        val accountId: Long,
        val accountName: String,
        val categoryId: Long?,
        val note: String
    )

    suspend fun setCurrency(code: String, symbol: String) = preferences.setCurrency(code, symbol)
    suspend fun setAppLockEnabled(enabled: Boolean) = preferences.setAppLockEnabled(enabled)
    suspend fun setPin(pin: String) = preferences.setPin(pin)
    suspend fun verifyPin(pin: String): Boolean = preferences.verifyPin(pin)

    suspend fun exportTransactionsCsv(): String {
        val rows = txnDao.getAllWithDetailsOnce()

        val header = listOf(
            "Tanggal", "Tipe", "Judul", "Rekening", "Kategori", "Nominal", "Catatan"
        )

        return buildString {
            append(header.joinToString(",") { it.toCsvField() }).append("\r\n")
            rows.forEach { row ->
                val t = row.txn
                val sourceName = row.account?.name ?: t.accountName.ifBlank { "Akun Diarsipkan" }
                val fields = listOf(
                    t.dateMillis.toLocalDateUtc().toString(),
                    t.type.name,
                    t.title,
                    sourceName,
                    row.category?.name.orEmpty(),
                    t.amountMinor.toString(),
                    t.note
                )
                append(fields.joinToString(",") { it.toCsvField() }).append("\r\n")
            }
        }
    }

    private fun String.toCsvField(): String {
        val safe = if (isNotEmpty() && first() in "=+-@\t\r") "'$this" else this
        val needsQuotes = safe.any { it == ',' || it == '"' || it == '\n' || it == '\r' }
        return if (needsQuotes) "\"${safe.replace("\"", "\"\"")}\"" else safe
    }

    private fun List<TypeTotal>.totalOf(type: UiTransactionType): Long =
        firstOrNull { it.type == type }?.total ?: 0L
}
