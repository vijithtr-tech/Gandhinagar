package com.gandhinagar.committee.data

import java.util.Calendar

object Calc {

    /** Default membership due date: May 30 of the given year. */
    fun defaultDueDate(year: Int = Calendar.getInstance().get(Calendar.YEAR)): Long {
        val c = Calendar.getInstance()
        c.set(year, Calendar.MAY, 30, 0, 0, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    /** Whole months elapsed between two timestamps (>=0). */
    fun monthsBetween(fromMillis: Long, toMillis: Long): Int {
        if (toMillis <= fromMillis) return 0
        val a = Calendar.getInstance().apply { timeInMillis = fromMillis }
        val b = Calendar.getInstance().apply { timeInMillis = toMillis }
        var months = (b.get(Calendar.YEAR) - a.get(Calendar.YEAR)) * 12 +
                (b.get(Calendar.MONTH) - a.get(Calendar.MONTH))
        if (b.get(Calendar.DAY_OF_MONTH) < a.get(Calendar.DAY_OF_MONTH)) months--
        return months.coerceAtLeast(0)
    }

    /**
     * Membership penalty. If unpaid past the due date, each month after due date
     * adds 10% of the membership amount.
     */
    fun membershipPenalty(member: Member, now: Long = System.currentTimeMillis()): Double {
        if (member.membershipPaid) return 0.0
        val monthsLate = monthsBetween(member.membershipDueDate, now)
        return member.membershipAmount * 0.10 * monthsLate
    }

    fun membershipTotalDue(member: Member, now: Long = System.currentTimeMillis()): Double {
        if (member.membershipPaid) return 0.0
        return member.membershipAmount + membershipPenalty(member, now)
    }

    /** Monthly interest accrued for an open loan up to now (10% of principal per month). */
    fun loanInterestAccrued(loan: Loan, now: Long = System.currentTimeMillis()): Double {
        val end = loan.closeDate ?: now
        val months = monthsBetween(loan.startDate, end).coerceAtLeast(1)
        return loan.monthlyInterest * months
    }
}
