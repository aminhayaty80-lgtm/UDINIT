package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.Customer
import com.example.data.DebtRecord
import com.example.ui.MainViewModel
import com.example.ui.components.AddCallLogDialog
import com.example.ui.components.AddDebtDialog
import com.example.ui.components.AddEditCustomerDialog
import com.example.ui.components.AddPaymentFollowUpDialog
import com.example.ui.components.AddReminderDialog
import com.example.ui.components.CustomerDetailsSheet
import com.example.ui.components.CustomerFinancialLedgerSheet
import com.example.ui.components.ReceiptImageViewerDialog
import com.example.ui.components.SettleDebtDialog
import com.example.ui.screens.ArchiveScreen
import com.example.ui.screens.CustomersScreen
import com.example.ui.screens.FinancialStatusScreen
import com.example.ui.screens.FollowUpsScreen
import com.example.ui.screens.FunnelAnalyticsScreen
import com.example.ui.screens.SalesCoachScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.util.CurrencyHelper
import com.example.util.PersianCalendarHelper
import kotlinx.coroutines.launch

sealed class CrmNavTab(val index: Int, val title: String, val icon: ImageVector) {
    data object Customers : CrmNavTab(0, "مشتریان", Icons.Default.People)
    data object Financial : CrmNavTab(1, "وضعیت مالی", Icons.Default.AccountBalanceWallet)
    data object FollowUps : CrmNavTab(2, "پیگیری‌ها", Icons.Default.NotificationsActive)
    data object Analytics : CrmNavTab(3, "مسیر بازاریابی", Icons.Default.Insights)
    data object Coach : CrmNavTab(4, "آموزش و لید", Icons.Default.Psychology)
    data object Archive : CrmNavTab(5, "بایگانی", Icons.Default.Archive)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Persian language RTL orientation
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    CrmAppContent()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrmAppContent(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Request notification permission on Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // State collections
    val filteredCustomers by viewModel.filteredCustomers.collectAsState()
    val activeCustomers by viewModel.activeCustomers.collectAsState()
    val archivedCustomers by viewModel.archivedCustomers.collectAsState()
    val staleCustomers by viewModel.staleCustomers.collectAsState()
    val reminders by viewModel.reminders.collectAsState()
    val totalCallsCount by viewModel.totalCallsCount.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedStatus by viewModel.selectedStatusFilter.collectAsState()

    // Financial Data State
    val customerFinancialSummaries by viewModel.customerFinancialSummaries.collectAsState()
    val allDebts by viewModel.allDebts.collectAsState()
    val allPaymentFollowUps by viewModel.allPaymentFollowUps.collectAsState()
    val totalDebtAmount by viewModel.totalDebtAmount.collectAsState()
    val totalActiveDebt by viewModel.totalActiveDebtAmount.collectAsState()
    val totalSettledDebt by viewModel.totalSettledAmount.collectAsState()

    val selectedCustomerForDetails by viewModel.selectedCustomer.collectAsState()
    val selectedCustomerLogs by viewModel.selectedCustomerLogs.collectAsState()

    val coachingAnalysisResult by viewModel.coachingAnalysisResult.collectAsState()
    val isAnalyzingCall by viewModel.isAnalyzingCall.collectAsState()

    // Navigation state
    var currentTab by remember { mutableIntStateOf(0) }

    // Dialog states
    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var customerToEdit by remember { mutableStateOf<Customer?>(null) }
    var customerForNewCallLog by remember { mutableStateOf<Customer?>(null) }
    var customerForNewReminder by remember { mutableStateOf<Customer?>(null) }
    var showStandaloneReminderDialog by remember { mutableStateOf(false) }

    // Financial dialog states
    var customerForNewDebt by remember { mutableStateOf<Customer?>(null) }
    var showAddDebtStandaloneDialog by remember { mutableStateOf(false) }
    var customerForNewPaymentFollowUp by remember { mutableStateOf<Customer?>(null) }
    var showAddPaymentFollowUpStandaloneDialog by remember { mutableStateOf(false) }
    var customerForLedgerSheet by remember { mutableStateOf<Customer?>(null) }
    var debtToSettleInSheet by remember { mutableStateOf<DebtRecord?>(null) }
    var receiptImageToViewInSheet by remember { mutableStateOf<Pair<String, DebtRecord>?>(null) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "udinit",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                val tabs = listOf(
                    CrmNavTab.Customers,
                    CrmNavTab.Financial,
                    CrmNavTab.FollowUps,
                    CrmNavTab.Analytics,
                    CrmNavTab.Coach,
                    CrmNavTab.Archive
                )

                tabs.forEach { tab ->
                    val isSelected = currentTab == tab.index
                    val hasBadge = when (tab) {
                        CrmNavTab.FollowUps -> staleCustomers.isNotEmpty() || reminders.isNotEmpty()
                        CrmNavTab.Financial -> customerFinancialSummaries.any { it.activeDebtToman > 0 }
                        else -> false
                    }
                    val badgeCount = when (tab) {
                        CrmNavTab.FollowUps -> staleCustomers.size + reminders.size
                        CrmNavTab.Financial -> customerFinancialSummaries.count { it.activeDebtToman > 0 }
                        else -> 0
                    }

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab.index },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 9.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        icon = {
                            if (hasBadge) {
                                BadgedBox(
                                    badge = {
                                        Badge(
                                            containerColor = if (tab == CrmNavTab.Financial) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.error
                                        ) {
                                            Text(PersianCalendarHelper.toPersianDigits(badgeCount.toString()))
                                        }
                                    }
                                ) {
                                    Icon(tab.icon, contentDescription = tab.title, modifier = Modifier.size(20.dp))
                                }
                            } else {
                                Icon(tab.icon, contentDescription = tab.title, modifier = Modifier.size(20.dp))
                            }
                        },
                        modifier = Modifier.testTag("nav_tab_${tab.index}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                0 -> CustomersScreen(
                    customers = filteredCustomers,
                    staleCount = staleCustomers.size,
                    searchQuery = searchQuery,
                    selectedStatus = selectedStatus,
                    onSearchChange = { viewModel.setSearchQuery(it) },
                    onStatusSelect = { viewModel.setStatusFilter(it) },
                    onCustomerClick = { viewModel.selectCustomer(it) },
                    onAddCallLog = { customerForNewCallLog = it },
                    onEditCustomer = { customerToEdit = it },
                    onToggleArchive = { customer ->
                        viewModel.archiveCustomer(customer.id, !customer.isArchived)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("مشتری ${if (!customer.isArchived) "به بایگانی منتقل شد" else "از بایگانی خارج شد"}")
                        }
                    },
                    onDeleteCustomer = { customer ->
                        viewModel.deleteCustomer(customer.id)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("مشتری «${customer.companyName}» حذف شد")
                        }
                    },
                    onAddNewCustomer = { showAddCustomerDialog = true },
                    onNavigateToStaleTab = { currentTab = 2 }
                )

                1 -> FinancialStatusScreen(
                    summaries = customerFinancialSummaries,
                    allDebts = allDebts,
                    allFollowUps = allPaymentFollowUps,
                    totalDebtAmount = totalDebtAmount,
                    totalActiveDebt = totalActiveDebt,
                    totalSettledDebt = totalSettledDebt,
                    onOpenCustomerLedger = { customer -> customerForLedgerSheet = customer },
                    onAddDebtForCustomer = { customer ->
                        if (customer != null) customerForNewDebt = customer
                        else showAddDebtStandaloneDialog = true
                    },
                    onAddFollowUpForCustomer = { customer ->
                        if (customer != null) customerForNewPaymentFollowUp = customer
                        else showAddPaymentFollowUpStandaloneDialog = true
                    },
                    onToggleDebtSettled = { debt ->
                        viewModel.toggleDebtSettled(debt.id, !debt.isSettled)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(if (!debt.isSettled) "بدهی به عنوان تسویه‌شده علامت‌گذاری شد" else "بدهی مجدداً فعال شد")
                        }
                    },
                    onSettleDebtWithProof = { debt, note, imageUri ->
                        viewModel.settleDebtWithProof(debt.id, note, imageUri)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("بدهی با موفقیت تسویه و به بایگانی تسویه‌ها منتقل شد")
                        }
                    },
                    onDeleteDebt = { debt ->
                        viewModel.deleteDebt(debt.id)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("رکورد بدهی حذف گردید")
                        }
                    },
                    onDeleteFollowUp = { followUp ->
                        viewModel.deletePaymentFollowUp(followUp.id)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("پیگیری پرداختی حذف شد")
                        }
                    }
                )

                2 -> FollowUpsScreen(
                    staleCustomers = staleCustomers,
                    reminders = reminders,
                    onAddCallLog = { customerForNewCallLog = it },
                    onDeleteCustomer = { customer ->
                        viewModel.deleteCustomer(customer.id)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("مشتری حذف گردید")
                        }
                    },
                    onOpenCustomerDetails = { viewModel.selectCustomer(it) },
                    onAddReminderForCustomer = { customerForNewReminder = it },
                    onAddStandaloneReminder = { showStandaloneReminderDialog = true },
                    onCompleteReminder = { id ->
                        viewModel.completeReminder(id)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("یادآوری انجام شد")
                        }
                    },
                    onDeleteReminder = { id ->
                        viewModel.deleteReminder(id)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("یادآوری حذف شد")
                        }
                    },
                    onTestNotification = {
                        viewModel.triggerTestNotification(
                            title = "تست یادآور بازاریابی",
                            message = "زمان تماس مجدد و پیگیری قرارداد فرا رسیده است!"
                        )
                        Toast.makeText(context, "اعلان آزمایشی ارسال شد", Toast.LENGTH_SHORT).show()
                    }
                )

                3 -> FunnelAnalyticsScreen(
                    activeCustomers = activeCustomers,
                    archivedCustomers = archivedCustomers,
                    staleCount = staleCustomers.size,
                    totalCallsCount = totalCallsCount
                )

                4 -> SalesCoachScreen(
                    analysisResult = coachingAnalysisResult,
                    isAnalyzing = isAnalyzingCall,
                    onAnalyzeCall = { viewModel.analyzeCallNotes(it) },
                    onClearAnalysis = { viewModel.clearCoachingResult() },
                    onAddSampleLead = { company, person, job, industry, phone, notes ->
                        viewModel.saveCustomer(
                            companyName = company,
                            contactName = person,
                            jobTitle = job,
                            industry = industry,
                            phoneNumber = phone,
                            notes = notes
                        )
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("لید «$company» به لیست مشتریان اضافه شد")
                        }
                    }
                )

                5 -> ArchiveScreen(
                    archivedCustomers = archivedCustomers,
                    onRestoreCustomer = { customer ->
                        viewModel.archiveCustomer(customer.id, false)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("«${customer.companyName}» به مشتریان فعال بازگردانده شد")
                        }
                    },
                    onDeleteCustomer = { customer ->
                        viewModel.deleteCustomer(customer.id)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("مشتری به طور قطعی حذف شد")
                        }
                    },
                    onViewCustomer = { viewModel.selectCustomer(it) }
                )
            }
        }
    }

    // Modal Sheet: Customer Full Details & Call History
    selectedCustomerForDetails?.let { customer ->
        val customerDebts = remember(allDebts, customer.id) { allDebts.filter { it.customerId == customer.id } }
        val customerFollowUps = remember(allPaymentFollowUps, customer.id) { allPaymentFollowUps.filter { it.customerId == customer.id } }

        CustomerDetailsSheet(
            customer = customer,
            logs = selectedCustomerLogs,
            customerDebts = customerDebts,
            customerFollowUps = customerFollowUps,
            onDismiss = { viewModel.selectCustomer(null) },
            onAddCallLog = { customerForNewCallLog = customer },
            onAddReminder = { customerForNewReminder = customer },
            onOpenFinancialLedger = { customerForLedgerSheet = customer },
            onAddDebt = { customerForNewDebt = customer },
            onAddPaymentFollowUp = { customerForNewPaymentFollowUp = customer },
            onStatusChange = { newStatus ->
                viewModel.updateCustomerStatus(customer.id, newStatus)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("مرحله مشتری به «${newStatus.labelFa}» تغییر یافت")
                }
            },
            onToggleArchive = {
                viewModel.archiveCustomer(customer.id, !customer.isArchived)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(if (!customer.isArchived) "انتقال به بایگانی انجام شد" else "از بایگانی خارج شد")
                }
            },
            onDeleteLog = { log ->
                viewModel.deleteCallLog(log)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("گزارش تماس حذف شد")
                }
            }
        )
    }

    // Modal Sheet: Full Customer Financial Ledger
    customerForLedgerSheet?.let { customer ->
        val debtsForCustomer = remember(allDebts, customer.id) {
            allDebts.filter { it.customerId == customer.id }
        }
        val followUpsForCustomer = remember(allPaymentFollowUps, customer.id) {
            allPaymentFollowUps.filter { it.customerId == customer.id }
        }

        CustomerFinancialLedgerSheet(
            customer = customer,
            debts = debtsForCustomer,
            followUps = followUpsForCustomer,
            onDismiss = { customerForLedgerSheet = null },
            onAddDebt = { customerForNewDebt = customer },
            onAddFollowUp = { customerForNewPaymentFollowUp = customer },
            onSettleDebt = { debt ->
                debtToSettleInSheet = debt
            },
            onViewReceipt = { uri, debt ->
                receiptImageToViewInSheet = Pair(uri, debt)
            },
            onToggleDebtSettled = { debt ->
                viewModel.toggleDebtSettled(debt.id, !debt.isSettled)
            },
            onDeleteDebt = { debt ->
                viewModel.deleteDebt(debt.id)
            },
            onDeleteFollowUp = { followUp ->
                viewModel.deletePaymentFollowUp(followUp.id)
            }
        )
    }

    // Settlement dialog from Customer Financial Ledger Sheet
    debtToSettleInSheet?.let { debt ->
        SettleDebtDialog(
            debt = debt,
            onDismiss = { debtToSettleInSheet = null },
            onConfirmSettlement = { note, receiptUri ->
                viewModel.settleDebtWithProof(debt.id, note, receiptUri)
                debtToSettleInSheet = null
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("بدهی با موفقیت تسویه و به بایگانی تسویه‌ها منتقل شد")
                }
            }
        )
    }

    // Receipt image viewer from Customer Financial Ledger Sheet
    receiptImageToViewInSheet?.let { (uri, debt) ->
        ReceiptImageViewerDialog(
            imageUri = uri,
            title = "سند پرداخت - ${debt.customerCompanyName}",
            subtitle = "${CurrencyHelper.formatToman(debt.amountToman)} - ${debt.settledPersianDateStr ?: ""}",
            onDismiss = { receiptImageToViewInSheet = null }
        )
    }

    // Dialog: Add Debt Record
    if (customerForNewDebt != null || showAddDebtStandaloneDialog) {
        AddDebtDialog(
            customers = activeCustomers,
            initialCustomer = customerForNewDebt,
            onDismiss = {
                customerForNewDebt = null
                showAddDebtStandaloneDialog = false
            },
            onSaveDebt = { customerId, companyName, amount, reason ->
                viewModel.addDebt(
                    customerId = customerId,
                    customerCompanyName = companyName,
                    amountToman = amount,
                    reason = reason
                )
                customerForNewDebt = null
                showAddDebtStandaloneDialog = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("بدهی ${CurrencyHelper.formatToman(amount)} با موفقیت ثبت شد و به جمع بدهی اضافه گردید")
                }
            }
        )
    }

    // Dialog: Add Payment Follow-Up with auto Persian Date
    if (customerForNewPaymentFollowUp != null || showAddPaymentFollowUpStandaloneDialog) {
        AddPaymentFollowUpDialog(
            customers = activeCustomers,
            initialCustomer = customerForNewPaymentFollowUp,
            onDismiss = {
                customerForNewPaymentFollowUp = null
                showAddPaymentFollowUpStandaloneDialog = false
            },
            onSaveFollowUp = { customerId, companyName, notes ->
                viewModel.addPaymentFollowUp(
                    customerId = customerId,
                    customerCompanyName = companyName,
                    notes = notes
                )
                customerForNewPaymentFollowUp = null
                showAddPaymentFollowUpStandaloneDialog = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("پیگیری پرداختی با تاریخ امروز با موفقیت ثبت شد")
                }
            }
        )
    }

    // Dialog: Add / Edit Customer
    if (showAddCustomerDialog || customerToEdit != null) {
        AddEditCustomerDialog(
            customerToEdit = customerToEdit,
            onDismiss = {
                showAddCustomerDialog = false
                customerToEdit = null
            },
            onSave = { companyName, contactName, jobTitle, industry, phoneNumber, email, notes, status ->
                viewModel.saveCustomer(
                    id = customerToEdit?.id ?: 0L,
                    companyName = companyName,
                    contactName = contactName,
                    jobTitle = jobTitle,
                    industry = industry,
                    phoneNumber = phoneNumber,
                    email = email,
                    notes = notes,
                    status = status
                )
                showAddCustomerDialog = false
                customerToEdit = null
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("اطلاعات مشتری با موفقیت ذخیره شد")
                }
            }
        )
    }

    // Dialog: Add Call Log with auto Persian Date
    customerForNewCallLog?.let { customer ->
        AddCallLogDialog(
            customer = customer,
            onDismiss = { customerForNewCallLog = null },
            onSaveLog = { resultText, outcome ->
                viewModel.addCallLog(customer.id, resultText, outcome)
                customerForNewCallLog = null
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("نتیجه تماس با تاریخ شمسی خودکار ثبت شد")
                }
            }
        )
    }

    // Dialog: Add Reminder (for a specific customer or standalone)
    if (customerForNewReminder != null || showStandaloneReminderDialog) {
        AddReminderDialog(
            initialCustomer = customerForNewReminder,
            onDismiss = {
                customerForNewReminder = null
                showStandaloneReminderDialog = false
            },
            onSaveReminder = { customerId, customerName, title, message, triggerTime ->
                viewModel.addReminder(
                    customerId = customerId,
                    customerName = customerName,
                    title = title,
                    message = message,
                    remindAtMillis = triggerTime
                )
                customerForNewReminder = null
                showStandaloneReminderDialog = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("یادآور با موفقیت فعال و زمان‌بندی شد")
                }
            }
        )
    }
}
