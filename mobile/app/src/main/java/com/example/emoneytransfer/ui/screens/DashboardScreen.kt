package com.example.emoneytransfer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.emoneytransfer.ui.theme.Background
import com.example.emoneytransfer.ui.theme.CardEnd
import com.example.emoneytransfer.ui.theme.CardStart
import com.example.emoneytransfer.ui.theme.Mint
import com.example.emoneytransfer.ui.theme.MintOnDark
import com.example.emoneytransfer.ui.theme.Surface as AppSurface
import com.example.emoneytransfer.ui.theme.TextPrimary
import com.example.emoneytransfer.ui.theme.TextSecondary
import com.example.emoneytransfer.ui.viewmodel.WalletState
import com.example.emoneytransfer.ui.viewmodel.WalletViewModel
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
fun DashboardScreen(
    onSendMoney: () -> Unit,
    onViewHistory: () -> Unit,
    onLogout: () -> Unit,
    walletViewModel: WalletViewModel
) {
    val balanceState by walletViewModel.balanceState.collectAsState()
    val scope = rememberCoroutineScope()
    var balanceHidden by remember { mutableStateOf(false) }

    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        else -> "Good evening"
    }

    LaunchedEffect(Unit) {
        walletViewModel.loadBalance()
    }

    LaunchedEffect(balanceState) {
        if (balanceState is WalletState.TokenExpired) onLogout()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                when (val state = balanceState) {
                    is WalletState.BalanceLoaded -> {
                        Text(
                            text = "$greeting,",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = state.userName.split(" ").firstOrNull() ?: state.userName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    else -> {
                        Text("Welcome", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                }
            }

            Surface(
                shape = CircleShape,
                color = AppSurface,
                modifier = Modifier.size(40.dp)
            ) {
                IconButton(onClick = {
                    scope.launch {
                        walletViewModel.logout()
                        onLogout()
                    }
                }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Logout",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Balance card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.linearGradient(listOf(CardStart, CardEnd)))
                .border(1.dp, Mint.copy(alpha = 0.22f), RoundedCornerShape(24.dp))
                .padding(28.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Balance",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        letterSpacing = 0.5.sp
                    )
                    IconButton(
                        onClick = { balanceHidden = !balanceHidden },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (balanceHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle balance",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                when (val state = balanceState) {
                    is WalletState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = Mint,
                            strokeWidth = 2.dp
                        )
                    }
                    is WalletState.BalanceLoaded -> {
                        Text(
                            text = if (balanceHidden) "₦ •••••" else "₦ ${state.balance}",
                            fontSize = 38.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            letterSpacing = (-1).sp
                        )
                    }
                    is WalletState.Error -> {
                        Text(
                            text = "Could not load",
                            fontSize = 16.sp,
                            color = TextSecondary
                        )
                    }
                    else -> {}
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Available for transfer",
                    fontSize = 12.sp,
                    color = TextSecondary.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Quick actions
        Text(
            text = "Quick Actions",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary,
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onSendMoney,
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Mint,
                    contentColor = MintOnDark
                )
            ) {
                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(17.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Send Money", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }

            OutlinedButton(
                onClick = onViewHistory,
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF333333)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
            ) {
                Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(17.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("History", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (balanceState is WalletState.Error) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF2A0D0D))
                    .padding(16.dp)
            ) {
                Text(
                    text = (balanceState as WalletState.Error).message,
                    color = Color(0xFFFF5252),
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
