package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.BackupSettingsScreen
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.EmployeeManagementScreen
import com.example.ui.screens.MarkAttendanceScreen
import com.example.ui.screens.OperatorDashboardScreen
import com.example.ui.screens.OperatorManagementScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.AttendanceViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                LoadingWorksApp()
            }
        }
    }
}

@Composable
fun LoadingWorksApp(viewModel: AttendanceViewModel = viewModel()) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToast()
        }
    }

    // Handle system back navigation
    BackHandler(enabled = currentScreen != AppScreen.AUTH) {
        val handled = viewModel.navigateBack()
        if (!handled && currentScreen != AppScreen.AUTH) {
            viewModel.logout()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { _ ->
        when (currentScreen) {
            AppScreen.AUTH -> AuthScreen(viewModel)
            AppScreen.ADMIN_DASHBOARD -> AdminDashboardScreen(viewModel)
            AppScreen.OPERATOR_DASHBOARD -> OperatorDashboardScreen(viewModel)
            AppScreen.MARK_ATTENDANCE -> MarkAttendanceScreen(viewModel)
            AppScreen.EMPLOYEE_MANAGEMENT -> EmployeeManagementScreen(viewModel)
            AppScreen.OPERATOR_MANAGEMENT -> OperatorManagementScreen(viewModel)
            AppScreen.REPORTS -> ReportsScreen(viewModel)
            AppScreen.CALENDAR -> CalendarScreen(viewModel)
            AppScreen.BACKUP_SETTINGS -> BackupSettingsScreen(viewModel)
        }
    }
}
