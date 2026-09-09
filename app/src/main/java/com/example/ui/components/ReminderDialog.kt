package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Customer
import com.example.util.PersianCalendarHelper
import java.util.Calendar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddReminderDialog(
    initialCustomer: Customer? = null,
    onDismiss: () -> Unit,
    onSaveReminder: (
        customerId: Long?,
        customerName: String,
        title: String,
        message: String,
        remindAtMillis: Long
    ) -> Unit
) {
    var title by remember {
        mutableStateOf(
            if (initialCustomer != null) "پیگیری با ${initialCustomer.companyName}" else "یادآور پیگیری بازاریابی"
        )
    }
    var message by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Selected trigger timestamp (defaults to 2 hours from now)
    var triggerAtMillis by remember {
        mutableLongStateOf(System.currentTimeMillis() + 2 * 60 * 60 * 1000L)
    }

    val persianTargetTime = remember(triggerAtMillis) {
        PersianCalendarHelper.formatToPersianDateTime(triggerAtMillis)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "تنظیم یادآور و نوتیفیکیشن",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (initialCustomer != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "مشتری: ${initialCustomer.companyName} (${initialCustomer.contactName})",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; errorMessage = null },
                    label = { Text("عنوان یادآوری *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_reminder_title")
                )

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it; errorMessage = null },
                    label = { Text("متن پیام یادآوری یا ماموریت خاص *") },
                    placeholder = { Text("مثال: بررسی تماس با مهندس رضایی بابت تخفیف ۵ درصدی و امضای نهایی") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth().testTag("input_reminder_message")
                )

                Text(
                    text = "زمان‌بندی سریع اعلان:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val presets = listOf(
                        "۱ ساعت بعد" to (1 * 60 * 60 * 1000L),
                        "۲ ساعت بعد" to (2 * 60 * 60 * 1000L),
                        "فردا صبح (۱۰:۰۰)" to getTomorrowMorningMillis(),
                        "۳ روز بعد" to (3 * 24 * 60 * 60 * 1000L),
                        "۱ هفته بعد" to (7 * 24 * 60 * 60 * 1000L),
                        "۲ هفته بعد (پیگیری دوره‌ای)" to (14 * 24 * 60 * 60 * 1000L)
                    )

                    presets.forEach { (label, durationOrTime) ->
                        val isComputedExact = label.contains("فردا")
                        val targetTime = if (isComputedExact) durationOrTime else System.currentTimeMillis() + durationOrTime
                        val isSelected = Math.abs(triggerAtMillis - targetTime) < 60000

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.clickable {
                                triggerAtMillis = targetTime
                            }
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Solar Date Display for Reminder
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "⏰ زمان ارسال نوتیفیکیشن (تقویم شمسی):",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = persianTargetTime,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank() || message.isBlank()) {
                        errorMessage = "لطفاً عنوان و متن پیام یادآوری را وارد کنید."
                        return@Button
                    }
                    onSaveReminder(
                        initialCustomer?.id,
                        initialCustomer?.companyName ?: "",
                        title,
                        message,
                        triggerAtMillis
                    )
                },
                modifier = Modifier.testTag("save_reminder_button")
            ) {
                Text("فعال‌سازی یادآور")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("انصراف")
            }
        }
    )
}

private fun getTomorrowMorningMillis(): Long {
    val cal = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, 1)
        set(Calendar.HOUR_OF_DAY, 10)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return cal.timeInMillis
}
