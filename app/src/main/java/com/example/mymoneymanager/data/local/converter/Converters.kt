package com.example.mymoneymanager.data.local.converter

import androidx.room.TypeConverter
import com.example.mymoneymanager.ui.model.UiAccountType
import com.example.mymoneymanager.ui.model.UiTransactionType
import java.time.LocalDate

class Converters {

    @TypeConverter
    fun localDateToEpochDay(date: LocalDate?): Long? = date?.toEpochDay()

    @TypeConverter
    fun epochDayToLocalDate(epochDay: Long?): LocalDate? = epochDay?.let(LocalDate::ofEpochDay)

    @TypeConverter
    fun transactionTypeToString(type: UiTransactionType?): String? = type?.name

    @TypeConverter
    fun stringToTransactionType(value: String?): UiTransactionType? =
        value?.let { enumValueOf<UiTransactionType>(it) }

    @TypeConverter
    fun accountTypeToString(type: UiAccountType?): String? = type?.name

    @TypeConverter
    fun stringToAccountType(value: String?): UiAccountType? =
        value?.let { enumValueOf<UiAccountType>(it) }
}
