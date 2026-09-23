package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MoreTime
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.FocusSession
import com.example.data.TimerMode
import com.example.data.TimerState
import com.example.ui.ClassNotesViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class AnalyticsRange {
    DAY,
    WEEK,
    MONTH,
    YEAR
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusTimerScreen(
    focusSessions: List<FocusSession>,
    courseNames: List<String>,
    viewModel: ClassNotesViewModel? = null,
    onRecordSession: (durationMinutes: Int, subject: String) -> Unit = { _, _ -> },
    onDeleteSession: (FocusSession) -> Unit = {},
    onBack: () -> Unit,
    bottomBar: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var activeTab by remember { mutableIntStateOf(0) } // 0: Timer, 1: Analytics

    // ViewModel-backed state (if available) with fallback to local state
    val vmTimerMode by viewModel?.timerMode?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(TimerMode.STUDY) }
    val vmTimerState by viewModel?.timerState?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(TimerState.IDLE) }
    val vmStudyMinutes by viewModel?.selectedStudyMinutes?.collectAsStateWithLifecycle() ?: remember { mutableIntStateOf(25) }
    val vmBreakMinutes by viewModel?.selectedBreakMinutes?.collectAsStateWithLifecycle() ?: remember { mutableIntStateOf(5) }
    val vmTotalDuration by viewModel?.totalDurationSeconds?.collectAsStateWithLifecycle() ?: remember { mutableIntStateOf(25 * 60) }
    val vmRemaining by viewModel?.remainingSeconds?.collectAsStateWithLifecycle() ?: remember { mutableIntStateOf(25 * 60) }
    val vmSubject by viewModel?.selectedSubject?.collectAsStateWithLifecycle() ?: remember { mutableStateOf("General Study") }
    val vmCustomSubjects by viewModel?.customSubjects?.collectAsStateWithLifecycle() ?: remember {
        mutableStateOf(listOf("General Study", "Mathematics", "Physics", "Chemistry", "Computer Science", "English"))
    }

    // Local fallback states
    var localCurrentMode by remember { mutableStateOf(TimerMode.STUDY) }
    var localTimerState by remember { mutableStateOf(TimerState.IDLE) }
    var localStudyMinutes by remember { mutableIntStateOf(25) }
    var localBreakMinutes by remember { mutableIntStateOf(5) }
    var localTotalDurationSeconds by remember { mutableIntStateOf(25 * 60) }
    var localRemainingSeconds by remember { mutableIntStateOf(25 * 60) }
    var localSubject by remember { mutableStateOf("General Study") }
    var localCustomSubjects by remember {
        mutableStateOf(listOf("General Study", "Mathematics", "Physics", "Chemistry", "Computer Science", "English"))
    }

    // Unified reactive properties
    val currentMode = if (viewModel != null) vmTimerMode else localCurrentMode
    val timerState = if (viewModel != null) vmTimerState else localTimerState
    val selectedStudyMinutes = if (viewModel != null) vmStudyMinutes else localStudyMinutes
    val selectedBreakMinutes = if (viewModel != null) vmBreakMinutes else localBreakMinutes
    val totalDurationSeconds = if (viewModel != null) vmTotalDuration else localTotalDurationSeconds
    val remainingSeconds = if (viewModel != null) vmRemaining else localRemainingSeconds
    val selectedSubject = if (viewModel != null) vmSubject else localSubject
    val customSubjectsList = if (viewModel != null) vmCustomSubjects else localCustomSubjects

    // All available subjects combined
    val allAvailableSubjects = remember(courseNames, customSubjectsList) {
        buildList {
            add("General Study")
            addAll(courseNames.filter { it.isNotBlank() })
            addAll(customSubjectsList.filter { it.isNotBlank() })
        }.distinct()
    }

    // Dialogs
    var showCustomTimeDialog by remember { mutableStateOf(false) }
    var customMinutesInput by remember { mutableStateOf("") }
    var showAddSubjectDialog by remember { mutableStateOf(false) }
    var customSubjectInput by remember { mutableStateOf("") }
    var showEarlyFinishConfirmDialog by remember { mutableStateOf(false) }
    var showCelebrationDialog by remember { mutableStateOf(false) }
    var showBreakDoneDialog by remember { mutableStateOf(false) }
    var completedSessionDuration by remember { mutableIntStateOf(25) }

    fun triggerCompletionFeedback() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createWaveform(longArrayOf(0, 300, 150, 300, 150, 500), -1)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 300, 150, 300, 150, 500), -1)
            }
        } catch (_: Exception) {}
    }

    // Timer actions
    fun applyPreset(minutes: Int, mode: TimerMode) {
        if (viewModel != null) {
            viewModel.setTimerPreset(minutes, mode)
        } else {
            if (mode == TimerMode.STUDY) {
                localStudyMinutes = minutes
                if (localCurrentMode == TimerMode.STUDY && localTimerState == TimerState.IDLE) {
                    localTotalDurationSeconds = minutes * 60
                    localRemainingSeconds = minutes * 60
                }
            } else {
                localBreakMinutes = minutes
                if (localCurrentMode == TimerMode.BREAK && localTimerState == TimerState.IDLE) {
                    localTotalDurationSeconds = minutes * 60
                    localRemainingSeconds = minutes * 60
                }
            }
        }
    }

    fun switchMode(newMode: TimerMode) {
        if (viewModel != null) {
            viewModel.switchTimerMode(newMode)
        } else {
            localCurrentMode = newMode
            localTimerState = TimerState.IDLE
            val mins = if (newMode == TimerMode.STUDY) localStudyMinutes else localBreakMinutes
            localTotalDurationSeconds = mins * 60
            localRemainingSeconds = mins * 60
        }
    }

    fun handleStart() {
        if (viewModel != null) {
            viewModel.startTimer()
        } else {
            if (localRemainingSeconds <= 0) {
                val mins = if (localCurrentMode == TimerMode.STUDY) localStudyMinutes else localBreakMinutes
                localTotalDurationSeconds = mins * 60
                localRemainingSeconds = mins * 60
            }
            localTimerState = TimerState.RUNNING
        }
    }

    fun handlePause() {
        if (viewModel != null) {
            viewModel.pauseTimer()
        } else {
            localTimerState = TimerState.PAUSED
        }
    }

    fun handleReset() {
        val elapsedSec = maxOf(0, totalDurationSeconds - remainingSeconds)
        if (currentMode == TimerMode.STUDY && elapsedSec >= 30) {
            showEarlyFinishConfirmDialog = true
        } else {
            if (viewModel != null) {
                viewModel.resetTimer()
            } else {
                localTimerState = TimerState.IDLE
                val mins = if (localCurrentMode == TimerMode.STUDY) localStudyMinutes else localBreakMinutes
                localTotalDurationSeconds = mins * 60
                localRemainingSeconds = mins * 60
            }
        }
    }

    fun handleAddFiveMinutes() {
        if (viewModel != null) {
            viewModel.addFiveMinutes()
        } else {
            localTotalDurationSeconds += 5 * 60
            localRemainingSeconds += 5 * 60
        }
    }

    fun handleFinishEarlyAndSave() {
        if (viewModel != null) {
            val mins = viewModel.finishEarlyAndSave()
            completedSessionDuration = mins
            showCelebrationDialog = true
        } else {
            val elapsedSec = maxOf(0, localTotalDurationSeconds - localRemainingSeconds)
            val mins = maxOf(1, elapsedSec / 60)
            completedSessionDuration = mins
            if (localCurrentMode == TimerMode.STUDY && elapsedSec >= 30) {
                onRecordSession(mins, localSubject)
                showCelebrationDialog = true
            }
            localTimerState = TimerState.IDLE
            val presetMins = if (localCurrentMode == TimerMode.STUDY) localStudyMinutes else localBreakMinutes
            localTotalDurationSeconds = presetMins * 60
            localRemainingSeconds = presetMins * 60
        }
    }

    fun handleSelectSubject(sub: String) {
        if (viewModel != null) {
            viewModel.setSelectedSubject(sub)
        } else {
            localSubject = sub
        }
    }

    fun handleAddCustomSubject(sub: String) {
        val trimmed = sub.trim()
        if (trimmed.isNotBlank()) {
            if (viewModel != null) {
                viewModel.addCustomSubject(trimmed)
                viewModel.setSelectedSubject(trimmed)
            } else {
                val list = localCustomSubjects.toMutableList()
                if (!list.contains(trimmed)) list.add(trimmed)
                localCustomSubjects = list
                localSubject = trimmed
            }
        }
    }

    // ViewModel completion event listener
    LaunchedEffect(viewModel) {
        viewModel?.timerCompletedEvent?.collect { finishedMode ->
            triggerCompletionFeedback()
            if (finishedMode == TimerMode.STUDY) {
                completedSessionDuration = maxOf(1, totalDurationSeconds / 60)
                showCelebrationDialog = true
            } else {
                showBreakDoneDialog = true
            }
        }
    }

    // Local fallback Timer Loop if viewModel is not provided
    LaunchedEffect(localTimerState) {
        if (viewModel == null && localTimerState == TimerState.RUNNING) {
            while (localRemainingSeconds > 0 && localTimerState == TimerState.RUNNING) {
                delay(1000L)
                if (localTimerState == TimerState.RUNNING) {
                    localRemainingSeconds--
                }
            }
            if (localRemainingSeconds <= 0 && localTimerState == TimerState.RUNNING) {
                localTimerState = TimerState.COMPLETED
                triggerCompletionFeedback()
                if (localCurrentMode == TimerMode.STUDY) {
                    val durationMins = maxOf(1, localTotalDurationSeconds / 60)
                    completedSessionDuration = durationMins
                    onRecordSession(durationMins, localSubject)
                    showCelebrationDialog = true
                } else {
                    showBreakDoneDialog = true
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Focus Timer",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("focus_timer_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = bottomBar
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Top Tabs: Timer vs Analytics
            PrimaryTabRow(
                selectedTabIndex = activeTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Timer", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.Timer, contentDescription = null) },
                    modifier = Modifier.testTag("tab_focus_timer")
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Analytics & Chart", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
                    modifier = Modifier.testTag("tab_focus_analytics")
                )
            }

            if (activeTab == 0) {
                // TIMER VIEW
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Mode Switcher (Study vs Break)
                    item {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val isStudy = currentMode == TimerMode.STUDY
                                Button(
                                    onClick = { switchMode(TimerMode.STUDY) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isStudy) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        contentColor = if (isStudy) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    shape = RoundedCornerShape(20.dp),
                                    elevation = null,
                                    modifier = Modifier.testTag("timer_mode_study_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.School,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Study Focus", fontWeight = FontWeight.SemiBold)
                                }

                                val isBreak = currentMode == TimerMode.BREAK
                                Button(
                                    onClick = { switchMode(TimerMode.BREAK) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isBreak) MaterialTheme.colorScheme.secondary else Color.Transparent,
                                        contentColor = if (isBreak) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    shape = RoundedCornerShape(20.dp),
                                    elevation = null,
                                    modifier = Modifier.testTag("timer_mode_break_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Coffee,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Break", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    // Active Subject Indicator (Study Mode)
                    if (currentMode == TimerMode.STUDY) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                                modifier = Modifier
                                    .clickable { showAddSubjectDialog = true }
                                    .testTag("active_subject_indicator")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.School,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Subject: $selectedSubject",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Subject",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Circular Countdown Timer Display
                    item {
                        val progress = if (totalDurationSeconds > 0) {
                            (remainingSeconds.toFloat() / totalDurationSeconds.toFloat()).coerceIn(0f, 1f)
                        } else 0f

                        val animatedProgress by animateFloatAsState(
                            targetValue = progress,
                            label = "TimerProgress"
                        )

                        val minutesLeft = remainingSeconds / 60
                        val secondsLeft = remainingSeconds % 60
                        val timeFormatted = String.format(Locale.US, "%02d:%02d", minutesLeft, secondsLeft)

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .padding(vertical = 12.dp)
                                .size(240.dp)
                                .testTag("timer_circle_box")
                        ) {
                            // Background Track
                            CircularProgressIndicator(
                                progress = { 1f },
                                modifier = Modifier.size(230.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                strokeWidth = 14.dp,
                                strokeCap = StrokeCap.Round
                            )

                            // Active Countdown Progress
                            val timerColor = if (currentMode == TimerMode.STUDY) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.secondary
                            }

                            CircularProgressIndicator(
                                progress = { animatedProgress },
                                modifier = Modifier.size(230.dp),
                                color = timerColor,
                                strokeWidth = 14.dp,
                                strokeCap = StrokeCap.Round
                            )

                            // Inner Center Text
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                val modeLabel = if (currentMode == TimerMode.STUDY) "STUDY FOCUS" else "RELAX BREAK"
                                Text(
                                    text = modeLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp,
                                    color = timerColor
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = timeFormatted,
                                    style = MaterialTheme.typography.displayMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 2.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                val stateText = when (timerState) {
                                    TimerState.IDLE -> "Ready"
                                    TimerState.RUNNING -> "In Progress..."
                                    TimerState.PAUSED -> "Paused"
                                    TimerState.COMPLETED -> "Done! 🎉"
                                }
                                Text(
                                    text = stateText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Main Controls: Start / Pause / Resume / Reset
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Reset Button
                                IconButton(
                                    onClick = { handleReset() },
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                        .testTag("timer_reset_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Reset Timer",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Start / Pause / Resume Primary Button
                                Button(
                                    onClick = {
                                        when (timerState) {
                                            TimerState.IDLE, TimerState.PAUSED, TimerState.COMPLETED -> handleStart()
                                            TimerState.RUNNING -> handlePause()
                                        }
                                    },
                                    shape = RoundedCornerShape(28.dp),
                                    modifier = Modifier
                                        .height(56.dp)
                                        .width(160.dp)
                                        .testTag("timer_main_action_button"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (currentMode == TimerMode.STUDY) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                    )
                                ) {
                                    val icon = if (timerState == TimerState.RUNNING) Icons.Default.Pause else Icons.Default.PlayArrow
                                    val label = when (timerState) {
                                        TimerState.RUNNING -> "Pause"
                                        TimerState.PAUSED -> "Resume"
                                        TimerState.COMPLETED -> "Restart"
                                        TimerState.IDLE -> "Start"
                                    }
                                    Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }

                                // Add 5 min quick boost
                                IconButton(
                                    onClick = { handleAddFiveMinutes() },
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                        .testTag("timer_add_5m_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreTime,
                                        contentDescription = "+5 Min",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Explicit "Finish Early & Save" button when user has studied for at least 30s
                            val elapsedSec = maxOf(0, totalDurationSeconds - remainingSeconds)
                            val elapsedMins = maxOf(1, elapsedSec / 60)
                            AnimatedVisibility(
                                visible = (timerState == TimerState.RUNNING || timerState == TimerState.PAUSED) &&
                                        currentMode == TimerMode.STUDY &&
                                        elapsedSec >= 30
                            ) {
                                OutlinedButton(
                                    onClick = { showEarlyFinishConfirmDialog = true },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.primary
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp)
                                        .testTag("finish_early_and_save_button")
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Finish & Save ($elapsedMins min studied)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    // Duration Presets & Custom
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp)),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (currentMode == TimerMode.STUDY) "Study Duration" else "Break Duration",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )

                                    TextButton(
                                        onClick = { showCustomTimeDialog = true },
                                        modifier = Modifier.testTag("custom_time_button")
                                    ) {
                                        Text("Custom Time", fontWeight = FontWeight.SemiBold)
                                    }
                                }

                                val presets = if (currentMode == TimerMode.STUDY) {
                                    listOf(15, 20, 25, 30, 45, 60)
                                } else {
                                    listOf(3, 5, 10, 15, 20)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    presets.forEach { mins ->
                                        val isSelected = if (currentMode == TimerMode.STUDY) {
                                            selectedStudyMinutes == mins
                                        } else {
                                            selectedBreakMinutes == mins
                                        }

                                        FilterChip(
                                            selected = isSelected,
                                            onClick = {
                                                applyPreset(mins, currentMode)
                                            },
                                            label = {
                                                Text(
                                                    text = "${mins}m",
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    fontSize = 12.sp
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = if (currentMode == TimerMode.STUDY) {
                                                    MaterialTheme.colorScheme.primaryContainer
                                                } else {
                                                    MaterialTheme.colorScheme.secondaryContainer
                                                }
                                            ),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Studying Subject Selector (for Study mode)
                    if (currentMode == TimerMode.STUDY) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp)),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.School,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = "Studying Subject",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        TextButton(
                                            onClick = {
                                                customSubjectInput = ""
                                                showAddSubjectDialog = true
                                            },
                                            modifier = Modifier.testTag("add_custom_subject_btn")
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("+ Add Custom", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    // Horizontally scrollable chip row of all subjects
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(allAvailableSubjects) { sub ->
                                            val isSelected = selectedSubject == sub
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = { handleSelectSubject(sub) },
                                                leadingIcon = if (isSelected) {
                                                    {
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                    }
                                                } else null,
                                                label = {
                                                    Text(
                                                        text = sub,
                                                        maxLines = 1,
                                                        fontSize = 12.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Note on Break Time
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Only real study minutes are recorded into your study hours chart. Break times are never added.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            } else {
                // ANALYTICS & CHART VIEW
                StudyAnalyticsView(
                    focusSessions = focusSessions,
                    onDeleteSession = onDeleteSession
                )
            }
        }
    }

    // Custom Minutes Dialog
    if (showCustomTimeDialog) {
        AlertDialog(
            onDismissRequest = { showCustomTimeDialog = false },
            title = { Text("Set Custom Minutes") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Enter desired duration in minutes (1 to 180):",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = customMinutesInput,
                        onValueChange = { customMinutesInput = it.filter { char -> char.isDigit() } },
                        label = { Text("Minutes") },
                        placeholder = { Text("e.g. 35") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_minutes_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val mins = customMinutesInput.toIntOrNull()?.coerceIn(1, 180) ?: 25
                        applyPreset(mins, currentMode)
                        showCustomTimeDialog = false
                        customMinutesInput = ""
                    }
                ) {
                    Text("Set Time")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomTimeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Celebration Dialog (Study Done)
    if (showCelebrationDialog) {
        AlertDialog(
            onDismissRequest = { showCelebrationDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text("Great Focus Session! 🎉", textAlign = TextAlign.Center)
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "You successfully completed $completedSessionDuration minutes of pure study focus for '$selectedSubject'!",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "This session has been automatically added to your study statistics chart.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCelebrationDialog = false
                        // Quick switch to 5 min break
                        switchMode(TimerMode.BREAK)
                        applyPreset(5, TimerMode.BREAK)
                        handleStart()
                    }
                ) {
                    Icon(Icons.Default.Coffee, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Take 5m Break")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showCelebrationDialog = false
                        handleReset()
                    }
                ) {
                    Text("Close")
                }
            }
        )
    }

    // Break Completed Dialog
    if (showBreakDoneDialog) {
        AlertDialog(
            onDismissRequest = { showBreakDoneDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Coffee,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text("Break Over! ☕", textAlign = TextAlign.Center)
            },
            text = {
                Text(
                    text = "Hope you feel refreshed! Ready to start another productive study session?",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showBreakDoneDialog = false
                        switchMode(TimerMode.STUDY)
                        handleStart()
                    }
                ) {
                    Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Start Study")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showBreakDoneDialog = false
                        switchMode(TimerMode.STUDY)
                    }
                ) {
                    Text("Close")
                }
            }
        )
    }

    // Add Custom Subject Dialog
    if (showAddSubjectDialog) {
        AlertDialog(
            onDismissRequest = { showAddSubjectDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = { Text("Add Subject") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Enter the subject or topic you want to study:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = customSubjectInput,
                        onValueChange = { customSubjectInput = it },
                        label = { Text("Subject Name") },
                        placeholder = { Text("e.g. Organic Chemistry, Biology") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_subject_text_field")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customSubjectInput.isNotBlank()) {
                            handleAddCustomSubject(customSubjectInput)
                            showAddSubjectDialog = false
                            customSubjectInput = ""
                        }
                    },
                    enabled = customSubjectInput.isNotBlank(),
                    modifier = Modifier.testTag("confirm_add_subject_button")
                ) {
                    Text("Add & Select")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSubjectDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Early Finish / Reset Confirmation Dialog (When >= 30s studied)
    if (showEarlyFinishConfirmDialog) {
        val elapsedSec = maxOf(0, totalDurationSeconds - remainingSeconds)
        val elapsedMins = maxOf(1, elapsedSec / 60)
        AlertDialog(
            onDismissRequest = { showEarlyFinishConfirmDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = { Text("Save Study Session?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "You have studied for $elapsedMins minute(s) on '$selectedSubject'.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Would you like to save this progress to your history before resetting the timer?",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showEarlyFinishConfirmDialog = false
                        handleFinishEarlyAndSave()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("dialog_save_and_finish_button")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save & Finish ($elapsedMins m)")
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(
                        onClick = {
                            showEarlyFinishConfirmDialog = false
                            if (viewModel != null) {
                                viewModel.resetTimer()
                            } else {
                                localTimerState = TimerState.IDLE
                                val mins = if (localCurrentMode == TimerMode.STUDY) localStudyMinutes else localBreakMinutes
                                localTotalDurationSeconds = mins * 60
                                localRemainingSeconds = mins * 60
                            }
                        }
                    ) {
                        Text("Discard & Reset", color = MaterialTheme.colorScheme.error)
                    }
                    TextButton(
                        onClick = { showEarlyFinishConfirmDialog = false }
                    ) {
                        Text("Keep Studying")
                    }
                }
            }
        )
    }
}

/**
 * Dedicated Study Analytics View with Day, Week, Month, and Year Bar Charts
 */
@Composable
fun StudyAnalyticsView(
    focusSessions: List<FocusSession>,
    onDeleteSession: (FocusSession) -> Unit,
    modifier: Modifier = Modifier
) {
    // 0: Date-wise & Month History, 1: Progress Charts
    var analyticsMode by remember { mutableIntStateOf(0) }
    var selectedMonthKey by remember { mutableStateOf("ALL") } // "ALL" or "yyyy-MM"
    var chartRange by remember { mutableStateOf(AnalyticsRange.WEEK) }

    // Prepare distinct months available from data (or current month)
    val availableMonths = remember(focusSessions) {
        val monthFormat = SimpleDateFormat("yyyy-MM", Locale.US)
        val monthDisplayFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        val keys = focusSessions.map { monthFormat.format(Date(it.completedAt)) }.distinct().sortedDescending().toMutableList()
        val currentMonthKey = monthFormat.format(Date())
        if (!keys.contains(currentMonthKey)) {
            keys.add(0, currentMonthKey)
        }
        keys.map { key ->
            try {
                val parsed = monthFormat.parse(key)
                Pair(key, parsed?.let { monthDisplayFormat.format(it) } ?: key)
            } catch (_: Exception) {
                Pair(key, key)
            }
        }
    }

    // Filter sessions based on selectedMonthKey
    val monthFormat = remember { SimpleDateFormat("yyyy-MM", Locale.US) }
    val filteredSessions = remember(focusSessions, selectedMonthKey) {
        if (selectedMonthKey == "ALL") {
            focusSessions
        } else {
            focusSessions.filter {
                monthFormat.format(Date(it.completedAt)) == selectedMonthKey
            }
        }
    }

    // Group sessions by day (e.g. "yyyy-MM-dd")
    val dayKeyFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val groupedByDay = remember(filteredSessions) {
        filteredSessions.groupBy {
            dayKeyFormat.format(Date(it.completedAt))
        }.toList().sortedByDescending { it.first } // Newest dates first
    }

    val totalMins = remember(filteredSessions) { filteredSessions.sumOf { it.durationMinutes } }
    val totalHours = totalMins / 60
    val remainingMins = totalMins % 60
    val activeDaysCount = remember(filteredSessions) {
        filteredSessions.map { dayKeyFormat.format(Date(it.completedAt)) }.distinct().size
    }

    val displayDateFormat = remember { SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()) }
    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
    val yesterdayCal = remember { Calendar.getInstance().apply { add(Calendar.DATE, -1) } }
    val yesterdayStr = remember(yesterdayCal) { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(yesterdayCal.time) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Mode Switcher: [📅 Date History] vs [📊 Charts]
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Date History Button
                    Button(
                        onClick = { analyticsMode = 0 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (analyticsMode == 0) MaterialTheme.colorScheme.primary else Color.Transparent,
                            contentColor = if (analyticsMode == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = null,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tab_date_history")
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Date History", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    // Charts Button
                    Button(
                        onClick = { analyticsMode = 1 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (analyticsMode == 1) MaterialTheme.colorScheme.primary else Color.Transparent,
                            contentColor = if (analyticsMode == 1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = null,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tab_progress_charts")
                    ) {
                        Icon(Icons.Default.BarChart, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Progress Charts", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        if (analyticsMode == 0) {
            // DATE-WISE & MONTH-WISE BREAKDOWN VIEW
            // Month Filter Chips
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Filter by Month",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = selectedMonthKey == "ALL",
                                onClick = { selectedMonthKey = "ALL" },
                                label = { Text("All Time (${focusSessions.size})", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                        items(availableMonths) { (key, display) ->
                            val count = focusSessions.count { monthFormat.format(Date(it.completedAt)) == key }
                            FilterChip(
                                selected = selectedMonthKey == key,
                                onClick = { selectedMonthKey = key },
                                label = { Text("$display ($count)", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }
            }

            // Period Summary Card
            item {
                val currentPeriodLabel = if (selectedMonthKey == "ALL") {
                    "All-Time Study Summary"
                } else {
                    availableMonths.firstOrNull { it.first == selectedMonthKey }?.second ?: selectedMonthKey
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = currentPeriodLabel,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = if (totalMins > 0) "Total: ${totalHours}h ${remainingMins}m studied" else "No study time recorded",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalFireDepartment,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "${filteredSessions.size} Sessions",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        // Metric Stat Columns
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Total Hours
                            Column {
                                Text(
                                    text = if (totalHours > 0) "${totalHours}h ${remainingMins}m" else "${remainingMins}m",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Total Focus",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                            }

                            // Active Days
                            Column {
                                Text(
                                    text = "$activeDaysCount Days",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Days Active",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                            }

                            // Daily Average
                            val dailyAvgMins = if (activeDaysCount > 0) totalMins / activeDaysCount else 0
                            Column {
                                Text(
                                    text = if (dailyAvgMins >= 60) String.format(Locale.US, "%.1fh", dailyAvgMins / 60.0) else "${dailyAvgMins}m",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Daily Avg",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }

                        // Subject Distribution Tags
                        val subjectBreakdown = remember(filteredSessions) {
                            filteredSessions.groupBy { it.subject }
                                .mapValues { entry -> entry.value.sumOf { it.durationMinutes } }
                                .toList()
                                .sortedByDescending { it.second }
                        }
                        if (subjectBreakdown.isNotEmpty()) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(subjectBreakdown) { (sub, mins) ->
                                    val h = mins / 60
                                    val m = mins % 60
                                    val formattedTime = if (h > 0) "${h}h ${m}m" else "${m}m"
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f)
                                    ) {
                                        Text(
                                            text = "$sub: $formattedTime",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Date-wise Grouped Sessions List
            if (filteredSessions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "No focus study sessions for this period",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Complete study sessions with the timer to see your daily breakdown here.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                groupedByDay.forEach { (dateKey, daySessions) ->
                    val dayTotalMins = daySessions.sumOf { it.durationMinutes }
                    val dayHours = dayTotalMins / 60
                    val dayRemMins = dayTotalMins % 60
                    val dayTimeStr = if (dayHours > 0) "${dayHours}h ${dayRemMins}m" else "${dayRemMins}m"

                    val firstTimestamp = daySessions.firstOrNull()?.completedAt ?: System.currentTimeMillis()
                    val dateHeaderLabel = when (dateKey) {
                        todayStr -> "Today • " + SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(firstTimestamp))
                        yesterdayStr -> "Yesterday • " + SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(firstTimestamp))
                        else -> displayDateFormat.format(Date(firstTimestamp))
                    }

                    // Date Header
                    item(key = "header_$dateKey") {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Today,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = dateHeaderLabel,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "$dayTimeStr (${daySessions.size})",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Sessions on this date
                    items(daySessions, key = { it.id }) { session ->
                        SessionItemCard(
                            session = session,
                            onDelete = { onDeleteSession(session) }
                        )
                    }
                }
            }
        } else {
            // PROGRESS CHARTS VIEW (Day / Week / Month / Year)
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        AnalyticsRange.entries.forEach { item ->
                            val isSelected = chartRange == item
                            Button(
                                onClick = { chartRange = item },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = RoundedCornerShape(12.dp),
                                elevation = null,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = when (item) {
                                        AnalyticsRange.DAY -> "Day"
                                        AnalyticsRange.WEEK -> "Week"
                                        AnalyticsRange.MONTH -> "Month"
                                        AnalyticsRange.YEAR -> "Year"
                                    },
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            item {
                when (chartRange) {
                    AnalyticsRange.DAY -> DayAnalyticsSection(focusSessions)
                    AnalyticsRange.WEEK -> WeekAnalyticsSection(focusSessions)
                    AnalyticsRange.MONTH -> MonthAnalyticsSection(focusSessions)
                    AnalyticsRange.YEAR -> YearAnalyticsSection(focusSessions)
                }
            }

            item {
                Text(
                    text = "Recent Sessions (${focusSessions.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (focusSessions.isEmpty()) {
                item {
                    Text(
                        text = "No study sessions recorded yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(focusSessions.take(10), key = { it.id }) { session ->
                    SessionItemCard(
                        session = session,
                        onDelete = { onDeleteSession(session) }
                    )
                }
            }
        }
    }
}

@Composable
fun DayAnalyticsSection(sessions: List<FocusSession>) {
    val todayCal = Calendar.getInstance()
    val todayYear = todayCal.get(Calendar.YEAR)
    val todayDayOfYear = todayCal.get(Calendar.DAY_OF_YEAR)

    val todaySessions = sessions.filter {
        val c = Calendar.getInstance().apply { timeInMillis = it.completedAt }
        c.get(Calendar.YEAR) == todayYear && c.get(Calendar.DAY_OF_YEAR) == todayDayOfYear
    }

    val totalMins = todaySessions.sumOf { it.durationMinutes }
    val hours = totalMins / 60
    val mins = totalMins % 60

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Today's Study Summary",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = if (hours > 0) "${hours}h ${mins}m" else "${mins} mins",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Pure Study Time (Excludes Breaks)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                ) {
                    Text(
                        text = "${todaySessions.size} Sessions",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            // Morning / Afternoon / Evening distribution
            val morningMins = todaySessions.filter {
                val h = Calendar.getInstance().apply { timeInMillis = it.completedAt }.get(Calendar.HOUR_OF_DAY)
                h in 5..11
            }.sumOf { it.durationMinutes }

            val afternoonMins = todaySessions.filter {
                val h = Calendar.getInstance().apply { timeInMillis = it.completedAt }.get(Calendar.HOUR_OF_DAY)
                h in 12..17
            }.sumOf { it.durationMinutes }

            val eveningMins = todaySessions.filter {
                val h = Calendar.getInstance().apply { timeInMillis = it.completedAt }.get(Calendar.HOUR_OF_DAY)
                h >= 18 || h < 5
            }.sumOf { it.durationMinutes }

            val maxPeriod = maxOf(morningMins, afternoonMins, eveningMins, 1)

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Time of Day Distribution",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )

                PeriodBarRow("Morning (5am - 12pm)", morningMins, maxPeriod)
                PeriodBarRow("Afternoon (12pm - 6pm)", afternoonMins, maxPeriod)
                PeriodBarRow("Night (6pm - 5am)", eveningMins, maxPeriod)
            }
        }
    }
}

@Composable
fun PeriodBarRow(label: String, mins: Int, maxMins: Int) {
    val fraction = (mins.toFloat() / maxMins.toFloat()).coerceIn(0.05f, 1f)
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
            Text("${mins}m", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@Composable
fun WeekAnalyticsSection(sessions: List<FocusSession>) {
    val cal = Calendar.getInstance()
    cal.firstDayOfWeek = Calendar.MONDAY
    cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    val startOfWeek = cal.timeInMillis

    cal.add(Calendar.DAY_OF_WEEK, 7)
    val endOfWeek = cal.timeInMillis

    val weekSessions = sessions.filter { it.completedAt in startOfWeek until endOfWeek }
    val totalMins = weekSessions.sumOf { it.durationMinutes }
    val totalHours = totalMins / 60.0
    val dailyAvgHours = totalHours / 7.0

    // Day by day calculations (Mon to Sun)
    val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val dayMinutes = IntArray(7)

    for (session in weekSessions) {
        val sCal = Calendar.getInstance().apply { timeInMillis = session.completedAt }
        var dayIdx = sCal.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY
        if (dayIdx < 0) dayIdx += 7
        if (dayIdx in 0..6) {
            dayMinutes[dayIdx] += session.durationMinutes
        }
    }

    val maxDayMins = maxOf(dayMinutes.maxOrNull() ?: 0, 60)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "This Week's Study Hours",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Text(
                        text = String.format(Locale.US, "%.1f Hours", totalHours),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                ) {
                    Text(
                        text = String.format(Locale.US, "Avg: %.1fh/day", dailyAvgHours),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            // 7-Day Bar Chart
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Weekly Chart (Pure Study Hours)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val currentDayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
                    var todayIdx = currentDayOfWeek - Calendar.MONDAY
                    if (todayIdx < 0) todayIdx += 7

                    for (i in 0..6) {
                        val mins = dayMinutes[i]
                        val hrs = mins / 60.0
                        val heightFraction = (mins.toFloat() / maxDayMins.toFloat()).coerceIn(0.04f, 1f)
                        val isToday = i == todayIdx

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = if (mins > 0) String.format(Locale.US, "%.1fh", hrs) else "",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Box(
                                modifier = Modifier
                                    .width(22.dp)
                                    .height((heightFraction * 90).dp)
                                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                    .background(
                                        if (isToday) MaterialTheme.colorScheme.primary
                                        else if (mins > 0) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = dayNames[i],
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 11.sp,
                                fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MonthAnalyticsSection(sessions: List<FocusSession>) {
    val cal = Calendar.getInstance()
    val currentYear = cal.get(Calendar.YEAR)
    val currentMonth = cal.get(Calendar.MONTH)

    val monthSessions = sessions.filter {
        val sCal = Calendar.getInstance().apply { timeInMillis = it.completedAt }
        sCal.get(Calendar.YEAR) == currentYear && sCal.get(Calendar.MONTH) == currentMonth
    }

    val totalMins = monthSessions.sumOf { it.durationMinutes }
    val totalHours = totalMins / 60.0
    val activeDays = monthSessions.map {
        val sCal = Calendar.getInstance().apply { timeInMillis = it.completedAt }
        sCal.get(Calendar.DAY_OF_MONTH)
    }.distinct().size

    // 4 Weeks breakdown
    val weekBuckets = IntArray(5)
    for (session in monthSessions) {
        val sCal = Calendar.getInstance().apply { timeInMillis = session.completedAt }
        val day = sCal.get(Calendar.DAY_OF_MONTH)
        val wIdx = ((day - 1) / 7).coerceIn(0, 4)
        weekBuckets[wIdx] += session.durationMinutes
    }

    val maxWeekMins = maxOf(weekBuckets.maxOrNull() ?: 0, 60)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    val monthName = SimpleDateFormat("MMMM yyyy", Locale.US).format(Date())
                    Text(
                        text = "$monthName Study",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Text(
                        text = String.format(Locale.US, "%.1f Hours", totalHours),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                ) {
                    Text(
                        text = "$activeDays Active Days",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            // Monthly Weeks Chart
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Weekly Breakdown This Month",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val weekLabels = listOf("Wk 1", "Wk 2", "Wk 3", "Wk 4", "Wk 5")
                    for (i in 0..4) {
                        val mins = weekBuckets[i]
                        val hrs = mins / 60.0
                        val heightFraction = (mins.toFloat() / maxWeekMins.toFloat()).coerceIn(0.04f, 1f)

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = if (mins > 0) String.format(Locale.US, "%.1fh", hrs) else "",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Box(
                                modifier = Modifier
                                    .width(28.dp)
                                    .height((heightFraction * 80).dp)
                                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                    .background(
                                        if (mins > 0) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = weekLabels[i],
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun YearAnalyticsSection(sessions: List<FocusSession>) {
    val cal = Calendar.getInstance()
    val currentYear = cal.get(Calendar.YEAR)

    val yearSessions = sessions.filter {
        val sCal = Calendar.getInstance().apply { timeInMillis = it.completedAt }
        sCal.get(Calendar.YEAR) == currentYear
    }

    val totalMins = yearSessions.sumOf { it.durationMinutes }
    val totalHours = totalMins / 60.0

    // 12 Months breakdown
    val monthMinutes = IntArray(12)
    for (session in yearSessions) {
        val sCal = Calendar.getInstance().apply { timeInMillis = session.completedAt }
        val m = sCal.get(Calendar.MONTH)
        if (m in 0..11) {
            monthMinutes[m] += session.durationMinutes
        }
    }

    val maxMonthMins = maxOf(monthMinutes.maxOrNull() ?: 0, 60)
    val monthAbbr = listOf("J", "F", "M", "A", "M", "J", "J", "A", "S", "O", "N", "D")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "$currentYear Total Study",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Text(
                        text = String.format(Locale.US, "%.1f Hours", totalHours),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                ) {
                    Text(
                        text = "${yearSessions.size} Sessions",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            // 12 Month Bar Chart
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "12 Months Study Trend",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val thisMonth = Calendar.getInstance().get(Calendar.MONTH)
                    for (i in 0..11) {
                        val mins = monthMinutes[i]
                        val hrs = mins / 60.0
                        val heightFraction = (mins.toFloat() / maxMonthMins.toFloat()).coerceIn(0.04f, 1f)
                        val isCurrent = i == thisMonth

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(16.dp)
                                    .height((heightFraction * 80).dp)
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(
                                        if (isCurrent) MaterialTheme.colorScheme.primary
                                        else if (mins > 0) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    )
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = monthAbbr[i],
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Normal,
                                color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SessionItemCard(
    session: FocusSession,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = session.subject,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    val dateFormatted = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
                        .format(Date(session.completedAt))
                    Text(
                        text = dateFormatted,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "+${session.durationMinutes} min",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Session",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
