package com.example.emoneytransfer.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.emoneytransfer.data.local.TokenManager
import com.example.emoneytransfer.data.model.LoginRequest
import com.example.emoneytransfer.data.model.RegisterRequest
import com.example.emoneytransfer.data.network.RetrofitClient
import com.google.gson.JsonParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val message: String) : AuthState()
    data class LoginSuccess(val name: String) : AuthState()
    data class RegisterSuccess(val name: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val api = RetrofitClient.apiService

    private val _registerState = MutableStateFlow<AuthState>(AuthState.Idle)
    val registerState: StateFlow<AuthState> = _registerState.asStateFlow()

    private val _loginState = MutableStateFlow<AuthState>(AuthState.Idle)
    val loginState: StateFlow<AuthState> = _loginState.asStateFlow()

    // Held in memory between Register → SetPin screens
    private var pendingName = ""
    private var pendingEmail = ""
    private var pendingPassword = ""

    fun savePendingRegistration(name: String, email: String, password: String) {
        pendingName = name.trim()
        pendingEmail = email.trim()
        pendingPassword = password
    }

    fun completeRegistration(pin: String) {
        viewModelScope.launch {
            _registerState.value = AuthState.Loading
            try {
                val response = api.register(
                    RegisterRequest(pendingName, pendingEmail, pendingPassword, pin)
                )
                if (response.isSuccessful) {
                    val body = response.body()!!
                    tokenManager.saveLoginData(
                        body.token, body.user.full_name,
                        body.user.email, body.user.account_number ?: ""
                    )
                    tokenManager.saveCredentials(pendingEmail, pendingPassword)
                    tokenManager.savePin(pin)
                    _registerState.value = AuthState.RegisterSuccess(body.user.full_name)
                } else {
                    val msg = parseError(response.errorBody()?.string()) ?: "Registration failed"
                    _registerState.value = AuthState.Error(msg)
                }
            } catch (e: Exception) {
                _registerState.value = AuthState.Error("Cannot reach server. Please try again.")
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = AuthState.Loading
            try {
                val response = api.login(LoginRequest(email.trim(), password))
                if (response.isSuccessful) {
                    val body = response.body()!!
                    tokenManager.saveLoginData(
                        body.token, body.user.full_name,
                        body.user.email, body.user.account_number ?: ""
                    )
                    tokenManager.saveCredentials(email.trim(), password)
                    _loginState.value = AuthState.LoginSuccess(body.user.full_name)
                } else {
                    val msg = parseError(response.errorBody()?.string()) ?: "Invalid credentials"
                    _loginState.value = AuthState.Error(msg)
                }
            } catch (e: Exception) {
                _loginState.value = AuthState.Error("Cannot reach server. Please try again.")
            }
        }
    }

    fun loginWithBiometric() {
        viewModelScope.launch {
            val email = tokenManager.userEmail.first() ?: return@launch
            val password = tokenManager.userPassword.first() ?: return@launch
            login(email, password)
        }
    }

    fun resetRegisterState() { _registerState.value = AuthState.Idle }
    fun resetLoginState() { _loginState.value = AuthState.Idle }

    private fun parseError(body: String?): String? {
        if (body == null) return null
        return try {
            JsonParser.parseString(body).asJsonObject.get("error")?.asString
        } catch (e: Exception) { null }
    }
}
