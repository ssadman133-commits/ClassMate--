package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.CoverPageData
import com.example.data.CoverPageTemplate

@Composable
fun CoverPageA4Preview(
    data: CoverPageData,
    modifier: Modifier = Modifier
) {
    val template = CoverPageTemplate.fromId(data.templateId)

    Card(
        modifier = modifier
            .aspectRatio(0.707f) // Standard A4 Aspect Ratio (1 : √2)
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(8.dp),
                spotColor = Color(0x350F172A),
                ambientColor = Color(0x200F172A)
            ),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            when (template) {
                CoverPageTemplate.MODERN_MINIMAL -> Template1ModernStripe(data)
                CoverPageTemplate.CORPORATE_RIBBON -> Template2RoyalBanner(data)
                CoverPageTemplate.CLASSIC_FORMAL -> Template3OxfordHeritage(data)
                CoverPageTemplate.TECH_GRID -> Template4StemMatrix(data)
                CoverPageTemplate.EMERALD_ACADEMIC -> Template5EmeraldScholar(data)
                CoverPageTemplate.CRIMSON_PRESTIGE -> Template6CrimsonMagazineSplit(data)
            }
        }
    }
}

// =========================================================================
// 1. MODERN SAPPHIRE STRIPE (Asymmetric Left Bar + Stacked Cards)
// =========================================================================
@Composable
private fun Template1ModernStripe(data: CoverPageData) {
    val sapphire = Color(0xFF2563EB)
    val sky = Color(0xFF0EA5E9)

    Row(modifier = Modifier.fillMaxSize()) {
        // Vertical Left Stripe
        Box(
            modifier = Modifier
                .width(12.dp)
                .fillMaxHeight()
                .background(Brush.verticalGradient(listOf(sapphire, sky)))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Left Uni Name, Right Logo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = data.universityName.ifBlank { "UNIVERSITY NAME" }.uppercase(),
                        color = Color(0xFF0F172A),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (data.department.isNotBlank()) {
                        Text(
                            text = data.department,
                            color = sapphire,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                LogoOrEmblem(data.logoUri, sapphire, size = 42.dp)
            }

            // Title Block
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.2.dp, sapphire),
                    modifier = Modifier.fillMaxWidth(0.95f)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = data.assignmentTitle.ifBlank { "Assignment Title" },
                            color = Color(0xFF0F172A),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 3
                        )
                    }
                }

                val course = "${data.courseCode} : ${data.courseTitle}".trim().trim(':').trim()
                if (course.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFEFF6FF)
                    ) {
                        Text(
                            text = course,
                            color = sapphire,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Bottom Stack: Prepared For (Faculty) & Prepared By (Student) stacked
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Prepared For (Faculty)
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(0.6.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Text("PREPARED FOR:", color = sapphire, fontSize = 6.sp, fontWeight = FontWeight.Bold)
                        Text(data.facultyName, color = Color(0xFF0F172A), fontSize = 7.5.sp, fontWeight = FontWeight.SemiBold)
                        val sub = "${data.facultyDesignation}, ${data.facultyDepartment}".trim().trim(',').trim()
                        if (sub.isNotBlank()) Text(sub, color = Color(0xFF64748B), fontSize = 6.5.sp)
                    }
                }

                // Prepared By (Student)
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(0.6.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Text("PREPARED BY:", color = sky, fontSize = 6.sp, fontWeight = FontWeight.Bold)
                        Text(data.studentName, color = Color(0xFF0F172A), fontSize = 7.5.sp, fontWeight = FontWeight.SemiBold)
                        val meta = listOf(data.studentId, data.batchSection).filter { it.isNotBlank() }.joinToString(" • ")
                        if (meta.isNotBlank()) Text(meta, color = Color(0xFF64748B), fontSize = 6.5.sp)
                    }
                }
            }

            // Footer Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(9.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text(text = data.submissionDate, color = Color(0xFF64748B), fontSize = 7.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

// =========================================================================
// 2. ROYAL EXECUTIVE BANNER (Top 24% Solid Navy Banner + Floating Badge)
// =========================================================================
@Composable
private fun Template2RoyalBanner(data: CoverPageData) {
    val royalIndigo = Color(0xFF1E1B4B)
    val accentViolet = Color(0xFF6366F1)

    Column(modifier = Modifier.fillMaxSize()) {
        // Top 22% Header Band
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.24f)
                .background(Brush.horizontalGradient(listOf(royalIndigo, Color(0xFF312E81)))),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = data.universityName.ifBlank { "INSTITUTION NAME" }.uppercase(),
                    color = Color.White,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
                if (data.department.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = data.department,
                        color = Color(0xFFC7D2FE),
                        fontSize = 7.5.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Body with floating logo
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            // Floating Center Logo overlapping banner boundary
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-20).dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    border = BorderStroke(2.dp, accentViolet),
                    shadowElevation = 4.dp
                ) {
                    Box(modifier = Modifier.padding(4.dp)) {
                        LogoOrEmblem(data.logoUri, accentViolet, size = 40.dp)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .padding(top = 26.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title Area
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = data.assignmentTitle.ifBlank { "Assignment Topic" },
                        color = Color(0xFF0F172A),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Box(modifier = Modifier.width(40.dp).height(2.dp).background(accentViolet))

                    val course = "${data.courseCode} • ${data.courseTitle}".trim().trim('•').trim()
                    if (course.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = course, color = Color(0xFF4B5563), fontSize = 7.5.sp)
                    }
                }

                // Two Floating Cards Side-by-Side
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(0.8.dp, accentViolet.copy(alpha = 0.4f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(6.dp)) {
                            Surface(shape = RoundedCornerShape(4.dp), color = accentViolet) {
                                Text("SUPERVISOR", color = Color.White, fontSize = 5.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(data.facultyName, fontSize = 7.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A), maxLines = 1)
                            Text(data.facultyDesignation, fontSize = 6.5.sp, color = Color(0xFF475569), maxLines = 1)
                            Text(data.facultyDepartment, fontSize = 6.sp, color = Color(0xFF64748B), maxLines = 1)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(0.8.dp, accentViolet.copy(alpha = 0.4f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(6.dp)) {
                            Surface(shape = RoundedCornerShape(4.dp), color = royalIndigo) {
                                Text("STUDENT", color = Color.White, fontSize = 5.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(data.studentName, fontSize = 7.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A), maxLines = 1)
                            Text("ID: ${data.studentId}", fontSize = 6.5.sp, color = Color(0xFF475569), maxLines = 1)
                            Text(data.batchSection, fontSize = 6.sp, color = Color(0xFF64748B), maxLines = 1)
                        }
                    }
                }

                // Footer
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Date of Submission: ${data.submissionDate}", fontSize = 7.sp, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(3.dp).background(royalIndigo))
                }
            }
        }
    }
}

// =========================================================================
// 3. OXFORD HERITAGE THESIS (Double Border + 100% Serif + Signature Columns)
// =========================================================================
@Composable
private fun Template3OxfordHeritage(data: CoverPageData) {
    val gold = Color(0xFFD97706)
    val navy = Color(0xFF1E3A8A)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(6.dp)
            .border(2.dp, gold)
            .padding(3.dp)
            .border(0.8.dp, navy)
            .padding(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = data.universityName.ifBlank { "UNIVERSITY NAME" }.uppercase(),
                    color = navy,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    textAlign = TextAlign.Center
                )
                if (data.department.isNotBlank()) {
                    Text(
                        text = data.department,
                        color = Color(0xFF475569),
                        fontSize = 7.sp,
                        fontFamily = FontFamily.Serif
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("─── ◆ ───", color = gold, fontSize = 7.sp)
                Spacer(modifier = Modifier.height(4.dp))
                LogoOrEmblem(data.logoUri, gold, size = 42.dp)
            }

            // Center Title
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = data.assignmentTitle.ifBlank { "Assignment Topic" }.uppercase(),
                    color = Color(0xFF0F172A),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    textAlign = TextAlign.Center,
                    maxLines = 3
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("─── ◆ ───", color = gold, fontSize = 7.sp)

                val course = "${data.courseCode} - ${data.courseTitle}".trim().trim('-').trim()
                if (course.isNotBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = course,
                        color = navy,
                        fontSize = 7.5.sp,
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic
                    )
                }
            }

            // Formal Thesis Columns (No modern cards, pure classic formal typography)
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Submitted To:", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 7.sp, color = navy)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(data.facultyName, fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold, fontSize = 7.5.sp, color = Color(0xFF0F172A))
                    Text(data.facultyDesignation, fontFamily = FontFamily.Serif, fontSize = 6.5.sp, color = Color(0xFF475569))
                    Text(data.facultyDepartment, fontFamily = FontFamily.Serif, fontSize = 6.sp, color = Color(0xFF64748B))
                }

                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text("Submitted By:", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 7.sp, color = gold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(data.studentName, fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold, fontSize = 7.5.sp, color = Color(0xFF0F172A))
                    Text("ID: ${data.studentId}", fontFamily = FontFamily.Serif, fontSize = 6.5.sp, color = Color(0xFF475569))
                    Text(data.batchSection, fontFamily = FontFamily.Serif, fontSize = 6.sp, color = Color(0xFF64748B))
                }
            }

            // Formal Centered Date
            Text(
                text = "Date of Submission: ${data.submissionDate}",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Medium,
                fontSize = 7.sp,
                color = navy
            )
        }
    }
}

// =========================================================================
// 4. STEM CYBER MATRIX (Technical Grid Boxes + Monospace Styling)
// =========================================================================
@Composable
private fun Template4StemMatrix(data: CoverPageData) {
    val cyan = Color(0xFF0284C7)
    val teal = Color(0xFF0D9488)
    val dark = Color(0xFF0F172A)

    Column(modifier = Modifier.fillMaxSize()) {
        // Tech Top Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp)
                .background(dark),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = "// ACADEMIC LAB MANUAL & PROJECT REPORT //",
                color = Color(0xFF38BDF8),
                fontSize = 6.5.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 10.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Left text, Right Logo in Tech Box
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = data.universityName.ifBlank { "UNIVERSITY NAME" }.uppercase(),
                        color = dark,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "DEPT: ${data.department.uppercase()}",
                        color = cyan,
                        fontSize = 7.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .border(1.dp, cyan)
                        .padding(2.dp)
                ) {
                    LogoOrEmblem(data.logoUri, cyan, size = 36.dp)
                }
            }

            // Tech Parameter Box for Title
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, cyan)
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(cyan)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("SPECIFICATION: PROJECT TITLE", color = Color.White, fontSize = 6.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = data.assignmentTitle.ifBlank { "Engineering Project Title" },
                            color = dark,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 3
                        )
                        val course = "[ CODE: ${data.courseCode.ifBlank { "N/A" }} ] [ UNIT: ${data.courseTitle.ifBlank { "N/A" }} ]"
                        Text(
                            text = course,
                            color = teal,
                            fontSize = 6.5.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Modular Grid Tables
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Evaluator Table
                Box(modifier = Modifier.weight(1f).border(0.8.dp, cyan)) {
                    Column {
                        Box(modifier = Modifier.fillMaxWidth().background(Color(0xFFE0F2FE)).padding(horizontal = 4.dp, vertical = 2.dp)) {
                            Text("[01] EVALUATOR", color = cyan, fontSize = 6.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                        Column(modifier = Modifier.padding(4.dp)) {
                            Text(data.facultyName, fontSize = 7.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, maxLines = 1)
                            Text(data.facultyDesignation, fontSize = 6.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF64748B), maxLines = 1)
                            Text(data.facultyDepartment, fontSize = 5.5.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF94A3B8), maxLines = 1)
                        }
                    }
                }

                // Candidate Table
                Box(modifier = Modifier.weight(1f).border(0.8.dp, teal)) {
                    Column {
                        Box(modifier = Modifier.fillMaxWidth().background(Color(0xFFCCFBF1)).padding(horizontal = 4.dp, vertical = 2.dp)) {
                            Text("[02] CANDIDATE", color = teal, fontSize = 6.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                        Column(modifier = Modifier.padding(4.dp)) {
                            Text(data.studentName, fontSize = 7.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, maxLines = 1)
                            Text("ID: ${data.studentId}", fontSize = 6.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF0F172A), maxLines = 1)
                            Text(data.batchSection, fontSize = 5.5.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF64748B), maxLines = 1)
                        }
                    }
                }
            }

            // Footer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF1F5F9))
                    .padding(vertical = 3.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "TIMESTAMP // ${data.submissionDate.uppercase()}",
                    color = Color(0xFF475569),
                    fontSize = 6.5.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// =========================================================================
// 5. EMERALD SCHOLAR (Geometric Corner Accents + Profile Cards)
// =========================================================================
@Composable
private fun Template5EmeraldScholar(data: CoverPageData) {
    val emerald = Color(0xFF059669)
    val mint = Color(0xFF10B981)

    Column(modifier = Modifier.fillMaxSize()) {
        // Emerald Top Strip
        Row(modifier = Modifier.fillMaxWidth().height(6.dp)) {
            Box(modifier = Modifier.weight(2f).fillMaxSize().background(emerald))
            Box(modifier = Modifier.weight(1f).fillMaxSize().background(Color(0xFFF59E0B)))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: Emblem & Uni Name
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                LogoOrEmblem(data.logoUri, emerald, size = 44.dp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = data.universityName.ifBlank { "UNIVERSITY NAME" }.uppercase(),
                    color = emerald,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                if (data.department.isNotBlank()) {
                    Text(data.department, color = Color(0xFF047857), fontSize = 7.5.sp)
                }
            }

            // Title in tinted emerald card with solid 4dp left border
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFECFDF5),
                border = BorderStroke(1.dp, emerald.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth(0.95f)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.width(4.dp).height(48.dp).background(emerald))
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                        Text(
                            text = data.assignmentTitle.ifBlank { "Assignment Topic" },
                            color = Color(0xFF064E3B),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2
                        )
                        val course = "${data.courseCode} : ${data.courseTitle}".trim().trim(':').trim()
                        if (course.isNotBlank()) {
                            Text(course, color = emerald, fontSize = 7.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Profile Rows (Faculty & Student)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Faculty Profile
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(0.8.dp, emerald.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(emerald), contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Default.School, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("SUPERVISED BY: ${data.facultyName}", fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                            Text("${data.facultyDesignation}, ${data.facultyDepartment}".trim().trim(',').trim(), fontSize = 6.5.sp, color = Color(0xFF64748B))
                        }
                    }
                }

                // Student Profile
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(0.8.dp, mint.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(mint), contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("SUBMITTED BY: ${data.studentName}", fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                            Text("ID: ${data.studentId} • ${data.batchSection}", fontSize = 6.5.sp, color = Color(0xFF64748B))
                        }
                    }
                }
            }

            // Date
            Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFD1FAE5)) {
                Text(
                    text = "Submission Date: ${data.submissionDate}",
                    color = Color(0xFF065F46),
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}

// =========================================================================
// 6. CRIMSON MAGAZINE SPLIT (28% Vertical Column Split + 72% White Area)
// =========================================================================
@Composable
private fun Template6CrimsonMagazineSplit(data: CoverPageData) {
    val crimson = Color(0xFFDC2626)

    Row(modifier = Modifier.fillMaxSize()) {
        // Left 28% Crimson Column
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.30f)
                .background(crimson)
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top: White circular logo & University
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(shape = CircleShape, color = Color.White, modifier = Modifier.size(38.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            LogoOrEmblem(data.logoUri, crimson, size = 32.dp)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = data.universityName.ifBlank { "UNIVERSITY" }.uppercase(),
                        color = Color.White,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                // Bottom Date in White
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("DATE", color = Color(0xFFFECDD3), fontSize = 6.sp, fontWeight = FontWeight.Bold)
                    Text(data.submissionDate, color = Color.White, fontSize = 6.5.sp, textAlign = TextAlign.Center)
                }
            }
        }

        // Right 72% White Area
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section
            Column {
                Text("ACADEMIC SUBMISSION", color = crimson, fontSize = 6.5.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = data.assignmentTitle.ifBlank { "Assignment Title" },
                    color = Color(0xFF0F172A),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 3
                )
                val course = "${data.courseCode} • ${data.courseTitle}".trim().trim('•').trim()
                if (course.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFFFF1F2)) {
                        Text(course, color = crimson, fontSize = 7.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFF1F5F9))

            // Faculty
            Column {
                Text("SUBMITTED TO", color = Color(0xFF64748B), fontSize = 6.sp, fontWeight = FontWeight.Bold)
                Text(data.facultyName, color = Color(0xFF0F172A), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                Text(data.facultyDesignation, color = Color(0xFF475569), fontSize = 6.5.sp)
                Text(data.facultyDepartment, color = Color(0xFF64748B), fontSize = 6.sp)
            }

            HorizontalDivider(color = Color(0xFFF1F5F9))

            // Student with highlighted ID
            Column {
                Text("SUBMITTED BY", color = Color(0xFF64748B), fontSize = 6.sp, fontWeight = FontWeight.Bold)
                Text(data.studentName, color = Color(0xFF0F172A), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(4.dp), color = crimson) {
                        Text("ID: ${data.studentId}", color = Color.White, fontSize = 6.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(data.batchSection, color = Color(0xFF475569), fontSize = 6.5.sp)
                }
                Text(data.department, color = Color(0xFF64748B), fontSize = 6.sp)
            }
        }
    }
}

// =========================================================================
// COMMON LOGO OR EMBLEM
// =========================================================================
@Composable
fun LogoOrEmblem(
    logoUri: String?,
    accentColor: Color,
    size: androidx.compose.ui.unit.Dp
) {
    if (!logoUri.isNullOrBlank()) {
        AsyncImage(
            model = logoUri,
            contentDescription = "University Logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(6.dp))
        )
    } else {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.12f))
                .border(1.dp, accentColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = "Emblem",
                tint = accentColor,
                modifier = Modifier.size(size * 0.55f)
            )
        }
    }
}
