package com.example.emoneytransfer.data.network

import com.example.emoneytransfer.data.model.BalanceResponse
import com.example.emoneytransfer.data.model.LoginRequest
import com.example.emoneytransfer.data.model.LoginResponse
import com.example.emoneytransfer.data.model.AccountLookupResponse
import com.example.emoneytransfer.data.model.RegisterRequest
import com.example.emoneytransfer.data.model.RegisterResponse
import com.example.emoneytransfer.data.model.TransactionsResponse
import com.example.emoneytransfer.data.model.TransferRequest
import com.example.emoneytransfer.data.model.TransferResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("api/wallet/balance")
    suspend fun getBalance(
        @Header("Authorization") token: String
    ): Response<BalanceResponse>

    @POST("api/wallet/transfer")
    suspend fun transfer(
        @Header("Authorization") token: String,
        @Body request: TransferRequest
    ): Response<TransferResponse>

    @GET("api/transactions/history")
    suspend fun getTransactionHistory(
        @Header("Authorization") token: String
    ): Response<TransactionsResponse>

    @GET("api/wallet/lookup")
    suspend fun lookupAccount(
        @Header("Authorization") token: String,
        @Query("account") account: String
    ): Response<AccountLookupResponse>
}
