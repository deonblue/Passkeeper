package com.sevennotes.passkeeper

import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sevennotes.passkeeper.data.respository.PasswordRepository
import com.sevennotes.passkeeper.data.respository.impl.PasswordRepositoryImpl
import com.sevennotes.passkeeper.ui.pages.MainView
import com.sevennotes.passkeeper.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private lateinit var passwordRepository: PasswordRepository
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        passwordRepository = PasswordRepositoryImpl.getInstance(applicationContext)
        sharedPreferences = getSharedPreferences("passkeeper", MODE_PRIVATE)
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        setContent {
            val mainViewModel: MainViewModel = viewModel(
                factory = MainViewModel.provideFactory(passwordRepository,sharedPreferences, this)
            )
            MainView(mainViewModel, cm)
        }
    }
}