package com.example.hyperlocal_forum.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class LocalUser(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val passwordHash: String,
    val email: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
