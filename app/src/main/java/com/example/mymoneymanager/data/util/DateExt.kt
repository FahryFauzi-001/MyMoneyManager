package com.example.mymoneymanager.data.util

import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset

fun LocalDate.toEpochMillisUtc(): Long =
    atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

fun Long.toLocalDateUtc(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

fun YearMonth.startMillis(): Long = atDay(1).toEpochMillisUtc()

fun YearMonth.endMillis(): Long = atEndOfMonth().toEpochMillisUtc()
