package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a customer's debt record / invoice
 * Example: 108,000,000 Toman for campaign 1405/05/12
 */
@Entity(
    tableName = "debt_records",
    foreignKeys = [
        ForeignKey(
            entity = Customer::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("customerId")]
)
data class DebtRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long,
    val customerCompanyName: String,
    val amountToman: Long,
    val reason: String,
    val createdAt: Long = System.currentTimeMillis(),
    val persianDateStr: String,
    val isSettled: Boolean = false,
    val settledAt: Long? = null,
    val settledPersianDateStr: String? = null,
    val settlementNote: String? = null,
    val receiptImageUri: String? = null
)

/**
 * Represents a payment follow-up entry
 * Auto-stamps the current Persian date/time
 */
@Entity(
    tableName = "payment_follow_ups",
    foreignKeys = [
        ForeignKey(
            entity = Customer::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("customerId")]
)
data class PaymentFollowUp(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long,
    val customerCompanyName: String,
    val resultNotes: String,
    val timestamp: Long = System.currentTimeMillis(),
    val persianDateStr: String
)

/**
 * Data summary holding customer information and their financial balance
 */
data class CustomerFinancialSummary(
    val customer: Customer,
    val totalDebtToman: Long,
    val activeDebtToman: Long,
    val settledDebtToman: Long,
    val debtRecordsCount: Int,
    val latestFollowUp: PaymentFollowUp? = null,
    val followUpsCount: Int = 0
)
