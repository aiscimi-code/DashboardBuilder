package com.dashboard.builder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import com.dashboard.builder.ui.screens.MainScreen
import com.dashboard.builder.ui.theme.DashboardBuilderTheme
import com.dashboard.builder.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        // Try to load saved state
        viewModel.loadFromInternalStorage(this)
        
        setContent {
            DashboardBuilderTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // Auto-save when app goes to background
        viewModel.saveToInternalStorage(this)
    }
}