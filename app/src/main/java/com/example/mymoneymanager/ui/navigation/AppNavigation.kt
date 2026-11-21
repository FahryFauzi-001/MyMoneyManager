package com.example.mymoneymanager.ui.navigation

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mymoneymanager.ui.components.AddAccountDialog
import com.example.mymoneymanager.ui.components.BudgetEditorDialog
import com.example.mymoneymanager.ui.components.ConfirmDeleteAccountDialog
import com.example.mymoneymanager.ui.components.ConfirmDeleteTransactionDialog
import com.example.mymoneymanager.ui.components.ConfirmResetDemoDialog
import com.example.mymoneymanager.ui.components.CurrencyPickerDialog
import com.example.mymoneymanager.ui.components.PinSetupDialog
import com.example.mymoneymanager.ui.components.PrivacyDialog
import com.example.mymoneymanager.ui.model.UiAccount
import com.example.mymoneymanager.ui.model.UiTransaction
import com.example.mymoneymanager.ui.model.UiTransactionType
import com.example.mymoneymanager.ui.screens.AccountsScreen
import com.example.mymoneymanager.ui.screens.AddTransactionScreen
import com.example.mymoneymanager.ui.screens.MoreSettingsScreen
import com.example.mymoneymanager.ui.screens.QuickOnboardingScreen
import com.example.mymoneymanager.ui.screens.SecurityLockScreen
import com.example.mymoneymanager.ui.screens.StatisticsScreen
import com.example.mymoneymanager.ui.screens.TransactionsScreen
import com.example.mymoneymanager.ui.security.BiometricHelper
import com.example.mymoneymanager.ui.util.CollectMessages
import com.example.mymoneymanager.ui.util.CurrencyCatalog
import com.example.mymoneymanager.ui.util.appVersionName
import com.example.mymoneymanager.ui.util.findFragmentActivity
import com.example.mymoneymanager.ui.viewmodel.AccountsViewModel
import com.example.mymoneymanager.ui.viewmodel.AddTransactionEvent
import com.example.mymoneymanager.ui.viewmodel.AddTransactionViewModel
import com.example.mymoneymanager.ui.viewmodel.MoreSettingsViewModel
import com.example.mymoneymanager.ui.viewmodel.QuickOnboardingViewModel
import com.example.mymoneymanager.ui.viewmodel.SecurityLockViewModel
import com.example.mymoneymanager.ui.viewmodel.StartState
import com.example.mymoneymanager.ui.viewmodel.StatisticsViewModel
import com.example.mymoneymanager.ui.viewmodel.TransactionsViewModel
import com.example.mymoneymanager.ui.viewmodel.ViewModelFactory
import java.time.LocalDate

object Routes {
    const val ONBOARDING = "onboarding"
    const val LOCK = "lock"
    const val TRANSACTIONS = "transactions"
    const val ADD_TRANSACTION_PATTERN = "add_transaction?type={type}"
    const val STATISTICS = "statistics"
    const val ACCOUNTS = "accounts"
    const val MORE = "more"

    fun addTransaction(type: UiTransactionType = UiTransactionType.EXPENSE): String {
        return "add_transaction?type=${type.name}"
    }
}

private data class TopLevelDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val topLevelDestinations = listOf(
    TopLevelDestination(Routes.TRANSACTIONS, "Beranda", Icons.AutoMirrored.Filled.ReceiptLong),
    TopLevelDestination(Routes.STATISTICS, "Statistik", Icons.Filled.BarChart),
    TopLevelDestination(Routes.ACCOUNTS, "Rekening", Icons.Filled.AccountBalanceWallet),
    TopLevelDestination(Routes.MORE, "Pengaturan", Icons.Filled.Settings)
)

@Composable
fun AppNavigation(
    startState: StartState,
    viewModelFactory: ViewModelFactory
) {
    if (startState == StartState.Loading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        )
        return
    }

    key(startState) {
        AppNavHost(
            startState = startState,
            factory = viewModelFactory
        )
    }
}

@Composable
private fun AppNavHost(
    startState: StartState,
    factory: ViewModelFactory
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = topLevelDestinations.any { it.route == currentRoute }

    val startDestination = when (startState) {
        StartState.Onboarding -> Routes.ONBOARDING
        StartState.Locked -> Routes.LOCK
        else -> Routes.TRANSACTIONS
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    topLevelDestinations.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = { navController.navigateToTab(destination.route) },
                            icon = {
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = destination.label
                                )
                            },
                            label = { Text(text = destination.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            composable(Routes.ONBOARDING) { OnboardingRoute(factory) }
            composable(Routes.LOCK) { LockRoute(factory) }
            composable(Routes.TRANSACTIONS) { TransactionsRoute(navController, factory) }
            composable(
                route = Routes.ADD_TRANSACTION_PATTERN,
                arguments = listOf(
                    navArgument("type") {
                        type = NavType.StringType
                        defaultValue = UiTransactionType.EXPENSE.name
                    }
                )
            ) { entry ->
                val rawType = entry.arguments?.getString("type")
                val initialType = runCatching {
                    UiTransactionType.valueOf(rawType ?: UiTransactionType.EXPENSE.name)
                }.getOrDefault(UiTransactionType.EXPENSE)
                AddTransactionRoute(
                    initialType = initialType,
                    navController = navController,
                    factory = factory
                )
            }
            composable(Routes.STATISTICS) { StatisticsRoute(factory) }
            composable(Routes.ACCOUNTS) { AccountsRoute(factory) }
            composable(Routes.MORE) { MoreRoute(factory) }
        }
    }
}

private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun OnboardingRoute(factory: ViewModelFactory) {
    val viewModel: QuickOnboardingViewModel = viewModel(factory = factory)
    CollectMessages(viewModel.messages)

    QuickOnboardingScreen(
        currencyOptions = CurrencyCatalog.options,
        onStartTrackingClick = { currencyCode, cashBalance, bankBalance ->
            viewModel.completeOnboarding(currencyCode, cashBalance, bankBalance)
        }
    )
}

@Composable
private fun LockRoute(factory: ViewModelFactory) {
    val viewModel: SecurityLockViewModel = viewModel(factory = factory)
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = remember(context) { context.findFragmentActivity() }
    val biometricAvailable = remember(context) { BiometricHelper.isAvailable(context) }

    fun promptBiometric() {
        if (biometricAvailable && activity != null) {
            BiometricHelper.authenticate(
                activity = activity,
                onSuccess = { viewModel.onBiometricSuccess() },
                onError = { message -> viewModel.onBiometricError(message) }
            )
        } else {
            Toast.makeText(
                context,
                "Sidik jari terverifikasi (Simulasi Biometrik)",
                Toast.LENGTH_SHORT
            ).show()
            viewModel.onBiometricSuccess()
        }
    }

    LaunchedEffect(Unit) {
        if (biometricAvailable && activity != null) {
            BiometricHelper.authenticate(
                activity = activity,
                onSuccess = { viewModel.onBiometricSuccess() },
                onError = { message -> viewModel.onBiometricError(message) }
            )
        }
    }

    SecurityLockScreen(
        isBiometricAvailable = biometricAvailable,
        errorMessage = state.errorMessage,
        onPinCompleted = { pin -> viewModel.onPinCompleted(pin) },
        onBiometricClick = { promptBiometric() }
    )
}

@Composable
private fun TransactionsRoute(
    navController: NavHostController,
    factory: ViewModelFactory
) {
    val viewModel: TransactionsViewModel = viewModel(factory = factory)
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var pendingDelete by remember { mutableStateOf<UiTransaction?>(null) }

    CollectMessages(viewModel.messages)

    TransactionsScreen(
        state = state,
        onToggleBalanceVisibility = { viewModel.toggleBalanceVisibility() },
        onOpenLinkBankDialog = { viewModel.openLinkBankDialog() },
        onCloseLinkBankDialog = { viewModel.closeLinkBankDialog() },
        onSelectBank = { bankName -> viewModel.simulateLinkBank(bankName) },
        onNavigateToTransfer = {
            navController.navigate(Routes.addTransaction(UiTransactionType.TRANSFER))
        },
        onNavigateToAddTransaction = {
            navController.navigate(Routes.addTransaction(UiTransactionType.EXPENSE))
        },
        onLoadSampleData = { viewModel.loadSampleData() },
        onTransactionClick = { transaction -> pendingDelete = transaction }
    )

    pendingDelete?.let { transaction ->
        ConfirmDeleteTransactionDialog(
            transaction = transaction,
            currencySymbol = state.currencySymbol,
            onConfirm = {
                viewModel.deleteTransaction(transaction.id)
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null }
        )
    }
}

@Composable
private fun StatisticsRoute(factory: ViewModelFactory) {
    val viewModel: StatisticsViewModel = viewModel(factory = factory)
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    StatisticsScreen(
        statistics = state.statistics,
        currencySymbol = state.currencySymbol
    )
}

@Composable
private fun AccountsRoute(factory: ViewModelFactory) {
    val viewModel: AccountsViewModel = viewModel(factory = factory)
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddAccountDialog by rememberSaveable { mutableStateOf(false) }
    var pendingDeleteAccount by remember { mutableStateOf<UiAccount?>(null) }

    CollectMessages(viewModel.messages)

    AccountsScreen(
        state = state,
        onAddManualAccountClick = { showAddAccountDialog = true },
        onOpenLinkBankDialog = { viewModel.openLinkBankDialog() },
        onCloseLinkBankDialog = { viewModel.closeLinkBankDialog() },
        onSelectBank = { bankName -> viewModel.simulateLinkBank(bankName) },
        onDeleteAccountClick = { account -> pendingDeleteAccount = account }
    )

    if (showAddAccountDialog) {
        AddAccountDialog(
            currencySymbol = state.currencySymbol,
            onConfirm = { name, type, openingBalance ->
                viewModel.addAccount(name, type, openingBalance)
                showAddAccountDialog = false
            },
            onDismiss = { showAddAccountDialog = false }
        )
    }

    pendingDeleteAccount?.let { account ->
        ConfirmDeleteAccountDialog(
            account = account,
            onConfirm = {
                viewModel.deleteAccount(account)
                pendingDeleteAccount = null
            },
            onDismiss = { pendingDeleteAccount = null }
        )
    }
}

@Composable
private fun MoreRoute(factory: ViewModelFactory) {
    val viewModel: MoreSettingsViewModel = viewModel(factory = factory)
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val appVersion = remember(context) { context.appVersionName() }

    var showCurrencyDialog by rememberSaveable { mutableStateOf(false) }
    var showBudgetDialog by rememberSaveable { mutableStateOf(false) }
    var showPinDialog by rememberSaveable { mutableStateOf(false) }
    var showPrivacyDialog by rememberSaveable { mutableStateOf(false) }
    var showResetDemoDialog by rememberSaveable { mutableStateOf(false) }

    CollectMessages(viewModel.messages)

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.exportCsv(uri, context.contentResolver)
        }
    }

    MoreSettingsScreen(
        isAppLockEnabled = state.isAppLockEnabled,
        currencySymbol = state.currencySymbol,
        appVersion = appVersion,
        onExportCsvClick = {
            exportLauncher.launch("mymoneymanager-${LocalDate.now()}.csv")
        },
        onResetDemoClick = { showResetDemoDialog = true },
        onBudgetsClick = { showBudgetDialog = true },
        onAppLockChanged = { enabled ->
            if (enabled) showPinDialog = true else viewModel.disableAppLock()
        },
        onChangePinClick = { showPinDialog = true },
        onCurrencyClick = { showCurrencyDialog = true },
        onPrivacyClick = { showPrivacyDialog = true }
    )

    if (showCurrencyDialog) {
        CurrencyPickerDialog(
            options = CurrencyCatalog.options,
            selectedSymbol = state.currencySymbol,
            onSelect = { option ->
                viewModel.setCurrency(option)
                showCurrencyDialog = false
            },
            onDismiss = { showCurrencyDialog = false }
        )
    }

    if (showBudgetDialog) {
        BudgetEditorDialog(
            categories = state.categories,
            budgets = state.budgets,
            currencySymbol = state.currencySymbol,
            onSave = { inputs ->
                viewModel.saveBudgets(inputs)
                showBudgetDialog = false
            },
            onDismiss = { showBudgetDialog = false }
        )
    }

    if (showPinDialog) {
        PinSetupDialog(
            onConfirm = { pin ->
                viewModel.enableAppLock(pin)
                showPinDialog = false
            },
            onDismiss = { showPinDialog = false }
        )
    }

    if (showPrivacyDialog) {
        PrivacyDialog(onDismiss = { showPrivacyDialog = false })
    }

    if (showResetDemoDialog) {
        ConfirmResetDemoDialog(
            onConfirm = {
                viewModel.resetDemoData()
                showResetDemoDialog = false
            },
            onDismiss = { showResetDemoDialog = false }
        )
    }
}

@Composable
private fun AddTransactionRoute(
    initialType: UiTransactionType,
    navController: NavHostController,
    factory: ViewModelFactory
) {
    val viewModel: AddTransactionViewModel = viewModel(factory = factory)
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                AddTransactionEvent.Saved -> {
                    Toast.makeText(context, "Transaksi berhasil disimpan", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
                is AddTransactionEvent.Error -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    AddTransactionScreen(
        initialType = initialType,
        accounts = state.accounts,
        categories = state.categories,
        currencySymbol = state.currencySymbol,
        selectedDate = LocalDate.now(),
        onSaveClick = { type, amountMinor, date, sourceAccountId, destinationAccountId, categoryId, note ->
            viewModel.saveTransaction(
                type = type,
                amountMinor = amountMinor,
                date = date,
                sourceAccountId = sourceAccountId,
                destinationAccountId = destinationAccountId,
                categoryId = categoryId,
                note = note
            )
        },
        onBackClick = { navController.popBackStack() }
    )
}
