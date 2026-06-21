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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.emoneytransfer.data.local.TokenManager
import com.example.emoneytransfer.ui.theme.Background
import com.example.emoneytransfer.ui.theme.ErrorRed
import com.example.emoneytransfer.ui.theme.Mint
import com.example.emoneytransfer.ui.theme.MintOnDark
import com.example.emoneytransfer.ui.theme.Surface
import com.example.emoneytransfer.ui.theme.SurfaceVariant
import com.example.emoneytransfer.ui.theme.TextPrimary
import com.example.emoneytransfer.ui.theme.TextSecondary
import com.example.emoneytransfer.ui.viewmodel.AuthState
import com.example.emoneytransfer.ui.viewmodel.AuthViewModel
import com.example.emoneytransfer.util.BiometricHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    authViewModel: AuthViewModel
) {
    val loginState by authViewModel.loginState.collectAsState()
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showBiometric by remember { mutableStateOf(false) }
    var biometricError by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val token = TokenManager(context).token.first()
        if (token != null) {
            onLoginSuccess()
            return@LaunchedEffect
        }
        val storedEmail = TokenManager(context).userEmail.first()
        val storedPass = TokenManager(context).userPassword.first()
        showBiometric = BiometricHelper.canAuthenticate(context) &&
                storedEmail != null && storedPass != null
    }

    LaunchedEffect(loginState) {
        if (loginState is AuthState.LoginSuccess) {
            authViewModel.resetLoginState()
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            Text(
                text = "₦",
                fontSize = 40.sp,
                fontWeight = FontWeight.Black,
                color = Mint
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Welcome back",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Sign in to your account",
                fontSize = 15.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(44.dp))

            Text(
                text = "Email",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; biometricError = "" },
                placeholder = { Text("you@example.com", color = TextSecondary.copy(alpha = 0.5f)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = darkFieldColors()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Password",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; biometricError = "" },
                placeholder = { Text("••••••••", color = TextSecondary.copy(alpha = 0.5f)) },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = darkFieldColors()
            )

            val errorMsg = when {
                loginState is AuthState.Error -> (loginState as AuthState.Error).message
                biometricError.isNotEmpty() -> biometricError
                else -> null
            }
            if (errorMsg != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = errorMsg, color = ErrorRed, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { authViewModel.login(email.trim(), password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                enabled = loginState !is AuthState.Loading && email.isNotBlank() && password.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Mint,
                    contentColor = MintOnDark,
                    disabledContainerColor = Mint.copy(alpha = 0.3f),
                    disabledContentColor = MintOnDark.copy(alpha = 0.5f)
                )
            ) {
                if (loginState is AuthState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = MintOnDark,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Sign In", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            if (showBiometric) {
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Surface)
                            .border(1.dp, Color(0xFF333333), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = {
                            (context as? FragmentActivity)?.let { activity ->
                                BiometricHelper.showPrompt(
                                    activity = activity,
                                    title = "Sign in",
                                    subtitle = "Use fingerprint to sign in",
                                    onSuccess = { authViewModel.loginWithBiometric() },
                                    onError = { msg -> biometricError = msg }
                                )
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = "Sign in with fingerprint",
                                tint = Mint,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("or use fingerprint", color = TextSecondary, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                TextButton(onClick = onNavigateToRegister) {
                    Text(
                        text = "Don't have an account? ",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Register",
                        color = Mint,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun darkFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedBorderColor = Mint,
    unfocusedBorderColor = Color(0xFF333333),
    focusedContainerColor = SurfaceVariant,
    unfocusedContainerColor = Surface,
    cursorColor = Mint,
    focusedLabelColor = Mint,
    unfocusedLabelColor = TextSecondary
)
