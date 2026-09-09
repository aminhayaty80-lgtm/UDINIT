package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Customer
import com.example.data.CustomerStatus
import com.example.ui.theme.StaleAlertColor
import com.example.ui.theme.StatusContactedColor
import com.example.ui.theme.StatusLostColor
import com.example.ui.theme.StatusNegotiationColor
import com.example.ui.theme.StatusNewColor
import com.example.ui.theme.StatusWonColor
import com.example.util.PersianCalendarHelper

@Composable
fun FunnelAnalyticsScreen(
    activeCustomers: List<Customer>,
    archivedCustomers: List<Customer>,
    staleCount: Int,
    totalCallsCount: Int,
    modifier: Modifier = Modifier
) {
    val totalActive = activeCustomers.size
    val totalAll = totalActive + archivedCustomers.size

    val newCount = activeCustomers.count { it.statusEnum == CustomerStatus.NEW }
    val contactedCount = activeCustomers.count { it.statusEnum == CustomerStatus.CONTACTED }
    val negotiationCount = activeCustomers.count { it.statusEnum == CustomerStatus.NEGOTIATION }
    val wonCount = activeCustomers.count { it.statusEnum == CustomerStatus.WON }
    val lostCount = activeCustomers.count { it.statusEnum == CustomerStatus.LOST } + archivedCustomers.size

    val conversionRate = if (totalAll > 0) {
        (wonCount.toFloat() / totalAll.toFloat()) * 100f
    } else 0f

    // Industry breakdown
    val industryGroups = remember(activeCustomers) {
        activeCustomers.groupBy { it.industry.ifBlank { "سایر حوزه‌ها" } }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top KPI Cards Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KpiCard(
                title = "نرخ تبدیل فروش",
                value = "${PersianCalendarHelper.toPersianDigits(String.format("%.1f", conversionRate))}٪",
                subtitle = "از کل لیدها",
                icon = Icons.Default.TrendingUp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            KpiCard(
                title = "معاملات موفق",
                value = PersianCalendarHelper.toPersianDigits(wonCount.toString()),
                subtitle = "قرارداد بسته شده",
                icon = Icons.Default.CheckCircle,
                color = StatusWonColor,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KpiCard(
                title = "کل مشتریان فعال",
                value = PersianCalendarHelper.toPersianDigits(totalActive.toString()),
                subtitle = "در پایگاه بازاریابی",
                icon = Icons.Default.People,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
            KpiCard(
                title = "کل تماس‌ها",
                value = PersianCalendarHelper.toPersianDigits(totalCallsCount.toString()),
                subtitle = "مکالمه ثبت شده",
                icon = Icons.Default.Call,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
        }

        // Funnel Visualizer Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Insights,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "نمودار قیف بازاریابی (Sales Funnel)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "مسیر تبدیل یک مخاطب از لید اولیه تا بستن معامله موفق:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Funnel Bars
                FunnelBar(
                    label = "۱. لیدهای جدید (New Leads)",
                    count = newCount,
                    total = totalActive,
                    color = StatusNewColor,
                    widthFraction = 1.0f
                )
                FunnelBar(
                    label = "۲. تماس اولیه و ارزیابی (Contacted)",
                    count = contactedCount,
                    total = totalActive,
                    color = StatusContactedColor,
                    widthFraction = 0.85f
                )
                FunnelBar(
                    label = "۳. مذاکره و ارسال پیش‌فاکتور (Negotiation)",
                    count = negotiationCount,
                    total = totalActive,
                    color = StatusNegotiationColor,
                    widthFraction = 0.65f
                )
                FunnelBar(
                    label = "۴. معامله موفق و بستن فروش (Won)",
                    count = wonCount,
                    total = totalActive,
                    color = StatusWonColor,
                    widthFraction = 0.45f
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // Lost/Cold leads row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(StatusLostColor, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "لیدهای سرد و انصرافی:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "${PersianCalendarHelper.toPersianDigits(lostCount.toString())} مشتری",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = StatusLostColor
                    )
                }
            }
        }

        // Industry Breakdown
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.PieChart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "تفکیک مشتریان بر اساس حوزه فعالیت",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (industryGroups.isEmpty()) {
                    Text(
                        text = "هنوز حوزه‌ای ثبت نشده است.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                } else {
                    industryGroups.take(6).forEach { (industry, count) ->
                        val pct = if (totalActive > 0) (count.toFloat() / totalActive.toFloat()) else 0f
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = industry,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${PersianCalendarHelper.toPersianDigits(count.toString())} مشتری (${PersianCalendarHelper.toPersianDigits(String.format("%.0f", pct * 100))}٪)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(pct.coerceIn(0.05f, 1f))
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.secondary)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Strategic Marketer Insights & Tips
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "راهبرد پیشنهادی برای رشد فروش شما:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (staleCount > 0) {
                        Text(
                            text = "• شما ${PersianCalendarHelper.toPersianDigits(staleCount.toString())} مشتری رها شده بالای ۲ هفته دارید. برقراری تماس سریع با آنها می‌تواند فوراً تا ۲۰٪ نرخ تبدیل را بالا ببرد.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (negotiationCount > wonCount) {
                        Text(
                            text = "• بیشترین ریزش شما در مرحله «مذاکره» است. ارسال پیشنهاد با محدودیت زمانی و تضمین بازگشت وجه، سرعت نهایی‌سازی قرارداد را دوبرابر می‌کند.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        Text(
                            text = "• روند بستن معامله مطلوب است. روی ورودی لیدهای جدید از اصناف پربازده تمرکز کنید.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FunnelBar(
    label: String,
    count: Int,
    total: Int,
    color: Color,
    widthFraction: Float
) {
    val pct = if (total > 0) (count.toFloat() / total.toFloat()) * 100f else 0f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${PersianCalendarHelper.toPersianDigits(count.toString())} (${PersianCalendarHelper.toPersianDigits(String.format("%.0f", pct))}٪)",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(widthFraction)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .background(color)
            )
        }
    }
}

@Composable
fun KpiCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(color.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}
