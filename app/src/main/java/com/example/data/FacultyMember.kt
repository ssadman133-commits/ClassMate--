package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a university faculty member / teacher
 */
@Entity(tableName = "faculty_members")
data class FacultyMember(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val designation: String = "",
    val department: String,
    val email: String = "",
    val phone: String = "",
    val roomNumber: String = "",
    val initials: String = "",
    val officeHours: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
