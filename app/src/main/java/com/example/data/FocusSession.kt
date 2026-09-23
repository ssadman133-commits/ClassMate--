package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "focus_sessions")
data class FocusSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val durationMinutes: Int,
    val subject: String = "General Study",
    val completedAt: Long = System.currentTimeMillis()
)

enum class TimerMode {
    STUDY,
    BREAK
}

enum class TimerState {
    IDLE,
    RUNNING,
    PAUSED,
    COMPLETED
}
