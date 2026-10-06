package com.example.wardenchat

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.wardenchat.data.AppConstants
import com.example.wardenchat.ui.ChatDetailScreen
import com.example.wardenchat.ui.ChatListScreen
import com.example.wardenchat.ui.LinkUpViewModel
import com.example.wardenchat.ui.ProfileScreen
import com.example.wardenchat.ui.theme.AccentBlue
import com.example.wardenchat.ui.theme.DarkBackground
import com.example.wardenchat.ui.theme.DarkBorder
import com.example.wardenchat.ui.theme.DarkSurface
import com.example.wardenchat.ui.theme.TextSecondary
import com.example.wardenchat.ui.theme.WardenChatTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Permission result handled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        var isReady = false
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            delay(800)
            isReady = true
        }
        splashScreen.setKeepOnScreenCondition { !isReady }

        enableEdgeToEdge()
        checkNotificationPermission()

        val viewModel = ViewModelProvider(this)[LinkUpViewModel::class.java]

        setContent {
            WardenChatTheme {
                MainAppScaffold(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setAppInForeground(true)
    }

    override fun onPause() {
        super.onPause()
        setAppInForeground(false)
    }

    private fun setAppInForeground(isForeground: Boolean) {
        val prefs = getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(AppConstants.KEY_IS_FOREGROUND, isForeground).apply()
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

@Composable
fun MainAppScaffold(viewModel: LinkUpViewModel) {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute == "messages" || currentRoute == "profile"

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBackground,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = DarkSurface,
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .background(DarkSurface)
                        .border(width = 1.dp, color = DarkBorder)
                ) {
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Default.ChatBubble,
                                contentDescription = "Mesajlar"
                            )
                        },
                        label = { Text("Mesajlar", fontSize = 11.sp) },
                        selected = currentRoute == "messages",
                        onClick = {
                            if (currentRoute != "messages") {
                                navController.navigate("messages") {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentBlue,
                            selectedTextColor = AccentBlue,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = AccentBlue.copy(alpha = 0.15f)
                        )
                    )

                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profil"
                            )
                        },
                        label = { Text("Profil", fontSize = 11.sp) },
                        selected = currentRoute == "profile",
                        onClick = {
                            if (currentRoute != "profile") {
                                navController.navigate("profile") {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentBlue,
                            selectedTextColor = AccentBlue,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = AccentBlue.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "messages",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("messages") {
                ChatListScreen(
                    viewModel = viewModel,
                    onNavigateToChat = { peerId ->
                        navController.navigate("chat/$peerId")
                    }
                )
            }
            composable("chat/{peerId}") { backStackEntry ->
                val peerId = backStackEntry.arguments?.getString("peerId") ?: ""
                ChatDetailScreen(
                    viewModel = viewModel,
                    peerId = peerId,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("profile") {
                ProfileScreen(viewModel = viewModel)
            }
        }
    }
}
