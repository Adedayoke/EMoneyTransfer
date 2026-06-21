package com.example.emoneytransfer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.emoneytransfer.ui.theme.Background
import com.example.emoneytransfer.ui.theme.ErrorRed
import com.example.emoneytransfer.ui.theme.Mint
import com.example.emoneytransfer.ui.theme.MintOnDark
import com.example.emoneytransfer.ui.theme.Surface
import com.example.emoneytransfer.ui.theme.SurfaceVariant
import com.example.emoneytransfer.ui.theme.TextPrimary
import com.example.emoneytransfer.ui.theme.TextSecondary
import com.example.emoneytransfer.ui.viewmodel.WalletState
import com.example.emoneytransfer.ui.viewmodel.WalletViewModel

@Composable
fun TransferScreen(
    onSuccess: () -> Unit,
    onLogout: () -> Unit,
    walletViewModel: WalletViewModel
) {
    val transferState by walletViewModel.transferState.collectAsState()

    var recipientEmail by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var successBalance by remember { mutableStateOf("") }
    var successReference by remember { mutableStateOf("") }

    LaunchedEffect(transferState) {
        when (val state = transferState) {
            is WalletState.TransferSuccess -> {
                successBalance = state.newBalance
                successReference = state.reference
                showSuccessDialog = true
            }
            is WalletState.TokenExpired -> onLogout()
            else -> {}
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {},
            containerColor = Surface,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            title = {
                Text("Transfer Successful", fontWeight = FontWeight.Bold, color = Mint)
            },
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
                    recipientEmail = ""
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
                    value = recipientEmail,
                    onValueChange = { recipientEmail = it },
                    placeholder = { Text("Email address", color = TextSecondary.copy(alpha = 0.5f)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
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
                        Text(
                            "₦ ",
                            color = Mint,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = darkFieldColors()
                )
            }
        }

        if (transferState is WalletState.Error) {
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF2A0D0D))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = (transferState as WalletState.Error).message,
                    color = ErrorRed,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { walletViewModel.transfer(recipientEmail.trim(), amount.trim()) },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp),
            enabled = transferState !is WalletState.Loading
                    && recipientEmail.isNotBlank()
                    && amount.isNotBlank(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Mint,
                contentColor = MintOnDark,
                disabledContainerColor = Mint.copy(alpha = 0.3f),
                disabledContentColor = MintOnDark.copy(alpha = 0.5f)
            )
        ) {
            if (transferState is WalletState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = MintOnDark,
                    strokeWidth = 2.dp
                )
            } else {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Text(
                        "Send Now",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
