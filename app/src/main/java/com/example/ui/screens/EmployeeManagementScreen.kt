package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.data.model.Employee
import com.example.ui.components.AppHeader
import com.example.ui.components.AppSearchBar
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
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeManagementScreen(viewModel: AttendanceViewModel) {
    val language by viewModel.language.collectAsState()
    val employees by viewModel.allEmployees.collectAsState()
    val operators by viewModel.allOperators.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingEmployee by remember { mutableStateOf<Employee?>(null) }
    var deletingEmployee by remember { mutableStateOf<Employee?>(null) }
    var limitReachedWarning by remember { mutableStateOf(false) }

    val filteredEmployees = employees.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.id.contains(searchQuery, ignoreCase = true) ||
                it.designation.contains(searchQuery, ignoreCase = true) ||
                it.phone.contains(searchQuery, ignoreCase = true)
    }

    val operatorMap = operators.associateBy { it.id }

    Scaffold(
        topBar = {
            AppHeader(
                title = AppStrings.t("manage_employees", language),
                subtitle = "${employees.size}/10 Employees • Admin Only",
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
                    if (employees.size >= 10) {
                        limitReachedWarning = true
                    } else {
                        editingEmployee = null
                        showAddEditDialog = true
                    }
                },
                containerColor = if (employees.size >= 10) Color.Gray else PrimaryBlueLight,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_employee_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Employee")
            }
        },
        containerColor = NeutralBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Capacity Card (Manage up to 10 employees)
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
                            Text(
                                text = if (language == AppLanguage.HI) "कर्मचारी सीमा (अधिकतम 10)" else "Employee Limit (Max 10)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "${employees.size} / 10",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = if (employees.size >= 10) StatusAbsentRed else PrimaryBlueLight
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { employees.size / 10f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (employees.size >= 10) StatusAbsentRed else PrimaryBlueLight,
                            trackColor = Color(0xFFE2E8F0)
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (employees.size >= 10)
                                (if (language == AppLanguage.HI) "अधिकतम 10 कर्मचारी सीमा पूरी हो चुकी है।" else "Capacity full! 10/10 slots used.")
                            else
                                (if (language == AppLanguage.HI) "${10 - employees.size} और कर्मचारी जोड़े जा सकते हैं।" else "${10 - employees.size} more employee slots available."),
                            fontSize = 11.sp,
                            color = if (employees.size >= 10) StatusAbsentRed else TextSecondary
                        )
                    }
                }

                AppSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = AppStrings.t("search_hint", language)
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                if (filteredEmployees.isEmpty()) {
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
                                Icon(Icons.Default.Group, contentDescription = null, tint = TextMuted, modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (language == AppLanguage.HI) "कोई कर्मचारी नहीं मिला" else "No employees found",
                                    fontSize = 14.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                } else {
                    items(filteredEmployees, key = { it.id }) { employee ->
                        val assignedOp = operatorMap[employee.assignedOperatorId]
                        EmployeeAdminCard(
                            employee = employee,
                            assignedOperator = assignedOp,
                            language = language,
                            onEdit = {
                                editingEmployee = employee
                                showAddEditDialog = true
                            },
                            onDelete = {
                                deletingEmployee = employee
                            }
                        )
                    }
                }
            }
        }

        // Limit Reached Dialog
        if (limitReachedWarning) {
            AlertDialog(
                onDismissRequest = { limitReachedWarning = false },
                icon = {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = StatusAbsentRed, modifier = Modifier.size(32.dp))
                },
                title = { Text(AppStrings.t("limit_reached_title", language), fontWeight = FontWeight.Bold) },
                text = { Text(AppStrings.t("limit_reached_msg", language), fontSize = 13.sp) },
                confirmButton = {
                    Button(
                        onClick = { limitReachedWarning = false },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlueLight)
                    ) {
                        Text("OK")
                    }
                }
            )
        }

        // Delete Confirmation Dialog
        if (deletingEmployee != null) {
            AlertDialog(
                onDismissRequest = { deletingEmployee = null },
                icon = {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = StatusAbsentRed, modifier = Modifier.size(32.dp))
                },
                title = { Text(AppStrings.t("delete_employee", language), fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        text = "${AppStrings.t("delete_confirm", language)}\n\n${deletingEmployee?.name} (${deletingEmployee?.id})",
                        fontSize = 13.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            deletingEmployee?.let { viewModel.deleteEmployee(it.id) }
                            deletingEmployee = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusAbsentRed)
                    ) {
                        Text(AppStrings.t("delete_employee", language))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { deletingEmployee = null }) {
                        Text(AppStrings.t("cancel", language))
                    }
                }
            )
        }

        // Add / Edit Employee Dialog
        if (showAddEditDialog) {
            AddEditEmployeeDialog(
                initialEmployee = editingEmployee,
                existingEmployees = employees,
                operators = operators,
                language = language,
                onDismiss = { showAddEditDialog = false; editingEmployee = null },
                onSave = { emp ->
                    viewModel.saveEmployee(emp) { success, msg ->
                        if (success) {
                            showAddEditDialog = false
                            editingEmployee = null
                            viewModel.showToast(msg)
                        } else {
                            viewModel.showToast(msg)
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun EmployeeAdminCard(
    employee: Employee,
    assignedOperator: com.example.data.model.Operator?,
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(PrimaryBlueContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = employee.name.take(1).uppercase(Locale.getDefault()),
                        color = PrimaryBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = employee.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = SurfaceVariant
                        ) {
                            Text(
                                text = employee.id,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = employee.designation,
                        fontSize = 11.sp,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Engineering, contentDescription = null, tint = PrimaryBlueLight, modifier = Modifier.size(12.dp))
                        Text(
                            text = if (assignedOperator != null)
                                "${assignedOperator.name} (${assignedOperator.shift.take(7)}..)"
                            else
                                AppStrings.t("unassigned", language),
                            fontSize = 10.sp,
                            color = if (assignedOperator != null) PrimaryBlueLight else TextMuted,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp).testTag("edit_emp_${employee.id}")) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = PrimaryBlueLight, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp).testTag("delete_emp_${employee.id}")) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StatusAbsentRed, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditEmployeeDialog(
    initialEmployee: Employee?,
    existingEmployees: List<Employee>,
    operators: List<com.example.data.model.Operator>,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSave: (Employee) -> Unit
) {
    val isEdit = initialEmployee != null

    // Suggest default ID like "LW-107"
    val defaultId = remember(existingEmployees) {
        val nextNum = (existingEmployees.mapNotNull { it.id.replace("LW-", "").toIntOrNull() }.maxOrNull() ?: 100) + 1
        "LW-$nextNum"
    }

    var id by remember { mutableStateOf(initialEmployee?.id ?: defaultId) }
    var name by remember { mutableStateOf(initialEmployee?.name ?: "") }
    var phone by remember { mutableStateOf(initialEmployee?.phone ?: "") }
    var designation by remember { mutableStateOf(initialEmployee?.designation ?: "Loading Worker") }
    var selectedOperatorId by remember { mutableStateOf(initialEmployee?.assignedOperatorId ?: "") }

    var operatorDropdownExpanded by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEdit) AppStrings.t("edit_employee", language) else AppStrings.t("add_employee", language),
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Employee ID
                OutlinedTextField(
                    value = id,
                    onValueChange = { if (!isEdit) id = it },
                    label = { Text(AppStrings.t("employee_id", language)) },
                    singleLine = true,
                    enabled = !isEdit,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_emp_id_input")
                )

                // Employee Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; validationError = null },
                    label = { Text(AppStrings.t("employee_name", language)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_emp_name_input")
                )

                // Phone
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(AppStrings.t("phone_number", language)) },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Designation
                OutlinedTextField(
                    value = designation,
                    onValueChange = { designation = it },
                    label = { Text(AppStrings.t("designation", language)) },
                    placeholder = { Text("e.g. Loader, Forklift Driver, Stacker") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Assigned Operator Selector
                ExposedDropdownMenuBox(
                    expanded = operatorDropdownExpanded,
                    onExpandedChange = { operatorDropdownExpanded = it }
                ) {
                    val assignedOpName = operators.firstOrNull { it.id == selectedOperatorId }?.let { "${it.name} (${it.shift})" }
                        ?: AppStrings.t("unassigned", language)

                    OutlinedTextField(
                        value = assignedOpName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(AppStrings.t("assigned_operator", language)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = operatorDropdownExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = operatorDropdownExpanded,
                        onDismissRequest = { operatorDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(AppStrings.t("unassigned", language)) },
                            onClick = {
                                selectedOperatorId = ""
                                operatorDropdownExpanded = false
                            }
                        )
                        operators.forEach { op ->
                            DropdownMenuItem(
                                text = { Text("${op.name} • ${op.shift}") },
                                onClick = {
                                    selectedOperatorId = op.id
                                    operatorDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                if (validationError != null) {
                    Text(
                        text = validationError ?: "",
                        color = StatusAbsentRed,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.trim().isEmpty()) {
                        validationError = "Please enter employee name / कृपया नाम दर्ज करें"
                        return@Button
                    }
                    if (id.trim().isEmpty()) {
                        validationError = "Please enter ID / कृपया आईडी दर्ज करें"
                        return@Button
                    }
                    val emp = Employee(
                        id = id.trim(),
                        name = name.trim(),
                        phone = phone.trim(),
                        designation = designation.trim().ifEmpty { "Loading Worker" },
                        assignedOperatorId = selectedOperatorId
                    )
                    onSave(emp)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlueLight),
                modifier = Modifier.testTag("dialog_emp_save_btn")
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
