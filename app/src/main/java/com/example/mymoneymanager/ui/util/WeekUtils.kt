package com.example.mymoneymanager.ui.util

import com.example.mymoneymanager.ui.model.UiWeeklyDay
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

fun buildWeeklyDays(
    selectedDate: LocalDate,
    today: LocalDate = LocalDate.now()
): List<UiWeeklyDay> {
    val monday = selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    return (0L..6L).map { offset ->
        val date = monday.plusDays(offset)
        UiWeeklyDay(
            date = date,
            shortLabel = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
            isSelected = date == selectedDate,
            isToday = date == today
        )
    }
}
