package com.example.ui.screens

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Assignment
import com.example.data.CachedSponsor
import com.example.data.Exam
import com.example.data.RoutineItem
import com.example.ui.AppScreen
import com.example.ui.components.AdMobBannerAd
import com.example.ui.components.AlertCalculator
import com.example.ui.components.AppBottomNavigationBar
import com.example.ui.components.CompactSponsorBanner
import com.example.ui.components.CompactSponsorCarousel
import com.example.ui.components.DashboardHubAdBanner
import com.example.ui.components.UpcomingAlertsBottomSheet
import com.example.util.CountryDetector
import java.util.Calendar

// Session-wide stabilization flag: once the app initializes, returning from any sub-screen
// will never re-trigger startup stabilization or banner reloading.
private var sHasAppStabilized = false

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardScreen(
    courseCount: Int,
    upcomingAssignmentCount: Int,
    upcomingExamCount: Int,
    activeSponsor: CachedSponsor? = null,
    activeSponsors: List<CachedSponsor> = emptyList(),
    assignments: List<Assignment> = emptyList(),
    exams: List<Exam> = emptyList(),
    routineItems: List<RoutineItem> = emptyList(),
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    onNavigateToClassNotes: () -> Unit,
    onNavigateToStudyPlanner: () -> Unit,
    onNavigateToTimer: () -> Unit = {},
    onNavigateToCgpa: () -> Unit,
    onNavigateToTuition: () -> Unit = {},
    onNavigateToAssignments: () -> Unit,
    onNavigateToExams: () -> Unit,
    onNavigateToRoutine: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToCoverPage: () -> Unit = {},
    onNavigateToFacultyInfo: () -> Unit = {},
    onSponsorClick: (String, String) -> Unit,
    onSponsorImpression: ((String) -> Unit)? = null,
    onImportSharedNote: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showNotificationSheet by remember { mutableStateOf(false) }
    var dismissedAlertIds by remember { mutableStateOf(setOf<String>()) }

    val allAlerts = remember(assignments, exams) {
        AlertCalculator.calculateAlerts(assignments, exams)
    }

    val activeAlerts = remember(allAlerts, dismissedAlertIds) {
        allAlerts.filter { it.id !in dismissedAlertIds }
    }

    val context = LocalContext.current
    val isBangladeshUser = remember(context) {
        CountryDetector.isBangladeshUser(context)
    }

    // Dynamic network connectivity detection
    fun checkInternetOnline(): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val activeNetwork = cm?.activeNetwork
            if (activeNetwork != null) {
                val caps = cm.getNetworkCapabilities(activeNetwork)
                caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
            } else {
                @Suppress("DEPRECATION")
                val activeInfo = cm?.activeNetworkInfo
                @Suppress("DEPRECATION")
                activeInfo?.isConnected == true
            }
        } catch (_: Throwable) {
            false
        }
    }

    var isOnline by remember {
        mutableStateOf(checkInternetOnline())
    }

    // Smooth Startup Stabilization:
    // Holds a quiet stabilizer strictly ONCE per process session (~180ms) upon cold app launch.
    // When navigating back from courses, assignments, routine or other tabs, it remains permanently true,
    // ensuring instantaneous zero-flicker ad/sponsor display without reloading the slot.
    var isInitialized by rememberSaveable { mutableStateOf(sHasAppStabilized) }

    LaunchedEffect(Unit) {
        if (!sHasAppStabilized) {
            delay(180L)
            isOnline = checkInternetOnline()
            sHasAppStabilized = true
            isInitialized = true
        } else {
            isOnline = checkInternetOnline()
        }
    }

    DisposableEffect(context) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                isOnline = checkInternetOnline()
            }
            override fun onLost(network: Network) {
                isOnline = checkInternetOnline()
            }
            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                isOnline = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            }
        }
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        try {
            cm?.registerNetworkCallback(request, callback)
        } catch (_: Throwable) {}

        onDispose {
            try {
                cm?.unregisterNetworkCallback(callback)
            } catch (_: Throwable) {}
        }
    }

    // Workspace action cards keep an elevated, balanced proportion (125.dp)
    // with no awkward stretching or empty voids, aligning the banner naturally above the bottom bar
    val gridCardHeight = 125.dp

    // Featured Hero Sponsor & Promo Banner (Only displays when admin has created real active sponsors)
    val displaySponsors = remember(activeSponsors, activeSponsor) {
        val list = activeSponsors.filter { it.isValidCurrently() }
        if (list.isNotEmpty()) list
        else if (activeSponsor != null && activeSponsor.isValidCurrently()) listOf(activeSponsor)
        else emptyList()
    }

    val todayDayOfWeek = remember {
        when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }
    }
    val todayDayName = remember(todayDayOfWeek) {
        when (todayDayOfWeek) {
            1 -> "Monday"
            2 -> "Tuesday"
            3 -> "Wednesday"
            4 -> "Thursday"
            5 -> "Friday"
            6 -> "Saturday"
            7 -> "Sunday"
            else -> "Today"
        }
    }
    val todayClasses = remember(routineItems, todayDayOfWeek) {
        routineItems.filter { it.dayOfWeek == todayDayOfWeek }
    }

    // Pull to refresh gesture handling
    val listState = rememberLazyListState()
    var pullOffsetY by remember { mutableStateOf(0f) }
    val animatedOffsetY by animateFloatAsState(
        targetValue = if (isRefreshing) 72f else pullOffsetY,
        label = "pull_offset_anim"
    )

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = if (isDark) Color(0xFF0B0F19) else Color(0xFFE2E8F0),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isDark) Color(0xFF1E293B) else Color(0xFFFFF7ED),
                            border = BorderStroke(2.dp, Color(0xFFF97316)),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                com.example.ui.components.ClassMateAppLogo(size = 24.dp)
                            }
                        }
                        Column {
                            Text(
                                text = "ClassMate",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color(0xFF0F172A)
                            )
                            Text(
                                text = "Student Productivity Suite",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                            )
                        }
                    }
                },
                actions = {
                    if (onImportSharedNote != null) {
                        IconButton(
                            onClick = onImportSharedNote,
                            modifier = Modifier.testTag("dashboard_import_note_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = "Import Shared Note",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Refresh Button to quickly sync sponsors & updates
                    IconButton(
                        onClick = onRefresh,
                        enabled = !isRefreshing,
                        modifier = Modifier.testTag("dashboard_refresh_button")
                    ) {
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh Dashboard",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Notification Bell replacing Settings icon
                    IconButton(
                        onClick = { showNotificationSheet = true },
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .testTag("notification_bell_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (activeAlerts.isNotEmpty()) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = MaterialTheme.colorScheme.onError
                                    ) {
                                        Text(
                                            text = if (activeAlerts.size > 9) "9+" else activeAlerts.size.toString(),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (activeAlerts.isNotEmpty()) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                                contentDescription = "Alerts & Reminders",
                                tint = if (activeAlerts.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isDark) Color(0xFF0B0F19) else Color(0xFFE2E8F0)
                )
            )
        },
        bottomBar = {
            // Elevated Bottom Navigation Bar pinned to bottom
            AppBottomNavigationBar(
                currentScreen = AppScreen.Home,
                onNavigateToHome = { /* Already on Home */ },
                onNavigateToTimer = onNavigateToTimer,
                onNavigateToSettings = onNavigateToSettings
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pointerInput(isRefreshing, listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
                    detectVerticalDragGestures(
                        onVerticalDrag = { _, dragAmount ->
                            if (listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0) {
                                if (dragAmount > 0 || pullOffsetY > 0) {
                                    pullOffsetY = (pullOffsetY + dragAmount * 0.45f).coerceIn(0f, 130f)
                                }
                            }
                        },
                        onDragEnd = {
                            if (pullOffsetY >= 70f && !isRefreshing) {
                                onRefresh()
                            }
                            pullOffsetY = 0f
                        },
                        onDragCancel = {
                            pullOffsetY = 0f
                        }
                    )
                }
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationY = animatedOffsetY
                    },
                contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 6.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
            // 1. Sponsored / Ad Banner Spot at Top (Right below TopAppBar)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 6.dp)
                ) {
                    Crossfade(
                        targetState = when {
                            !isInitialized -> 0 // Initializing / Stabilizing
                            isOnline -> 1 // Online (Sponsor / AdMob)
                            else -> 2 // Offline (Today's Academic Agenda)
                        },
                        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
                        label = "BannerSlotCrossfade"
                    ) { state ->
                        when (state) {
                            0 -> {
                                BannerSlotStabilizerPlaceholder()
                            }
                            1 -> {
                                if (displaySponsors.isNotEmpty()) {
                                    CompactSponsorCarousel(
                                        sponsors = displaySponsors,
                                        onSponsorClick = onSponsorClick,
                                        onSponsorImpression = onSponsorImpression,
                                        slideIntervalMillis = 4000L
                                    )
                                } else {
                                    AdMobBannerAd(
                                        fallback = {
                                            TodayAcademicAgendaBanner(
                                                todayClasses = todayClasses,
                                                todayDayName = todayDayName,
                                                assignments = assignments,
                                                exams = exams,
                                                onNavigateToRoutine = onNavigateToRoutine,
                                                onNavigateToAssignments = onNavigateToAssignments,
                                                onNavigateToExams = onNavigateToExams
                                            )
                                        }
                                    )
                                }
                            }
                            2 -> {
                                TodayAcademicAgendaBanner(
                                    todayClasses = todayClasses,
                                    todayDayName = todayDayName,
                                    assignments = assignments,
                                    exams = exams,
                                    onNavigateToRoutine = onNavigateToRoutine,
                                    onNavigateToAssignments = onNavigateToAssignments,
                                    onNavigateToExams = onNavigateToExams
                                )
                            }
                        }
                    }
                }
            }

            // 2. Academic Workspace Section Header with Search Action
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(11.dp),
                            color = if (isDark) Color(0xFF451A03) else Color(0xFFFEF3C7),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Text(
                            text = "Academic Workspace",
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color(0xFF0F172A)
                        )
                    }

                    // Eye-catching Vibrant Orange Circular Search Button (matching reference screen)
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFF97316),
                        shadowElevation = 4.dp,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onNavigateToSearch)
                            .testTag("academic_search_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search Notes",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // 3. 6 Grid Action Cards (2x3 Grid) - Balanced, Aesthetic proportions
            // Row 1: Class Notes & Photos | Study Planner
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HubGridCard(
                        title = "Class Notes &\nPhotos",
                        icon = Icons.Default.MenuBook,
                        iconContainerColor = if (isDark) Color(0xFF0C4A6E) else Color(0xFFE0F2FE),
                        iconTint = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                        testTag = "hub_class_notes_card",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToClassNotes
                    )

                    HubGridCard(
                        title = "Study Planner",
                        icon = Icons.Default.CalendarMonth,
                        iconContainerColor = if (isDark) Color(0xFF312E81) else Color(0xFFEEF2FF),
                        iconTint = if (isDark) Color(0xFF818CF8) else Color(0xFF4F46E5),
                        testTag = "hub_study_planner_card",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToStudyPlanner
                    )
                }
            }

            // Row 2: CGPA Calculator | Tuition Fee
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HubGridCard(
                        title = "CGPA Calculator",
                        icon = Icons.Default.Calculate,
                        iconContainerColor = if (isDark) Color(0xFF451A03) else Color(0xFFFEF3C7),
                        iconTint = if (isDark) Color(0xFFFBBF24) else Color(0xFFEA580C),
                        testTag = "hub_cgpa_card",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToCgpa
                    )

                    HubGridCard(
                        title = "Tuition Fee\nCalculator",
                        icon = Icons.Default.Payments,
                        iconContainerColor = if (isDark) Color(0xFF064E3B) else Color(0xFFD1FAE5),
                        iconTint = if (isDark) Color(0xFF34D399) else Color(0xFF059669),
                        testTag = "hub_tuition_card",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToTuition
                    )
                }
            }

            // Row 3: Assignments | Exams
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HubGridCard(
                        title = "Assignments",
                        badgeText = if (upcomingAssignmentCount > 0) "$upcomingAssignmentCount Due" else null,
                        icon = Icons.Default.Assignment,
                        iconContainerColor = if (isDark) Color(0xFF0F2E23) else Color(0xFFECFDF5),
                        iconTint = if (isDark) Color(0xFF6EE7B7) else Color(0xFF10B981),
                        testTag = "hub_assignments_card",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToAssignments
                    )

                    HubGridCard(
                        title = "Exams",
                        badgeText = if (upcomingExamCount > 0) "$upcomingExamCount" else null,
                        icon = Icons.Default.EventNote,
                        iconContainerColor = if (isDark) Color(0xFF431407) else Color(0xFFFFF7ED),
                        iconTint = if (isDark) Color(0xFFFB923C) else Color(0xFFEA580C),
                        testTag = "hub_exams_card",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToExams
                    )
                }
            }

            // Row 4: Class Routine | Focus Timer
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HubGridCard(
                        title = "Class Routine",
                        icon = Icons.Default.CalendarMonth,
                        iconContainerColor = if (isDark) Color(0xFF0C4A6E) else Color(0xFFE0F2FE),
                        iconTint = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                        testTag = "hub_routine_card",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToRoutine
                    )

                    HubGridCard(
                        title = "Focus Timer",
                        icon = Icons.Default.Timer,
                        iconContainerColor = if (isDark) Color(0xFF312E81) else Color(0xFFEDE9FE),
                        iconTint = if (isDark) Color(0xFFA78BFA) else Color(0xFF7C3AED),
                        testTag = "hub_timer_card",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToTimer
                    )
                }
            }

            // AdMob Ad Banner directly below Class Routine & Focus Timer (Large Banner 320x100)
            item {
                DashboardHubAdBanner(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 4.dp)
                )
            }

            // Row 5: Cover Page Generator (Box type matching other workspace functions)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HubGridCard(
                        title = "Cover Page\nGenerator",
                        icon = Icons.Default.Description,
                        iconContainerColor = if (isDark) Color(0xFF1E1B4B) else Color(0xFFEEF2FF),
                        iconTint = if (isDark) Color(0xFF818CF8) else Color(0xFF4F46E5),
                        testTag = "hub_cover_page_card",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToCoverPage
                    )

                    HubGridCard(
                        title = "Faculty\nInfo",
                        icon = Icons.Default.School,
                        iconContainerColor = if (isDark) Color(0xFF1E3A8A) else Color(0xFFDBEAFE),
                        iconTint = if (isDark) Color(0xFF60A5FA) else Color(0xFF2563EB),
                        testTag = "hub_faculty_info_card",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToFacultyInfo
                    )
                }
            }

            // Extra bottom spacer for smooth scrolling above bottom bar
            item {
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        // Pull to refresh top indicator banner / pill
        if (animatedOffsetY > 8f || isRefreshing) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
                tonalElevation = 6.dp,
                shadowElevation = 6.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset { IntOffset(0, (animatedOffsetY * 0.7f).toInt().coerceAtMost(60)) }
                    .padding(top = 8.dp)
                    .testTag("pull_to_refresh_indicator")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        val releaseToRefresh = pullOffsetY >= 70f
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = if (releaseToRefresh) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(16.dp)
                                .graphicsLayer {
                                    rotationZ = if (releaseToRefresh) 180f else 0f
                                }
                        )
                    }
                }
            }
        }
    }
}

    // Upcoming Alerts & Reminders Bottom Sheet
    if (showNotificationSheet) {
        UpcomingAlertsBottomSheet(
            alerts = activeAlerts,
            onDismiss = { showNotificationSheet = false },
            onClearAll = {
                dismissedAlertIds = dismissedAlertIds + allAlerts.map { it.id }
            },
            onNavigateToAssignments = {
                showNotificationSheet = false
                onNavigateToAssignments()
            },
            onNavigateToExams = {
                showNotificationSheet = false
                onNavigateToExams()
            }
        )
    }
}

@Composable
fun HubCategoryCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    badgeText: String,
    gradientColors: List<Color>,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Header accent line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(Brush.horizontalGradient(gradientColors))
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.linearGradient(gradientColors)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = badgeText,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun HubGridCard(
    title: String,
    icon: ImageVector,
    iconContainerColor: Color,
    iconTint: Color,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeText: String? = null,
    cardHeight: androidx.compose.ui.unit.Dp = 104.dp
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val containerColor = if (isDark) Color(0xFF131B2E) else Color.White
    val titleColor = if (isDark) Color.White else Color(0xFF0F172A)
    val chevronTint = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)
    val borderColor = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)

    val shadowElevation = if (isDark) 3.dp else 8.dp
    val shadowSpotColor = if (isDark) Color(0x60000000) else Color(0x3E0F172A)
    val shadowAmbientColor = if (isDark) Color(0x40000000) else Color(0x221E293B)

    Card(
        modifier = modifier
            .height(108.dp)
            .shadow(
                elevation = shadowElevation,
                shape = RoundedCornerShape(20.dp),
                spotColor = shadowSpotColor,
                ambientColor = shadowAmbientColor
            )
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDark) 3.dp else 6.dp,
            pressedElevation = 10.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            // Subtle pastel decorative aura in top right corner (matching reference app)
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 16.dp, y = (-16).dp)
                    .clip(CircleShape)
                    .background(iconContainerColor.copy(alpha = if (isDark) 0.12f else 0.28f))
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Row: Soft squircle icon + subtle chevron or badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(iconContainerColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    if (badgeText != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isDark) Color(0xFF1976D2) else Color(0xFF2563EB)
                        ) {
                            Text(
                                text = badgeText,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = chevronTint,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }

                // Bottom: Clean, bold Title (matching reference app layout)
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 16.5.sp
                    ),
                    color = titleColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun TodayScheduleHighlightCard(
    todayClasses: List<RoutineItem>,
    todayDayName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val shadowSpot = if (isDark) Color(0x60000000) else Color(0x350F172A)
    val shadowAmbient = if (isDark) Color(0x40000000) else Color(0x1F1E293B)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isDark) 3.dp else 7.dp,
                shape = RoundedCornerShape(18.dp),
                spotColor = shadowSpot,
                ambientColor = shadowAmbient
            )
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag("dashboard_today_schedule_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f) else Color.White
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (todayClasses.isNotEmpty())
                if (isDark) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f) else Color(0xFF0284C7).copy(alpha = 0.3f)
            else
                if (isDark) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f) else Color(0xFFE2E8F0)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 2.dp else 5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header: Icon + Title + Count Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                if (todayClasses.isNotEmpty()) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.secondaryContainer
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = if (todayClasses.isNotEmpty()) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "$todayDayName's Classes",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (todayClasses.isNotEmpty()) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceContainerHighest
                ) {
                    Text(
                        text = if (todayClasses.isNotEmpty()) "${todayClasses.size} ${if (todayClasses.size == 1) "Class" else "Classes"}" else "Free Day",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (todayClasses.isNotEmpty()) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            if (todayClasses.isNotEmpty()) {
                // Show first 2 classes preview
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    todayClasses.take(2).forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.75f))
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.courseName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (!item.roomNumber.isNullOrBlank()) {
                                    Text(
                                        text = "Room: ${item.roomNumber}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                            ) {
                                Text(
                                    text = "${item.startTime} - ${item.endTime}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    if (todayClasses.size > 2) {
                        Text(
                            text = "+ ${todayClasses.size - 2} more classes • Tap to view full routine",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "No classes scheduled for today. Enjoy your day! 🌴",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "View Routine →",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Sleek initial stabilizer placeholder matching exact dimensions (158.dp height, 18.dp radius).
 * Prevents any layout shift or visual flickering while network connection and sponsor cache initialize.
 */
@Composable
fun BannerSlotStabilizerPlaceholder(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(158.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(18.dp),
                spotColor = Color(0x450F172A),
                ambientColor = Color(0x220F172A)
            )
            .clip(RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F172A)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = Color(0xFF1E293B).copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Quiet, elegant dark surface holding the space smoothly for 180ms
        }
    }
}

/**
 * Premium Offline Academic Agenda Banner:
 * Occupies the exact same dimensions (158.dp height, 18.dp corner radius) as the Sponsor Carousel & AdMob Banner.
 * Shows today's classes, imminent deadlines, upcoming exams, and live academic status pills.
 */
@Composable
fun TodayAcademicAgendaBanner(
    todayClasses: List<RoutineItem>,
    todayDayName: String,
    assignments: List<Assignment>,
    exams: List<Exam>,
    onNavigateToRoutine: () -> Unit,
    onNavigateToAssignments: () -> Unit,
    onNavigateToExams: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pendingAssignments = remember(assignments) {
        assignments.filter { !it.isCompleted }.sortedBy { it.dueDate }
    }
    val upcomingExams = remember(exams) {
        val now = System.currentTimeMillis() - 86400000L
        exams.filter { it.examDate >= now }.sortedBy { it.examDate }
    }

    // Determine primary destination when clicking the overall card
    val onClickAction = remember(todayClasses, pendingAssignments, upcomingExams) {
        when {
            todayClasses.isNotEmpty() -> onNavigateToRoutine
            pendingAssignments.isNotEmpty() -> onNavigateToAssignments
            upcomingExams.isNotEmpty() -> onNavigateToExams
            else -> onNavigateToRoutine
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(158.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(18.dp),
                spotColor = Color(0x450F172A),
                ambientColor = Color(0x220F172A)
            )
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClickAction)
            .testTag("offline_academic_agenda_banner"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F172A) // Dark Slate Blue / Midnight matching app theme
        ),
        border = BorderStroke(
            width = 1.dp,
            color = Color(0xFF1E293B)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Subtle aesthetic background watermark icon
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = null,
                tint = Color(0xFF38BDF8).copy(alpha = 0.05f),
                modifier = Modifier
                    .size(130.dp)
                    .align(Alignment.CenterEnd)
                    .offset(x = 20.dp, y = 10.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF0284C7), Color(0xFF0369A1))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EventNote,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        Text(
                            text = "$todayDayName's Agenda",
                            style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.5.sp),
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF1F5F9)
                        )

                        // Status chip
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (todayClasses.isNotEmpty()) Color(0xFF0369A1).copy(alpha = 0.35f) else Color(0xFF1E293B)
                        ) {
                            Text(
                                text = if (todayClasses.isNotEmpty()) "${todayClasses.size} ${if (todayClasses.size == 1) "Class" else "Classes"}" else "No Classes",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.SemiBold,
                                color = if (todayClasses.isNotEmpty()) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "View",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF38BDF8)
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // Middle Spotlight Section
                if (todayClasses.isNotEmpty()) {
                    val firstClass = todayClasses[0]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1E293B).copy(alpha = 0.7f))
                            .clickable(onClick = onNavigateToRoutine)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = firstClass.courseName,
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            val roomText = if (!firstClass.roomNumber.isNullOrBlank()) "Room: ${firstClass.roomNumber}" else "Class Today"
                            Text(
                                text = roomText,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = Color(0xFF94A3B8)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF0F172A).copy(alpha = 0.8f),
                            border = BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Text(
                                text = "${firstClass.startTime} - ${firstClass.endTime}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF38BDF8),
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }
                } else if (upcomingExams.isNotEmpty() || pendingAssignments.isNotEmpty()) {
                    val nextExam = upcomingExams.firstOrNull()
                    val nextAssignment = pendingAssignments.firstOrNull()
                    val spotlightClick = if (nextExam != null) onNavigateToExams else onNavigateToAssignments

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1E293B).copy(alpha = 0.7f))
                            .clickable(onClick = spotlightClick)
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (nextExam != null) {
                            val daysUntil = ((nextExam.examDate - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).coerceAtLeast(0)
                            Icon(
                                imageVector = Icons.Default.EventNote,
                                contentDescription = null,
                                tint = Color(0xFFFF9100),
                                modifier = Modifier.size(18.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Exam: ${nextExam.courseName} (${nextExam.title})",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.5.sp),
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (daysUntil == 0L) "Happening Today!" else "Scheduled in $daysUntil day${if (daysUntil > 1) "s" else ""}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                                    color = if (daysUntil <= 1) Color(0xFFFF9100) else Color(0xFF94A3B8)
                                )
                            }
                        } else if (nextAssignment != null) {
                            val daysUntil = ((nextAssignment.dueDate - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).coerceAtLeast(0)
                            Icon(
                                imageVector = Icons.Default.Assignment,
                                contentDescription = null,
                                tint = Color(0xFF4ADE80),
                                modifier = Modifier.size(18.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Assignment: ${nextAssignment.title}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.5.sp),
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${nextAssignment.courseName} • Due in $daysUntil day${if (daysUntil > 1) "s" else ""}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                } else {
                    // No classes or urgent pending tasks
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1E293B).copy(alpha = 0.5f))
                            .clickable(onClick = onNavigateToRoutine)
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "No classes scheduled today! Great time to study or relax.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                            color = Color(0xFF94A3B8),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Bottom 3 Quick Status Indicators
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AcademicStatPill(
                        label = "Classes",
                        value = "${todayClasses.size}",
                        accentColor = Color(0xFF38BDF8),
                        onClick = onNavigateToRoutine,
                        modifier = Modifier.weight(1f)
                    )
                    AcademicStatPill(
                        label = "Tasks",
                        value = "${pendingAssignments.size}",
                        accentColor = Color(0xFF4ADE80),
                        onClick = onNavigateToAssignments,
                        modifier = Modifier.weight(1f)
                    )
                    AcademicStatPill(
                        label = "Exams",
                        value = "${upcomingExams.size}",
                        accentColor = Color(0xFFFF9100),
                        onClick = onNavigateToExams,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AcademicStatPill(
    label: String,
    value: String,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF1E293B).copy(alpha = 0.7f),
        border = BorderStroke(0.5.dp, Color(0xFF334155).copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(accentColor)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = Color(0xFF94A3B8),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
