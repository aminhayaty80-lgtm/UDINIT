package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class CallOutcome(val labelFa: String) {
    SUCCESSFUL("موفق و مثبت"),
    FOLLOWUP_NEEDED("نیاز به پیگیری"),
    NO_ANSWER("عدم پاسخگویی"),
    REJECTED("عدم تمایل / رد")
}

@Entity(
    tableName = "call_logs",
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
data class CallLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val persianDateStr: String,
    val callResult: String,
    val outcomeType: String = CallOutcome.SUCCESSFUL.name
) {
    val outcomeEnum: CallOutcome
        get() = try {
            CallOutcome.valueOf(outcomeType)
        } catch (_: Exception) {
            CallOutcome.SUCCESSFUL
        }
}
