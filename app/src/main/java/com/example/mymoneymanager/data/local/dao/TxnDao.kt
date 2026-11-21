package com.example.mymoneymanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.mymoneymanager.data.local.entity.TxnEntity
import com.example.mymoneymanager.data.local.entity.TxnWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface TxnDao {

    @Transaction
    @Query("SELECT * FROM transactions ORDER BY dateMillis DESC, id DESC")
    fun observeAll(): Flow<List<TxnWithDetails>>

    @Transaction
    @Query(
        """
        SELECT * FROM transactions
        WHERE dateMillis = :dayMillis
        ORDER BY id DESC
        """
    )
    fun observeByDay(dayMillis: Long): Flow<List<TxnWithDetails>>

    @Transaction
    @Query(
        """
        SELECT * FROM transactions
        WHERE dateMillis BETWEEN :startMillis AND :endMillis
        ORDER BY dateMillis DESC, id DESC
        """
    )
    fun observeBetween(startMillis: Long, endMillis: Long): Flow<List<TxnWithDetails>>

    @Transaction
    @Query("SELECT * FROM transactions ORDER BY dateMillis DESC, id DESC")
    suspend fun getAllWithDetailsOnce(): List<TxnWithDetails>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): TxnEntity?

    @Query(
        """
        SELECT type, SUM(amountMinor) AS total
        FROM transactions
        WHERE dateMillis BETWEEN :startMillis AND :endMillis
        GROUP BY type
        """
    )
    fun observeTotalsByType(startMillis: Long, endMillis: Long): Flow<List<TypeTotal>>

    @Query(
        """
        SELECT categoryId, SUM(amountMinor) AS total
        FROM transactions
        WHERE type = 'EXPENSE' AND dateMillis BETWEEN :startMillis AND :endMillis
        GROUP BY categoryId
        ORDER BY total DESC
        """
    )
    fun observeExpenseByCategory(startMillis: Long, endMillis: Long): Flow<List<CategoryTotal>>

    @Query(
        """
        SELECT strftime('%Y-%m', dateMillis / 1000, 'unixepoch') AS ym,
               type,
               SUM(amountMinor) AS total
        FROM transactions
        WHERE type IN ('INCOME', 'EXPENSE')
          AND dateMillis BETWEEN :startMillis AND :endMillis
        GROUP BY ym, type
        """
    )
    fun observeMonthlyTotals(startMillis: Long, endMillis: Long): Flow<List<MonthTypeTotal>>

    @Insert
    suspend fun insert(txn: TxnEntity): Long

    @Update
    suspend fun update(txn: TxnEntity)

    @Query("UPDATE transactions SET accountName = :accountName WHERE accountId = :accountId")
    suspend fun snapshotAccountName(accountId: Long, accountName: String)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()
}
