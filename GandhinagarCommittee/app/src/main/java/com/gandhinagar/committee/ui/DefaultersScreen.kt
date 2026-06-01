package com.gandhinagar.committee.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gandhinagar.committee.data.Loan
import com.gandhinagar.committee.messaging.Messaging
import java.util.Calendar

/** Interest due date for the current month = same day-of-month as the loan start date. */
private fun currentMonthDueDate(loan: Loan): Long {
    val start = Calendar.getInstance().apply { timeInMillis = loan.startDate }
    val due = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        val maxDay = getActualMaximum(Calendar.DAY_OF_MONTH)
        set(Calendar.DAY_OF_MONTH, minOf(start.get(Calendar.DAY_OF_MONTH), maxDay))
    }
    return due.timeInMillis
}

@Composable
fun DefaultersScreen(vm: CommitteeViewModel, modifier: Modifier = Modifier) {
    val loans by vm.loans.collectAsState()
    val ctx = LocalContext.current
    val now = System.currentTimeMillis()

    // Open loans = interest defaulters this month; closed loans excluded (no messages).
    val openLoans = loans.filter { !it.isClosed }
        .map { it to currentMonthDueDate(it) }
        .sortedBy { it.second }

    LazyColumn(modifier.fillMaxSize().padding(horizontal = 12.dp)) {
        item {
            Text("Current Month Loan Defaulters", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
            Text("${openLoans.size} open loan(s) — interest due this month", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))
        }
        if (openLoans.isEmpty()) item { Text("No open loans. ") }
        items(openLoans, key = { it.first.id }) { (loan, due) ->
            val name = vm.memberName(loan.memberId)
            val overdue = due < now
            ElevatedCard(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Column(Modifier.padding(12.dp)) {
                    Text(name, fontWeight = FontWeight.Bold)
                    Text("Interest due: ${money(loan.monthlyInterest)}")
                    Text("Due date: ${fmtDate(due)}", color = if (overdue) Red else Green,
                        fontWeight = FontWeight.Bold)
                    Text("Principal: ${money(loan.principal)}")
                    TextButton(onClick = {
                        val phone = vm.members.value.first { it.id == loan.memberId }.phone
                        Messaging.sendWhatsApp(ctx, phone, Messaging.interestReminder(name, loan.monthlyInterest))
                    }) { Icon(Icons.Filled.Chat, null); Spacer(Modifier.width(4.dp)); Text("Send reminder") }
                }
            }
        }
    }
}
