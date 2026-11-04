package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Operator
import com.example.ui.components.AppHeader
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.theme.NeutralBackground
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueContainer
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.StatusAbsentRed
import com.example.ui.theme.SurfaceVariant
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AttendanceViewModel

@Composable
fun OperatorManagementScreen(viewModel: AttendanceViewModel) {
    val language by viewModel.language.collectAsState()
    val operators by viewModel.allOperators.collectAsState()
    val employees by viewModel.allEmployees.collectAsState()

    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingOperator by remember { mutableStateOf<Operator?>(null) }
    var deletingOperator by remember { mutableStateOf<Operator?>(null) }

    // Count assigned employees per operator
    val assignedCounts = employees.groupBy { it.assignedOperatorId }

    Scaffold(
        topBar = {
            AppHeader(
                title = AppStrings.t("manage_operators", language),
                subtitle = "${operators.size} Active Operators • Admin Only",
                currentLanguage = language,
                onLanguageChange = { viewModel.setLanguage(it) },
                showBackButton = true,
                onBackClick = { viewModel.navigateBack() },
                showLogout = false
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingOperator = null
                    showAddEditDialog = true
                },
                containerColor = PrimaryBlueLight,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_operator_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Operator")
            }
        },
        containerColor = NeutralBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryBlueContainer.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(PrimaryBlueLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Engineering, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Column {
                            Text(
                                text = if (language == AppLanguage.HI) "लोडिंग वर्क्स ऑपरेटर प्रबंधन" else "Loading Works Operators",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = PrimaryBlue
                            )
                            Text(
                                text = if (language == AppLanguage.HI)
                                    "ऑपरेटर केवल अपने आवंटित मजदूरों की हज़िरी भर सकते हैं और एडमिन सेटिंग्स तक नहीं पहुंच सकते।"
                                else
                                    "Operators can only log attendance for employees assigned to their shift.",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            if (operators.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        modifier = Modifier.fillMaxWidth().padding(top = 20.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "No operators created yet.", color = TextMuted)
                        }
                    }
                }
            } else {
                items(operators, key = { it.id }) { operator ->
                    val workerCount = assignedCounts[operator.id]?.size ?: 0
                    OperatorCard(
                        operator = operator,
                        assignedWorkerCount = workerCount,
                        language = language,
                        onEdit = {
                            editingOperator = operator
                            showAddEditDialog = true
                        },
                        onDelete = {
                            deletingOperator = operator
                        }
                    )
                }
            }
        }

        // Add / Edit Dialog
        if (showAddEditDialog) {
            AddEditOperatorDialog(
                initialOperator = editingOperator,
                existingOperators = operators,
                language = language,
                onDismiss = { showAddEditDialog = false; editingOperator = null },
                onSave = { op ->
                    viewModel.saveOperator(op) { success, msg ->
                        if (success) {
                            showAddEditDialog = false
                            editingOperator = null
                            viewModel.showToast(msg)
                        }
                    }
                }
            )
        }

        // Delete Dialog
        if (deletingOperator != null) {
            AlertDialog(
                onDismissRequest = { deletingOperator = null },
                icon = {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = StatusAbsentRed, modifier = Modifier.size(32.dp))
                },
                title = { Text(AppStrings.t("delete_employee", language), fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        text = "${AppStrings.t("delete_confirm", language)}\n\n${deletingOperator?.name} (${deletingOperator?.id})",
                        fontSize = 13.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            deletingOperator?.let { viewModel.deleteOperator(it) }
                            deletingOperator = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusAbsentRed)
                    ) {
                        Text(AppStrings.t("delete_employee", language))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { deletingOperator = null }) {
                        Text(AppStrings.t("cancel", language))
                    }
                }
            )
        }
    }
}

@Composable
fun OperatorCard(
    operator: Operator,
    assignedWorkerCount: Int,
    language: AppLanguage,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(PrimaryBlueContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Engineering, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = operator.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = SurfaceVariant
                            ) {
                                Text(
                                    text = operator.id,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = operator.shift,
                            fontSize = 12.sp,
                            color = PrimaryBlueLight,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = PrimaryBlueLight, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StatusAbsentRed, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = SurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Group, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                        Text(
                            text = "$assignedWorkerCount assigned workers",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Key, contentDescription = null, tint = TextMuted, modifier = Modifier.size(12.dp))
                        Text(
                            text = "Login: ${operator.username} / ${operator.password}",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddEditOperatorDialog(
    initialOperator: Operator?,
    existingOperators: List<Operator>,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSave: (Operator) -> Unit
) {
    val isEdit = initialOperator != null

    val nextId = remember(existingOperators) {
        val nextNum = (existingOperators.mapNotNull { it.id.replace("OP-0", "").replace("OP-", "").toIntOrNull() }.maxOrNull() ?: 0) + 1
        "OP-0$nextNum"
    }

    var id by remember { mutableStateOf(initialOperator?.id ?: nextId) }
    var name by remember { mutableStateOf(initialOperator?.name ?: "") }
    var username by remember { mutableStateOf(initialOperator?.username ?: "") }
    var password by remember { mutableStateOf(initialOperator?.password ?: "123") }
    var phone by remember { mutableStateOf(initialOperator?.phone ?: "") }
    var shift by remember { mutableStateOf(initialOperator?.shift ?: "Morning Shift (सुबह की शिफ्ट)") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEdit) "Edit Operator" else AppStrings.t("add_operator", language),
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = id,
                    onValueChange = { if (!isEdit) id = it },
                    label = { Text("Operator ID") },
                    singleLine = true,
                    enabled = !isEdit,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorMsg = null },
                    label = { Text("Operator Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it; errorMsg = null },
                    label = { Text(AppStrings.t("username", language)) },
                    placeholder = { Text("e.g. operator3") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; errorMsg = null },
                    label = { Text(AppStrings.t("password", language)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(AppStrings.t("phone_number", language)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = shift,
                    onValueChange = { shift = it },
                    label = { Text(AppStrings.t("shift", language)) },
                    placeholder = { Text("e.g. Morning Shift, Night Shift") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMsg != null) {
                    Text(text = errorMsg ?: "", color = StatusAbsentRed, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.trim().isEmpty() || username.trim().isEmpty() || password.trim().isEmpty()) {
                        errorMsg = "Please fill all required fields / कृपया सभी जानकारी भरें"
                        return@Button
                    }
                    val op = Operator(
                        id = id.trim(),
                        name = name.trim(),
                        username = username.trim(),
                        password = password.trim(),
                        phone = phone.trim(),
                        shift = shift.trim()
                    )
                    onSave(op)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlueLight)
            ) {
                Text(AppStrings.t("save", language))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(AppStrings.t("cancel", language))
            }
        }
    )
}
