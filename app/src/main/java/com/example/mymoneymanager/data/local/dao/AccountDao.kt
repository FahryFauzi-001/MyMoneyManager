package com.example.mymoneymanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.mymoneymanager.data.local.entity.AccountEntity
import com.example.mymoneymanager.ui.model.UiAccountType
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {

    @Query("SELECT * FROM accounts ORDER BY id ASC")
    fun observeAll(): Flow<List<AccountEntity>>

    @Query("SELECT COALESCE(SUM(balanceMinor), 0) FROM accounts")
    fun observeTotalBalance(): Flow<Long>

    @Query("SELECT * FROM accounts ORDER BY id ASC")
    suspend fun getAllOnce(): List<AccountEntity>

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getById(id: Long): AccountEntity?

    @Query("SELECT * FROM accounts WHERE type = :type ORDER BY id ASC LIMIT 1")
    suspend fun getFirstByType(type: UiAccountType): AccountEntity?

    @Query("SELECT COUNT(*) FROM accounts")
    suspend fun count(): Int

    @Insert
    suspend fun insert(account: AccountEntity): Long

    @Update
    suspend fun update(account: AccountEntity)

    @Query("UPDATE accounts SET balanceMinor = balanceMinor + :delta WHERE id = :id")
    suspend fun adjustBalance(id: Long, delta: Long)

    @Query("UPDATE accounts SET balanceMinor = :balanceMinor WHERE id = :id")
    suspend fun setBalance(id: Long, balanceMinor: Long)

    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM accounts")
    suspend fun deleteAll()
}
