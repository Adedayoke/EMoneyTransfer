package com.example.emoneytransfer.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.emoneytransfer.ui.screens.LoginScreen
import com.example.emoneytransfer.ui.screens.MainScreen
import com.example.emoneytransfer.ui.screens.RegisterScreen
import com.example.emoneytransfer.ui.screens.SetPinScreen
import com.example.emoneytransfer.ui.viewmodel.AuthViewModel
import com.example.emoneytransfer.ui.viewmodel.WalletViewModel

private object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val SET_PIN = "set_pin"
    const val MAIN = "main"
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    // Activity-scoped so state is shared across all screens
    val walletViewModel: WalletViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()

    NavHost(navController = navController, startDestination = Routes.LOGIN) {

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
                },
                authViewModel = authViewModel
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onNavigateToSetPin = { navController.navigate(Routes.SET_PIN) },
                authViewModel = authViewModel
            )
        }

        composable(Routes.SET_PIN) {
            SetPinScreen(
                onRegistered = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
                authViewModel = authViewModel
            )
        }

        composable(Routes.MAIN) {
            MainScreen(
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.MAIN) { inclusive = true }
                    }
                },
                walletViewModel = walletViewModel
            )
        }
    }
}
