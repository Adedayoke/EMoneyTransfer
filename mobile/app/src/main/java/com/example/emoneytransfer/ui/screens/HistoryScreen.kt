package com.example.emoneytransfer.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.emoneytransfer.data.model.Transaction
import com.example.emoneytransfer.ui.theme.Background
import com.example.emoneytransfer.ui.theme.ColorReceived
import com.example.emoneytransfer.ui.theme.ColorReceivedBg
import com.example.emoneytransfer.ui.theme.ColorSent
import com.example.emoneytransfer.ui.theme.ColorSentBg
import com.example.emoneytransfer.ui.theme.Mint
import com.example.emoneytransfer.ui.theme.MintOnDark
import com.example.emoneytransfer.ui.theme.Surface as AppSurface
import com.example.emoneytransfer.ui.theme.TextPrimary
import com.example.emoneytransfer.ui.theme.TextSecondary
import com.example.emoneytransfer.ui.viewmodel.WalletState
import com.example.emoneytransfer.ui.viewmodel.WalletViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onLogout: () -> Unit,
    walletViewModel: WalletViewModel
) {
    val historyState by walletViewModel.historyState.collectAsState()
    var selectedTx by remember { mutableStateOf<Transaction?>(null) }

    LaunchedEffect(Unit) { walletViewModel.loadHistory() }

    LaunchedEffect(historyState) {
        if (historyState is WalletState.TokenExpired) onLogout()
    }

    // Receipt bottom sheet
    selectedTx?.let { tx ->
        ModalBottomSheet(
            onDismissRequest = { selectedTx = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = AppSurface
        ) {
            TransactionReceiptSheet(tx = tx, onDismiss = { selectedTx = null })
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Transactions",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(text = "Your recent activity", fontSize = 13.sp, color = TextSecondary)
            }
        }

        when (val state = historyState) {
            is WalletState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Mint, strokeWidth = 2.dp)
                }
            }
            is WalletState.HistoryLoaded -> {
                if (state.transactions.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No transactions yet", fontSize = 16.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Send money to get started",
                                fontSize = 13.sp,
                                color = TextSecondary.copy(alpha = 0.5f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item { Spacer(modifier = Modifier.height(4.dp)) }
                        items(state.transactions) { tx ->
                            TransactionItem(tx = tx, onClick = { selectedTx = tx })
                        }
                        item { Spacer(modifier = Modifier.height(8.dp)) }
                    }
                }
            }
            is WalletState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            state.message,
                            color = ColorSent,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { walletViewModel.loadHistory() },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Mint, contentColor = MintOnDark)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Retry")
                        }
                    }
                }
            }
            else -> {}
        }
    }
}

@Composable
fun TransactionItem(tx: Transaction, onClick: () -> Unit) {
    val isSent = tx.direction == "sent"
    val accentColor = if (isSent) ColorSent else ColorReceived
    val bgColor = if (isSent) ColorSentBg else ColorReceivedBg
    val counterparty = if (isSent) tx.receiver_name else tx.sender_name
    val counterEmail = if (isSent) tx.receiver_email else tx.sender_email
    val sign = if (isSent) "- ₦" else "+ ₦"
    val date = tx.created_at.take(10)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AppSurface)
            .border(1.dp, Color(0xFF2A2A2A), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Surface(shape = CircleShape, color = bgColor, modifier = Modifier.size(44.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isSent) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = counterparty,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(text = counterEmail, fontSize = 12.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = date, fontSize = 11.sp, color = TextSecondary.copy(alpha = 0.5f))
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$sign${tx.amount}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(bgColor)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isSent) "Sent" else "Received",
                        fontSize = 10.sp,
                        color = accentColor,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.3.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun TransactionReceiptSheet(tx: Transaction, onDismiss: () -> Unit) {
    val isSent = tx.direction == "sent"
    val accentColor = if (isSent) ColorSent else ColorReceived
    val accentBg = if (isSent) ColorSentBg else ColorReceivedBg

    // Parse date and time
    val rawDate = tx.created_at
    val date = rawDate.take(10)
    val time = try { rawDate.substring(11, 16) } catch (e: Exception) { "" }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Status header
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(ColorReceivedBg)
                .padding(12.dp)
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Mint,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Transfer ${tx.status.replaceFirstChar { it.uppercase() }}",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Amount
        Text(
            text = "₦${tx.amount}",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = accentColor
        )

        Spacer(modifier = Modifier.height(20.dp))
        DashedDivider()
        Spacer(modifier = Modifier.height(20.dp))

        // FROM
        ReceiptRow(label = "From", name = tx.sender_name, account = tx.sender_account ?: "")
        Spacer(modifier = Modifier.height(16.dp))
        ReceiptRow(label = "To", name = tx.receiver_name, account = tx.receiver_account ?: "")

        Spacer(modifier = Modifier.height(20.dp))
        DashedDivider()
        Spacer(modifier = Modifier.height(20.dp))

        // Details
        ReceiptDetail(label = "Reference", value = tx.reference ?: "—")
        Spacer(modifier = Modifier.height(10.dp))
        ReceiptDetail(label = "Date", value = date)
        if (time.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            ReceiptDetail(label = "Time", value = "${time} UTC")
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Status badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(accentBg)
                .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = tx.status.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
private fun DashedDivider() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
    ) {
        drawLine(
            color = Color(0xFF333333),
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(
                intervals = floatArrayOf(8.dp.toPx(), 5.dp.toPx()),
                phase = 0f
            )
        )
    }
}

@Composable
private fun ReceiptRow(label: String, name: String, account: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(text = label, fontSize = 13.sp, color = TextSecondary, modifier = Modifier.width(48.dp))
        Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                textAlign = TextAlign.End
            )
            if (account.isNotEmpty()) {
                Text(
                    text = account,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    letterSpacing = 0.5.sp,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Composable
private fun ReceiptDetail(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = TextSecondary)
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
    }
}
