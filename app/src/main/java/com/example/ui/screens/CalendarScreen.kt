package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceStatus
import com.example.ui.components.AppHeader
import com.example.ui.components.StatusBadge
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.theme.NeutralBackground
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueContainer
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.StatusAbsentRed
import com.example.ui.theme.StatusHalfDayOrange
import com.example.ui.theme.StatusLeavePurple
import com.example.ui.theme.StatusPresentGreen
import com.example.ui.theme.SurfaceVariant
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.AttendanceViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CalendarScreen(viewModel: AttendanceViewModel) {
    val language by viewModel.language.collectAsState()
    val allAttendance by viewModel.allAttendance.collectAsState()
    val allEmployees by viewModel.allEmployees.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val monthTitleFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }

    var currentCalendar by remember {
        mutableStateOf(Calendar.getInstance().apply {
            try {
                time = dateFormat.parse(selectedDate) ?: Date()
            } catch (e: Exception) {
                time = Date()
            }
        })
    }

    // Group all records by date
    val attendanceByDate = remember(allAttendance) {
        allAttendance.groupBy { it.date }
    }

    // Daily records for currently selected date
    val selectedDayRecords = attendanceByDate[selectedDate] ?: emptyList()
    val recordMap = selectedDayRecords.associateBy { it.employeeId }

    Scaffold(
        topBar = {
            AppHeader(
                title = AppStrings.t("calendar", language),
                subtitle = "Monthly Attendance View",
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
            // Calendar Month Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Month Header with Prev & Next
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    val nextCal = currentCalendar.clone() as Calendar
                                    nextCal.add(Calendar.MONTH, -1)
                                    currentCalendar = nextCal
                                }
                            ) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = "Prev Month", tint = PrimaryBlue)
                            }

                            Text(
                                text = monthTitleFormat.format(currentCalendar.time),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = PrimaryBlue
                            )

                            IconButton(
                                onClick = {
                                    val nextCal = currentCalendar.clone() as Calendar
                                    nextCal.add(Calendar.MONTH, 1)
                                    currentCalendar = nextCal
                                }
                            ) {
                                Icon(Icons.Default.ChevronRight, contentDescription = "Next Month", tint = PrimaryBlue)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Days of week row
                        val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            daysOfWeek.forEach { day ->
                                Text(
                                    text = day,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (day == "Sun") StatusAbsentRed else TextSecondary,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Calendar Grid Days
                        val cal = currentCalendar.clone() as Calendar
                        cal.set(Calendar.DAY_OF_MONTH, 1)
                        val startDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1 // 0-based Sunday
                        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

                        val totalCells = ((startDayOfWeek + maxDays + 6) / 7) * 7

                        val rows = totalCells / 7

                        for (r in 0 until rows) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                for (c in 0 until 7) {
                                    val dayIndex = r * 7 + c
                                    val dayNum = dayIndex - startDayOfWeek + 1

                                    if (dayNum in 1..maxDays) {
                                        cal.set(Calendar.DAY_OF_MONTH, dayNum)
                                        val cellDateStr = dateFormat.format(cal.time)
                                        val isSelected = cellDateStr == selectedDate
                                        val recordsOnDay = attendanceByDate[cellDateStr] ?: emptyList()

                                        val hasPresent = recordsOnDay.any { it.status == AttendanceStatus.PRESENT.name }
                                        val hasAbsent = recordsOnDay.any { it.status == AttendanceStatus.ABSENT.name }
                                        val hasLeave = recordsOnDay.any { it.status == AttendanceStatus.LEAVE.name || it.status == AttendanceStatus.HALF_DAY.name }

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSelected) PrimaryBlueLight else Color.Transparent)
                                                .border(
                                                    width = if (isSelected) 0.dp else 0.5.dp,
                                                    color = if (isSelected) Color.Transparent else Color(0xFFF1F5F9),
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .clickable {
                                                    viewModel.setSelectedDate(cellDateStr)
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    text = "$dayNum",
                                                    fontSize = 12.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) Color.White else TextPrimary
                                                )

                                                // Attendance mini dots
                                                if (recordsOnDay.isNotEmpty()) {
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                        modifier = Modifier.padding(top = 2.dp)
                                                    ) {
                                                        if (hasPresent) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(4.dp)
                                                                    .clip(CircleShape)
                                                                    .background(if (isSelected) Color.White else StatusPresentGreen)
                                                            )
                                                        }
                                                        if (hasAbsent) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(4.dp)
                                                                    .clip(CircleShape)
                                                                    .background(if (isSelected) Color.White else StatusAbsentRed)
                                                            )
                                                        }
                                                        if (hasLeave) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(4.dp)
                                                                    .clip(CircleShape)
                                                                    .background(if (isSelected) Color.White else StatusHalfDayOrange)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Selected Day Summary
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(2.dp),
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
                                text = "Selected Date / चुनी गई तारीख",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                            Text(
                                text = selectedDate,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = PrimaryBlue
                            )
                            Text(
                                text = "${selectedDayRecords.size} / ${allEmployees.size} employees marked",
                                fontSize = 12.sp,
                                color = StatusPresentGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Button(
                            onClick = { viewModel.navigateTo(AppScreen.MARK_ATTENDANCE) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlueLight),
                            modifier = Modifier.testTag("calendar_mark_date_btn")
                        ) {
                            Icon(Icons.Default.HowToReg, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = AppStrings.t("mark_attendance", language),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Roster for Selected Date
            item {
                Text(
                    text = "Attendance on $selectedDate",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextPrimary
                )
            }

            if (allEmployees.isEmpty()) {
                item {
                    Text("No employees configured.", color = TextMuted)
                }
            } else {
                items(allEmployees, key = { it.id }) { emp ->
                    val record = recordMap[emp.id]
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryBlueContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = emp.name.take(1).uppercase(Locale.getDefault()),
                                        color = PrimaryBlue,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                                Column {
                                    Text(
                                        text = emp.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${emp.id} • ${emp.designation}",
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
                                        fontSize = 9.sp,
                                        color = TextMuted,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = SurfaceVariant
                                ) {
                                    Text(
                                        text = "Not Marked",
                                        fontSize = 10.sp,
                                        color = TextMuted,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
