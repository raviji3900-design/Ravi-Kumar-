package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attendance_records",
    indices = [
        Index(value = ["employeeId", "date"], unique = true),
        Index(value = ["date"]),
        Index(value = ["markedById"])
    ]
)
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val employeeId: String,
    val employeeName: String,
    val date: String, // "YYYY-MM-DD"
    val status: String, // "PRESENT", "ABSENT", "HALF_DAY", "LEAVE"
    val time: String, // e.g. "09:30 AM"
    val markedByRole: String, // "ADMIN" or "OPERATOR"
    val markedById: String,
    val markedByName: String,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
