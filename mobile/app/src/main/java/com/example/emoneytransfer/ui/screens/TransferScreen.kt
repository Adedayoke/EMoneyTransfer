package com.example.emoneytransfer.ui.screens

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.emoneytransfer.data.local.TokenManager
import com.example.emoneytransfer.ui.theme.Background
import com.example.emoneytransfer.ui.theme.ErrorRed
import com.example.emoneytransfer.ui.theme.Mint
import com.example.emoneytransfer.ui.theme.MintOnDark
import com.example.emoneytransfer.ui.theme.Surface
import com.example.emoneytransfer.ui.theme.SurfaceHighlight
import com.example.emoneytransfer.ui.theme.TextPrimary
import com.example.emoneytransfer.ui.theme.TextSecondary
import com.example.emoneytransfer.ui.viewmodel.WalletState
import com.example.emoneytransfer.ui.viewmodel.WalletViewModel
import com.example.emoneytransfer.util.BiometricHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferScreen(
    onSuccess: () -> Unit,
    onLogout: () -> Unit,
    walletViewModel: WalletViewModel
) {
    val transferState by walletViewModel.transferState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var recipientAccount by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var showPinSheet by remember { mutableStateOf(false) }
    var pinEntry by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var successBalance by remember { mutableStateOf("") }
    var successReference by remember { mutableStateOf("") }
    val canUseBiometric = remember { BiometricHelper.canAuthenticate(context) }

    LaunchedEffect(transferState) {
        when (val state = transferState) {
            is WalletState.TransferSuccess -> {
                successBalance = state.newBalance
                successReference = state.reference
                showPinSheet = false
                pinEntry = ""
                pinError = ""
                showSuccessDialog = true
            }
            is WalletState.Error -> {
                pinError = state.message
                pinEntry = ""
            }
            is WalletState.TokenExpired -> onLogout()
            else -> {}
        }
    }

    // Auto-submit when 4 digits entered
    LaunchedEffect(pinEntry) {
        if (pinEntry.length == 4 && transferState !is WalletState.Loading) {
            walletViewModel.transfer(recipientAccount.trim(), amount.trim(), pinEntry)
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {},
            containerColor = Surface,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            title = { Text("Transfer Successful", fontWeight = FontWeight.Bold, color = Mint) },
            text = {
                Column {
                    Text("Money sent successfully.", color = TextPrimary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("New balance:", color = TextSecondary, fontSize = 13.sp)
                    Text(
                        text = "₦$successBalance",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Mint
                    )
                    if (successReference.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Reference:", color = TextSecondary, fontSize = 13.sp)
                        Text(
                            text = successReference,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showSuccessDialog = false
                    recipientAccount = ""
                    amount = ""
                    walletViewModel.resetTransferState()
                    walletViewModel.loadBalance()
                    onSuccess()
                }) {
                    Text("Done", color = Mint, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }

    // PIN Bottom Sheet
    if (showPinSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showPinSheet = false
                pinEntry = ""
                pinError = ""
                walletViewModel.resetTransferState()
            },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Enter your PIN",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Authorise transfer of ₦${amount.trim()}",
                    fontSize = 13.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(28.dp))

                // 4-dot indicator
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    repeat(4) { i ->
                        val filled = i < pinEntry.length
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(if (filled) Mint else Color.Transparent)
                                .border(
                                    width = 2.dp,
                                    color = if (filled) Mint else Color(0xFF444444),
                                    shape = CircleShape
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (transferState is WalletState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Mint,
                        strokeWidth = 2.dp
                    )
                } else if (pinError.isNotEmpty()) {
                    Text(
                        text = pinError,
                        color = ErrorRed,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Biometric option
                if (canUseBiometric) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                (context as? FragmentActivity)?.let { activity ->
                                    BiometricHelper.showPrompt(
                                        activity = activity,
                                        title = "Authorise transfer",
                                        subtitle = "Use fingerprint to confirm",
                                        onSuccess = {
                                            scope.launch {
                                                val savedPin = TokenManager(context).pin.first()
                                                if (savedPin != null) {
                                                    pinEntry = savedPin
                                                } else {
                                                    pinError = "No PIN saved. Enter PIN manually."
                                                }
                                            }
                                        },
                                        onError = { msg -> pinError = msg }
                                    )
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            Icons.Default.Fingerprint,
                            contentDescription = null,
                            tint = Mint,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Use fingerprint", color = Mint, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Numpad
                val rows = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("", "0", "⌫")
                )
                val isLoading = transferState is WalletState.Loading

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    rows.forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                            row.forEach { key ->
                                when {
                                    key == "⌫" -> {
                                        Box(
                                            modifier = Modifier
                                                .size(68.dp)
                                                .clip(CircleShape)
                                                .clickable(enabled = !isLoading) {
                                                    if (pinEntry.isNotEmpty()) {
                                                        pinEntry = pinEntry.dropLast(1)
                                                        pinError = ""
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Backspace,
                                                contentDescription = "Backspace",
                                                tint = TextSecondary,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                    key.isEmpty() -> Spacer(modifier = Modifier.size(68.dp))
                                    else -> {
                                        Box(
                                            modifier = Modifier
                                                .size(68.dp)
                                                .clip(CircleShape)
                                                .background(SurfaceHighlight)
                                                .clickable(enabled = !isLoading && pinEntry.length < 4) {
                                                    pinError = ""
                                                    pinEntry += key
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = key,
                                                fontSize = 22.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = TextPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Main content
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Send Money",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Enter recipient details below",
            fontSize = 14.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(36.dp))

        // Recipient section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Surface)
                .border(1.dp, Color(0xFF2A2A2A), RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = "RECIPIENT",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = recipientAccount,
                    onValueChange = { if (it.length <= 10 && it.all(Char::isDigit)) recipientAccount = it },
                    placeholder = { Text("10-digit account number", color = TextSecondary.copy(alpha = 0.5f)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = darkFieldColors()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Amount section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Surface)
                .border(1.dp, Color(0xFF2A2A2A), RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = "AMOUNT",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    placeholder = { Text("0.00", color = TextSecondary.copy(alpha = 0.5f)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    prefix = {
                        Text("₦ ", color = Mint, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = darkFieldColors()
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                walletViewModel.resetTransferState()
                pinEntry = ""
                pinError = ""
                showPinSheet = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp),
            enabled = recipientAccount.length == 10 && amount.isNotBlank(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Mint,
                contentColor = MintOnDark,
                disabledContainerColor = Mint.copy(alpha = 0.3f),
                disabledContentColor = MintOnDark.copy(alpha = 0.5f)
            )
        ) {
            Text("Send Now", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
