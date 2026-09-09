package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers WHERE isArchived = 0 ORDER BY lastContactAt DESC")
    fun getActiveCustomers(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE isArchived = 1 ORDER BY lastContactAt DESC")
    fun getArchivedCustomers(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    suspend fun getCustomerById(id: Long): Customer?

    @Query("""
        SELECT * FROM customers 
        WHERE isArchived = 0 AND (:currentTime - lastContactAt) >= :twoWeeksMillis 
        ORDER BY lastContactAt ASC
    """)
    fun getStaleCustomers(currentTime: Long, twoWeeksMillis: Long = 14L * 24 * 60 * 60 * 1000L): Flow<List<Customer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer): Long

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Query("UPDATE customers SET lastContactAt = :timestamp WHERE id = :customerId")
    suspend fun updateLastContact(customerId: Long, timestamp: Long)

    @Query("UPDATE customers SET isArchived = :isArchived WHERE id = :customerId")
    suspend fun setArchived(customerId: Long, isArchived: Boolean)

    @Query("UPDATE customers SET status = :newStatus WHERE id = :customerId")
    suspend fun updateStatus(customerId: Long, newStatus: String)

    @Delete
    suspend fun deleteCustomer(customer: Customer)

    @Query("DELETE FROM customers WHERE id = :customerId")
    suspend fun deleteCustomerById(customerId: Long)
}

@Dao
interface CallLogDao {
    @Query("SELECT * FROM call_logs WHERE customerId = :customerId ORDER BY timestamp DESC")
    fun getLogsForCustomer(customerId: Long): Flow<List<CallLog>>

    @Query("SELECT * FROM call_logs ORDER BY timestamp DESC LIMIT 50")
    fun getRecentLogs(): Flow<List<CallLog>>

    @Query("SELECT COUNT(*) FROM call_logs")
    fun getTotalCallsCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCallLog(callLog: CallLog): Long

    @Delete
    suspend fun deleteCallLog(callLog: CallLog)
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE isCompleted = 0 ORDER BY remindAtMillis ASC")
    fun getActiveReminders(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders ORDER BY remindAtMillis DESC")
    fun getAllReminders(): Flow<List<Reminder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder): Long

    @Query("UPDATE reminders SET isCompleted = :completed WHERE id = :reminderId")
    suspend fun setCompleted(reminderId: Long, completed: Boolean)

    @Delete
    suspend fun deleteReminder(reminder: Reminder)

    @Query("DELETE FROM reminders WHERE id = :reminderId")
    suspend fun deleteReminderById(reminderId: Long)
}

@Dao
interface DebtRecordDao {
    @Query("SELECT * FROM debt_records ORDER BY createdAt DESC")
    fun getAllDebts(): Flow<List<DebtRecord>>

    @Query("SELECT * FROM debt_records WHERE customerId = :customerId ORDER BY createdAt DESC")
    fun getDebtsForCustomer(customerId: Long): Flow<List<DebtRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebt(debt: DebtRecord): Long

    @Update
    suspend fun updateDebt(debt: DebtRecord)

    @Query("UPDATE debt_records SET isSettled = :isSettled, settledAt = :settledAt WHERE id = :debtId")
    suspend fun setSettled(debtId: Long, isSettled: Boolean, settledAt: Long?)

    @Query("UPDATE debt_records SET isSettled = :isSettled, settledAt = :settledAt, settledPersianDateStr = :settledPersianDateStr, settlementNote = :settlementNote, receiptImageUri = :receiptImageUri WHERE id = :debtId")
    suspend fun settleDebtWithProof(
        debtId: Long,
        isSettled: Boolean,
        settledAt: Long?,
        settledPersianDateStr: String?,
        settlementNote: String?,
        receiptImageUri: String?
    )

    @Delete
    suspend fun deleteDebt(debt: DebtRecord)

    @Query("DELETE FROM debt_records WHERE id = :debtId")
    suspend fun deleteDebtById(debtId: Long)
}

@Dao
interface PaymentFollowUpDao {
    @Query("SELECT * FROM payment_follow_ups ORDER BY timestamp DESC")
    fun getAllFollowUps(): Flow<List<PaymentFollowUp>>

    @Query("SELECT * FROM payment_follow_ups WHERE customerId = :customerId ORDER BY timestamp DESC")
    fun getFollowUpsForCustomer(customerId: Long): Flow<List<PaymentFollowUp>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFollowUp(followUp: PaymentFollowUp): Long

    @Delete
    suspend fun deleteFollowUp(followUp: PaymentFollowUp)

    @Query("DELETE FROM payment_follow_ups WHERE id = :followUpId")
    suspend fun deleteFollowUpById(followUpId: Long)
}

