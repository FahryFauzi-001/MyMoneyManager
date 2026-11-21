package com.example.mymoneymanager

import android.app.Application
import android.content.Context
import com.example.mymoneymanager.data.local.AppDatabase
import com.example.mymoneymanager.data.local.preferences.UserPreferences
import com.example.mymoneymanager.data.repository.MoneyRepository
import com.example.mymoneymanager.ui.viewmodel.SessionState

class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val database: AppDatabase by lazy { AppDatabase.getInstance(appContext) }
    val userPreferences: UserPreferences by lazy { UserPreferences(appContext) }
    val repository: MoneyRepository by lazy { MoneyRepository(database, userPreferences) }
    val sessionState: SessionState = SessionState()
}

class MyMoneyManagerApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
