package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.StatusAbsentBg
import com.example.ui.theme.StatusAbsentRed
import com.example.ui.theme.StatusHalfDayBg
import com.example.ui.theme.StatusHalfDayOrange
import com.example.ui.theme.StatusLeaveBg
import com.example.ui.theme.StatusLeavePurple
import com.example.ui.theme.StatusPresentBg
import com.example.ui.theme.StatusPresentGreen

enum class AttendanceStatus(
    val code: String,
    val labelEn: String,
    val labelHi: String,
    val color: Color,
    val bgColor: Color
) {
    PRESENT("P", "Present", "उपस्थित", StatusPresentGreen, StatusPresentBg),
    ABSENT("A", "Absent", "अनुपस्थित", StatusAbsentRed, StatusAbsentBg),
    HALF_DAY("H", "Half Day", "आधा दिन", StatusHalfDayOrange, StatusHalfDayBg),
    LEAVE("L", "Leave", "छुट्टी", StatusLeavePurple, StatusLeaveBg);

    companion object {
        fun fromCode(code: String): AttendanceStatus {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) || it.name.equals(code, ignoreCase = true) }
                ?: PRESENT
        }
    }
}
