package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.CallLog
import com.example.data.CallOutcome
import com.example.data.Customer
import com.example.data.CustomerRepository
import com.example.data.CustomerStatus
import com.example.data.CustomerFinancialSummary
import com.example.data.DebtRecord
import com.example.data.PaymentFollowUp
import com.example.data.Reminder
import com.example.reminder.ReminderManager
import com.example.service.SalesCoachingService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CustomerRepository = CustomerRepository(
        AppDatabase.getInstance(application)
    )

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    val activeCustomers: StateFlow<List<Customer>> = repository.activeCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val archivedCustomers: StateFlow<List<Customer>> = repository.archivedCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val staleCustomers: StateFlow<List<Customer>> = repository.getStaleCustomers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reminders: StateFlow<List<Reminder>> = repository.activeReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalCallsCount: StateFlow<Int> = repository.totalCallsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Financial Data Flows
    val allDebts: StateFlow<List<DebtRecord>> = repository.allDebts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPaymentFollowUps: StateFlow<List<PaymentFollowUp>> = repository.allPaymentFollowUps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customerFinancialSummaries: StateFlow<List<CustomerFinancialSummary>> = combine(
        activeCustomers,
        allDebts,
        allPaymentFollowUps
    ) { customers, debts, followUps ->
        customers.map { customer ->
            val customerDebts = debts.filter { it.customerId == customer.id }
            val customerFollowUps = followUps.filter { it.customerId == customer.id }
            val totalDebt = customerDebts.sumOf { it.amountToman }
            val activeDebt = customerDebts.filter { !it.isSettled }.sumOf { it.amountToman }
            val settledDebt = customerDebts.filter { it.isSettled }.sumOf { it.amountToman }
            val latestFollowUp = customerFollowUps.maxByOrNull { it.timestamp }

            CustomerFinancialSummary(
                customer = customer,
                totalDebtToman = totalDebt,
                activeDebtToman = activeDebt,
                settledDebtToman = settledDebt,
                debtRecordsCount = customerDebts.size,
                latestFollowUp = latestFollowUp,
                followUpsCount = customerFollowUps.size
            )
        }.sortedByDescending { it.activeDebtToman }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalDebtAmount: StateFlow<Long> = allDebts.map { debts ->
        debts.sumOf { it.amountToman }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val totalActiveDebtAmount: StateFlow<Long> = allDebts.map { debts ->
        debts.filter { !it.isSettled }.sumOf { it.amountToman }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val totalSettledAmount: StateFlow<Long> = allDebts.map { debts ->
        debts.filter { it.isSettled }.sumOf { it.amountToman }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    // UI filters & search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedStatusFilter = MutableStateFlow<CustomerStatus?>(null)
    val selectedStatusFilter: StateFlow<CustomerStatus?> = _selectedStatusFilter.asStateFlow()

    // Filtered active customers
    val filteredCustomers: StateFlow<List<Customer>> = combine(
        activeCustomers,
        _searchQuery,
        _selectedStatusFilter
    ) { list, query, statusFilter ->
        list.filter { customer ->
            val matchesQuery = query.isBlank() ||
                    customer.companyName.contains(query, ignoreCase = true) ||
                    customer.contactName.contains(query, ignoreCase = true) ||
                    customer.industry.contains(query, ignoreCase = true) ||
                    customer.phoneNumber.contains(query)
            val matchesStatus = statusFilter == null || customer.status == statusFilter.name
            matchesQuery && matchesStatus
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected customer for Call Logs & Details sheet
    private val _selectedCustomer = MutableStateFlow<Customer?>(null)
    val selectedCustomer: StateFlow<Customer?> = _selectedCustomer.asStateFlow()

    private val _selectedCustomerLogs = MutableStateFlow<List<CallLog>>(emptyList())
    val selectedCustomerLogs: StateFlow<List<CallLog>> = _selectedCustomerLogs.asStateFlow()

    // AI Coaching State
    private val _coachingAnalysisResult = MutableStateFlow<String?>(null)
    val coachingAnalysisResult: StateFlow<String?> = _coachingAnalysisResult.asStateFlow()

    private val _isAnalyzingCall = MutableStateFlow(false)
    val isAnalyzingCall: StateFlow<Boolean> = _isAnalyzingCall.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setStatusFilter(status: CustomerStatus?) {
        _selectedStatusFilter.value = status
    }

    fun selectCustomer(customer: Customer?) {
        _selectedCustomer.value = customer
        if (customer != null) {
            viewModelScope.launch {
                repository.getLogsForCustomer(customer.id).collect { logs ->
                    _selectedCustomerLogs.value = logs
                }
            }
        } else {
            _selectedCustomerLogs.value = emptyList()
        }
    }

    fun saveCustomer(
        id: Long = 0,
        companyName: String,
        contactName: String,
        jobTitle: String,
        industry: String,
        phoneNumber: String,
        email: String = "",
        notes: String = "",
        status: CustomerStatus = CustomerStatus.NEW
    ) {
        viewModelScope.launch {
            val customer = Customer(
                id = id,
                companyName = companyName.trim(),
                contactName = contactName.trim(),
                jobTitle = jobTitle.trim(),
                industry = industry.trim(),
                phoneNumber = phoneNumber.trim(),
                email = email.trim(),
                notes = notes.trim(),
                status = status.name,
                createdAt = if (id == 0L) System.currentTimeMillis() else (_selectedCustomer.value?.createdAt ?: System.currentTimeMillis()),
                lastContactAt = if (id == 0L) System.currentTimeMillis() else (_selectedCustomer.value?.lastContactAt ?: System.currentTimeMillis())
            )
            repository.saveCustomer(customer)
        }
    }

    fun updateCustomerStatus(customerId: Long, newStatus: CustomerStatus) {
        viewModelScope.launch {
            repository.updateStatus(customerId, newStatus.name)
            // If the selected customer is being modified, update it
            if (_selectedCustomer.value?.id == customerId) {
                _selectedCustomer.value = _selectedCustomer.value?.copy(status = newStatus.name)
            }
        }
    }

    fun archiveCustomer(customerId: Long, isArchived: Boolean) {
        viewModelScope.launch {
            repository.setArchived(customerId, isArchived)
            if (_selectedCustomer.value?.id == customerId) {
                _selectedCustomer.value = null
            }
        }
    }

    fun deleteCustomer(customerId: Long) {
        viewModelScope.launch {
            repository.deleteCustomer(customerId)
            if (_selectedCustomer.value?.id == customerId) {
                _selectedCustomer.value = null
            }
        }
    }

    fun addCallLog(
        customerId: Long,
        resultText: String,
        outcome: CallOutcome = CallOutcome.SUCCESSFUL
    ) {
        viewModelScope.launch {
            repository.addCallLog(customerId, resultText, outcome)
            // Refresh selected customer's last contact
            val updated = repository.getCustomerById(customerId)
            if (updated != null && _selectedCustomer.value?.id == customerId) {
                _selectedCustomer.value = updated
            }
        }
    }

    fun deleteCallLog(callLog: CallLog) {
        viewModelScope.launch {
            repository.deleteCallLog(callLog)
        }
    }

    fun addReminder(
        customerId: Long? = null,
        customerName: String = "",
        title: String,
        message: String,
        remindAtMillis: Long
    ) {
        viewModelScope.launch {
            val reminderId = repository.addReminder(
                customerId = customerId,
                customerName = customerName,
                title = title,
                message = message,
                remindAtMillis = remindAtMillis
            )
            ReminderManager.scheduleReminder(
                context = getApplication(),
                reminderId = reminderId,
                title = title,
                message = message,
                customerName = customerName,
                triggerAtMillis = remindAtMillis
            )
        }
    }

    fun completeReminder(reminderId: Long) {
        viewModelScope.launch {
            repository.completeReminder(reminderId)
            ReminderManager.cancelReminder(getApplication(), reminderId)
        }
    }

    fun deleteReminder(reminderId: Long) {
        viewModelScope.launch {
            repository.deleteReminder(reminderId)
            ReminderManager.cancelReminder(getApplication(), reminderId)
        }
    }

    fun triggerTestNotification(title: String, message: String) {
        ReminderManager.showImmediateNotification(
            context = getApplication(),
            title = title,
            message = message
        )
    }

    fun analyzeCallNotes(notes: String) {
        if (notes.isBlank()) return
        viewModelScope.launch {
            _isAnalyzingCall.value = true
            _coachingAnalysisResult.value = null
            val result = SalesCoachingService.analyzeCallText(notes)
            _coachingAnalysisResult.value = result
            _isAnalyzingCall.value = false
        }
    }

    fun clearCoachingResult() {
        _coachingAnalysisResult.value = null
    }

    fun addDebt(customerId: Long, customerCompanyName: String, amountToman: Long, reason: String) {
        viewModelScope.launch {
            repository.addDebtRecord(
                customerId = customerId,
                customerCompanyName = customerCompanyName,
                amountToman = amountToman,
                reason = reason
            )
        }
    }

    fun toggleDebtSettled(debtId: Long, isSettled: Boolean) {
        viewModelScope.launch {
            repository.toggleDebtSettled(debtId, isSettled)
        }
    }

    fun settleDebtWithProof(
        debtId: Long,
        settlementNote: String?,
        receiptImageUri: String?
    ) {
        viewModelScope.launch {
            repository.settleDebtWithProof(
                debtId = debtId,
                settlementNote = settlementNote,
                receiptImageUri = receiptImageUri
            )
        }
    }

    fun deleteDebt(debtId: Long) {
        viewModelScope.launch {
            repository.deleteDebtRecord(debtId)
        }
    }

    /**
     * Records a payment follow-up note with automatic current day & time Persian timestamp
     */
    fun addPaymentFollowUp(customerId: Long, customerCompanyName: String, notes: String) {
        viewModelScope.launch {
            repository.addPaymentFollowUp(
                customerId = customerId,
                customerCompanyName = customerCompanyName,
                resultNotes = notes
            )
        }
    }

    fun deletePaymentFollowUp(followUpId: Long) {
        viewModelScope.launch {
            repository.deletePaymentFollowUp(followUpId)
        }
    }

    fun getCustomerDebts(customerId: Long): Flow<List<DebtRecord>> {
        return repository.getDebtsForCustomer(customerId)
    }

    fun getCustomerPaymentFollowUps(customerId: Long): Flow<List<PaymentFollowUp>> {
        return repository.getFollowUpsForCustomer(customerId)
    }
}
