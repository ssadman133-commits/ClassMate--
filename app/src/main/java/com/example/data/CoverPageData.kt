package com.example.data

/**
 * Available Cover Page Templates with vibrant, simple, and colorful modern themes
 */
enum class CoverPageTemplate(
    val id: String,
    val title: String,
    val description: String,
    val primaryColorHex: Long,
    val secondaryColorHex: Long,
    val styleCategory: String
) {
    MODERN_MINIMAL(
        id = "modern_minimal",
        title = "Sapphire Blue",
        description = "Clean aesthetic with royal sapphire accents & modern cards",
        primaryColorHex = 0xFF2563EB,
        secondaryColorHex = 0xFF0EA5E9,
        styleCategory = "Modern"
    ),
    EMERALD_ACADEMIC(
        id = "emerald_academic",
        title = "Emerald Green",
        description = "Fresh emerald & mint styling with elegant colored ribbons",
        primaryColorHex = 0xFF059669,
        secondaryColorHex = 0xFF10B981,
        styleCategory = "Academic"
    ),
    CORPORATE_RIBBON(
        id = "corporate_ribbon",
        title = "Royal Indigo",
        description = "Vibrant indigo & violet header with stylish white typography",
        primaryColorHex = 0xFF4F46E5,
        secondaryColorHex = 0xFF7C3AED,
        styleCategory = "Executive"
    ),
    CRIMSON_PRESTIGE(
        id = "crimson_prestige",
        title = "Ruby Crimson",
        description = "Rich crimson red framing bands with elegant contrast",
        primaryColorHex = 0xFFDC2626,
        secondaryColorHex = 0xFFF43F5E,
        styleCategory = "Prestige"
    ),
    TECH_GRID(
        id = "tech_grid",
        title = "Ocean & Teal",
        description = "Electric cyan & teal badges tailored for STEM & lab reports",
        primaryColorHex = 0xFF0284C7,
        secondaryColorHex = 0xFF0D9488,
        styleCategory = "STEM / Tech"
    ),
    CLASSIC_FORMAL(
        id = "classic_formal",
        title = "Sunset Gold",
        description = "Warm amber gold & navy double borders for formal submissions",
        primaryColorHex = 0xFFD97706,
        secondaryColorHex = 0xFF1E3A8A,
        styleCategory = "Formal"
    );

    companion object {
        fun fromId(id: String): CoverPageTemplate {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: MODERN_MINIMAL
        }
    }
}

/**
 * Data model containing all cover page fields
 */
data class CoverPageData(
    val templateId: String = CoverPageTemplate.MODERN_MINIMAL.id,
    val universityName: String = "DHAKA UNIVERSITY",
    val assignmentTitle: String = "DATABASE MANAGEMENT SYSTEMS",
    val assignmentSubtitle: String = "",
    val courseTitle: String = "Database Systems & Architecture",
    val courseCode: String = "CSE-301",
    val studentName: String = "Sadman Sakib",
    val studentId: String = "2023-1-60-045",
    val department: String = "Computer Science & Engineering",
    val batchSection: String = "Batch 52, Section B",
    val facultyName: String = "Dr. Md. Tariqul Islam",
    val facultyDesignation: String = "Associate Professor",
    val facultyDepartment: String = "Department of CSE",
    val submissionDate: String = "24 September, 2026",
    val logoUri: String? = null,
    val defaultIcon: String = "graduation"
)
