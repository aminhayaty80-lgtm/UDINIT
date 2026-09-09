package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class CustomerStatus(val labelFa: String, val stepIndex: Int) {
    NEW("لید جدید", 0),
    CONTACTED("تماس اولیه", 1),
    NEGOTIATION("مذاکره و پیشنهاد", 2),
    WON("معامله موفق (فروش)", 3),
    LOST("انصراف / سرد", 4)
}

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val companyName: String,
    val contactName: String,
    val jobTitle: String,
    val industry: String,
    val phoneNumber: String,
    val email: String = "",
    val notes: String = "",
    val status: String = CustomerStatus.NEW.name,
    val createdAt: Long = System.currentTimeMillis(),
    val lastContactAt: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false
) {
    val statusEnum: CustomerStatus
        get() = try {
            CustomerStatus.valueOf(status)
        } catch (_: Exception) {
            CustomerStatus.NEW
        }
}
