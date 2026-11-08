package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.AlarmsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.QiblaAndAthkarScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SoundLibraryScreen
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NavyDarkCard
import com.example.ui.theme.TextLightGray
import com.example.ui.theme.TextMuted

enum class AppTab(val route: String, val titleArabic: String, val icon: ImageVector) {
    HOME("home", "الرئيسية", Icons.Default.Home),
    ALARMS("alarms", "المنبهات", Icons.Default.Alarm),
    SOUNDS("sounds", "الأصوات", Icons.Default.LibraryMusic),
    QIBLA("qibla", "القبلة والذكر", Icons.Default.CompassCalibration),
    SETTINGS("settings", "الإعدادات", Icons.Default.Settings)
}

@Composable
fun MainAppScaffold(
    viewModel: SalatiViewModel,
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf(AppTab.HOME) }

    // Intercept back button if on secondary screen
    BackHandler(enabled = currentTab != AppTab.HOME) {
        currentTab = AppTab.HOME
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = GoldBright,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .border(
                        1.dp,
                        GoldPrimary.copy(alpha = 0.2f),
                        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                    )
            ) {
                AppTab.values().forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.titleArabic,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.titleArabic,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF0F172A),
                            selectedTextColor = GoldBright,
                            indicatorColor = GoldPrimary,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppTab.HOME -> HomeScreen(
                    viewModel = viewModel,
                    onNavigateToAlarms = { currentTab = AppTab.ALARMS },
                    onNavigateToSounds = { currentTab = AppTab.SOUNDS },
                    onNavigateToQibla = { currentTab = AppTab.QIBLA },
                    onNavigateToSettings = { currentTab = AppTab.SETTINGS }
                )
                AppTab.ALARMS -> AlarmsScreen(viewModel = viewModel)
                AppTab.SOUNDS -> SoundLibraryScreen(viewModel = viewModel)
                AppTab.QIBLA -> QiblaAndAthkarScreen(viewModel = viewModel)
                AppTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
            }
        }
    }
}
