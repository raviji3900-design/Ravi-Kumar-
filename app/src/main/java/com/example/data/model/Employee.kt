package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "employees")
data class Employee(
    @PrimaryKey
    val id: String, // e.g. "LW-101"
    val name: String,
    val phone: String = "",
    val designation: String = "Loading Worker",
    val assignedOperatorId: String = "", // empty means unassigned or general pool
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
