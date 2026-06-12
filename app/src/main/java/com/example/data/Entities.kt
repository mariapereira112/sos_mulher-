package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1, // Only one active user profile locally
    val name: String,
    val email: String,
    val cpf: String,
    val gender: String,
    val state: String = "Normal",
    val onboardingCompleted: Boolean = false,
    val checkInTimerSeconds: Int = 0,
    val checkInEndTime: Long = 0L,
    val isCheckInActive: Boolean = false
)

@Entity(tableName = "contacts")
data class Contact(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val relationship: String,
    val isPriority: Boolean = true
)

@Entity(tableName = "alert_history")
data class AlertHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val location: String,
    val status: String,
    val contactsNotifiedCount: Int
)

@Entity(tableName = "community_posts")
data class CommunityPost(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val author: String,
    val content: String,
    val location: String,
    val timestamp: Long = System.currentTimeMillis(),
    val likesCount: Int = 0,
    val dislikesCount: Int = 0
)

@Entity(tableName = "digital_bulletins")
data class DigitalBulletin(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val incidentType: String,
    val description: String,
    val location: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "Enviado para análise"
)
