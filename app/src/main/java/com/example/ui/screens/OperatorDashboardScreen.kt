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
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.ui.components.DateSelectorBar
import com.example.ui.components.StatusBadge
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.theme.NeutralBackground
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueContainer
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.StatusHalfDayOrange
import com.example.ui.theme.StatusPresentGreen
import com.example.ui.theme.SurfaceVariant
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AttendanceViewModel
import java.util.Locale

@Composable
fun OperatorDashboardScreen(viewModel: AttendanceViewModel) {
    val language by viewModel.language.collectAsState()
    val operator by viewModel.currentOperator.collectAsState()
    val allEmployees by viewModel.allEmployees.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val attendanceRecords by viewModel.attendanceForSelectedDate.collectAsState()
    val allAttendance by viewModel.allAttendance.collectAsState()

    var activeTab by remember { mutableIntStateOf(0) } // 0: Mark Attendance, 1: My Activity History

    // Strict Operator Isolation: Filter employees assigned ONLY to this operator
    val opId = operator?.id ?: ""
    val assignedEmployees = allEmployees.filter { it.assignedOperatorId == opId && it.isActive }
    val recordMap = attendanceRecords.associateBy { it.employeeId }

    // Attendance marked specifically by this operator
    val operatorMarkedRecords = allAttendance.filter { it.markedById == opId }

    Scaffold(
        topBar = {
            AppHeader(
                title = AppStrings.t("my_dashboard", language),
                subtitle = "Operator: ${operator?.name ?: "Operator"} • ${operator?.shift ?: ""}",
                currentLanguage = language,
                onLanguageChange = { viewModel.setLanguage(it) },
                showBackButton = false,
                showLogout = true,
                onLogoutClick = { viewModel.logout() }
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
            // 1. Operator Info Banner with Security Isolation Tag
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryBlue),
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
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Engineering,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = operator?.name ?: "Operator",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${operator?.id ?: ""} • ${operator?.shift ?: ""}",
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                    Text(
                                        text = if (language == AppLanguage.HI) "ऑपरेटर मोड" else "Operator Mode",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = if (language == AppLanguage.HI)
                                "आप केवल अपने आवंटित ${assignedEmployees.size} मजदूरों की हज़िरी लगा सकते हैं।"
                            else
                                "You have ${assignedEmployees.size} loading workers assigned to your shift.",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            // 2. Date Selector Bar
            item {
                DateSelectorBar(
                    currentDateStr = selectedDate,
                    onDateChanged = { viewModel.setSelectedDate(it) },
                    language = language
                )
            }

            // 3. Tab Navigation (Mark Attendance vs My Activity)
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TabRow(
                        selectedTabIndex = activeTab,
                        containerColor = SurfaceWhite,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                                color = PrimaryBlueLight,
                                height = 3.dp
                            )
                        }
                    ) {
                        Tab(
                            selected = activeTab == 0,
                            onClick = { activeTab = 0 },
                            modifier = Modifier.padding(vertical = 12.dp).testTag("op_mark_tab"),
                            text = {
                                Text(
                                    text = AppStrings.t("mark_attendance", language),
                                    fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Medium,
                                    color = if (activeTab == 0) PrimaryBlueLight else TextSecondary,
                                    fontSize = 13.sp
                                )
                            }
                        )
                        Tab(
                            selected = activeTab == 1,
                            onClick = { activeTab = 1 },
                            modifier = Modifier.padding(vertical = 12.dp).testTag("op_history_tab"),
                            text = {
                                Text(
                                    text = AppStrings.t("my_activity_log", language),
                                    fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Medium,
                                    color = if (activeTab == 1) PrimaryBlueLight else TextSecondary,
                                    fontSize = 13.sp
                                )
                            }
                        )
                    }
                }
            }

            if (activeTab == 0) {
                // === MARK ATTENDANCE FOR ASSIGNED EMPLOYEES ===
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${AppStrings.t("assigned_workers_count", language)} (${assignedEmployees.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )

                        if (assignedEmployees.isNotEmpty()) {
                            Button(
                                onClick = {
                                    viewModel.markAllPresentForActiveList(assignedEmployees)
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = StatusPresentGreen),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("op_mark_all_present_btn")
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

                if (assignedEmployees.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = StatusHalfDayOrange,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = if (language == AppLanguage.HI)
                                        "आपकी शिफ्ट में अभी कोई कर्मचारी असाइन नहीं है।"
                                    else
                                        "No workers currently assigned to your shift.",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = if (language == AppLanguage.HI)
                                        "कर्मचारी असाइन करवाने के लिए कृपया एडमिन से संपर्क करें।"
                                    else
                                        "Please ask the Administrator to assign loading workers to your Operator ID ($opId).",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                } else {
                    items(assignedEmployees, key = { it.id }) { emp ->
                        val record = recordMap[emp.id]
                        OperatorWorkerAttendanceCard(
                            employee = emp,
                            record = record,
                            language = language,
                            onStatusSelected = { status ->
                                viewModel.markAttendance(
                                    employeeId = emp.id,
                                    employeeName = emp.name,
                                    status = status
                                )
                            }
                        )
                    }
                }
            } else {
                // === OPERATOR'S OWN ACTIVITY HISTORY LOG ===
                item {
                    Text(
                        text = if (language == AppLanguage.HI)
                            "मेरे द्वारा दर्ज किए गए रिकॉर्ड्स (${operatorMarkedRecords.size})"
                        else
                            "Records Marked By You (${operatorMarkedRecords.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                }

                if (operatorMarkedRecords.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.History, contentDescription = null, tint = TextMuted, modifier = Modifier.size(36.dp))
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
                    items(operatorMarkedRecords, key = { "${it.id}_${it.employeeId}_${it.date}" }) { record ->
                        OperatorHistoryItem(record = record, language = language)
                    }
                }
            }
        }
    }
}

@Composable
fun OperatorWorkerAttendanceCard(
    employee: Employee,
    record: AttendanceRecord?,
    language: AppLanguage,
    onStatusSelected: (AttendanceStatus) -> Unit
) {
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
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                            fontSize = 18.sp
                        )
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = employee.name,
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
                                    text = employee.id,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = employee.designation,
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }

                if (record != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        StatusBadge(
                            status = AttendanceStatus.fromCode(record.status),
                            compact = false,
                            language = language
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = TextMuted, modifier = Modifier.size(11.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = record.time,
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFFFF3E0)
                    ) {
                        Text(
                            text = if (language == AppLanguage.HI) "हज़िरी दर्ज करें" else "Mark Now",
                            fontSize = 11.sp,
                            color = StatusHalfDayOrange,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4 Big Large tactile buttons as demanded in guidelines
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AttendanceStatus.entries.forEach { status ->
                    val isSelected = record?.status == status.name
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) status.color else status.bgColor,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onStatusSelected(status) }
                            .testTag("op_mark_${employee.id}_${status.code}")
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
                                fontSize = 16.sp
                            )
                            Text(
                                text = if (language == AppLanguage.HI) status.labelHi else status.labelEn,
                                color = if (isSelected) Color.White.copy(alpha = 0.9f) else status.color,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OperatorHistoryItem(
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
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = record.employeeName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
                Text(
                    text = "${record.employeeId} • ${record.date} at ${record.time}",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            StatusBadge(
                status = AttendanceStatus.fromCode(record.status),
                compact = true,
                language = language
            )
        }
    }
}
