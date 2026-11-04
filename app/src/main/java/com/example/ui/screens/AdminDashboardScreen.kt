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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceStatus
import com.example.ui.components.AppHeader
import com.example.ui.components.DateSelectorBar
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.theme.NeutralBackground
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueContainer
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
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.AttendanceViewModel
import java.util.Locale

@Composable
fun AdminDashboardScreen(viewModel: AttendanceViewModel) {
    val language by viewModel.language.collectAsState()
    val employees by viewModel.allEmployees.collectAsState()
    val operators by viewModel.allOperators.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val attendanceRecords by viewModel.attendanceForSelectedDate.collectAsState()
    val summary by viewModel.todaySummary.collectAsState()

    // Map records by employee ID
    val recordMap = attendanceRecords.associateBy { it.employeeId }

    Scaffold(
        topBar = {
            AppHeader(
                title = AppStrings.t("admin", language) + " " + AppStrings.t("dashboard", language),
                subtitle = "Loading Works • Admin Console",
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
            // 1. Date Selector
            item {
                DateSelectorBar(
                    currentDateStr = selectedDate,
                    onDateChanged = { viewModel.setSelectedDate(it) },
                    language = language
                )
            }

            // 2. Capacity Card (Manage up to 10 employees mandate)
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
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryBlueContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Group,
                                        contentDescription = null,
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = AppStrings.t("manage_employees", language),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${employees.size} / 10 Active Workers",
                                        fontSize = 12.sp,
                                        color = if (employees.size >= 10) StatusAbsentRed else PrimaryBlueLight,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Button(
                                onClick = { viewModel.navigateTo(AppScreen.EMPLOYEE_MANAGEMENT) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlueLight),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("manage_emp_btn")
                            ) {
                                Text(
                                    text = if (language == AppLanguage.HI) "प्रबंधन" else "Manage",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { employees.size / 10f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (employees.size >= 10) StatusAbsentRed else PrimaryBlueLight,
                            trackColor = Color(0xFFE2E8F0),
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (employees.size >= 10)
                                (if (language == AppLanguage.HI) "अधिकतम 10 कर्मचारियों की सीमा पूरी हो गई है" else "Max limit of 10 employees reached")
                            else
                                (if (language == AppLanguage.HI) "${10 - employees.size} नए कर्मचारी जोड़े जा सकते हैं" else "${10 - employees.size} slots available to add"),
                            fontSize = 11.sp,
                            color = if (employees.size >= 10) StatusAbsentRed else TextSecondary
                        )
                    }
                }
            }

            // 3. Today's Attendance Overview Metrics
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
                            Column {
                                Text(
                                    text = AppStrings.t("today_attendance", language),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${AppStrings.t("attendance_rate", language)}: ${String.format(Locale.getDefault(), "%.1f", summary.attendanceRate)}%",
                                    fontSize = 12.sp,
                                    color = StatusPresentGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = { viewModel.navigateTo(AppScreen.MARK_ATTENDANCE) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = StatusPresentGreen),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                modifier = Modifier.testTag("admin_mark_att_quick_btn")
                            ) {
                                Icon(Icons.Default.HowToReg, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = AppStrings.t("mark_attendance", language),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 4 Stat Cards in 2x2 grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatCard(
                                title = AppStrings.t("present", language),
                                value = "${summary.presentCount}",
                                accentColor = StatusPresentGreen,
                                bgColor = StatusPresentBg,
                                iconLetter = "P",
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = AppStrings.t("absent", language),
                                value = "${summary.absentCount}",
                                accentColor = StatusAbsentRed,
                                bgColor = StatusAbsentBg,
                                iconLetter = "A",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatCard(
                                title = AppStrings.t("half_day", language),
                                value = "${summary.halfDayCount}",
                                accentColor = StatusHalfDayOrange,
                                bgColor = StatusHalfDayBg,
                                iconLetter = "H",
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = AppStrings.t("leave", language),
                                value = "${summary.leaveCount}",
                                accentColor = StatusLeavePurple,
                                bgColor = StatusLeaveBg,
                                iconLetter = "L",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 4. Quick Actions Hub (Large tactile navigation buttons)
            item {
                Text(
                    text = if (language == AppLanguage.HI) "त्वरित सेवाएं / Actions" else "Quick Actions",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickActionTile(
                            icon = Icons.Default.Assessment,
                            title = AppStrings.t("reports", language),
                            subtitle = if (language == AppLanguage.HI) "दैनिक, साप्ताहिक, मासिक" else "Daily / Weekly / Monthly",
                            bgColor = Color(0xFFE8EAF6),
                            accentColor = Color(0xFF283593),
                            onClick = { viewModel.navigateTo(AppScreen.REPORTS) },
                            modifier = Modifier.weight(1f).testTag("action_reports_btn")
                        )

                        QuickActionTile(
                            icon = Icons.Default.Engineering,
                            title = AppStrings.t("manage_operators", language),
                            subtitle = "${operators.size} Active Operators",
                            bgColor = Color(0xFFE0F7FA),
                            accentColor = Color(0xFF006064),
                            onClick = { viewModel.navigateTo(AppScreen.OPERATOR_MANAGEMENT) },
                            modifier = Modifier.weight(1f).testTag("action_operators_btn")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickActionTile(
                            icon = Icons.Default.CalendarMonth,
                            title = AppStrings.t("calendar", language),
                            subtitle = if (language == AppLanguage.HI) "कैलेंडर दृश्य" else "Monthly Calendar View",
                            bgColor = Color(0xFFFFF8E1),
                            accentColor = Color(0xFFF57F17),
                            onClick = { viewModel.navigateTo(AppScreen.CALENDAR) },
                            modifier = Modifier.weight(1f).testTag("action_calendar_btn")
                        )

                        QuickActionTile(
                            icon = Icons.Default.Backup,
                            title = AppStrings.t("settings", language),
                            subtitle = if (language == AppLanguage.HI) "डेटा बैकअप & पासवर्ड" else "Backup & Password",
                            bgColor = Color(0xFFECEFF1),
                            accentColor = Color(0xFF37474F),
                            onClick = { viewModel.navigateTo(AppScreen.BACKUP_SETTINGS) },
                            modifier = Modifier.weight(1f).testTag("action_backup_btn")
                        )
                    }
                }
            }

            // 5. Today's Attendance Roster Preview
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (language == AppLanguage.HI) "कर्मचारी हज़िरी स्थिति" else "Today's Worker Roster",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )

                    Text(
                        text = "${attendanceRecords.size}/${employees.size} Marked",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (employees.isEmpty()) {
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
                            Text("No employees added yet.", color = TextMuted)
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(onClick = { viewModel.resetSampleData() }) {
                                Text("Load Sample Employees")
                            }
                        }
                    }
                }
            } else {
                items(employees, key = { it.id }) { emp ->
                    val record = recordMap[emp.id]
                    WorkerRosterItem(
                        employee = emp,
                        record = record,
                        language = language,
                        onQuickMark = { status ->
                            viewModel.markAttendance(
                                employeeId = emp.id,
                                employeeName = emp.name,
                                status = status
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    bgColor: Color,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = TextPrimary,
                maxLines = 1
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = TextMuted,
                maxLines = 1
            )
        }
    }
}

@Composable
fun WorkerRosterItem(
    employee: com.example.data.model.Employee,
    record: com.example.data.model.AttendanceRecord?,
    language: AppLanguage,
    onQuickMark: (AttendanceStatus) -> Unit
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
                            .size(38.dp)
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
                            text = record.time,
                            fontSize = 10.sp,
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
                            text = if (language == AppLanguage.HI) "लंबित" else "Pending",
                            fontSize = 11.sp,
                            color = StatusHalfDayOrange,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick one-touch attendance buttons for each status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AttendanceStatus.entries.forEach { status ->
                    val isSelected = record?.status == status.name
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) status.color else status.bgColor,
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onQuickMark(status) }
                            .testTag("quick_mark_${employee.id}_${status.code}"),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = status.code,
                                color = if (isSelected) Color.White else status.color,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
