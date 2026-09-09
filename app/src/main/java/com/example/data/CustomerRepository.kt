package com.example.data

import com.example.util.PersianCalendarHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class CustomerRepository(private val database: AppDatabase) {
    private val customerDao = database.customerDao()
    private val callLogDao = database.callLogDao()
    private val reminderDao = database.reminderDao()
    private val debtRecordDao = database.debtRecordDao()
    private val paymentFollowUpDao = database.paymentFollowUpDao()

    val activeCustomers: Flow<List<Customer>> = customerDao.getActiveCustomers()
    val archivedCustomers: Flow<List<Customer>> = customerDao.getArchivedCustomers()
    val activeReminders: Flow<List<Reminder>> = reminderDao.getActiveReminders()
    val totalCallsCount: Flow<Int> = callLogDao.getTotalCallsCount()
    val allDebts: Flow<List<DebtRecord>> = debtRecordDao.getAllDebts()
    val allPaymentFollowUps: Flow<List<PaymentFollowUp>> = paymentFollowUpDao.getAllFollowUps()

    fun getStaleCustomers(): Flow<List<Customer>> {
        return customerDao.getStaleCustomers(System.currentTimeMillis())
    }

    fun getDebtsForCustomer(customerId: Long): Flow<List<DebtRecord>> {
        return debtRecordDao.getDebtsForCustomer(customerId)
    }

    fun getFollowUpsForCustomer(customerId: Long): Flow<List<PaymentFollowUp>> {
        return paymentFollowUpDao.getFollowUpsForCustomer(customerId)
    }

    suspend fun addDebtRecord(
        customerId: Long,
        customerCompanyName: String,
        amountToman: Long,
        reason: String,
        timestamp: Long = System.currentTimeMillis()
    ): Long {
        val persianDateStr = PersianCalendarHelper.formatToPersianDateShort(timestamp)
        val debt = DebtRecord(
            customerId = customerId,
            customerCompanyName = customerCompanyName,
            amountToman = amountToman,
            reason = reason.trim(),
            createdAt = timestamp,
            persianDateStr = persianDateStr,
            isSettled = false
        )
        return debtRecordDao.insertDebt(debt)
    }

    suspend fun toggleDebtSettled(debtId: Long, isSettled: Boolean) {
        val settledAt = if (isSettled) System.currentTimeMillis() else null
        val settledPersian = if (isSettled) PersianCalendarHelper.formatToPersianDateTime(settledAt!!) else null
        debtRecordDao.settleDebtWithProof(
            debtId = debtId,
            isSettled = isSettled,
            settledAt = settledAt,
            settledPersianDateStr = settledPersian,
            settlementNote = if (isSettled) "تسویه سریع بدون سند" else null,
            receiptImageUri = null
        )
    }

    suspend fun settleDebtWithProof(
        debtId: Long,
        settlementNote: String?,
        receiptImageUri: String?,
        timestamp: Long = System.currentTimeMillis()
    ) {
        val settledPersian = PersianCalendarHelper.formatToPersianDateTime(timestamp)
        debtRecordDao.settleDebtWithProof(
            debtId = debtId,
            isSettled = true,
            settledAt = timestamp,
            settledPersianDateStr = settledPersian,
            settlementNote = settlementNote?.trim(),
            receiptImageUri = receiptImageUri
        )
    }

    suspend fun deleteDebtRecord(debtId: Long) {
        debtRecordDao.deleteDebtById(debtId)
    }

    /**
     * Adds a payment follow-up note with AUTOMATIC timestamp of the current day & time
     */
    suspend fun addPaymentFollowUp(
        customerId: Long,
        customerCompanyName: String,
        resultNotes: String,
        timestamp: Long = System.currentTimeMillis()
    ): Long {
        val persianDateTimeStr = PersianCalendarHelper.formatToPersianDateTime(timestamp)
        val followUp = PaymentFollowUp(
            customerId = customerId,
            customerCompanyName = customerCompanyName,
            resultNotes = resultNotes.trim(),
            timestamp = timestamp,
            persianDateStr = persianDateTimeStr
        )
        return paymentFollowUpDao.insertFollowUp(followUp)
    }

    suspend fun deletePaymentFollowUp(followUpId: Long) {
        paymentFollowUpDao.deleteFollowUpById(followUpId)
    }

    suspend fun getCustomerById(id: Long): Customer? = customerDao.getCustomerById(id)

    suspend fun saveCustomer(customer: Customer): Long {
        return if (customer.id == 0L) {
            customerDao.insertCustomer(customer)
        } else {
            customerDao.updateCustomer(customer)
            customer.id
        }
    }

    suspend fun updateStatus(customerId: Long, newStatus: String) {
        customerDao.updateStatus(customerId, newStatus)
    }

    suspend fun setArchived(customerId: Long, isArchived: Boolean) {
        customerDao.setArchived(customerId, isArchived)
    }

    suspend fun deleteCustomer(customerId: Long) {
        customerDao.deleteCustomerById(customerId)
    }

    fun getLogsForCustomer(customerId: Long): Flow<List<CallLog>> {
        return callLogDao.getLogsForCustomer(customerId)
    }

    /**
     * Adds a call log, automatically creates Persian date/time timestamp,
     * and updates customer's lastContactAt.
     */
    suspend fun addCallLog(
        customerId: Long,
        resultText: String,
        outcome: CallOutcome = CallOutcome.SUCCESSFUL,
        customTimestamp: Long = System.currentTimeMillis()
    ): Long {
        val persianDateStr = PersianCalendarHelper.formatToPersianDateTime(customTimestamp)
        val log = CallLog(
            customerId = customerId,
            timestamp = customTimestamp,
            persianDateStr = persianDateStr,
            callResult = resultText,
            outcomeType = outcome.name
        )
        val logId = callLogDao.insertCallLog(log)
        customerDao.updateLastContact(customerId, customTimestamp)
        return logId
    }

    suspend fun deleteCallLog(callLog: CallLog) {
        callLogDao.deleteCallLog(callLog)
    }

    suspend fun addReminder(
        customerId: Long?,
        customerName: String,
        title: String,
        message: String,
        remindAtMillis: Long
    ): Long {
        val persianDateStr = PersianCalendarHelper.formatToPersianDateTime(remindAtMillis)
        val reminder = Reminder(
            customerId = customerId,
            customerName = customerName,
            title = title,
            message = message,
            remindAtMillis = remindAtMillis,
            persianDateStr = persianDateStr,
            isCompleted = false
        )
        return reminderDao.insertReminder(reminder)
    }

    suspend fun completeReminder(reminderId: Long) {
        reminderDao.setCompleted(reminderId, true)
    }

    suspend fun deleteReminder(reminderId: Long) {
        reminderDao.deleteReminderById(reminderId)
    }

    /**
     * Populates starter marketing leads so the user immediately experiences
     * all CRM features (solar logs, 14-day stale alerts, reminders, funnel).
     */
    suspend fun seedInitialDataIfEmpty() {
        val existing = activeCustomers.firstOrNull()
        if (existing.isNullOrEmpty()) {
            val now = System.currentTimeMillis()
            val day = 24L * 60 * 60 * 1000L

            // 1. Stale lead (> 16 days ago without call)
            val c1Id = customerDao.insertCustomer(
                Customer(
                    companyName = "شرکت فناوران نوین آریا",
                    contactName = "مهندس علیرضا رضایی",
                    jobTitle = "مدیر بازرگانی و خرید",
                    industry = "فناوری اطلاعات و نرم‌افزار",
                    phoneNumber = "09121112233",
                    email = "rezaei@aryanovin.ir",
                    notes = "علاقه‌مند به پنل سازمانی، نیازمند پیگیری تلفنی تخفیف",
                    status = CustomerStatus.CONTACTED.name,
                    createdAt = now - (20 * day),
                    lastContactAt = now - (16 * day) // Older than 14 days!
                )
            )
            addCallLog(
                c1Id,
                "تماس اولیه با منشی برقرار شد، کاتالوگ و فرم معرفی ایمیل شد.",
                CallOutcome.FOLLOWUP_NEEDED,
                now - (16 * day)
            )

            // 2. Active lead in negotiation
            val c2Id = customerDao.insertCustomer(
                Customer(
                    companyName = "پخش دارویی سپهر سلامت",
                    contactName = "خانم دکتر مریم مهدوی",
                    jobTitle = "معاونت فروش و بازاریابی",
                    industry = "پزشکی و سلامت",
                    phoneNumber = "09355554433",
                    email = "mahdavi@sepehr-pharma.com",
                    notes = "جلسه دمو آنلاین تایید شد، منتظر تایید بودجه هیئت مدیره",
                    status = CustomerStatus.NEGOTIATION.name,
                    createdAt = now - (8 * day),
                    lastContactAt = now - (2 * day)
                )
            )
            addCallLog(
                c2Id,
                "جلسه پرزنت آنلاین با تیم فروش. رضایت کامل داشتند، پیش‌فاکتور ارسال گردید.",
                CallOutcome.SUCCESSFUL,
                now - (2 * day)
            )

            // 3. Won deal
            val c3Id = customerDao.insertCustomer(
                Customer(
                    companyName = "گروه صنعتی پارس فولاد",
                    contactName = "مهندس کامران رستمی",
                    jobTitle = "مدیر ارشد تدارکات",
                    industry = "صنایع فولاد و معدن",
                    phoneNumber = "09128889900",
                    email = "rostami@parssteel.co",
                    notes = "قرارداد سالانه منعقد شد، نیاز به پشتیبانی ماه اول",
                    status = CustomerStatus.WON.name,
                    createdAt = now - (30 * day),
                    lastContactAt = now - (1 * day)
                )
            )
            addCallLog(
                c3Id,
                "امضای نهایی قرارداد همکاری و دریافت چک پیش‌پرداخت.",
                CallOutcome.SUCCESSFUL,
                now - (1 * day)
            )

            // 4. Stale lead 2 (> 18 days ago)
            val c4Id = customerDao.insertCustomer(
                Customer(
                    companyName = "مجتمع ساختمانی برج نگین",
                    contactName = "آقای بهزاد قنبری",
                    jobTitle = "مجری طرح و سرمایه‌گذار",
                    industry = "ساختمان و املاک",
                    phoneNumber = "09124447788",
                    email = "behzad.negin@gmail.com",
                    notes = "در فاز مناقصه تجهیزات هستند. تماس تلفنی پیگیری انجام شود.",
                    status = CustomerStatus.NEW.name,
                    createdAt = now - (18 * day),
                    lastContactAt = now - (18 * day) // Older than 14 days!
                )
            )

            // 5. Archived customer
            val c5Id = customerDao.insertCustomer(
                Customer(
                    companyName = "بازرگانی بین‌الملل خلیج",
                    contactName = "آقای احسان کاظمی",
                    jobTitle = "مدیر عامل",
                    industry = "صادرات و واردات",
                    phoneNumber = "09173336655",
                    email = "kazemi@khalij-trade.com",
                    notes = "فعلاً به دلیل نوسانات ارزی پروژه را متوقف کردند.",
                    status = CustomerStatus.LOST.name,
                    createdAt = now - (45 * day),
                    lastContactAt = now - (25 * day),
                    isArchived = true
                )
            )
            addCallLog(
                c5Id,
                "تماس برای تمدید مذاکره، اعلام کردند تا ۳ ماه آینده خریدی ندارند.",
                CallOutcome.REJECTED,
                now - (25 * day)
            )

            // Add an initial reminder
            addReminder(
                customerId = c2Id,
                customerName = "پخش دارویی سپهر سلامت",
                title = "تماس پیگیری پیش‌فاکتور",
                message = "پیگیری با خانم دکتر مهدوی بابت تایید پیش‌فاکتور ارسالی",
                remindAtMillis = now + (2 * 60 * 60 * 1000L) // 2 hours from now
            )

            // Seed initial debts and payment follow-ups
            addDebtRecord(
                customerId = c1Id,
                customerCompanyName = "شرکت فناوران نوین آریا",
                amountToman = 108_000_000L,
                reason = "بابت کمپین 1405/05/12",
                timestamp = now - (5 * day)
            )
            addDebtRecord(
                customerId = c1Id,
                customerCompanyName = "شرکت فناوران نوین آریا",
                amountToman = 32_000_000L,
                reason = "بابت خدمات پشتیبانی و سرور ماه گذشته",
                timestamp = now - (15 * day)
            )
            addPaymentFollowUp(
                customerId = c1Id,
                customerCompanyName = "شرکت فناوران نوین آریا",
                resultNotes = "تماس با واحد مالی؛ فاکتور کمپین تایید گردیده و قول پرداخت تا پایان هفته داده شد.",
                timestamp = now - (1 * day)
            )

            addDebtRecord(
                customerId = c3Id,
                customerCompanyName = "گروه صنعتی پارس فولاد",
                amountToman = 75_000_000L,
                reason = "قسط دوم قرارداد سالانه تجهیزات",
                timestamp = now - (3 * day)
            )
            addPaymentFollowUp(
                customerId = c3Id,
                customerCompanyName = "گروه صنعتی پارس فولاد",
                resultNotes = "ارسال پیام و استعلام از حسابداری؛ چک صیادی برای تاریخ دوشنبه صادر گردید.",
                timestamp = now
            )
        }
    }
}
