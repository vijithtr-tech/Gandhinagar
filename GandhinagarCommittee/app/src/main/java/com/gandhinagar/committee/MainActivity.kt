package com.gandhinagar.committee

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.gandhinagar.committee.ui.*

class MainActivity : ComponentActivity() {
    private val vm: CommitteeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { AppRoot(vm) } }
    }
}

private enum class Tab(val label: String, val icon: ImageVector) {
    Home("Home", Icons.Filled.Home),
    Members("Members", Icons.Filled.People),
    Loans("Loans", Icons.Filled.AccountBalance),
    Defaulters("Defaulters", Icons.Filled.Warning)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot(vm: CommitteeViewModel) {
    var tab by remember { mutableStateOf(Tab.Home) }
    Scaffold(
        bottomBar = {
            NavigationBar {
                Tab.values().forEach { t ->
                    NavigationBarItem(
                        selected = tab == t,
                        onClick = { tab = t },
                        icon = { Icon(t.icon, contentDescription = t.label) },
                        label = { Text(t.label) }
                    )
                }
            }
        }
    ) { padding ->
        val m = Modifier.padding(padding)
        when (tab) {
            Tab.Home -> HomeScreen(vm, m)
            Tab.Members -> MembersScreen(vm, m)
            Tab.Loans -> LoansScreen(vm, m)
            Tab.Defaulters -> DefaultersScreen(vm, m)
        }
    }
}
