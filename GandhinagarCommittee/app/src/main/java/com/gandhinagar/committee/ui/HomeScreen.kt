package com.gandhinagar.committee.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gandhinagar.committee.export.ShareUtil
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(vm: CommitteeViewModel, modifier: Modifier = Modifier) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val settings by vm.settings.collectAsState()
    val totalOpen by vm.totalOpenPrincipal.collectAsState()

    val opening = settings?.openingBalance ?: 0.0
    val current = opening - totalOpen

    var showSettings by remember { mutableStateOf(false) }

    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("GANDHINAGAR COMMITTEE", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        BalanceCard("Opening Balance", money(opening))
        Spacer(Modifier.height(8.dp))
        BalanceCard("Total Loans Given (open)", money(totalOpen))
        Spacer(Modifier.height(8.dp))
        BalanceCard("Current Balance", money(current))

        Spacer(Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = { showSettings = true }) { Text("Edit Balance / Loan Limit") }
        }
        Spacer(Modifier.height(8.dp))
        Text("Per-member loan limit: ${money(settings?.loanLimit ?: 10000.0)}")

        Spacer(Modifier.height(24.dp))
        Text("Backup", fontWeight = FontWeight.Bold)
        Text("Auto backup runs daily at 8:00 PM.", fontSize = 13.sp)
        Spacer(Modifier.height(8.dp))
        Button(onClick = {
            scope.launch {
                val f = vm.exportNow()
                ShareUtil.shareExcel(ctx, f)
            }
        }) { Text("Export & Share Excel now") }
    }

    if (showSettings) {
        var openTxt by remember { mutableStateOf(opening.toLong().toString()) }
        var limitTxt by remember { mutableStateOf((settings?.loanLimit ?: 10000.0).toLong().toString()) }
        AlertDialog(
            onDismissRequest = { showSettings = false },
            title = { Text("Edit Balance & Limit") },
            text = {
                Column {
                    OutlinedTextField(openTxt, { openTxt = it }, label = { Text("Opening Balance") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(limitTxt, { limitTxt = it }, label = { Text("Per-member Loan Limit") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.updateSettings(openTxt.toDoubleOrNull() ?: 0.0, limitTxt.toDoubleOrNull() ?: 10000.0)
                    showSettings = false
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showSettings = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun BalanceCard(label: String, value: String) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(label, fontSize = 14.sp)
            Text(value, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        }
    }
}
