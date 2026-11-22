package com.example.mymoneymanager.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.mymoneymanager.data.local.converter.Converters
import com.example.mymoneymanager.data.local.dao.AccountDao
import com.example.mymoneymanager.data.local.dao.BudgetDao
import com.example.mymoneymanager.data.local.dao.CategoryDao
import com.example.mymoneymanager.data.local.dao.TxnDao
import com.example.mymoneymanager.data.local.entity.AccountEntity
import com.example.mymoneymanager.data.local.entity.BudgetEntity
import com.example.mymoneymanager.data.local.entity.CategoryEntity
import com.example.mymoneymanager.data.local.entity.TxnEntity
import com.example.mymoneymanager.ui.model.UiAccountType

@Database(
    entities = [
        AccountEntity::class,
        CategoryEntity::class,
        TxnEntity::class,
        BudgetEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun txnDao(): TxnDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        private const val DB_NAME = "mymoneymanager.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS transactions_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        amountMinor INTEGER NOT NULL,
                        type TEXT NOT NULL,
                        dateMillis INTEGER NOT NULL,
                        accountId INTEGER,
                        accountName TEXT NOT NULL,
                        destinationAccountId INTEGER,
                        categoryId INTEGER,
                        note TEXT NOT NULL,
                        FOREIGN KEY(accountId) REFERENCES accounts(id) ON UPDATE NO ACTION ON DELETE SET NULL,
                        FOREIGN KEY(destinationAccountId) REFERENCES accounts(id) ON UPDATE NO ACTION ON DELETE SET NULL,
                        FOREIGN KEY(categoryId) REFERENCES categories(id) ON UPDATE NO ACTION ON DELETE SET NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO transactions_new (
                        id, title, amountMinor, type, dateMillis, accountId, accountName, destinationAccountId, categoryId, note
                    )
                    SELECT
                        t.id, t.title, t.amountMinor, t.type, t.dateMillis, t.accountId,
                        COALESCE(a.name, 'Akun Diarsipkan'),
                        t.destinationAccountId, t.categoryId, t.note
                    FROM transactions t
                    LEFT JOIN accounts a ON a.id = t.accountId
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE transactions")
                db.execSQL("ALTER TABLE transactions_new RENAME TO transactions")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_accountId ON transactions (accountId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_destinationAccountId ON transactions (destinationAccountId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_categoryId ON transactions (categoryId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_dateMillis ON transactions (dateMillis)")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS transactions_v3 (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        amountMinor INTEGER NOT NULL,
                        type TEXT NOT NULL,
                        dateMillis INTEGER NOT NULL,
                        accountId INTEGER,
                        accountName TEXT NOT NULL,
                        categoryId INTEGER,
                        note TEXT NOT NULL,
                        FOREIGN KEY(accountId) REFERENCES accounts(id) ON UPDATE NO ACTION ON DELETE SET NULL,
                        FOREIGN KEY(categoryId) REFERENCES categories(id) ON UPDATE NO ACTION ON DELETE SET NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO transactions_v3 (
                        id, title, amountMinor, type, dateMillis, accountId, accountName, categoryId, note
                    )
                    SELECT
                        t.id, t.title, t.amountMinor, t.type, t.dateMillis, t.accountId,
                        t.accountName, t.categoryId, t.note
                    FROM transactions t
                    WHERE t.type IN ('INCOME', 'EXPENSE')
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE transactions")
                db.execSQL("ALTER TABLE transactions_v3 RENAME TO transactions")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_accountId ON transactions (accountId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_categoryId ON transactions (categoryId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_dateMillis ON transactions (dateMillis)")
            }
        }

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: build(context.applicationContext).also { INSTANCE = it }
            }

        private fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, DB_NAME)
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .fallbackToDestructiveMigration(dropAllTables = true)
                .addCallback(SeedCallback)
                .build()
    }

    private object SeedCallback : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            val accounts = listOf(
                AccountEntity(name = "Kas Tunai", type = UiAccountType.CASH, balanceMinor = 0L, colorHex = "#1E5E3A"),
                AccountEntity(name = "BCA", type = UiAccountType.BANK, balanceMinor = 0L, colorHex = "#005E9C")
            )
            accounts.forEach {
                db.execSQL(
                    "INSERT INTO accounts (name, type, balanceMinor, colorHex) VALUES (?, ?, ?, ?)",
                    arrayOf<Any?>(it.name, it.type.name, it.balanceMinor, it.colorHex)
                )
            }

            val categories = listOf(
                CategoryEntity(name = "Makanan", colorHex = "#E67E22", iconEmoji = "🍜"),
                CategoryEntity(name = "Transportasi", colorHex = "#1976D2", iconEmoji = "🚕"),
                CategoryEntity(name = "Belanja", colorHex = "#8E24AA", iconEmoji = "🛍️"),
                CategoryEntity(name = "Tagihan", colorHex = "#D84315", iconEmoji = "🧾"),
                CategoryEntity(name = "Gaji", colorHex = "#2E7D32", iconEmoji = "💼")
            )
            categories.forEach {
                db.execSQL(
                    "INSERT INTO categories (name, colorHex, iconEmoji) VALUES (?, ?, ?)",
                    arrayOf<Any?>(it.name, it.colorHex, it.iconEmoji)
                )
            }
        }
    }
}
