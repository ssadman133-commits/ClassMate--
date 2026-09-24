package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.FacultyMember
import com.example.ui.ClassNotesViewModel
import com.example.util.AppThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacultyInfoScreen(
    viewModel: ClassNotesViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val facultyList by viewModel.facultyMembers.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

    // Consistent theme check with the rest of the application
    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    // Clear any sample mock faculty so user begins with their own personal faculty directory
    LaunchedEffect(Unit) {
        viewModel.removeSampleFacultyIfPresent()
    }

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedDepartmentFilter by rememberSaveable { mutableStateOf("All") }
    var expandedFacultyId by rememberSaveable { mutableStateOf<Long?>(null) }

    // Dialog state
    var showAddEditDialog by remember { mutableStateOf(false) }
    var facultyToEdit by remember { mutableStateOf<FacultyMember?>(null) }
    var facultyToDelete by remember { mutableStateOf<FacultyMember?>(null) }

    // Deduplicate any repeated faculty entries for pristine UI
    val distinctFacultyList = remember(facultyList) {
        val seen = mutableSetOf<String>()
        facultyList.filter { faculty ->
            val key = "${faculty.name.trim().lowercase()}|${faculty.department.trim().lowercase()}"
            if (key in seen) false else {
                seen.add(key)
                true
            }
        }
    }

    // Unique departments
    val departments = remember(distinctFacultyList) {
        listOf("All") + distinctFacultyList.map { it.department.trim() }.filter { it.isNotBlank() }.distinct()
    }

    // Filtered list
    val filteredList = remember(distinctFacultyList, searchQuery, selectedDepartmentFilter) {
        distinctFacultyList.filter { faculty ->
            val matchesSearch = searchQuery.isBlank() ||
                faculty.name.contains(searchQuery, ignoreCase = true) ||
                faculty.department.contains(searchQuery, ignoreCase = true) ||
                faculty.roomNumber.contains(searchQuery, ignoreCase = true) ||
                faculty.designation.contains(searchQuery, ignoreCase = true) ||
                faculty.initials.contains(searchQuery, ignoreCase = true)

            val matchesDept = selectedDepartmentFilter == "All" ||
                faculty.department.equals(selectedDepartmentFilter, ignoreCase = true)

            matchesSearch && matchesDept
        }
    }

    // Light mode: Clean, soft off-white background (#F5F7FA) so pure white cards float with a gentle, beautiful shadow
    val screenBgColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF5F7FA)
    val topBarBgColor = if (isDark) Color(0xFF1E293B) else Color(0xFFF5F7FA)
    val topBarTextColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF1F2937)
    val subtitleTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF6B7280)

    Scaffold(
        containerColor = screenBgColor,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Faculty Directory",
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp,
                            color = topBarTextColor
                        )
                        Text(
                            text = "Teachers & Department Contacts",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = subtitleTextColor
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("faculty_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = topBarTextColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = topBarBgColor
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    facultyToEdit = null
                    showAddEditDialog = true
                },
                containerColor = Color(0xFF2563EB),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .shadow(elevation = 6.dp, shape = CircleShape, spotColor = Color(0x402563EB))
                    .testTag("add_faculty_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Faculty",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(screenBgColor)
        ) {
            // Subtle separator line below top bar
            HorizontalDivider(
                color = if (isDark) Color(0xFF334155) else Color(0xFFEEF2F6),
                thickness = 1.dp
            )

            // Search Bar with crisp floating card appearance
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = if (isDark) 0.dp else 3.dp,
                            shape = RoundedCornerShape(14.dp),
                            spotColor = Color(0x260F172A),
                            ambientColor = Color(0x1A0F172A)
                        )
                        .testTag("faculty_search_input"),
                    placeholder = {
                        Text(
                            text = "Search faculty by name, dept, or room...",
                            fontSize = 14.sp,
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF9CA3AF)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF2563EB)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search",
                                    tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF6B7280)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = if (isDark) Color(0xFF1E293B) else Color.White,
                        unfocusedContainerColor = if (isDark) Color(0xFF1E293B) else Color.White,
                        focusedBorderColor = Color(0xFF2563EB),
                        unfocusedBorderColor = if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1)
                    )
                )
            }

            // Department Filter Chips
            if (departments.size > 2) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(departments) { dept ->
                        val isSelected = selectedDepartmentFilter == dept
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) Color(0xFF2563EB) else (if (isDark) Color(0xFF1E293B) else Color.White),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) Color(0xFF2563EB) else (if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1))
                            ),
                            shadowElevation = if (isSelected || isDark) 0.dp else 2.dp,
                            modifier = Modifier.clickable { selectedDepartmentFilter = dept }
                        ) {
                            Text(
                                text = dept,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else (if (isDark) Color(0xFFE2E8F0) else Color(0xFF374151))
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Faculty List
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(72.dp),
                            shape = CircleShape,
                            color = if (isDark) Color(0xFF1E3A8A) else Color(0xFFDBEAFE)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    modifier = Modifier.size(36.dp),
                                    tint = Color(0xFF2563EB)
                                )
                            }
                        }
                        Text(
                            text = if (searchQuery.isNotBlank()) "No faculty member found" else "No faculty members added yet",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) Color(0xFFF1F5F9) else Color(0xFF1F2937)
                        )
                        Text(
                            text = "Save your professors' and instructors' contacts, office rooms, and emails for easy access.",
                            fontSize = 13.sp,
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF6B7280),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Button(
                            onClick = {
                                facultyToEdit = null
                                showAddEditDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2563EB)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Faculty Member")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredList, key = { it.id }) { faculty ->
                        val isExpanded = expandedFacultyId == faculty.id
                        FacultyCardItem(
                            faculty = faculty,
                            isExpanded = isExpanded,
                            isDark = isDark,
                            onClick = {
                                expandedFacultyId = if (isExpanded) null else faculty.id
                            },
                            onCopyText = { text, label ->
                                copyToClipboard(context, text, label)
                            },
                            onDialPhone = { phone ->
                                dialPhoneNumber(context, phone)
                            },
                            onSendEmail = { email ->
                                sendEmailIntent(context, email)
                            },
                            onEdit = {
                                facultyToEdit = faculty
                                showAddEditDialog = true
                            },
                            onDelete = {
                                facultyToDelete = faculty
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(88.dp))
                    }
                }
            }
        }
    }

    // Add / Edit Dialog
    if (showAddEditDialog) {
        AddEditFacultyDialog(
            facultyToEdit = facultyToEdit,
            onDismiss = { showAddEditDialog = false },
            onSave = { name, designation, department, email, phone, roomNumber, initials, officeHours ->
                if (facultyToEdit != null) {
                    viewModel.updateFacultyMember(
                        facultyToEdit!!.copy(
                            name = name,
                            designation = designation,
                            department = department,
                            email = email,
                            phone = phone,
                            roomNumber = roomNumber,
                            initials = initials,
                            officeHours = officeHours
                        )
                    )
                } else {
                    viewModel.addFacultyMember(
                        name = name,
                        designation = designation,
                        department = department,
                        email = email,
                        phone = phone,
                        roomNumber = roomNumber,
                        initials = initials,
                        officeHours = officeHours
                    )
                }
                showAddEditDialog = false
            }
        )
    }

    // Delete Confirmation Dialog
    if (facultyToDelete != null) {
        AlertDialog(
            onDismissRequest = { facultyToDelete = null },
            title = { Text("Delete Faculty Record") },
            text = { Text("Are you sure you want to remove \"${facultyToDelete?.name}\" from your faculty directory?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        facultyToDelete?.let { viewModel.deleteFacultyMember(it) }
                        facultyToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { facultyToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Elongated box with prominent, crystal-clear drop shadow displaying teacher's name and department.
 * Multi-layer canvas shadow rendering ensures it is unmistakably visible in any light/display conditions.
 * Clicking expands to show detailed contact info with copy options.
 */
@Composable
fun FacultyCardItem(
    faculty: FacultyMember,
    isExpanded: Boolean,
    isDark: Boolean,
    onClick: () -> Unit,
    onCopyText: (String, String) -> Unit,
    onDialPhone: (String) -> Unit,
    onSendEmail: (String) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val cardContainerColor = if (isDark) Color(0xFF1E293B) else Color.White
    val cardBorderColor = if (isDark) Color(0xFF334155) else Color(0xFFEEF2F6)

    // Soft, balanced, and aesthetically pleasing drop shadow
    val shadowSpreadColor = if (isDark) Color(0x33000000) else Color(0x0A0F172A)
    val shadowDirectColor = if (isDark) Color(0x55000000) else Color(0x120F172A)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            // Natural, soft dual-layer canvas shadow that looks clean and premium
            .drawBehind {
                val radius = 18.dp.toPx()
                // Layer 1: Gentle ambient halo
                drawRoundRect(
                    color = shadowSpreadColor,
                    topLeft = Offset(0f, 1.5.dp.toPx()),
                    size = Size(size.width, size.height + 1.5.dp.toPx()),
                    cornerRadius = CornerRadius(radius, radius)
                )
                // Layer 2: Subtle downward drop shadow
                drawRoundRect(
                    color = shadowDirectColor,
                    topLeft = Offset(0f, 3.5.dp.toPx()),
                    size = Size(size.width, size.height),
                    cornerRadius = CornerRadius(radius, radius)
                )
            }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = if (isDark) 4.dp else 2.dp,
                    shape = RoundedCornerShape(18.dp),
                    spotColor = if (isDark) Color.Black else Color(0x1A0F172A),
                    ambientColor = if (isDark) Color.Black else Color(0x0D0F172A)
                )
                .clip(RoundedCornerShape(18.dp))
                .clickable(onClick = onClick)
                .testTag("faculty_card_${faculty.id}"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = cardContainerColor
            ),
            border = BorderStroke(1.dp, cardBorderColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(animationSpec = tween(220))
                    .padding(16.dp)
            ) {
                // Header Row: Avatar, Name & Department, and Expand Arrow
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Initial Letter Icon Avatar
                    val initialText = faculty.initials.ifBlank {
                        faculty.name.split(" ")
                            .filter { it.isNotBlank() }
                            .take(2)
                            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                            .joinToString("")
                            .ifBlank { "T" }
                    }

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF2563EB),
                                        Color(0xFF60A5FA)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initialText,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    // Name on top, Department directly underneath
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = faculty.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = if (isDark) Color(0xFFF1F5F9) else Color(0xFF111827),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = faculty.department,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) Color(0xFF60A5FA) else Color(0xFF2563EB),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (faculty.designation.isNotBlank()) {
                            Text(
                                text = faculty.designation,
                                fontSize = 12.sp,
                                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF6B7280),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Expand / Collapse Chevron indicator
                    IconButton(
                        onClick = onClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "Collapse" else "Expand details",
                            tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF6B7280)
                        )
                    }
                }

                // Expanded Detailed View
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn(animationSpec = tween(180)),
                    exit = fadeOut(animationSpec = tween(140))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        HorizontalDivider(
                            color = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0),
                            thickness = 1.dp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Room Number Item
                        if (faculty.roomNumber.isNotBlank()) {
                            DetailInfoRow(
                                icon = Icons.Default.MeetingRoom,
                                label = "Room Number",
                                value = faculty.roomNumber,
                                isDark = isDark,
                                onCopy = { onCopyText(faculty.roomNumber, "Room number") }
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Email Address Item with Copy & Send Intent
                        if (faculty.email.isNotBlank()) {
                            DetailInfoRow(
                                icon = Icons.Default.Email,
                                label = "Email Address",
                                value = faculty.email,
                                isDark = isDark,
                                onCopy = { onCopyText(faculty.email, "Email") },
                                actionIcon = Icons.Default.Email,
                                actionLabel = "Email",
                                onActionClick = { onSendEmail(faculty.email) }
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Phone Number Item with Copy & Call Intent
                        if (faculty.phone.isNotBlank()) {
                            DetailInfoRow(
                                icon = Icons.Default.Call,
                                label = "Phone Number",
                                value = faculty.phone,
                                isDark = isDark,
                                onCopy = { onCopyText(faculty.phone, "Phone number") },
                                actionIcon = Icons.Default.Call,
                                actionLabel = "Call",
                                onActionClick = { onDialPhone(faculty.phone) }
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Office Hours / Counseling Hours
                        if (faculty.officeHours.isNotBlank()) {
                            DetailInfoRow(
                                icon = Icons.Default.Schedule,
                                label = "Office Hours",
                                value = faculty.officeHours,
                                isDark = isDark,
                                onCopy = { onCopyText(faculty.officeHours, "Office hours") }
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Edit & Delete Action Buttons
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = onEdit,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (isDark) Color(0xFF475569) else Color(0xFFCBD5E1)
                                ),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = if (isDark) Color(0xFFE2E8F0) else Color(0xFF374151)
                                ),
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Edit", fontSize = 13.sp)
                            }

                            OutlinedButton(
                                onClick = onDelete,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFFEF4444)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Delete", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Individual Detail Info row with 1-tap Copy and Action buttons
 */
@Composable
fun DetailInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    isDark: Boolean,
    onCopy: () -> Unit,
    actionIcon: ImageVector? = null,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC),
        border = BorderStroke(
            1.dp,
            if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (isDark) Color(0xFF60A5FA) else Color(0xFF2563EB)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF6B7280)
                )
                Text(
                    text = value,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) Color(0xFFF1F5F9) else Color(0xFF111827)
                )
            }

            // Direct Action button (e.g. Call or Email)
            if (actionIcon != null && onActionClick != null) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF2563EB).copy(alpha = 0.12f),
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onActionClick)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = actionIcon,
                            contentDescription = actionLabel,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
            }

            // Copy button
            Surface(
                shape = CircleShape,
                color = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0),
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onCopy)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy $label",
                        tint = if (isDark) Color(0xFFE2E8F0) else Color(0xFF374151),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Add / Edit Faculty Dialog (All English)
 */
@Composable
fun AddEditFacultyDialog(
    facultyToEdit: FacultyMember?,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        designation: String,
        department: String,
        email: String,
        phone: String,
        roomNumber: String,
        initials: String,
        officeHours: String
    ) -> Unit
) {
    var name by rememberSaveable { mutableStateOf(facultyToEdit?.name ?: "") }
    var designation by rememberSaveable { mutableStateOf(facultyToEdit?.designation ?: "Lecturer") }
    var department by rememberSaveable { mutableStateOf(facultyToEdit?.department ?: "Computer Science & Engineering") }
    var email by rememberSaveable { mutableStateOf(facultyToEdit?.email ?: "") }
    var phone by rememberSaveable { mutableStateOf(facultyToEdit?.phone ?: "") }
    var roomNumber by rememberSaveable { mutableStateOf(facultyToEdit?.roomNumber ?: "") }
    var initials by rememberSaveable { mutableStateOf(facultyToEdit?.initials ?: "") }
    var officeHours by rememberSaveable { mutableStateOf(facultyToEdit?.officeHours ?: "") }

    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (facultyToEdit != null) "Edit Faculty Info" else "Add Faculty Member",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (it.isNotBlank()) isError = false
                    },
                    label = { Text("Teacher's Full Name *") },
                    placeholder = { Text("e.g. Dr. Mohammad Rahman") },
                    isError = isError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = department,
                    onValueChange = { department = it },
                    label = { Text("Department *") },
                    placeholder = { Text("e.g. CSE, EEE, BBA") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = designation,
                    onValueChange = { designation = it },
                    label = { Text("Designation") },
                    placeholder = { Text("e.g. Professor / Assistant Professor / Lecturer") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = roomNumber,
                        onValueChange = { roomNumber = it },
                        label = { Text("Room No") },
                        placeholder = { Text("Room 402") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = initials,
                        onValueChange = { initials = it },
                        label = { Text("Initials") },
                        placeholder = { Text("MRN") },
                        singleLine = true,
                        modifier = Modifier.weight(0.8f)
                    )
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    placeholder = { Text("teacher@university.edu") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone / Mobile Number") },
                    placeholder = { Text("+880 1700-000000") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = officeHours,
                    onValueChange = { officeHours = it },
                    label = { Text("Office / Counseling Hours") },
                    placeholder = { Text("Sun & Tue: 2:00 PM - 4:00 PM") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank() || department.isBlank()) {
                        isError = true
                    } else {
                        onSave(name, designation, department, email, phone, roomNumber, initials, officeHours)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Copies string to Android Clipboard and shows Toast in English
 */
private fun copyToClipboard(context: Context, text: String, label: String) {
    if (text.isBlank()) return
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "$label copied to clipboard", Toast.LENGTH_SHORT).show()
}

/**
 * Launches Android Dialer
 */
private fun dialPhoneNumber(context: Context, phone: String) {
    try {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:${phone.trim()}")
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "Could not launch dialer", Toast.LENGTH_SHORT).show()
    }
}

/**
 * Launches Email app
 */
private fun sendEmailIntent(context: Context, email: String) {
    try {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:${email.trim()}")
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "Could not launch email app", Toast.LENGTH_SHORT).show()
    }
}
