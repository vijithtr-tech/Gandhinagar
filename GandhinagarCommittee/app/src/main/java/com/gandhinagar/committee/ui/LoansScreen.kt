package com.gandhinagar.committee.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.gandhinagar.committee.data.Calc
import com.gandhinagar.committee.data.Loan
import com.gandhinagar.committee.messaging.Messaging
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoansScreen(vm: CommitteeViewModel, modifier: Modifier = Modifier) {
    val loans by vm.loans.collectAsState()
    val members by vm.members.collectAsState()
    var showEditor by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            if (members.isNotEmpty())
                FloatingActionButton(onClick = { showEditor = true }) { Icon(Icons.Filled.Add, "Add loan") }
        }
    ) { p ->
        LazyColumn(Modifier.padding(p).fillMaxSize().padding(horizontal = 12.dp)) {
            item { Text("Loans (${loans.count { !it.isClosed }} open)", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp)) }
            if (members.isEmpty()) item { Text("Add members first.") }
            items(loans, key = { it.id }) { l ->
                LoanRow(l, vm.memberName(l.memberId),
                    onClose = { vm.closeLoan(l, it) },
                    onDelete = { vm.deleteLoan(l) },
                    vm = vm)
            }
        }
    }

    if (showEditor) LoanEditor(vm, onDismiss = { showEditor = false })
}

@Composable
private fun LoanRow(l: Loan, memberName: String, onClose: (Long) -> Unit, onDelete: () -> Unit, vm: CommitteeViewModel) {
    val ctx = LocalContext.current
    var showClose by remember { mutableStateOf(false) }
    val statusColor = if (l.isClosed) Green else Red
    ElevatedCard(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(memberName, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text(if (l.isClosed) "CLOSED" else "OPEN", color = statusColor, fontWeight = FontWeight.Bold)
            }
            Text("Principal: ${money(l.principal)}  •  Interest: ${money(l.monthlyInterest)}/month (10%)")
            Text("Started: ${fmtDate(l.startDate)}")
            if (l.isClosed) Text("Closed: ${fmtDate(l.closeDate)}", color = Green)
            else Text("Interest accrued: ${money(Calc.loanInterestAccrued(l))}")
            if (l.note.isNotBlank()) Text("Note: ${l.note}")
            Row {
                if (!l.isClosed) {
                    TextButton(onClick = {
                        Messaging.sendWhatsApp(ctx, vm.members.value.first { it.id == l.memberId }.phone,
                            Messaging.interestReminder(memberName, l.monthlyInterest))
                    }) { Icon(Icons.Filled.Chat, null); Spacer(Modifier.width(4.dp)); Text("WhatsApp") }
                    TextButton(onClick = {
                        Messaging.sendSms(ctx, vm.members.value.first { it.id == l.memberId }.phone,
                            Messaging.interestReminder(memberName, l.monthlyInterest))
                    }) { Icon(Icons.Filled.Sms, null); Spacer(Modifier.width(4.dp)); Text("SMS") }
                    TextButton(onClick = { showClose = true }) { Text("Close") }
                }
                TextButton(onClick = onDelete) { Icon(Icons.Filled.Delete, null, tint = Red); Spacer(Modifier.width(4.dp)); Text("Delete", color = Red) }
            }
        }
    }
    DatePickerDialogField(showClose, System.currentTimeMillis(), { showClose = false }) { onClose(it) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoanEditor(vm: CommitteeViewModel, onDismiss: () -> Unit) {
    val members by vm.members.collectAsState()
    val scope = rememberCoroutineScope()
    var memberId by remember { mutableStateOf(members.firstOrNull()?.id ?: 0L) }
    var principal by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDate by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var warning by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Loan") },
        text = {
            Column {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = members.firstOrNull { it.id == memberId }?.name ?: "",
                        onValueChange = {}, readOnly = true, label = { Text("Member") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded, onDismissRequest = { expanded = false }) {
                        members.forEach { mem ->
                            DropdownMenuItem(text = { Text(mem.name) }, onClick = { memberId = mem.id; expanded = false })
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(principal, { principal = it; warning = null }, label = { Text("Principal Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { showDate = true }) { Text("Start: ${fmtDate(startDate)}") }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(note, { note = it }, label = { Text("Note (optional)") })
                warning?.let { Text(it, color = Red, modifier = Modifier.padding(top = 8.dp)) }
            }
        },
        confirmButton = {
            TextButton(enabled = principal.toDoubleOrNull() != null && memberId != 0L, onClick = {
                val amt = principal.toDouble()
                scope.launch {
                    val check = vm.checkLoan(memberId, amt)
                    if (!check.allowed) warning = check.warning
                    else {
                        vm.saveLoan(Loan(memberId = memberId, principal = amt, startDate = startDate, note = note.trim()))
                        onDismiss()
                    }
                }
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
    DatePickerDialogField(showDate, startDate, { showDate = false }) { startDate = it }
}
