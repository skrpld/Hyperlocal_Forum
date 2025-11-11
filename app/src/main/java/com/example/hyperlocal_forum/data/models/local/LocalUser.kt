package com.example.hyperlocal_forum.data.models.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class LocalUser(
    @PrimaryKey
    val id: String,
    val username: String,
    val passwordHash: String,
    val email: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
