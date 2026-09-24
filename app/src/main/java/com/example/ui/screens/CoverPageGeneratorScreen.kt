package com.example.ui.screens

import android.app.DatePickerDialog
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.CoverPageData
import com.example.data.CoverPageTemplate
import com.example.ui.components.CoverPageA4Preview
import com.example.util.CoverPageExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoverPageGeneratorScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    // SharedPreferences for persistent default student info
    val prefs = remember { context.getSharedPreferences("cover_page_prefs", Context.MODE_PRIVATE) }

    // Clear legacy mock demo values from prefs if they were previously saved
    LaunchedEffect(Unit) {
        val oldSavedUni = prefs.getString("university_name", null)
        val oldStudent = prefs.getString("student_name", null)
        if (oldSavedUni == "DHAKA UNIVERSITY" || oldStudent == "Sadman Sakib") {
            prefs.edit()
                .remove("university_name")
                .remove("student_name")
                .remove("student_id")
                .remove("department")
                .remove("batch_section")
                .apply()
        }
    }

    // Cover Page Form States (clean and empty by default for the user to fill)
    var selectedTemplateId by rememberSaveable {
        mutableStateOf(prefs.getString("template_id", CoverPageTemplate.MODERN_MINIMAL.id) ?: CoverPageTemplate.MODERN_MINIMAL.id)
    }
    var universityName by rememberSaveable {
        val saved = prefs.getString("university_name", "") ?: ""
        mutableStateOf(if (saved == "DHAKA UNIVERSITY") "" else saved)
    }
    var assignmentTitle by rememberSaveable {
        mutableStateOf("")
    }
    var assignmentSubtitle by rememberSaveable {
        mutableStateOf("")
    }
    var courseTitle by rememberSaveable {
        mutableStateOf("")
    }
    var courseCode by rememberSaveable {
        mutableStateOf("")
    }
    var studentName by rememberSaveable {
        val saved = prefs.getString("student_name", "") ?: ""
        mutableStateOf(if (saved == "Sadman Sakib") "" else saved)
    }
    var studentId by rememberSaveable {
        val saved = prefs.getString("student_id", "") ?: ""
        mutableStateOf(if (saved == "2023-1-60-045") "" else saved)
    }
    var department by rememberSaveable {
        val saved = prefs.getString("department", "") ?: ""
        mutableStateOf(if (saved == "Computer Science & Engineering") "" else saved)
    }
    var batchSection by rememberSaveable {
        val saved = prefs.getString("batch_section", "") ?: ""
        mutableStateOf(if (saved == "Batch 52, Section B") "" else saved)
    }
    var facultyName by rememberSaveable {
        mutableStateOf("")
    }
    var facultyDesignation by rememberSaveable {
        mutableStateOf("")
    }
    var facultyDepartment by rememberSaveable {
        mutableStateOf("")
    }

    val defaultToday = remember {
        val sdf = SimpleDateFormat("dd MMMM, yyyy", Locale.ENGLISH)
        sdf.format(Calendar.getInstance().time)
    }
    var submissionDate by rememberSaveable { mutableStateOf(defaultToday) }

    var logoUriString by rememberSaveable {
        mutableStateOf(prefs.getString("logo_uri", null))
    }

    // UI Tab State: 0 = Form Editor, 1 = Live Preview
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    // Exporting State
    var isExporting by remember { mutableStateOf(false) }
    var exportSuccessDialogUri by remember { mutableStateOf<Uri?>(null) }
    var exportSuccessMimeType by remember { mutableStateOf("application/pdf") }
    var exportSuccessTitle by remember { mutableStateOf("") }

    // Photo Picker for University Logo
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                val localCopyUri = copyUriToInternalStorage(context, uri)
                if (localCopyUri != null) {
                    logoUriString = localCopyUri.toString()
                    prefs.edit().putString("logo_uri", logoUriString).apply()
                    Toast.makeText(context, "Logo updated successfully", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Auto-save student info helper
    fun saveStudentDefaults() {
        prefs.edit()
            .putString("template_id", selectedTemplateId)
            .putString("university_name", universityName)
            .putString("student_name", studentName)
            .putString("student_id", studentId)
            .putString("department", department)
            .putString("batch_section", batchSection)
            .apply()
        Toast.makeText(context, "Profile information saved as default!", Toast.LENGTH_SHORT).show()
    }

    val currentCoverPageData = remember(
        selectedTemplateId, universityName, assignmentTitle, assignmentSubtitle,
        courseTitle, courseCode, studentName, studentId, department, batchSection,
        facultyName, facultyDesignation, facultyDepartment, submissionDate, logoUriString
    ) {
        CoverPageData(
            templateId = selectedTemplateId,
            universityName = universityName,
            assignmentTitle = assignmentTitle,
            assignmentSubtitle = assignmentSubtitle,
            courseTitle = courseTitle,
            courseCode = courseCode,
            studentName = studentName,
            studentId = studentId,
            department = department,
            batchSection = batchSection,
            facultyName = facultyName,
            facultyDesignation = facultyDesignation,
            facultyDepartment = facultyDepartment,
            submissionDate = submissionDate,
            logoUri = logoUriString
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Cover Page Generator",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "6 Colorful Templates • A4 Print Ready",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Quick Fill Sample Demo
                    IconButton(
                        onClick = {
                            universityName = "UNIVERSITY OF DHAKA"
                            assignmentTitle = "Design and Analysis of Modern Operating Systems"
                            assignmentSubtitle = ""
                            courseTitle = "Operating Systems & Concurrency"
                            courseCode = "CSE 315"
                            studentName = "Sadman Sakib"
                            studentId = "2023-1-60-045"
                            department = "Computer Science & Engineering"
                            batchSection = "Batch 58, Section A"
                            facultyName = "Dr. Md. Tariqul Islam"
                            facultyDesignation = "Professor"
                            facultyDepartment = "Department of CSE"
                            Toast.makeText(context, "Sample academic details loaded", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Load Sample Data",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Quick Switch Preview / Edit
                    IconButton(
                        onClick = {
                            selectedTab = if (selectedTab == 0) 1 else 0
                        }
                    ) {
                        Icon(
                            imageVector = if (selectedTab == 0) Icons.Default.Visibility else Icons.Default.Edit,
                            contentDescription = if (selectedTab == 0) "Preview" else "Edit"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Switcher (100% Clean English)
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MaterialTheme.colorScheme.primary,
                        height = 3.dp
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Text("1. Fill Details", fontWeight = FontWeight.SemiBold)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Text("2. Preview & Export", fontWeight = FontWeight.SemiBold)
                        }
                    }
                )
            }

            if (selectedTab == 0) {
                // Form Tab
                CoverPageFormTab(
                    templates = CoverPageTemplate.entries,
                    selectedTemplateId = selectedTemplateId,
                    onSelectTemplate = { selectedTemplateId = it },
                    logoUri = logoUriString,
                    onPickLogo = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onRemoveLogo = {
                        logoUriString = null
                        prefs.edit().remove("logo_uri").apply()
                        Toast.makeText(context, "Logo removed", Toast.LENGTH_SHORT).show()
                    },
                    universityName = universityName,
                    onUniversityChange = { universityName = it },
                    assignmentTitle = assignmentTitle,
                    onAssignmentTitleChange = { assignmentTitle = it },
                    assignmentSubtitle = assignmentSubtitle,
                    onAssignmentSubtitleChange = { assignmentSubtitle = it },
                    courseTitle = courseTitle,
                    onCourseTitleChange = { courseTitle = it },
                    courseCode = courseCode,
                    onCourseCodeChange = { courseCode = it },
                    studentName = studentName,
                    onStudentNameChange = { studentName = it },
                    studentId = studentId,
                    onStudentIdChange = { studentId = it },
                    department = department,
                    onDepartmentChange = { department = it },
                    batchSection = batchSection,
                    onBatchSectionChange = { batchSection = it },
                    facultyName = facultyName,
                    onFacultyNameChange = { facultyName = it },
                    facultyDesignation = facultyDesignation,
                    onFacultyDesignationChange = { facultyDesignation = it },
                    facultyDepartment = facultyDepartment,
                    onFacultyDepartmentChange = { facultyDepartment = it },
                    submissionDate = submissionDate,
                    onSubmissionDateChange = { submissionDate = it },
                    onSaveDefaults = { saveStudentDefaults() },
                    onGoToPreview = { selectedTab = 1 }
                )
            } else {
                // Preview & Export Tab
                CoverPagePreviewTab(
                    data = currentCoverPageData,
                    onTemplateChange = { selectedTemplateId = it },
                    isExporting = isExporting,
                    onDownloadPdf = {
                        isExporting = true
                        coroutineScope.launch {
                            val uri = withContext(Dispatchers.IO) {
                                CoverPageExporter.savePdfToDownloads(context, currentCoverPageData)
                            }
                            isExporting = false
                            if (uri != null) {
                                exportSuccessUri(uri, "application/pdf", "Cover Page PDF Downloaded") { u, m, t ->
                                    exportSuccessDialogUri = u
                                    exportSuccessMimeType = m
                                    exportSuccessTitle = t
                                }
                            } else {
                                Toast.makeText(context, "Failed to save PDF", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onDownloadImage = {
                        isExporting = true
                        coroutineScope.launch {
                            val uri = withContext(Dispatchers.IO) {
                                CoverPageExporter.saveImageToGallery(context, currentCoverPageData)
                            }
                            isExporting = false
                            if (uri != null) {
                                exportSuccessUri(uri, "image/png", "Cover Page HD Image Saved") { u, m, t ->
                                    exportSuccessDialogUri = u
                                    exportSuccessMimeType = m
                                    exportSuccessTitle = t
                                }
                            } else {
                                Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onShare = {
                        isExporting = true
                        coroutineScope.launch {
                            val uri = withContext(Dispatchers.IO) {
                                CoverPageExporter.generatePdf(context, currentCoverPageData)
                            }
                            isExporting = false
                            if (uri != null) {
                                CoverPageExporter.shareFile(
                                    context,
                                    uri,
                                    "application/pdf",
                                    "Assignment Cover Page - ${currentCoverPageData.assignmentTitle}"
                                )
                            } else {
                                Toast.makeText(context, "Could not prepare document for sharing", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
            }
        }
    }

    // Success Dialog after Export
    if (exportSuccessDialogUri != null) {
        AlertDialog(
            onDismissRequest = { exportSuccessDialogUri = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = exportSuccessTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = "Your cover page has been generated and saved! You can open it in your PDF viewer or share it with classmates and printing shops.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val uri = exportSuccessDialogUri
                        exportSuccessDialogUri = null
                        if (uri != null) {
                            CoverPageExporter.openFile(context, uri, exportSuccessMimeType)
                        }
                    }
                ) {
                    Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open File")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        val uri = exportSuccessDialogUri
                        exportSuccessDialogUri = null
                        if (uri != null) {
                            CoverPageExporter.shareFile(context, uri, exportSuccessMimeType, currentCoverPageData.assignmentTitle)
                        }
                    }
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share")
                }
            }
        )
    }
}

private fun exportSuccessUri(
    uri: Uri,
    mimeType: String,
    title: String,
    callback: (Uri, String, String) -> Unit
) {
    callback(uri, mimeType, title)
}

// ==========================================
// FORM TAB COMPONENT (100% ENGLISH)
// ==========================================
@Composable
private fun CoverPageFormTab(
    templates: List<CoverPageTemplate>,
    selectedTemplateId: String,
    onSelectTemplate: (String) -> Unit,
    logoUri: String?,
    onPickLogo: () -> Unit,
    onRemoveLogo: () -> Unit,
    universityName: String,
    onUniversityChange: (String) -> Unit,
    assignmentTitle: String,
    onAssignmentTitleChange: (String) -> Unit,
    assignmentSubtitle: String,
    onAssignmentSubtitleChange: (String) -> Unit,
    courseTitle: String,
    onCourseTitleChange: (String) -> Unit,
    courseCode: String,
    onCourseCodeChange: (String) -> Unit,
    studentName: String,
    onStudentNameChange: (String) -> Unit,
    studentId: String,
    onStudentIdChange: (String) -> Unit,
    department: String,
    onDepartmentChange: (String) -> Unit,
    batchSection: String,
    onBatchSectionChange: (String) -> Unit,
    facultyName: String,
    onFacultyNameChange: (String) -> Unit,
    facultyDesignation: String,
    onFacultyDesignationChange: (String) -> Unit,
    facultyDepartment: String,
    onFacultyDepartmentChange: (String) -> Unit,
    submissionDate: String,
    onSubmissionDateChange: (String) -> Unit,
    onSaveDefaults: () -> Unit,
    onGoToPreview: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section: Template Picker
        FormSectionHeader(title = "1. Choose Template", icon = Icons.Default.Description)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            templates.forEach { template ->
                TemplateSelectorCard(
                    template = template,
                    isSelected = template.id.equals(selectedTemplateId, ignoreCase = true),
                    onClick = { onSelectTemplate(template.id) }
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // Section: Logo Upload
        FormSectionHeader(title = "2. University / Institution Logo (Optional)", icon = Icons.Default.School)

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Logo Preview Box
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!logoUri.isNullOrBlank()) {
                        AsyncImage(
                            model = logoUri,
                            contentDescription = "Selected Logo",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize().padding(4.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (!logoUri.isNullOrBlank()) "Custom Logo Selected" else "Academic Seal Emblem",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = if (!logoUri.isNullOrBlank()) "Your gallery logo will be displayed on the page" else "Upload your university/college logo from gallery (optional)",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onPickLogo,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = ButtonDefaults.TextButtonContentPadding
                        ) {
                            Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (!logoUri.isNullOrBlank()) "Change" else "Choose Logo", fontSize = 12.sp)
                        }

                        if (!logoUri.isNullOrBlank()) {
                            OutlinedButton(
                                onClick = onRemoveLogo,
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = ButtonDefaults.TextButtonContentPadding
                            ) {
                                Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Remove", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // Section: Institution & Assignment Details
        FormSectionHeader(title = "3. Assignment Details", icon = Icons.Default.Edit)

        OutlinedTextField(
            value = universityName,
            onValueChange = onUniversityChange,
            label = { Text("University / College Name") },
            placeholder = { Text("Enter your university or college name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = assignmentTitle,
            onValueChange = onAssignmentTitleChange,
            label = { Text("Assignment Topic / Title *") },
            placeholder = { Text("Enter assignment topic or project title") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 3
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = courseCode,
                onValueChange = onCourseCodeChange,
                label = { Text("Course Code *") },
                placeholder = { Text("Course Code") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = courseTitle,
                onValueChange = onCourseTitleChange,
                label = { Text("Course Title") },
                placeholder = { Text("Course Title") },
                modifier = Modifier.weight(1.3f),
                singleLine = true
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // Section: Student Information
        FormSectionHeader(title = "4. Submitted By (Student Information)", icon = Icons.Default.School)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = studentName,
                onValueChange = onStudentNameChange,
                label = { Text("Student Name *") },
                placeholder = { Text("Your full name") },
                modifier = Modifier.weight(1.2f),
                singleLine = true
            )
            OutlinedTextField(
                value = studentId,
                onValueChange = onStudentIdChange,
                label = { Text("Student ID / Roll *") },
                placeholder = { Text("ID or Roll") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = department,
                onValueChange = onDepartmentChange,
                label = { Text("Department") },
                placeholder = { Text("Department") },
                modifier = Modifier.weight(1.2f),
                singleLine = true
            )
            OutlinedTextField(
                value = batchSection,
                onValueChange = onBatchSectionChange,
                label = { Text("Batch / Section") },
                placeholder = { Text("Batch / Section") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // Section: Faculty Information
        FormSectionHeader(title = "5. Submitted To (Faculty Information)", icon = Icons.Default.School)

        OutlinedTextField(
            value = facultyName,
            onValueChange = onFacultyNameChange,
            label = { Text("Faculty / Teacher Name *") },
            placeholder = { Text("Teacher / Professor name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = facultyDesignation,
                onValueChange = onFacultyDesignationChange,
                label = { Text("Designation") },
                placeholder = { Text("Designation (e.g. Lecturer)") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = facultyDepartment,
                onValueChange = onFacultyDepartmentChange,
                label = { Text("Faculty Department") },
                placeholder = { Text("Department") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // Section: Submission Date
        FormSectionHeader(title = "6. Submission Date", icon = Icons.Default.CalendarMonth)

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = submissionDate,
                onValueChange = onSubmissionDateChange,
                label = { Text("Submission Date") },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            val cal = Calendar.getInstance()
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    val selectedCal = Calendar.getInstance().apply {
                                        set(year, month, dayOfMonth)
                                    }
                                    val sdf = SimpleDateFormat("dd MMMM, yyyy", Locale.ENGLISH)
                                    onSubmissionDateChange(sdf.format(selectedCal.time))
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }
                    ) {
                        Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = "Pick Date")
                    }
                },
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            // Button to set Today
            OutlinedButton(
                onClick = {
                    val sdf = SimpleDateFormat("dd MMMM, yyyy", Locale.ENGLISH)
                    onSubmissionDateChange(sdf.format(Calendar.getInstance().time))
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Today")
            }
        }

        // Action Buttons
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onSaveDefaults,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("💾 Save My Details")
            }

            Button(
                onClick = onGoToPreview,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1.3f)
            ) {
                Text("View Live Preview →", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ==========================================
// PREVIEW & EXPORT TAB COMPONENT (100% ENGLISH)
// ==========================================
@Composable
private fun CoverPagePreviewTab(
    data: CoverPageData,
    onTemplateChange: (String) -> Unit,
    isExporting: Boolean,
    onDownloadPdf: () -> Unit,
    onDownloadImage: () -> Unit,
    onShare: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Template Switcher Carousel right above the A4 sheet
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CoverPageTemplate.entries.forEach { template ->
                val isSelected = template.id.equals(data.templateId, ignoreCase = true)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) Color(template.primaryColorHex) else MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) Color(template.primaryColorHex) else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onTemplateChange(template.id) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(template.secondaryColorHex))
                        )
                        Text(
                            text = template.title,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Live A4 Sheet Preview
        Text(
            text = "Live A4 Print Preview (1 : 1.414)",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        CoverPageA4Preview(
            data = data,
            modifier = Modifier.fillMaxWidth(0.92f)
        )

        // Action Buttons: PDF, Image, Share
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Download & Export Options",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                if (isExporting) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Generating high resolution document...", fontSize = 13.sp)
                    }
                } else {
                    // Primary Button: Download PDF
                    Button(
                        onClick = onDownloadPdf,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Download PDF (Print Ready)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Download Image
                        OutlinedButton(
                            onClick = onDownloadImage,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save HD Image", fontSize = 12.sp)
                        }

                        // Share Document
                        OutlinedButton(
                            onClick = onShare,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share PDF", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

// ==========================================
// TEMPLATE SELECTOR CARD
// ==========================================
@Composable
private fun TemplateSelectorCard(
    template: CoverPageTemplate,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier
            .width(135.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mini stylized preview frame reflecting each distinct layout
            Box(
                modifier = Modifier
                    .size(width = 54.dp, height = 70.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White)
                    .border(1.2.dp, Color(template.primaryColorHex), RoundedCornerShape(4.dp))
                    .padding(3.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                when (template) {
                    CoverPageTemplate.MODERN_MINIMAL -> {
                        // Left stripe + stacked lines
                        Row(modifier = Modifier.fillMaxSize()) {
                            Box(modifier = Modifier.width(5.dp).fillMaxHeight().background(Color(template.primaryColorHex)))
                            Column(
                                modifier = Modifier.fillMaxSize().padding(2.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(modifier = Modifier.width(20.dp).height(3.dp).background(Color(template.primaryColorHex)))
                                Box(modifier = Modifier.fillMaxWidth().height(14.dp).background(Color(0xFFF1F5F9)))
                                Box(modifier = Modifier.fillMaxWidth().height(8.dp).background(Color(0xFFEFF6FF)))
                            }
                        }
                    }
                    CoverPageTemplate.CORPORATE_RIBBON -> {
                        // Top full banner + circle emblem
                        Column(modifier = Modifier.fillMaxSize()) {
                            Box(modifier = Modifier.fillMaxWidth().height(18.dp).background(Color(template.primaryColorHex)))
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .offset(y = (-6).dp)
                                    .align(Alignment.CenterHorizontally)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(1.dp, Color(template.secondaryColorHex), CircleShape)
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                Box(modifier = Modifier.weight(1f).height(12.dp).background(Color(0xFFF8FAFC)))
                                Box(modifier = Modifier.weight(1f).height(12.dp).background(Color(0xFFF8FAFC)))
                            }
                            Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(Color(template.primaryColorHex)))
                        }
                    }
                    CoverPageTemplate.CLASSIC_FORMAL -> {
                        // Double border + centered lines
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .border(1.dp, Color(template.primaryColorHex))
                                .padding(2.dp)
                                .border(0.5.dp, Color(template.secondaryColorHex))
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(modifier = Modifier.width(24.dp).height(2.5.dp).background(Color(template.secondaryColorHex)))
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(template.primaryColorHex)))
                                Box(modifier = Modifier.width(30.dp).height(4.dp).background(Color(0xFF0F172A)))
                                Box(modifier = Modifier.width(20.dp).height(2.dp).background(Color(template.primaryColorHex)))
                            }
                        }
                    }
                    CoverPageTemplate.TECH_GRID -> {
                        // Top dark bar + grid boxes
                        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                            Box(modifier = Modifier.fillMaxWidth().height(6.dp).background(Color(0xFF0F172A)))
                            Box(modifier = Modifier.fillMaxWidth().height(16.dp).border(0.8.dp, Color(template.primaryColorHex)))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                Box(modifier = Modifier.weight(1f).height(14.dp).border(0.8.dp, Color(template.primaryColorHex)))
                                Box(modifier = Modifier.weight(1f).height(14.dp).border(0.8.dp, Color(template.secondaryColorHex)))
                            }
                            Box(modifier = Modifier.fillMaxWidth().height(3.dp).background(Color(0xFFF1F5F9)))
                        }
                    }
                    CoverPageTemplate.EMERALD_ACADEMIC -> {
                        // Top dual bar + profile cards
                        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                            Row(modifier = Modifier.fillMaxWidth().height(4.dp)) {
                                Box(modifier = Modifier.weight(2f).fillMaxHeight().background(Color(template.primaryColorHex)))
                                Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFFF59E0B)))
                            }
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(template.primaryColorHex)).align(Alignment.CenterHorizontally))
                            Box(modifier = Modifier.fillMaxWidth().height(12.dp).background(Color(0xFFECFDF5)))
                            Box(modifier = Modifier.fillMaxWidth().height(10.dp).background(Color(0xFFF8FAFC)))
                            Box(modifier = Modifier.width(24.dp).height(4.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFFD1FAE5)).align(Alignment.CenterHorizontally))
                        }
                    }
                    CoverPageTemplate.CRIMSON_PRESTIGE -> {
                        // Asymmetric left 30% split
                        Row(modifier = Modifier.fillMaxSize()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(16.dp)
                                    .background(Color(template.primaryColorHex)),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                Box(modifier = Modifier.padding(top = 4.dp).size(8.dp).clip(CircleShape).background(Color.White))
                            }
                            Column(
                                modifier = Modifier.fillMaxSize().padding(3.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(modifier = Modifier.width(18.dp).height(3.dp).background(Color(template.primaryColorHex)))
                                Box(modifier = Modifier.fillMaxWidth().height(14.dp).background(Color(0xFFFFF1F2)))
                                Box(modifier = Modifier.fillMaxWidth().height(10.dp).background(Color(0xFFF8FAFC)))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = template.title,
                fontSize = 11.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            Text(
                text = template.styleCategory,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FormSectionHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private suspend fun copyUriToInternalStorage(context: Context, sourceUri: Uri): Uri? {
    return withContext(Dispatchers.IO) {
        try {
            val logoDir = File(context.filesDir, "cover_logos").apply { mkdirs() }
            val destFile = File(logoDir, "university_logo_${System.currentTimeMillis()}.png")

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            Uri.fromFile(destFile)
        } catch (_: Exception) {
            null
        }
    }
}
