package com.example.mymoneymanager

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mymoneymanager.ui.navigation.AppNavigation
import com.example.mymoneymanager.ui.theme.MyMoneyManagerTheme
import com.example.mymoneymanager.ui.viewmodel.MainViewModel
import com.example.mymoneymanager.ui.viewmodel.ViewModelFactory

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as MyMoneyManagerApp).container
        val factory = ViewModelFactory(container)

        setContent {
            MyMoneyManagerTheme {
                val mainViewModel: MainViewModel = viewModel(factory = factory)
                val startState by mainViewModel.startState.collectAsStateWithLifecycle()

                AppNavigation(
                    startState = startState,
                    viewModelFactory = factory
                )
            }
        }
    }
}
