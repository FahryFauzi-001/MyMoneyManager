package com.example.mymoneymanager.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mymoneymanager.ui.model.UiCategoryStatistic
import com.example.mymoneymanager.ui.model.UiMonthlyTrend
import com.example.mymoneymanager.ui.model.UiStatistics
import com.example.mymoneymanager.ui.util.MoneyFormat

private val ForestStart = Color(0xFF124328)
private val ForestEnd = Color(0xFF1E5E3A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    statistics: UiStatistics,
    currencySymbol: String
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { StatisticsTopBar() }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                MonthlyExpenseHeroCard(
                    statistics = statistics,
                    currencySymbol = currencySymbol
                )
            }
            item {
                CategoryDonutCard(
                    statistics = statistics,
                    currencySymbol = currencySymbol
                )
            }
            if (statistics.categoryStatistics.isNotEmpty()) {
                item {
                    CategoryBreakdownCard(
                        statistics = statistics,
                        currencySymbol = currencySymbol
                    )
                }
            }
            item {
                MonthlyTrendCard(
                    statistics = statistics,
                    currencySymbol = currencySymbol
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatisticsTopBar() {
    TopAppBar(
        title = {
            Text(
                text = "Analisis & Statistik",
                fontWeight = FontWeight.Bold
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        )
    )
}

@Composable
private fun MonthlyExpenseHeroCard(
    statistics: UiStatistics,
    currencySymbol: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(ForestStart, ForestEnd)
                    )
                )
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Pengeluaran Bulan Ini • ${statistics.monthLabel}",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.85f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = MoneyFormat.format(
                        amountMinor = statistics.totalExpenseMinor,
                        symbol = currencySymbol,
                        showSign = false
                    ),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.16f),
                    contentColor = Color.White
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (statistics.isPositiveComparison) {
                                Icons.AutoMirrored.Filled.TrendingDown
                            } else {
                                Icons.AutoMirrored.Filled.TrendingUp
                            },
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = statistics.comparisonText,
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryDonutCard(
    statistics: UiStatistics,
    currencySymbol: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Komposisi Kategori",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(18.dp))

            if (statistics.categoryStatistics.isEmpty()) {
                Text(
                    text = "Belum ada pengeluaran tercatat di bulan ini. Catat transaksi atau muat data demo di Beranda.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Box(
                        modifier = Modifier.size(152.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CategoryDonutChart(categoryStatistics = statistics.categoryStatistics)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Total",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = MoneyFormat.formatCompact(
                                    amountMinor = statistics.categoryStatistics.sumOf { it.amountMinor },
                                    symbol = currencySymbol
                                ),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        statistics.categoryStatistics.forEach { statistic ->
                            DonutLegendItem(
                                emoji = statistic.category.iconEmoji,
                                label = statistic.category.name,
                                percentage = statistic.percentage,
                                color = statistic.category.colorHex.toColor()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryDonutChart(categoryStatistics: List<UiCategoryStatistic>) {
    val total = categoryStatistics.map { it.percentage }.sum().coerceAtLeast(0.01f)

    Canvas(modifier = Modifier.fillMaxSize()) {
        val stroke = 24.dp.toPx()
        val inset = stroke / 2f
        val arcSize = Size(width = size.width - stroke, height = size.height - stroke)

        var startAngle = -90f
        categoryStatistics.forEach { statistic ->
            val sweepAngle = (statistic.percentage / total) * 360f
            drawArc(
                color = statistic.category.colorHex.toColor(),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Butt)
            )
            startAngle += sweepAngle
        }
    }
}

@Composable
private fun DonutLegendItem(
    emoji: String,
    label: String,
    percentage: Float,
    color: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color = color, shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$emoji $label",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "${(percentage * 100).toInt()}%",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CategoryBreakdownCard(
    statistics: UiStatistics,
    currencySymbol: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Rincian Kategori",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            statistics.categoryStatistics.forEach { statistic ->
                CategoryProgressRow(
                    emoji = statistic.category.iconEmoji,
                    label = statistic.category.name,
                    amount = MoneyFormat.format(
                        amountMinor = statistic.amountMinor,
                        symbol = currencySymbol,
                        showSign = false
                    ),
                    percentage = statistic.percentage,
                    color = statistic.category.colorHex.toColor()
                )
            }
        }
    }
}

@Composable
private fun CategoryProgressRow(
    emoji: String,
    label: String,
    amount: String,
    percentage: Float,
    color: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$emoji $label",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "$amount (${(percentage * 100).toInt()}%)",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        LinearProgressIndicator(
            progress = { percentage.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(50)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    }
}

@Composable
private fun MonthlyTrendCard(
    statistics: UiStatistics,
    currencySymbol: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Tren Pengeluaran 6 Bulan",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Bulan berjalan ditandai warna hijau tebal",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(20.dp))
            MonthlyTrendChart(
                monthlyTrend = statistics.monthlyTrend,
                currencySymbol = currencySymbol
            )
        }
    }
}

@Composable
private fun MonthlyTrendChart(
    monthlyTrend: List<UiMonthlyTrend>,
    currencySymbol: String
) {
    val maxValue = monthlyTrend.maxOfOrNull { it.amountMinor }?.coerceAtLeast(1L) ?: 1L

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(152.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            monthlyTrend.forEach { trend ->
                val ratio = trend.amountMinor.toFloat() / maxValue.toFloat()
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = MoneyFormat.formatCompact(trend.amountMinor, currencySymbol),
                        fontSize = 10.sp,
                        fontWeight = if (trend.isCurrentMonth) FontWeight.Bold else FontWeight.Medium,
                        color = if (trend.isCurrentMonth) ForestEnd else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Clip,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(118.dp * ratio.coerceIn(0.06f, 1f))
                            .background(
                                color = if (trend.isCurrentMonth) ForestEnd else Color(0xFFA7D3BA),
                                shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)
                            )
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            monthlyTrend.forEach { trend ->
                Text(
                    text = trend.monthLabel,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (trend.isCurrentMonth) FontWeight.Bold else FontWeight.Normal,
                    color = if (trend.isCurrentMonth) ForestEnd else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
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
