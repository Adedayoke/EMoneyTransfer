package com.example.emoneytransfer

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.emoneytransfer.navigation.NavGraph
import com.example.emoneytransfer.ui.theme.EMoneyTransferTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EMoneyTransferTheme {
                NavGraph()
            }
        }
    }
}
