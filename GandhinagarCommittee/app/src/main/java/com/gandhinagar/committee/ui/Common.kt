package com.gandhinagar.committee.ui

import androidx.compose.material3.*
import androidx.compose.runtime.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val Green = androidx.compose.ui.graphics.Color(0xFF2E7D32)
val Red = androidx.compose.ui.graphics.Color(0xFFC62828)

private val dateFmt = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
fun fmtDate(ts: Long?): String = ts?.let { dateFmt.format(Date(it)) } ?: "—"
fun money(v: Double): String = "₹" + "%,.0f".format(v)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialogField(
    show: Boolean,
    initial: Long?,
    onDismiss: () -> Unit,
    onPicked: (Long) -> Unit
) {
    if (!show) return
    val state = rememberDatePickerState(initialSelectedDateMillis = initial ?: System.currentTimeMillis())
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { state.selectedDateMillis?.let(onPicked); onDismiss() }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    ) { DatePicker(state = state) }
}
