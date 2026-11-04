package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "operators")
data class Operator(
    @PrimaryKey
    val id: String, // e.g. "OP-01"
    val name: String,
    val username: String,
    val password: String,
    val phone: String = "",
    val shift: String = "Morning Shift",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
