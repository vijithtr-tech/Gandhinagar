package com.gandhinagar.committee.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gandhinagar.committee.data.*
import com.gandhinagar.committee.export.ExcelExporter
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

data class LoanCheck(val allowed: Boolean, val total: Double, val limit: Double, val warning: String?)

class CommitteeViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase.get(app)
    private val memberDao = db.memberDao()
    private val loanDao = db.loanDao()
    private val settingsDao = db.settingsDao()

    val members = memberDao.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val loans = loanDao.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val totalOpenPrincipal = loanDao.observeTotalOpenPrincipal()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    val settings = settingsDao.observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            if (settingsDao.get() == null) settingsDao.upsert(AppSettings())
        }
    }

    // ---- Members ----
    fun saveMember(m: Member) = viewModelScope.launch { memberDao.upsert(m) }
    fun deleteMember(m: Member) = viewModelScope.launch { memberDao.delete(m) }
    fun setMembershipPaid(m: Member, paid: Boolean) = viewModelScope.launch {
        memberDao.upsert(m.copy(membershipPaid = paid, membershipPaidDate = if (paid) System.currentTimeMillis() else null))
    }

    // ---- Loans ----
    /** Validates against per-member loan limit. */
    suspend fun checkLoan(memberId: Long, newPrincipal: Double): LoanCheck {
        val limit = settingsDao.get()?.loanLimit ?: 10000.0
        val existing = loanDao.openPrincipalForMember(memberId)
        val total = existing + newPrincipal
        val warning = if (total > limit)
            "Warning: total open loan for this member would be Rs. ${"%.0f".format(total)}, exceeding the limit of Rs. ${"%.0f".format(limit)}."
        else null
        return LoanCheck(total <= limit, total, limit, warning)
    }

    fun saveLoan(l: Loan) = viewModelScope.launch { loanDao.upsert(l) }
    fun deleteLoan(l: Loan) = viewModelScope.launch { loanDao.delete(l) }
    fun closeLoan(l: Loan, closeDate: Long) = viewModelScope.launch { loanDao.upsert(l.copy(closeDate = closeDate)) }

    // ---- Settings ----
    fun updateSettings(opening: Double, limit: Double) = viewModelScope.launch {
        settingsDao.upsert((settingsDao.get() ?: AppSettings()).copy(openingBalance = opening, loanLimit = limit))
    }

    // ---- Export ----
    suspend fun exportNow(): File = ExcelExporter.export(getApplication())

    fun memberName(id: Long): String = members.value.firstOrNull { it.id == id }?.name ?: "#$id"
}
