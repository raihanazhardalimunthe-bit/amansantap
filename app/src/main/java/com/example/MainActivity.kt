package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.data.database.AmanSantapDatabase
import com.example.data.repository.AmanSantapRepository
import com.example.ui.AmanSantapApp
import com.example.ui.AmanSantapViewModel
import com.example.ui.AmanSantapViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val database by lazy {
        AmanSantapDatabase.getDatabase(this, lifecycleScope)
    }

    private val repository by lazy {
        AmanSantapRepository(database)
    }

    private val viewModel: AmanSantapViewModel by viewModels {
        AmanSantapViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AmanSantapApp(viewModel = viewModel)
                }
            }
        }
    }
}
