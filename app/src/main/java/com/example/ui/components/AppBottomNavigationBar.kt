package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppScreen

sealed class BottomNavTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    data object Home : BottomNavTab(
        route = "home",
        label = "Home",
        selectedIcon = Icons.Default.Home,
        unselectedIcon = Icons.Outlined.Home,
        testTag = "nav_tab_home"
    )

    data object FocusTimer : BottomNavTab(
        route = "timer",
        label = "Focus",
        selectedIcon = Icons.Default.Timer,
        unselectedIcon = Icons.Outlined.Timer,
        testTag = "nav_tab_timer"
    )

    data object Settings : BottomNavTab(
        route = "settings",
        label = "Settings",
        selectedIcon = Icons.Default.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        testTag = "nav_tab_settings"
    )
}

@Composable
fun AppBottomNavigationBar(
    currentScreen: AppScreen,
    onNavigateToHome: () -> Unit,
    onNavigateToTimer: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = listOf(
        BottomNavTab.Home,
        BottomNavTab.FocusTimer,
        BottomNavTab.Settings
    )

    val currentTab = when (currentScreen) {
        is AppScreen.Home -> BottomNavTab.Home
        is AppScreen.FocusTimer -> BottomNavTab.FocusTimer
        is AppScreen.Settings -> BottomNavTab.Settings
        else -> null
    }

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        color = MaterialTheme.colorScheme.background,
        border = BorderStroke(
            width = 1.dp,
            color = if (isDark) MaterialTheme.colorScheme.outlineVariant else Color(0xFFE2E8F0)
        )
    ) {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.background,
            tonalElevation = 0.dp
        ) {
            tabs.forEach { tab ->
                val selected = currentTab == tab
                NavigationBarItem(
                    selected = selected,
                    onClick = {
                        when (tab) {
                            BottomNavTab.Home -> onNavigateToHome()
                            BottomNavTab.FocusTimer -> onNavigateToTimer()
                            BottomNavTab.Settings -> onNavigateToSettings()
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                            contentDescription = tab.label
                        )
                    },
                    label = {
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = if (isDark) Color(0xFF2DD4BF) else Color.White,
                        selectedTextColor = if (isDark) Color(0xFF2DD4BF) else Color(0xFF064E3B),
                        indicatorColor = if (isDark) Color(0xFF122E2B) else Color(0xFF064E3B),
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    ),
                    modifier = Modifier.testTag(tab.testTag)
                )
            }
        }
    }
}
