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
import com.gandhinagar.committee.data.Member
import com.gandhinagar.committee.messaging.Messaging

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembersScreen(vm: CommitteeViewModel, modifier: Modifier = Modifier) {
    val members by vm.members.collectAsState()
    var editing by remember { mutableStateOf<Member?>(null) }
    var showEditor by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(onClick = { editing = null; showEditor = true }) {
                Icon(Icons.Filled.Add, "Add member")
            }
        }
    ) { p ->
        LazyColumn(Modifier.padding(p).fillMaxSize().padding(horizontal = 12.dp)) {
            item { Text("Members (${members.size})", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp)) }
            items(members, key = { it.id }) { m ->
                MemberRow(m,
                    onTogglePaid = { vm.setMembershipPaid(m, !m.membershipPaid) },
                    onEdit = { editing = m; showEditor = true },
                    onDelete = { vm.deleteMember(m) }
                )
            }
        }
    }

    if (showEditor) {
        MemberEditor(editing, onDismiss = { showEditor = false }) { vm.saveMember(it); showEditor = false }
    }
}

@Composable
private fun MemberRow(m: Member, onTogglePaid: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    val paidColor = if (m.membershipPaid) Green else Red
    val totalDue = Calc.membershipTotalDue(m)
    val ctx = LocalContext.current
    ElevatedCard(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(m.name, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                AssistChip(onClick = onTogglePaid,
                    label = { Text(if (m.membershipPaid) "PAID" else "UNPAID") },
                    colors = AssistChipDefaults.assistChipColors(labelColor = paidColor, leadingIconContentColor = paidColor),
                    leadingIcon = { Icon(if (m.membershipPaid) Icons.Filled.CheckCircle else Icons.Filled.Cancel, null, tint = paidColor) })
            }
            Text(m.phone)
            Text("Membership: ${money(m.membershipAmount)}  •  Due: ${fmtDate(m.membershipDueDate)}")
            if (!m.membershipPaid && totalDue > m.membershipAmount)
                Text("With penalty due now: ${money(totalDue)}", color = Red)
            if (m.membershipPaid) Text("Paid on ${fmtDate(m.membershipPaidDate)}", color = Green)
            Row {
                TextButton(onClick = onEdit) { Icon(Icons.Filled.Edit, null); Spacer(Modifier.width(4.dp)); Text("Edit") }
                TextButton(onClick = {
                    if (!m.membershipPaid)
                        Messaging.sendWhatsApp(ctx, m.phone, Messaging.membershipReminder(m.name, totalDue))
                }) { Icon(Icons.Filled.Chat, null); Spacer(Modifier.width(4.dp)); Text("Remind") }
                TextButton(onClick = onDelete) { Icon(Icons.Filled.Delete, null, tint = Red); Spacer(Modifier.width(4.dp)); Text("Delete", color = Red) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MemberEditor(existing: Member?, onDismiss: () -> Unit, onSave: (Member) -> Unit) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var phone by remember { mutableStateOf(existing?.phone ?: "") }
    var amount by remember { mutableStateOf((existing?.membershipAmount ?: 2000.0).toLong().toString()) }
    var dueDate by remember { mutableStateOf(existing?.membershipDueDate ?: Calc.defaultDueDate()) }
    var showDate by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Add Member" else "Edit Member") },
        text = {
            Column {
                OutlinedTextField(name, { name = it }, label = { Text("Name") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(phone, { phone = it }, label = { Text("Phone (10-digit)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(amount, { amount = it }, label = { Text("Membership Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { showDate = true }) { Text("Due date: ${fmtDate(dueDate)}") }
            }
        },
        confirmButton = {
            TextButton(enabled = name.isNotBlank(), onClick = {
                onSave((existing ?: Member(name = "", phone = "", membershipDueDate = dueDate)).copy(
                    name = name.trim(), phone = phone.trim(),
                    membershipAmount = amount.toDoubleOrNull() ?: 2000.0,
                    membershipDueDate = dueDate
                ))
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
    DatePickerDialogField(showDate, dueDate, { showDate = false }) { dueDate = it }
}
