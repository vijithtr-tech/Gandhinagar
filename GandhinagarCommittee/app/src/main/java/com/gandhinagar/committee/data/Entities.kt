package com.gandhinagar.committee.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "members")
data class Member(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,                 // 10-digit or with country code; used for SMS/WhatsApp
    val membershipAmount: Double = 2000.0,
    val membershipDueDate: Long,       // millis; default May 30 of current year
    val membershipPaid: Boolean = false,
    val membershipPaidDate: Long? = null
)

@Entity(tableName = "loans")
data class Loan(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val memberId: Long,
    val principal: Double,
    val interestRatePercent: Double = 10.0,   // 10% of principal
    val startDate: Long,
    val closeDate: Long? = null,              // when set, loan is closed -> no more reminders
    val note: String = ""
) {
    val isClosed: Boolean get() = closeDate != null
    val monthlyInterest: Double get() = principal * interestRatePercent / 100.0
}

// Single-row settings table (id is always 1)
@Entity(tableName = "settings")
data class AppSettings(
    @PrimaryKey val id: Int = 1,
    val openingBalance: Double = 0.0,
    val loanLimit: Double = 10000.0          // adjustable in future
)
