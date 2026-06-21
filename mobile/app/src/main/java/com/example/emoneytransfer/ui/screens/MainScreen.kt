package com.example.emoneytransfer.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.emoneytransfer.ui.theme.Background
import com.example.emoneytransfer.ui.theme.Mint
import com.example.emoneytransfer.ui.theme.Surface
import com.example.emoneytransfer.ui.theme.SurfaceVariant
import com.example.emoneytransfer.ui.theme.TextMuted
import com.example.emoneytransfer.ui.theme.TextSecondary
import com.example.emoneytransfer.ui.viewmodel.WalletViewModel

private data class BottomTab(
    val label: String,
    val icon: ImageVector,
    val index: Int
)

private val tabs = listOf(
    BottomTab("Home", Icons.Default.Home, 0),
    BottomTab("Send", Icons.Default.Send, 1),
    BottomTab("History", Icons.Default.History, 2)
)

@Composable
fun MainScreen(
    onLogout: () -> Unit,
    walletViewModel: WalletViewModel
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        containerColor = Background,
        bottomBar = {
            NavigationBar(
                containerColor = Surface,
                tonalElevation = 0.dp,
                modifier = Modifier.navigationBarsPadding()
            ) {
                tabs.forEach { tab ->
                    val selected = selectedTab == tab.index
                    NavigationBarItem(
                        selected = selected,
                        onClick = { selectedTab = tab.index },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Mint,
                            selectedTextColor = Mint,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextMuted,
                            indicatorColor = SurfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                val direction = if (targetState > initialState) 1 else -1
                (slideInHorizontally { w -> direction * (w / 3) } + fadeIn()) togetherWith
                        (slideOutHorizontally { w -> -direction * (w / 3) } + fadeOut())
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            label = "tab_transition"
        ) { tab ->
            when (tab) {
                0 -> DashboardScreen(
                    onSendMoney = { selectedTab = 1 },
                    onViewHistory = { selectedTab = 2 },
                    onLogout = onLogout,
                    walletViewModel = walletViewModel
                )
                1 -> TransferScreen(
                    onSuccess = { selectedTab = 0 },
                    onLogout = onLogout,
                    walletViewModel = walletViewModel
                )
                else -> HistoryScreen(
                    onLogout = onLogout,
                    walletViewModel = walletViewModel
                )
            }
        }
    }
}
