package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.UserRole
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.AlertsScreen
import com.example.ui.screens.EducationScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MerchantScreen
import com.example.ui.screens.ReportScreen
import com.example.ui.screens.SurveillanceDashboardScreen
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.MintContainer
import com.example.ui.theme.MintLight
import com.example.ui.theme.WarningOrange

enum class AppScreen(val label: String, val icon: ImageVector) {
    HOME("Beranda", Icons.Default.Home),
    REPORT("Lapor", Icons.Default.ReportProblem),
    SURVEILLANCE("Surveilans", Icons.Default.Analytics),
    ALERTS("Peringatan", Icons.Default.NotificationsActive),
    EDUCATION("Edukasi", Icons.AutoMirrored.Filled.MenuBook),
    MERCHANT("Mitra Pedagang", Icons.Default.Restaurant),
    ADMIN("Admin", Icons.Default.AdminPanelSettings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmanSantapApp(
    viewModel: AmanSantapViewModel,
    modifier: Modifier = Modifier
) {
    var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val alerts by viewModel.alerts.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

    val pendingAlertCount = alerts.count { it.status != "SELESAI" }
    var roleMenuExpanded by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    // Handle Android system back press
    if (currentScreen != AppScreen.HOME) {
        BackHandler {
            currentScreen = AppScreen.HOME
        }
    }

    // Available tabs depending on active role
    val navigationItems = when (currentRole) {
        UserRole.MASYARAKAT -> listOf(
            AppScreen.HOME,
            AppScreen.REPORT,
            AppScreen.EDUCATION
        )
        UserRole.PETUGAS -> listOf(
            AppScreen.HOME,
            AppScreen.SURVEILLANCE,
            AppScreen.ALERTS,
            AppScreen.EDUCATION
        )
        UserRole.PEDAGANG -> listOf(
            AppScreen.HOME,
            AppScreen.MERCHANT,
            AppScreen.EDUCATION
        )
        UserRole.ADMIN -> listOf(
            AppScreen.HOME,
            AppScreen.SURVEILLANCE,
            AppScreen.ALERTS,
            AppScreen.ADMIN
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_amansantap_logo),
                                contentDescription = null,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "AmanSantap",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "Peringatan Dini Pangan",
                                style = MaterialTheme.typography.labelSmall,
                                color = MintLight
                            )
                        }
                    }
                },
                actions = {
                    // Pending alerts quick indicator
                    if (currentRole == UserRole.PETUGAS || currentRole == UserRole.ADMIN) {
                        IconButton(
                            onClick = { currentScreen = AppScreen.ALERTS },
                            modifier = Modifier.testTag("top_bar_alerts_icon")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (pendingAlertCount > 0) {
                                        Badge(containerColor = WarningOrange) {
                                            Text("$pendingAlertCount", color = Color.White)
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Peringatan",
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    // Role Switcher Dropdown
                    Box {
                        Surface(
                            onClick = { roleMenuExpanded = true },
                            color = Color.White.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .testTag("role_switcher_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = currentRole.label,
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = roleMenuExpanded,
                            onDismissRequest = { roleMenuExpanded = false }
                        ) {
                            UserRole.values().forEach { role ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = role.label,
                                                fontWeight = if (role == currentRole) FontWeight.Bold else FontWeight.Normal
                                            )
                                            if (role == currentRole) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = ForestGreenPrimary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        viewModel.switchRole(role)
                                        roleMenuExpanded = false
                                        // Reset to home when switching role
                                        currentScreen = AppScreen.HOME
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ForestGreenPrimary)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                navigationItems.forEach { screen ->
                    val isSelected = currentScreen == screen
                    val badgeCount = if (screen == AppScreen.ALERTS) pendingAlertCount else 0

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentScreen = screen },
                        icon = {
                            if (badgeCount > 0) {
                                BadgedBox(badge = { Badge(containerColor = WarningOrange) { Text("$badgeCount") } }) {
                                    Icon(screen.icon, contentDescription = screen.label)
                                }
                            } else {
                                Icon(screen.icon, contentDescription = screen.label)
                            }
                        },
                        label = {
                            Text(
                                text = screen.label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ForestGreenPrimary,
                            selectedTextColor = ForestGreenPrimary,
                            indicatorColor = MintContainer
                        ),
                        modifier = Modifier.testTag("nav_item_${screen.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AppScreen.HOME -> HomeScreen(
                    viewModel = viewModel,
                    onNavigateToReport = { currentScreen = AppScreen.REPORT },
                    onNavigateToEducation = { currentScreen = AppScreen.EDUCATION }
                )
                AppScreen.REPORT -> ReportScreen(
                    viewModel = viewModel,
                    onNavigateBack = { currentScreen = AppScreen.HOME }
                )
                AppScreen.SURVEILLANCE -> SurveillanceDashboardScreen(
                    viewModel = viewModel,
                    onNavigateToAlerts = { currentScreen = AppScreen.ALERTS }
                )
                AppScreen.ALERTS -> AlertsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { currentScreen = AppScreen.HOME }
                )
                AppScreen.EDUCATION -> EducationScreen(
                    viewModel = viewModel,
                    onNavigateBack = { currentScreen = AppScreen.HOME }
                )
                AppScreen.MERCHANT -> MerchantScreen(
                    viewModel = viewModel
                )
                AppScreen.ADMIN -> AdminScreen(
                    viewModel = viewModel
                )
            }
        }
    }
}
