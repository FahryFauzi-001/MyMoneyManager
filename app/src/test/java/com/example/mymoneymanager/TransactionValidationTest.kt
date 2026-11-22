package com.example.mymoneymanager

import com.example.mymoneymanager.ui.model.UiTransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TransactionValidationTest {

    @Test
    fun transactionTypes_containOnlyIncomeAndExpense() {
        val types = UiTransactionType.entries.map { it.name }
        assertEquals(2, types.size)
        assertTrue(types.contains("INCOME"))
        assertTrue(types.contains("EXPENSE"))
    }
}
