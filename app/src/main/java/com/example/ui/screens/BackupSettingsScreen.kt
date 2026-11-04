package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AppHeader
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.theme.NeutralBackground
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueContainer
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.StatusAbsentRed
import com.example.ui.theme.StatusPresentGreen
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AttendanceViewModel

@Composable
fun BackupSettingsScreen(viewModel: AttendanceViewModel) {
    val context = LocalContext.current
    val language by viewModel.language.collectAsState()

    var showPasswordDialog by remember { mutableStateOf(false) }
    var currentPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var passError by remember { mutableStateOf<String?>(null) }

    var showBackupDialog by remember { mutableStateOf(false) }
    var backupJsonString by remember { mutableStateOf("") }

    var showRestoreDialog by remember { mutableStateOf(false) }
    var restoreJsonInput by remember { mutableStateOf("") }
    var restoreError by remember { mutableStateOf<String?>(null) }

    var showResetDialog by remember { mutableStateOf(false) }
    var isSyncing by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppHeader(
                title = AppStrings.t("backup_restore", language),
                subtitle = "Security & Local Storage Management",
                currentLanguage = language,
                onLanguageChange = { viewModel.setLanguage(it) },
                showBackButton = true,
                onBackClick = { viewModel.navigateBack() },
                showLogout = false
            )
        },
        containerColor = NeutralBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Offline & Sync Status Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE8F5E9)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudDone,
                                        contentDescription = null,
                                        tint = StatusPresentGreen,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = AppStrings.t("offline_status", language),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = if (language == AppLanguage.HI)
                                            "ऐप बिना इंटरनेट के पूरी तरह काम करता है"
                                        else
                                            "All attendance is safely stored on-device",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    isSyncing = true
                                    viewModel.showToast(AppStrings.t("sync_success", language))
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlueLight),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("sync_data_btn")
                            ) {
                                Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = AppStrings.t("sync_now", language),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // 2. Backup & Restore Data
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = AppStrings.t("backup_restore", language),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = PrimaryBlue
                        )
                        Text(
                            text = if (language == AppLanguage.HI)
                                "अपने सभी कर्मचारियों, ऑपरेटरों और हज़िरी का सुरक्षित बैकअप निकालें या रीस्टोर करें।"
                            else
                                "Export your complete database into a portable backup file, or restore from a previous backup.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.createBackupJson { json ->
                                        backupJsonString = json
                                        showBackupDialog = true
                                    }
                                },
                                modifier = Modifier.weight(1f).testTag("export_backup_btn"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlueLight)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (language == AppLanguage.HI) "बैकअप निकालें" else "Export Backup",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    restoreJsonInput = ""
                                    restoreError = null
                                    showRestoreDialog = true
                                },
                                modifier = Modifier.weight(1f).testTag("restore_backup_btn"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (language == AppLanguage.HI) "रीस्टोर करें" else "Restore",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlueLight
                                )
                            }
                        }
                    }
                }
            }

            // 3. Security & Admin Password
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = if (language == AppLanguage.HI) "सुरक्षा & एडमिन पासवर्ड" else "Security & Admin Access",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = PrimaryBlue
                        )
                        Text(
                            text = if (language == AppLanguage.HI)
                                "एडमिन पासवर्ड केवल व्यवस्थापक के पास होना चाहिए ताकि कोई अनधिकृत बदलाव न हो सके।"
                            else
                                "Ensure your admin password is kept secure to prevent unauthorized employee changes.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )

                        Button(
                            onClick = {
                                currentPass = ""
                                newPass = ""
                                passError = null
                                showPasswordDialog = true
                            },
                            modifier = Modifier.fillMaxWidth().testTag("change_admin_pass_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37474F))
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = AppStrings.t("change_admin_pass", language),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 4. Reset to Initial Sample Data
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = AppStrings.t("reset_sample_data", language),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = PrimaryBlue
                        )
                        Text(
                            text = if (language == AppLanguage.HI)
                                "यदि आप डिफ़ॉल्ट लोडिंग वर्क्स नमूना डेटा (6 मजदूर और 2 ऑपरेटर) पुनः लोड करना चाहते हैं।"
                            else
                                "Reload factory sample data with 6 loading employees, 2 operators, and recent records.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )

                        OutlinedButton(
                            onClick = { showResetDialog = true },
                            modifier = Modifier.fillMaxWidth().testTag("reset_sample_data_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = null, tint = StatusAbsentRed, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = AppStrings.t("reset_sample_data", language),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = StatusAbsentRed
                            )
                        }
                    }
                }
            }
        }

        // Change Password Dialog
        if (showPasswordDialog) {
            AlertDialog(
                onDismissRequest = { showPasswordDialog = false },
                icon = { Icon(Icons.Default.Key, contentDescription = null, tint = PrimaryBlueLight, modifier = Modifier.size(30.dp)) },
                title = { Text(AppStrings.t("change_admin_pass", language), fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = currentPass,
                            onValueChange = { currentPass = it; passError = null },
                            label = { Text(AppStrings.t("current_password", language)) },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = newPass,
                            onValueChange = { newPass = it; passError = null },
                            label = { Text(AppStrings.t("new_password", language)) },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (passError != null) {
                            Text(text = passError ?: "", color = StatusAbsentRed, fontSize = 12.sp)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.changeAdminPassword(currentPass, newPass) { success, msg ->
                                if (success) {
                                    showPasswordDialog = false
                                    viewModel.showToast(msg)
                                } else {
                                    passError = msg
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlueLight)
                    ) {
                        Text(AppStrings.t("update_password", language))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPasswordDialog = false }) {
                        Text(AppStrings.t("cancel", language))
                    }
                }
            )
        }

        // Export Backup Display Dialog
        if (showBackupDialog) {
            AlertDialog(
                onDismissRequest = { showBackupDialog = false },
                icon = { Icon(Icons.Default.Download, contentDescription = null, tint = StatusPresentGreen, modifier = Modifier.size(30.dp)) },
                title = { Text("Backup Data (JSON)", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Copy your backup data below:", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = backupJsonString,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Backup JSON", backupJsonString))
                            showBackupDialog = false
                            viewModel.showToast("Backup JSON copied to clipboard! / बैकअप कॉपी हो गया!")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlueLight)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy Backup")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showBackupDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }

        // Restore Backup Dialog
        if (showRestoreDialog) {
            AlertDialog(
                onDismissRequest = { showRestoreDialog = false },
                icon = { Icon(Icons.Default.Upload, contentDescription = null, tint = PrimaryBlueLight, modifier = Modifier.size(30.dp)) },
                title = { Text(AppStrings.t("restore_backup", language), fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Paste your backup JSON below:", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = restoreJsonInput,
                            onValueChange = { restoreJsonInput = it; restoreError = null },
                            placeholder = { Text("{\n  \"employees\": [...],\n  \"attendance\": [...]\n}") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp)
                        )
                        if (restoreError != null) {
                            Text(text = restoreError ?: "", color = StatusAbsentRed, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (restoreJsonInput.trim().isEmpty()) {
                                restoreError = "Please paste JSON backup string"
                                return@Button
                            }
                            viewModel.restoreBackup(restoreJsonInput) { ok, msg ->
                                if (ok) {
                                    showRestoreDialog = false
                                    viewModel.showToast(msg)
                                } else {
                                    restoreError = msg
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlueLight)
                    ) {
                        Text("Restore Now")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRestoreDialog = false }) {
                        Text(AppStrings.t("cancel", language))
                    }
                }
            )
        }

        // Reset Sample Data Confirmation Dialog
        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = { Text(AppStrings.t("reset_sample_data", language), fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        if (language == AppLanguage.HI)
                            "क्या आप वाकई नमूना डेटा रीसेट करना चाहते हैं? इससे सभी मौजूदा रिकॉर्ड्स रीसेट हो जाएंगे।"
                        else
                            "Resetting will reload the default loading yard workers and operators. Continue?"
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.resetSampleData()
                            showResetDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusAbsentRed)
                    ) {
                        Text("Reset")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text(AppStrings.t("cancel", language))
                    }
                }
            )
        }
    }
}
