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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.emoneytransfer.ui.theme.Background
import com.example.emoneytransfer.ui.theme.ErrorRed
import com.example.emoneytransfer.ui.theme.Mint
import com.example.emoneytransfer.ui.theme.Surface
import com.example.emoneytransfer.ui.theme.SurfaceHighlight
import com.example.emoneytransfer.ui.theme.TextPrimary
import com.example.emoneytransfer.ui.theme.TextSecondary
import com.example.emoneytransfer.ui.viewmodel.AuthState
import com.example.emoneytransfer.ui.viewmodel.AuthViewModel

@Composable
fun SetPinScreen(
    onRegistered: () -> Unit,
    onBack: () -> Unit,
    authViewModel: AuthViewModel
) {
    val registerState by authViewModel.registerState.collectAsState()

    var step by remember { mutableIntStateOf(0) } // 0 = set PIN, 1 = confirm PIN
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    val isLoading = registerState is AuthState.Loading

    LaunchedEffect(registerState) {
        when (registerState) {
            is AuthState.RegisterSuccess -> {
                authViewModel.resetRegisterState()
                onRegistered()
            }
            is AuthState.Error -> {
                error = (registerState as AuthState.Error).message
                // Reset so user can retry
                step = 0
                pin = ""
                confirmPin = ""
            }
            else -> {}
        }
    }

    val activePin = if (step == 0) pin else confirmPin

    fun onDigit(d: String) {
        if (isLoading) return
        error = ""
        if (step == 0) {
            if (pin.length < 4) {
                pin += d
                if (pin.length == 4) {
                    step = 1 // auto-advance to confirm
                }
            }
        } else {
            if (confirmPin.length < 4) {
                confirmPin += d
                if (confirmPin.length == 4) {
                    if (confirmPin == pin) {
                        authViewModel.completeRegistration(pin)
                    } else {
                        error = "PINs don't match. Try again."
                        pin = ""
                        confirmPin = ""
                        step = 0
                    }
                }
            }
        }
    }

    fun onBackspace() {
        if (isLoading) return
        error = ""
        if (step == 1 && confirmPin.isEmpty()) {
            step = 0
        } else if (step == 1) {
            confirmPin = confirmPin.dropLast(1)
        } else {
            pin = pin.dropLast(1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                if (step == 1) { step = 0; confirmPin = ""; error = "" }
                else onBack()
            }) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Step indicator
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(2) { i ->
                Box(
                    modifier = Modifier
                        .height(3.dp)
                        .width(32.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (i <= step) Mint else Surface)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = if (step == 0) "Set your PIN" else "Confirm your PIN",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (step == 0) "You'll use this to authorise transfers"
                   else "Re-enter the same PIN to confirm",
            fontSize = 14.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(48.dp))

        // 4-dot PIN indicator
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            repeat(4) { i ->
                val filled = i < activePin.length
                Box(
                    modifier = Modifier
                        .size(18.dp)
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

        Spacer(modifier = Modifier.height(16.dp))

        // Error / loading
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Mint,
                strokeWidth = 2.dp
            )
        } else if (error.isNotEmpty()) {
            Text(
                text = error,
                color = ErrorRed,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 40.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Numpad
        val rows = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("", "0", "⌫")
        )

        Column(
            modifier = Modifier.padding(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            rows.forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    row.forEach { key ->
                        when {
                            key == "⌫" -> NumpadBackspaceKey(onClick = { onBackspace() })
                            key.isEmpty() -> Spacer(modifier = Modifier.size(72.dp))
                            else -> NumpadKey(label = key, onClick = { onDigit(key) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NumpadKey(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(SurfaceHighlight)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
    }
}

@Composable
private fun NumpadBackspaceKey(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Backspace,
            contentDescription = "Backspace",
            tint = TextSecondary,
            modifier = Modifier.size(24.dp)
        )
    }
}
