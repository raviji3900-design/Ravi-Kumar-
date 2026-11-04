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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.Employee
import com.example.ui.components.AppHeader
import com.example.ui.components.AppSearchBar
import com.example.ui.components.DateSelectorBar
import com.example.ui.components.StatusBadge
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.theme.NeutralBackground
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueContainer
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.StatusAbsentRed
import com.example.ui.theme.StatusHalfDayOrange
import com.example.ui.theme.StatusPresentGreen
import com.example.ui.theme.SurfaceVariant
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AttendanceViewModel
import com.example.ui.viewmodel.UserRole
import java.util.Locale

@Composable
fun MarkAttendanceScreen(viewModel: AttendanceViewModel) {
    val language by viewModel.language.collectAsState()
    val allEmployees by viewModel.allEmployees.collectAsState()
    val currentOperator by viewModel.currentOperator.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val attendanceRecords by viewModel.attendanceForSelectedDate.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf<String?>("ALL") }

    // State for duplicate attendance confirmation dialog
    var pendingChangeTarget by remember { mutableStateOf<Pair<Employee, AttendanceStatus>?>(null) }
    var existingRecordForTarget by remember { mutableStateOf<AttendanceRecord?>(null) }

    // If Operator, only show their assigned employees. If Admin, show all.
    val baseEmployees = if (userRole == UserRole.OPERATOR) {
        val opId = currentOperator?.id ?: ""
        allEmployees.filter { it.assignedOperatorId == opId && it.isActive }
    } else {
        allEmployees.filter { it.isActive }
    }

    val recordMap = attendanceRecords.associateBy { it.employeeId }

    val filteredEmployees = baseEmployees.filter { emp ->
        val matchesSearch = emp.name.contains(searchQuery, ignoreCase = true) ||
                emp.id.contains(searchQuery, ignoreCase = true) ||
                emp.designation.contains(searchQuery, ignoreCase = true)

        val record = recordMap[emp.id]
        val matchesStatus = when (selectedStatusFilter) {
            "ALL" -> true
            "PENDING" -> record == null
            else -> record?.status == selectedStatusFilter
        }

        matchesSearch && matchesStatus
    }

    Scaffold(
        topBar = {
            AppHeader(
                title = AppStrings.t("mark_attendance", language),
                subtitle = "Date: $selectedDate",
                currentLanguage = language,
                onLanguageChange = { viewModel.setLanguage(it) },
                showBackButton = true,
                onBackClick = { viewModel.navigateBack() },
                showLogout = false
            )
        },
        containerColor = NeutralBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header Controls: Date & Search
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DateSelectorBar(
                    currentDateStr = selectedDate,
                    onDateChanged = { viewModel.setSelectedDate(it) },
                    language = language
                )

                AppSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = AppStrings.t("search_hint", language)
                )

                // Quick Filter Chips (All, Pending, Present, Absent, Half Day, Leave)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val filterOptions = listOf(
                        "ALL" to (if (language == AppLanguage.HI) "सभी" else "All"),
                        "PENDING" to (if (language == AppLanguage.HI) "बाकी" else "Pending"),
                        AttendanceStatus.PRESENT.name to "P",
                        AttendanceStatus.ABSENT.name to "A",
                        AttendanceStatus.HALF_DAY.name to "H",
                        AttendanceStatus.LEAVE.name to "L"
                    )

                    filterOptions.forEach { (key, label) ->
                        val isSelected = selectedStatusFilter == key
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedStatusFilter = key },
                            label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryBlueLight,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(32.dp)
                        )
                    }
                }

                // Batch Mark All Present Action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${filteredEmployees.size} Workers (${attendanceRecords.size}/${baseEmployees.size} marked)",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (filteredEmployees.isNotEmpty()) {
                        Button(
                            onClick = { viewModel.markAllPresentForActiveList(filteredEmployees) },
                            colors = ButtonDefaults.buttonColors(containerColor = StatusPresentGreen),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("mark_all_present_btn")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = AppStrings.t("mark_all_present", language),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Employee List with Attendance Status & Selection Buttons
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
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
                                Icon(Icons.Default.Info, contentDescription = null, tint = TextMuted, modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (language == AppLanguage.HI) "कोई कर्मचारी नहीं मिला" else "No matching employees found",
                                    fontSize = 14.sp,
                                    color = TextMuted,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                } else {
                    items(filteredEmployees, key = { it.id }) { employee ->
                        val record = recordMap[employee.id]
                        AttendanceCard(
                            employee = employee,
                            record = record,
                            language = language,
                            onStatusClick = { newStatus ->
                                if (record != null && record.status != newStatus.name) {
                                    // Prevent duplicate attendance: show confirmation dialog before altering
                                    existingRecordForTarget = record
                                    pendingChangeTarget = Pair(employee, newStatus)
                                } else {
                                    viewModel.markAttendance(
                                        employeeId = employee.id,
                                        employeeName = employee.name,
                                        status = newStatus
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }

        // Duplicate Attendance Confirmation Dialog
        if (pendingChangeTarget != null && existingRecordForTarget != null) {
            val (emp, newStatus) = pendingChangeTarget!!
            val oldRecord = existingRecordForTarget!!
            val oldStatusObj = AttendanceStatus.fromCode(oldRecord.status)

            AlertDialog(
                onDismissRequest = {
                    pendingChangeTarget = null
                    existingRecordForTarget = null
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = StatusHalfDayOrange,
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = {
                    Text(
                        text = AppStrings.t("duplicate_attendance", language),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "${emp.name} (${emp.id})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = PrimaryBlue
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (language == AppLanguage.HI)
                                "आज की हज़िरी पहले ही '${oldStatusObj.labelHi} (${oldStatusObj.code})' के रूप में समय ${oldRecord.time} पर दर्ज हो चुकी है। क्या आप इसे '${newStatus.labelHi} (${newStatus.code})' में बदलना चाहते हैं?"
                            else
                                "Attendance was already marked as '${oldStatusObj.labelEn} (${oldStatusObj.code})' at ${oldRecord.time}. Do you want to update it to '${newStatus.labelEn} (${newStatus.code})'?",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.markAttendance(
                                employeeId = emp.id,
                                employeeName = emp.name,
                                status = newStatus
                            )
                            pendingChangeTarget = null
                            existingRecordForTarget = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlueLight)
                    ) {
                        Text(if (language == AppLanguage.HI) "हाँ, अपडेट करें" else "Update Attendance")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            pendingChangeTarget = null
                            existingRecordForTarget = null
                        }
                    ) {
                        Text(AppStrings.t("cancel", language))
                    }
                }
            )
        }
    }
}

@Composable
fun AttendanceCard(
    employee: Employee,
    record: AttendanceRecord?,
    language: AppLanguage,
    onStatusClick: (AttendanceStatus) -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(1.5.dp),
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
                        Text(
                            text = employee.name.take(1).uppercase(Locale.getDefault()),
                            color = PrimaryBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
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
                    }
                }

                if (record != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        StatusBadge(
                            status = AttendanceStatus.fromCode(record.status),
                            compact = true,
                            language = language
                        )
                        Text(
                            text = "${record.time} • ${record.markedByName}",
                            fontSize = 9.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFFFF3E0)
                    ) {
                        Text(
                            text = if (language == AppLanguage.HI) "हज़िरी बाकी" else "Not Marked",
                            fontSize = 10.sp,
                            color = StatusHalfDayOrange,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4 Large tactile choice buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AttendanceStatus.entries.forEach { status ->
                    val isSelected = record?.status == status.name
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) status.color else status.bgColor,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onStatusClick(status) }
                            .testTag("att_btn_${employee.id}_${status.code}")
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = status.code,
                                color = if (isSelected) Color.White else status.color,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = if (language == AppLanguage.HI) status.labelHi else status.labelEn,
                                color = if (isSelected) Color.White.copy(alpha = 0.9f) else status.color,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
