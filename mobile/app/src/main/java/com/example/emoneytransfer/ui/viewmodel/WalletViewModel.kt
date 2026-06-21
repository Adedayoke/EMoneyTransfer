package com.example.emoneytransfer.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.emoneytransfer.data.local.TokenManager
import com.example.emoneytransfer.data.model.Transaction
import com.example.emoneytransfer.data.model.TransferRequest
import com.example.emoneytransfer.data.network.RetrofitClient
import com.google.gson.JsonParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class WalletState {
    object Idle : WalletState()
    object Loading : WalletState()
    data class BalanceLoaded(val balance: String, val userName: String) : WalletState()
    data class TransferSuccess(val newBalance: String, val reference: String) : WalletState()
    data class HistoryLoaded(val transactions: List<Transaction>) : WalletState()
    data class Error(val message: String) : WalletState()
    object TokenExpired : WalletState()
}

class WalletViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val api = RetrofitClient.apiService

    private val _balanceState = MutableStateFlow<WalletState>(WalletState.Idle)
    val balanceState: StateFlow<WalletState> = _balanceState.asStateFlow()

    private val _transferState = MutableStateFlow<WalletState>(WalletState.Idle)
    val transferState: StateFlow<WalletState> = _transferState.asStateFlow()

    private val _historyState = MutableStateFlow<WalletState>(WalletState.Idle)
    val historyState: StateFlow<WalletState> = _historyState.asStateFlow()

    fun loadBalance() {
        viewModelScope.launch {
            _balanceState.value = WalletState.Loading
            val token = tokenManager.token.first()
            val name = tokenManager.userName.first()
            if (token == null) { _balanceState.value = WalletState.TokenExpired; return@launch }
            try {
                val response = api.getBalance("Bearer $token")
                when {
                    response.isSuccessful -> {
                        val balance = response.body()?.balance ?: "0.00"
                        _balanceState.value = WalletState.BalanceLoaded(balance, name ?: "User")
                    }
                    response.code() == 401 -> {
                        tokenManager.clearAll()
                        _balanceState.value = WalletState.TokenExpired
                    }
                    else -> _balanceState.value = WalletState.Error("Failed to load balance")
                }
            } catch (e: Exception) {
                _balanceState.value = WalletState.Error("Cannot reach server. Is it running?")
            }
        }
    }

    fun transfer(recipientEmail: String, amount: String) {
        viewModelScope.launch {
            val parsedAmount = amount.toDoubleOrNull()
            if (parsedAmount == null || parsedAmount <= 0) {
                _transferState.value = WalletState.Error("Enter a valid positive amount")
                return@launch
            }
            _transferState.value = WalletState.Loading
            val token = tokenManager.token.first()
            if (token == null) { _transferState.value = WalletState.TokenExpired; return@launch }
            try {
                val response = api.transfer("Bearer $token", TransferRequest(recipientEmail, parsedAmount))
                when {
                    response.isSuccessful -> {
                        val body = response.body()!!
                        _transferState.value = WalletState.TransferSuccess(
                            newBalance = body.new_balance,
                            reference = body.reference ?: ""
                        )
                    }
                    response.code() == 401 -> {
                        tokenManager.clearAll()
                        _transferState.value = WalletState.TokenExpired
                    }
                    else -> {
                        val msg = parseError(response.errorBody()?.string()) ?: "Transfer failed"
                        _transferState.value = WalletState.Error(msg)
                    }
                }
            } catch (e: Exception) {
                _transferState.value = WalletState.Error("Cannot reach server. Is it running?")
            }
        }
    }

    fun loadHistory() {
        viewModelScope.launch {
            _historyState.value = WalletState.Loading
            val token = tokenManager.token.first()
            if (token == null) { _historyState.value = WalletState.TokenExpired; return@launch }
            try {
                val response = api.getTransactionHistory("Bearer $token")
                when {
                    response.isSuccessful -> {
                        val list = response.body()?.transactions ?: emptyList()
                        _historyState.value = WalletState.HistoryLoaded(list)
                    }
                    response.code() == 401 -> {
                        tokenManager.clearAll()
                        _historyState.value = WalletState.TokenExpired
                    }
                    else -> _historyState.value = WalletState.Error("Failed to load history")
                }
            } catch (e: Exception) {
                _historyState.value = WalletState.Error("Cannot reach server. Is it running?")
            }
        }
    }

    fun resetTransferState() { _transferState.value = WalletState.Idle }

    suspend fun logout() { tokenManager.clearAll() }

    private fun parseError(body: String?): String? {
        if (body == null) return null
        return try {
            JsonParser.parseString(body).asJsonObject.get("error")?.asString
        } catch (e: Exception) { null }
    }
}
