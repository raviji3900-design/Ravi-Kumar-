package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.ui.components.AppHeader
import com.example.ui.components.DateSelectorBar
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.theme.NeutralBackground
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.StatusAbsentBg
import com.example.ui.theme.StatusAbsentRed
import com.example.ui.theme.StatusHalfDayBg
import com.example.ui.theme.StatusHalfDayOrange
import com.example.ui.theme.StatusLeaveBg
import com.example.ui.theme.StatusLeavePurple
import com.example.ui.theme.StatusPresentBg
import com.example.ui.theme.StatusPresentGreen
import com.example.ui.theme.SurfaceVariant
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AttendanceViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(viewModel: AttendanceViewModel) {
    val context = LocalContext.current
    val language by viewModel.language.collectAsState()
    val allAttendance by viewModel.allAttendance.collectAsState()
    val allEmployees by viewModel.allEmployees.collectAsState()
    val allOperators by viewModel.allOperators.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    // 0: Daily, 1: Weekly, 2: Monthly, 3: Employee-wise, 4: Operator-wise
    var reportTab by remember { mutableIntStateOf(0) }

    var selectedEmployeeId by remember { mutableStateOf(allEmployees.firstOrNull()?.id ?: "") }
    var employeeDropdownExpanded by remember { mutableStateOf(false) }

    var selectedOperatorId by remember { mutableStateOf(allOperators.firstOrNull()?.id ?: "") }
    var operatorDropdownExpanded by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    // Filtered records depending on active report tab
    val filteredRecords = remember(reportTab, selectedDate, selectedEmployeeId, selectedOperatorId, allAttendance) {
        val cal = Calendar.getInstance()
        val baseDate = try { dateFormat.parse(selectedDate) ?: Date() } catch (e: Exception) { Date() }

        when (reportTab) {
            0 -> {
                // Daily Report for selectedDate
                allAttendance.filter { it.date == selectedDate }
            }
            1 -> {
                // Weekly Report: Last 7 days up to selectedDate
                cal.time = baseDate
                cal.add(Calendar.DAY_OF_YEAR, -6)
                val startStr = dateFormat.format(cal.time)
                allAttendance.filter { it.date in startStr..selectedDate }
            }
            2 -> {
                // Monthly Report: Same Year-Month as selectedDate
                val ym = selectedDate.take(7) // "YYYY-MM"
                allAttendance.filter { it.date.startsWith(ym) }
            }
            3 -> {
                // Employee-wise
                if (selectedEmployeeId.isNotEmpty()) {
                    allAttendance.filter { it.employeeId == selectedEmployeeId }
                } else {
                    allAttendance
                }
            }
            4 -> {
                // Operator-wise
                if (selectedOperatorId.isNotEmpty()) {
                    allAttendance.filter { it.markedById == selectedOperatorId }
                } else {
                    allAttendance
                }
            }
            else -> allAttendance
        }
    }

    // Metrics for filtered records
    var pCount = 0
    var aCount = 0
    var hCount = 0
    var lCount = 0
    for (r in filteredRecords) {
        when (AttendanceStatus.fromCode(r.status)) {
            AttendanceStatus.PRESENT -> pCount++
            AttendanceStatus.ABSENT -> aCount++
            AttendanceStatus.HALF_DAY -> hCount++
            AttendanceStatus.LEAVE -> lCount++
        }
    }
    val totalCount = filteredRecords.size
    val rate = if (totalCount > 0) {
        ((pCount + (hCount * 0.5f)) / totalCount.toFloat()) * 100f
    } else 0f

    val reportTitle = when (reportTab) {
        0 -> "Daily Attendance Report ($selectedDate)"
        1 -> "Weekly Attendance Report (Last 7 Days)"
        2 -> "Monthly Attendance Report (${selectedDate.take(7)})"
        3 -> "Employee Report (${allEmployees.firstOrNull { it.id == selectedEmployeeId }?.name ?: selectedEmployeeId})"
        4 -> "Operator Activity Report (${allOperators.firstOrNull { it.id == selectedOperatorId }?.name ?: selectedOperatorId})"
        else -> "Attendance Report"
    }

    val summaryString = "Total Logs: $totalCount | Present: $pCount | Absent: $aCount | Half Day: $hCount | Leave: $lCount | Rate: ${String.format(Locale.getDefault(), "%.1f", rate)}%"

    Scaffold(
        topBar = {
            AppHeader(
                title = AppStrings.t("reports", language),
                subtitle = "Export to Excel, PDF & Analytics",
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Report Type Filter Tabs
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val tabs = listOf(
                        0 to AppStrings.t("daily_report", language),
                        1 to AppStrings.t("weekly_report", language),
                        2 to AppStrings.t("monthly_report", language),
                        3 to AppStrings.t("employee_report", language),
                        4 to AppStrings.t("operator_report", language)
                    )

                    items(tabs) { (index, label) ->
                        val isSelected = reportTab == index
                        FilterChip(
                            selected = isSelected,
                            onClick = { reportTab = index },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryBlueLight,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            // 2. Contextual Controls (Date Bar or Dropdowns)
            item {
                when (reportTab) {
                    0, 1, 2 -> {
                        DateSelectorBar(
                            currentDateStr = selectedDate,
                            onDateChanged = { viewModel.setSelectedDate(it) },
                            language = language
                        )
                    }
                    3 -> {
                        // Employee Selector Dropdown
                        ExposedDropdownMenuBox(
                            expanded = employeeDropdownExpanded,
                            onExpandedChange = { employeeDropdownExpanded = it }
                        ) {
                            val curName = allEmployees.firstOrNull { it.id == selectedEmployeeId }?.let { "${it.name} (${it.id})" } ?: "Select Employee"
                            OutlinedTextField(
                                value = curName,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(AppStrings.t("employee_report", language)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = employeeDropdownExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            ExposedDropdownMenu(
                                expanded = employeeDropdownExpanded,
                                onDismissRequest = { employeeDropdownExpanded = false }
                            ) {
                                allEmployees.forEach { emp ->
                                    DropdownMenuItem(
                                        text = { Text("${emp.name} (${emp.id}) • ${emp.designation}") },
                                        onClick = {
                                            selectedEmployeeId = emp.id
                                            employeeDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    4 -> {
                        // Operator Selector Dropdown
                        ExposedDropdownMenuBox(
                            expanded = operatorDropdownExpanded,
                            onExpandedChange = { operatorDropdownExpanded = it }
                        ) {
                            val curOpName = allOperators.firstOrNull { it.id == selectedOperatorId }?.let { "${it.name} (${it.shift})" } ?: "Select Operator"
                            OutlinedTextField(
                                value = curOpName,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(AppStrings.t("operator_report", language)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = operatorDropdownExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            ExposedDropdownMenu(
                                expanded = operatorDropdownExpanded,
                                onDismissRequest = { operatorDropdownExpanded = false }
                            ) {
                                allOperators.forEach { op ->
                                    DropdownMenuItem(
                                        text = { Text("${op.name} (${op.id}) • ${op.shift}") },
                                        onClick = {
                                            selectedOperatorId = op.id
                                            operatorDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Overview Statistics Card
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
                            Text(
                                text = reportTitle,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = PrimaryBlue,
                                maxLines = 1,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${String.format(Locale.getDefault(), "%.1f", rate)}%",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = StatusPresentGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatCard(
                                title = AppStrings.t("present", language),
                                value = "$pCount",
                                accentColor = StatusPresentGreen,
                                bgColor = StatusPresentBg,
                                iconLetter = "P",
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = AppStrings.t("absent", language),
                                value = "$aCount",
                                accentColor = StatusAbsentRed,
                                bgColor = StatusAbsentBg,
                                iconLetter = "A",
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = AppStrings.t("half_day", language),
                                value = "$hCount",
                                accentColor = StatusHalfDayOrange,
                                bgColor = StatusHalfDayBg,
                                iconLetter = "H",
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = AppStrings.t("leave", language),
                                value = "$lCount",
                                accentColor = StatusLeavePurple,
                                bgColor = StatusLeaveBg,
                                iconLetter = "L",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 4. Export Buttons (Excel, PDF, Copy)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.exportCsvReport(filteredRecords, reportTitle.replace(" ", "_").replace("(", "").replace(")", ""))
                        },
                        modifier = Modifier.weight(1f).testTag("export_excel_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)) // Excel Forest Green
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = AppStrings.t("export_excel", language),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.printPdfReport(reportTitle, filteredRecords, summaryString)
                        },
                        modifier = Modifier.weight(1f).testTag("export_pdf_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlueLight)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = AppStrings.t("export_pdf", language),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }

            // Quick Copy summary text
            item {
                OutlinedButton(
                    onClick = {
                        val textToCopy = buildString {
                            appendLine("Loading Works Attendance Report")
                            appendLine(reportTitle)
                            appendLine(summaryString)
                            appendLine("--- Records (${filteredRecords.size}) ---")
                            for (r in filteredRecords) {
                                appendLine("${r.date} | ${r.employeeId} - ${r.employeeName} | ${r.status} (${r.time}) | By: ${r.markedByName}")
                            }
                        }
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Attendance Report", textToCopy))
                        viewModel.showToast("Report copied to clipboard! / रिपोर्ट कॉपी हो गई!")
                    },
                    modifier = Modifier.fillMaxWidth().testTag("copy_report_btn"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (language == AppLanguage.HI) "रिपोर्ट टेक्स्ट कॉपी करें" else "Copy Report Summary",
                        fontSize = 12.sp
                    )
                }
            }

            // 5. Detailed Records Table / List
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (language == AppLanguage.HI) "हज़िरी रिकॉर्ड विवरण" else "Attendance Records Breakdown",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "${filteredRecords.size} Entries",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (filteredRecords.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Description, contentDescription = null, tint = TextMuted, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = AppStrings.t("no_records", language),
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            } else {
                items(filteredRecords, key = { "${it.id}_${it.employeeId}_${it.date}" }) { record ->
                    ReportRecordCard(record = record, language = language)
                }
            }
        }
    }
}

@Composable
fun ReportRecordCard(
    record: AttendanceRecord,
    language: AppLanguage
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = record.employeeName,
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
                            text = record.employeeId,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = "${record.date} at ${record.time} • Marked by ${record.markedByName} (${record.markedByRole})",
                    fontSize = 11.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(top = 2.dp)
                )

                if (record.notes.isNotEmpty()) {
                    Text(
                        text = "Note: ${record.notes}",
                        fontSize = 10.sp,
                        color = PrimaryBlueLight,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            StatusBadge(
                status = AttendanceStatus.fromCode(record.status),
                compact = true,
                language = language
            )
        }
    }
}
