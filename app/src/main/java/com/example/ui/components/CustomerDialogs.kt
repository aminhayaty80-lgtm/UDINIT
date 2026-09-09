package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CallOutcome
import com.example.data.Customer
import com.example.data.CustomerStatus
import com.example.util.PersianCalendarHelper

@Composable
fun AddEditCustomerDialog(
    customerToEdit: Customer? = null,
    onDismiss: () -> Unit,
    onSave: (
        companyName: String,
        contactName: String,
        jobTitle: String,
        industry: String,
        phoneNumber: String,
        email: String,
        notes: String,
        status: CustomerStatus
    ) -> Unit
) {
    var companyName by remember { mutableStateOf(customerToEdit?.companyName ?: "") }
    var contactName by remember { mutableStateOf(customerToEdit?.contactName ?: "") }
    var jobTitle by remember { mutableStateOf(customerToEdit?.jobTitle ?: "") }
    var industry by remember { mutableStateOf(customerToEdit?.industry ?: "") }
    var phoneNumber by remember { mutableStateOf(customerToEdit?.phoneNumber ?: "") }
    var email by remember { mutableStateOf(customerToEdit?.email ?: "") }
    var notes by remember { mutableStateOf(customerToEdit?.notes ?: "") }
    var status by remember { mutableStateOf(customerToEdit?.statusEnum ?: CustomerStatus.NEW) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (customerToEdit == null) "افزودن مشتری جدید" else "ویرایش اطلاعات مشتری",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                OutlinedTextField(
                    value = companyName,
                    onValueChange = { companyName = it; errorMessage = null },
                    label = { Text("نام شرکت یا سازمان *") },
                    leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_company_name"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = contactName,
                    onValueChange = { contactName = it; errorMessage = null },
                    label = { Text("نام شخص رابط *") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_contact_name"),
                    singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = jobTitle,
                        onValueChange = { jobTitle = it },
                        label = { Text("سمت شخص") },
                        leadingIcon = { Icon(Icons.Default.Work, contentDescription = null) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_job_title"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = industry,
                        onValueChange = { industry = it },
                        label = { Text("حوزه فعالیت") },
                        leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_industry"),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it; errorMessage = null },
                    label = { Text("شماره تماس *") },
                    leadingIcon = { Icon(Icons.Default.Call, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_phone_number"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("ایمیل (اختیاری)") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("توضیحات و نیازمندی اولیه") },
                    leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3
                )

                // Status picker
                Text(
                    text = "وضعیت در قیف فروش:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    CustomerStatus.entries.forEach { st ->
                        val isSelected = status == st
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { status = st },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = st.labelFa.split(" ").first(),
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (companyName.isBlank() || contactName.isBlank() || phoneNumber.isBlank()) {
                        errorMessage = "لطفاً نام شرکت، نام شخص و شماره تماس را وارد کنید."
                        return@Button
                    }
                    onSave(
                        companyName,
                        contactName,
                        jobTitle,
                        industry,
                        phoneNumber,
                        email,
                        notes,
                        status
                    )
                },
                modifier = Modifier.testTag("save_customer_button")
            ) {
                Text("ذخیره مشتری")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("انصراف")
            }
        }
    )
}

@Composable
fun AddCallLogDialog(
    customer: Customer,
    onDismiss: () -> Unit,
    onSaveLog: (resultText: String, outcome: CallOutcome) -> Unit
) {
    var resultText by remember { mutableStateOf("") }
    var selectedOutcome by remember { mutableStateOf(CallOutcome.SUCCESSFUL) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Automatic Persian current date & time preview
    val currentPersianTime = remember {
        PersianCalendarHelper.formatToPersianDateTime(System.currentTimeMillis())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "ثبت نتیجه تماس با مشتری",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "${customer.companyName} (${customer.contactName})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
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
                // Solar Timestamp Banner
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📅 تاریخ شمسی ثبت خودکار:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = currentPersianTime,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
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
                    value = resultText,
                    onValueChange = { resultText = it; errorMessage = null },
                    label = { Text("نتیجه مکالمه و توافقات تماس *") },
                    placeholder = { Text("مثال: درباره تخفیف و نحوه پرداخت صحبت شد، قرار شد فردا ساعت ۱۰ پیش‌فاکتور بفرستم...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_call_result"),
                    minLines = 3,
                    maxLines = 5
                )

                Text(
                    text = "نتیجه کلی تماس:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                CallOutcome.entries.forEach { outcome ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedOutcome = outcome }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedOutcome == outcome,
                            onClick = { selectedOutcome = outcome }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = outcome.labelFa,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (resultText.isBlank()) {
                        errorMessage = "لطفاً شرح نتیجه تماس را وارد فرمایید."
                        return@Button
                    }
                    onSaveLog(resultText, selectedOutcome)
                },
                modifier = Modifier.testTag("submit_call_log_button")
            ) {
                Text("ثبت و درج تاریخ شمسی")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("انصراف")
            }
        }
    )
}
