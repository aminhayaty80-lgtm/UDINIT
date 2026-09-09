package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long? = null,
    val customerName: String = "",
    val title: String,
    val message: String,
    val remindAtMillis: Long,
    val persianDateStr: String,
    val isCompleted: Boolean = false
)
