package com.example.emoneytransfer.data.model

data class RegisterRequest(
    val full_name: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class TransferRequest(
    val recipient_email: String,
    val amount: Double
)

data class MessageResponse(val message: String)

data class UserResponse(
    val id: Int,
    val full_name: String,
    val email: String
)

data class LoginResponse(
    val token: String,
    val user: UserResponse
)

data class BalanceResponse(val balance: String)

data class TransferResponse(
    val message: String,
    val new_balance: String,
    val reference: String?
)

data class Transaction(
    val id: Int,
    val amount: String,
    val status: String,
    val created_at: String,
    val sender_name: String,
    val sender_email: String,
    val receiver_name: String,
    val receiver_email: String,
    val direction: String
)

data class TransactionsResponse(val transactions: List<Transaction>)
