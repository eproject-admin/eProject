package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.ui.AppViewModel
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LoginRegisterScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Room Database & Repository
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = AppRepository(applicationContext, database.appDao())

        // ViewModel factory
        val viewModelFactory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return AppViewModel(application, repository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }

        val viewModel = ViewModelProvider(this, viewModelFactory)[AppViewModel::class.java]

        setContent {
            MyApplicationTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                    val currentRoute by viewModel.currentRoute.collectAsState()

                    when (currentRoute) {
                        "login" -> {
                            LoginRegisterScreen(viewModel)
                        }
                        "main" -> {
                            DashboardScreen(viewModel)
                        }
                    }
                }
            }
        }
    }
}
