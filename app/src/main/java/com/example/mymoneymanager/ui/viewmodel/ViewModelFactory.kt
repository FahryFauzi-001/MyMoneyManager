package com.example.mymoneymanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mymoneymanager.AppContainer

class ViewModelFactory(
    private val container: AppContainer
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repository = container.repository
        val viewModel: ViewModel = when (modelClass) {
            MainViewModel::class.java ->
                MainViewModel(repository, container.sessionState)
            TransactionsViewModel::class.java ->
                TransactionsViewModel(repository)
            AddTransactionViewModel::class.java ->
                AddTransactionViewModel(repository)
            AccountsViewModel::class.java ->
                AccountsViewModel(repository)
            StatisticsViewModel::class.java ->
                StatisticsViewModel(repository)
            MoreSettingsViewModel::class.java ->
                MoreSettingsViewModel(repository)
            QuickOnboardingViewModel::class.java ->
                QuickOnboardingViewModel(repository)
            SecurityLockViewModel::class.java ->
                SecurityLockViewModel(repository, container.sessionState)
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
        return viewModel as T
    }
}
