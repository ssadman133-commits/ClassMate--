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
import androidx.compose.material.icons.filled.AutoAwesome
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
import androidx.compose.runtime.mutableFloatStateOf
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
import com.example.ui.theme.*
import java.util.Calendar

// Reusable static shapes & brushes to completely eliminate recomposition GC overhead
private val GridCardCornerShape = RoundedCornerShape(18.dp)
private val GridIconCornerShape = RoundedCornerShape(13.dp)
private val GridBadgeCornerShape = RoundedCornerShape(8.dp)
private val CardDropShadowBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF64748B).copy(alpha = 0.04f),
        Color(0xFF334155).copy(alpha = 0.15f)
    )
)

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
    var pullOffsetY by remember { mutableFloatStateOf(0f) }
    val animatedOffsetY by animateFloatAsState(
        targetValue = if (isRefreshing) 72f else pullOffsetY,
        label = "pull_offset_anim"
    )

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        com.example.ui.components.ClassMateAppLogo(size = 38.dp)
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Class",
                                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color.White else Color(0xFF0F172A)
                                )
                                Text(
                                    text = "Mate",
                                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color(0xFF60A5FA) else Color(0xFF2563EB)
                                )
                            }
                            Text(
                                text = "Student Productivity Suite",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = if (isDark) Color(0xFF8E9DA8) else Color(0xFF64748B)
                            )
                        }
                    }
                },
                actions = {
                    val actionBtnBg = if (isDark) Color(0xFF121D21) else Color(0xFFF1F5F9)
                    val actionBtnBorder = if (isDark) Color(0xFF1A2B31) else Color(0xFFE2E8F0)
                    val actionIconTint = if (isDark) Color(0xFF2DD4BF) else Color(0xFF475569)

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        if (onImportSharedNote != null) {
                            Surface(
                                shape = CircleShape,
                                color = actionBtnBg,
                                border = BorderStroke(1.dp, actionBtnBorder),
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .clickable(onClick = onImportSharedNote)
                                    .testTag("dashboard_import_note_button")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.FileDownload,
                                        contentDescription = "Import Shared Note",
                                        tint = actionIconTint,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // Refresh Button to quickly sync sponsors & updates
                        Surface(
                            shape = CircleShape,
                            color = actionBtnBg,
                            border = BorderStroke(1.dp, actionBtnBorder),
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .clickable(enabled = !isRefreshing, onClick = onRefresh)
                                .testTag("dashboard_refresh_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isRefreshing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = actionIconTint
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Refresh Dashboard",
                                        tint = actionIconTint,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // Notification Bell with Mint dot
                        Surface(
                            shape = CircleShape,
                            color = actionBtnBg,
                            border = BorderStroke(1.dp, actionBtnBorder),
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .clickable { showNotificationSheet = true }
                                .testTag("notification_bell_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Alerts & Reminders",
                                    tint = actionIconTint,
                                    modifier = Modifier.size(18.dp)
                                )
                                // Active indicator dot
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .align(Alignment.TopEnd)
                                        .offset(x = (-6).dp, y = 6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF2DD4BF))
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
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
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onVerticalDrag = { _, dragAmount ->
                            val isAtTop = listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
                            if (isAtTop && dragAmount > 0f || pullOffsetY > 0f) {
                                pullOffsetY = (pullOffsetY + dragAmount * 0.4f).coerceIn(0f, 110f)
                            }
                        },
                        onDragEnd = {
                            if (pullOffsetY >= 65f && !isRefreshing) {
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
            // 1. Sponsored / Ad Banner Spot at Top (Right below TopAppBar)
            item(key = "banner_slot") {
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
            item(key = "workspace_header") {
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
                            color = if (isDark) Color(0xFF102E28) else Color(0xFFCCFBF1),
                            border = BorderStroke(1.dp, if (isDark) Color(0xFF163E36) else Color(0xFF99F6E4)),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = if (isDark) Color(0xFF2DD4BF) else Color(0xFF0F766E),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Text(
                            text = "Academic Workspace",
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color(0xFF064E3B)
                        )
                    }

                    // Clean, harmonious circular search button matching workspace aesthetic
                    Surface(
                        shape = CircleShape,
                        color = if (isDark) Color(0xFF121D21) else Color(0xFFF1F5F9),
                        border = BorderStroke(1.dp, if (isDark) Color(0xFF1A2B31) else Color(0xFFE2E8F0)),
                        shadowElevation = 0.dp,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onNavigateToSearch)
                            .testTag("academic_search_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search Notes",
                                tint = if (isDark) Color(0xFF2DD4BF) else Color(0xFF475569),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // 3. 6 Grid Action Cards (2x3 Grid) - Balanced, Aesthetic proportions
            // Row 1: Class Notes & Photos | Study Planner
            item(key = "grid_row_1") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HubGridCard(
                        title = "Class Notes &\nPhotos",
                        icon = Icons.Default.MenuBook,
                        iconContainerColor = if (isDark) SoftIconSkyBgDark else SoftIconSkyBgLight,
                        iconTint = if (isDark) SoftIconSky else Color(0xFF0284C7),
                        testTag = "hub_class_notes_card",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToClassNotes
                    )

                    HubGridCard(
                        title = "Study Planner",
                        icon = Icons.Default.CalendarMonth,
                        iconContainerColor = if (isDark) SoftIconPurpleBgDark else SoftIconPurpleBgLight,
                        iconTint = if (isDark) SoftIconPurple else Color(0xFF7E22CE),
                        testTag = "hub_study_planner_card",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToStudyPlanner
                    )
                }
            }

            // Row 2: CGPA Calculator | Tuition Fee
            item(key = "grid_row_2") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HubGridCard(
                        title = "CGPA Calculator",
                        icon = Icons.Default.Calculate,
                        iconContainerColor = if (isDark) SoftIconTealBgDark else Color(0xFFE0F7FA),
                        iconTint = if (isDark) SoftIconTeal else Color(0xFF06B6D4),
                        testTag = "hub_cgpa_card",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToCgpa
                    )

                    HubGridCard(
                        title = "Tuition Fee\nCalculator",
                        icon = Icons.Default.Payments,
                        iconContainerColor = if (isDark) SoftIconMintBgDark else Color(0xFFDCFCE7),
                        iconTint = if (isDark) SoftIconMint else Color(0xFF10B981),
                        testTag = "hub_tuition_card",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToTuition
                    )
                }
            }

            // Row 3: Assignments | Exams
            item(key = "grid_row_3") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HubGridCard(
                        title = "Assignments",
                        badgeText = if (upcomingAssignmentCount > 0) "$upcomingAssignmentCount Due" else null,
                        icon = Icons.Default.Assignment,
                        iconContainerColor = if (isDark) SoftIconPeachBgDark else Color(0xFFFEF3C7),
                        iconTint = if (isDark) SoftIconPeach else Color(0xFFF59E0B),
                        testTag = "hub_assignments_card",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToAssignments
                    )

                    HubGridCard(
                        title = "Exams",
                        badgeText = if (upcomingExamCount > 0) "$upcomingExamCount" else null,
                        icon = Icons.Default.EventNote,
                        iconContainerColor = if (isDark) SoftIconCoralBgDark else Color(0xFFFFE4E6),
                        iconTint = if (isDark) SoftIconCoral else Color(0xFFF43F5E),
                        testTag = "hub_exams_card",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToExams
                    )
                }
            }

            // Row 4: Class Routine | Focus Timer
            item(key = "grid_row_4") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HubGridCard(
                        title = "Class Routine",
                        icon = Icons.Default.CalendarMonth,
                        iconContainerColor = if (isDark) SoftIconBlueBgDark else Color(0xFFEFF6FF),
                        iconTint = if (isDark) SoftIconBlue else Color(0xFF3B82F6),
                        testTag = "hub_routine_card",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToRoutine
                    )

                    HubGridCard(
                        title = "Focus Timer",
                        icon = Icons.Default.Timer,
                        iconContainerColor = if (isDark) SoftIconVioletBgDark else Color(0xFFFFE4E6),
                        iconTint = if (isDark) SoftIconViolet else Color(0xFFEF4444),
                        testTag = "hub_timer_card",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToTimer
                    )
                }
            }

            // AdMob Ad Banner directly below Class Routine & Focus Timer (Large Banner 320x100)
            item(key = "hub_ad_banner") {
                DashboardHubAdBanner(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 4.dp)
                )
            }

            // Row 5: Cover Page Generator (Box type matching other workspace functions)
            item(key = "grid_row_5") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HubGridCard(
                        title = "Cover Page\nGenerator",
                        icon = Icons.Default.Description,
                        iconContainerColor = if (isDark) SoftIconIndigoBgDark else SoftIconIndigoBgLight,
                        iconTint = if (isDark) SoftIconIndigo else Color(0xFF4338CA),
                        testTag = "hub_cover_page_card",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToCoverPage
                    )

                    HubGridCard(
                        title = "Faculty\nInfo",
                        icon = Icons.Default.School,
                        iconContainerColor = if (isDark) SoftIconGoldBgDark else SoftIconGoldBgLight,
                        iconTint = if (isDark) SoftIconGold else Color(0xFFCA8A04),
                        testTag = "hub_faculty_info_card",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToFacultyInfo
                    )
                }
            }

            // Extra bottom spacer for smooth scrolling above bottom bar
            item(key = "bottom_spacer") {
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
    cardHeight: androidx.compose.ui.unit.Dp = 118.dp
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val containerColor = if (isDark) MaterialTheme.colorScheme.surface else Color.White
    val titleColor = if (isDark) MaterialTheme.colorScheme.onSurface else Color(0xFF0F172A)
    val chevronTint = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else Color(0xFF64748B)
    val borderBrush = if (isDark) {
        Brush.horizontalGradient(
            colors = listOf(
                iconTint,
                iconTint.copy(alpha = 0.5f),
                Color(0xFF1E2D34),
                Color(0xFF162328)
            )
        )
    } else {
        Brush.horizontalGradient(
            listOf(iconTint.copy(alpha = 0.85f), iconTint.copy(alpha = 0.35f))
        )
    }

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(cardHeight)
            .testTag(testTag),
        shape = GridCardCornerShape,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(if (isDark) 1.2.dp else 1.4.dp, borderBrush),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDark) 0.dp else 1.5.dp,
            pressedElevation = 3.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
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
                            .size(44.dp)
                            .clip(GridIconCornerShape)
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
                            shape = GridBadgeCornerShape,
                            color = if (isDark) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = badgeText,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) MaterialTheme.colorScheme.onPrimaryContainer else Color.White,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = chevronTint,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Bottom: Clean, bold Title
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 17.sp
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
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag("dashboard_today_schedule_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) MaterialTheme.colorScheme.surface else Color.White
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isDark) MaterialTheme.colorScheme.outlineVariant else Color(0xFFDFE5E8)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
            pressedElevation = 2.dp
        )
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
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(158.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(18.dp),
                spotColor = if (isDark) Color(0x450F172A) else Color(0x150F172A),
                ambientColor = if (isDark) Color(0x220F172A) else Color(0x0D0F172A)
            )
            .clip(RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF0F172A) else Color(0xFFE2E8F0)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isDark) Color(0xFF1E293B).copy(alpha = 0.6f) else Color(0xFFCBD5E1)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Smooth surface holding the space
        }
    }
}

/**
 * Premium Offline Academic Agenda Banner:
 * In Light Mode: Displays the ethereal pastel aura / holographic gradient matching screenshot.
 * In Dark Mode: Displays the sleek cyber teal glowing surface.
 * Occupies 158.dp height, 18.dp corner radius.
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
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

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

    // Light mode: Gorgeous pastel holographic / aura gradient matching screenshot
    // Flows: Sky Blue (#BCE7FD) -> Soft Lilac/Purple (#D6C8FD) -> Light Mint Green (#BAF7D0)
    val lightBannerBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFFBCE7FD),
            Color(0xFFD6C8FD),
            Color(0xFFBAF7D0)
        )
    )

    val cardBorder = if (isDark) {
        BorderStroke(
            width = 1.2.dp,
            brush = Brush.horizontalGradient(
                listOf(
                    Color(0xFF2DD4BF),
                    Color(0xFF2DD4BF).copy(alpha = 0.5f),
                    Color(0xFF1B2C33),
                    Color(0xFF16252B)
                )
            )
        )
    } else {
        BorderStroke(
            width = 1.2.dp,
            brush = Brush.horizontalGradient(
                listOf(
                    Color(0xFF7DD3FC).copy(alpha = 0.7f),
                    Color(0xFFC4B5FD).copy(alpha = 0.7f),
                    Color(0xFF86EFAC).copy(alpha = 0.7f)
                )
            )
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(158.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClickAction)
            .testTag("offline_academic_agenda_banner"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF10191D) else Color(0xFFE0F2FE)
        ),
        border = cardBorder,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (!isDark) Modifier.background(lightBannerBrush)
                    else Modifier
                )
        ) {
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
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isDark) Color(0xFF102E28)
                                    else Color(0xFF0F172A).copy(alpha = 0.12f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = if (isDark) Color(0xFF2DD4BF) else Color(0xFF0F172A),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Text(
                            text = "$todayDayName's Agenda",
                            style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFFF1F5F9) else Color(0xFF0F172A)
                        )

                        // Status chip
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isDark) Color(0xFF132828) else Color(0xFF0F172A).copy(alpha = 0.10f),
                            border = if (isDark) BorderStroke(0.5.dp, Color(0xFF1B3D3B)) else null
                        ) {
                            Text(
                                text = if (todayClasses.isNotEmpty()) "${todayClasses.size} ${if (todayClasses.size == 1) "Class" else "Classes"}" else "No Classes",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDark) Color(0xFF2DD4BF) else Color(0xFF1E293B),
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
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) Color(0xFF2DD4BF) else Color(0xFF0F172A)
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = if (isDark) Color(0xFF2DD4BF) else Color(0xFF0F172A),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Middle Spotlight Section (Frosted banner matching screenshot)
                if (todayClasses.isNotEmpty()) {
                    val firstClass = todayClasses[0]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isDark) Color(0xFF132024)
                                else Color.White.copy(alpha = 0.45f)
                            )
                            .border(
                                BorderStroke(
                                    1.dp,
                                    if (isDark) Color(0xFF1B2D33)
                                    else Color.White.copy(alpha = 0.7f)
                                ),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable(onClick = onNavigateToRoutine)
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = firstClass.courseName,
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDark) Color.White else Color(0xFF0F172A),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            val roomText = if (!firstClass.roomNumber.isNullOrBlank()) "Room: ${firstClass.roomNumber}" else "Class Today"
                            Text(
                                text = roomText,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isDark) Color(0xFF0F171A) else Color.White.copy(alpha = 0.75f),
                            border = BorderStroke(
                                1.dp,
                                if (isDark) Color(0xFF1B2D33) else Color.White.copy(alpha = 0.9f)
                            )
                        ) {
                            Text(
                                text = "${firstClass.startTime} - ${firstClass.endTime}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color(0xFF2DD4BF) else Color(0xFF0F172A),
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }
                } else {
                    // No classes or urgent pending tasks (Matches screenshot banner)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isDark) Color(0xFF132024)
                                else Color.White.copy(alpha = 0.45f)
                            )
                            .border(
                                BorderStroke(
                                    1.dp,
                                    if (isDark) Color(0xFF1B2D33)
                                    else Color.White.copy(alpha = 0.7f)
                                ),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable(onClick = onNavigateToRoutine)
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = if (isDark) Color(0xFF2DD4BF) else Color(0xFF0F172A),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "No classes scheduled today! Great time to study or relax.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF1E293B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Bottom 3 Quick Status Indicators (Exact match to screenshot pills)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AcademicStatPill(
                        label = "Classes",
                        value = "${todayClasses.size}",
                        accentColor = Color(0xFF00C0FF),
                        isDark = isDark,
                        pillBgLight = Color(0xFFDDF2FD),
                        pillBorderLight = Color(0xFFBAE6FD),
                        onClick = onNavigateToRoutine,
                        modifier = Modifier.weight(1f)
                    )
                    AcademicStatPill(
                        label = "Tasks",
                        value = "${pendingAssignments.size}",
                        accentColor = Color(0xFF10B981),
                        isDark = isDark,
                        pillBgLight = Color(0xFFDCFCE7),
                        pillBorderLight = Color(0xFFBBF7D0),
                        onClick = onNavigateToAssignments,
                        modifier = Modifier.weight(1f)
                    )
                    AcademicStatPill(
                        label = "Exams",
                        value = "${upcomingExams.size}",
                        accentColor = Color(0xFFF59E0B),
                        isDark = isDark,
                        pillBgLight = Color(0xFFFEF3C7),
                        pillBorderLight = Color(0xFFFDE68A),
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
    isDark: Boolean,
    pillBgLight: Color = Color(0xFFDDF2FD),
    pillBorderLight: Color = Color(0xFFBAE6FD),
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = if (isDark) Color(0xFF132024) else pillBgLight,
        border = BorderStroke(1.dp, if (isDark) Color(0xFF1B2D33) else pillBorderLight)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(accentColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "$value $label",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                fontWeight = FontWeight.SemiBold,
                color = if (isDark) Color.White else Color(0xFF0F172A),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
