<div align="center">

# 💰 MyMoneyManager

**A minimalist, local-first personal finance tracker engineered for Android.**  
*Zero bloat. Zero tracking. 100% offline financial clarity.*

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Android-SDK%2026%20--%2037-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Room DB](https://img.shields.io/badge/Room%20DB-2.7.2%20(v3)-FFA800?style=for-the-badge&logo=sqlite&logoColor=white)](https://developer.android.com/training/data-storage/room)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](https://opensource.org/licenses/MIT)

---

</div>

## 📌 Overview

**MyMoneyManager** is a modern native Android application crafted with **Kotlin**, **Jetpack Compose (Material 3)**, and **Room Database**, following strict **MVVM Clean Architecture** principles.

Most budget tracking applications are weighed down by bloated feature sets, opaque cloud syncing, and confusing transfer mechanics. **MyMoneyManager** strips away the unnecessary complexity to focus on what matters most: logging **Income (Pemasukan)** and **Expense (Pengeluaran)** with zero friction, instant balance recalculation, and complete local privacy.

---

## ✨ Core Highlights

- **🎯 Streamlined Transaction Engine (Income vs. Expense Only)**
  Eliminates convoluted multi-account transfer topologies and internal loops. Transactions are strictly constrained to two deterministic states: `INCOME` and `EXPENSE`, ensuring clean bookkeeping and crystal-clear transaction histories.

- **⚡ Instant Balance & Budget Computation**
  Reactive aggregation pipelines powered by **Kotlin Coroutines** and **Room SQL Reactive Flows** ensure account balances, category breakdowns, and monthly expense budgets are recalculated with zero perceptible latency.

- **✨ Branded Animated Splash & DB Preload**
  On application launch, users are greeted with a lightweight, fluid Splash screen featuring the custom **MyMoneyManager** leather-fold wallet emblem with dual-tone currency flows and tagline (*"Kelola Keuangan Lebih Bijak"*). Preloads the Room database asynchronously and smoothly transitions directly to the main dashboard—eliminating unwanted redirects.

- **🎨 Material 3 Elegance & Non-Interactive Decorative Accents**
  Designed to solve flat UI fatigue by layering micro-vector financial illustrations (faint geometric coins, currency sparkles, and flowing ribbon lines) with low opacity (`alpha = 0.08 - 0.15`). All decorative layers enforce strict touch isolation (`clickable="false"`, `focusable="false"`, `importantForAccessibility="no"` / `clearAndSetSemantics`), preventing any click or scroll event hijacking.

- **🔒 100% Local-First & Private**
  Your financial data never leaves your device. Data is persisted exclusively in local SQLite storage managed by Room with automated schema migrations (`MIGRATION_1_2`, `MIGRATION_2_3`), backed by optional biometric / PIN security lock.

---

## 🏗️ Architecture & Tech Stack

MyMoneyManager implements the official Android Architecture recommendations using **Unidirectional Data Flow (UDF)** and a clean separation of concerns:

```
┌────────────────────────────────────────────────────────┐
│                   Presentation Layer                   │
│  Jetpack Compose • Material 3 • Navigation Compose    │
└───────────────────────────▲────────────────────────────┘
                            │ StateFlow / UI Events
┌───────────────────────────┴────────────────────────────┐
│                    ViewModel Layer                     │
│  StateFlow • Channel • ViewModelScope • Coroutines     │
└───────────────────────────▲────────────────────────────┘
                            │ Reactive Flows / Suspend
┌───────────────────────────┴────────────────────────────┐
│                 Domain / Repository Layer              │
│  MoneyRepository • Clean Data Mappers • Currency Calc │
└───────────────────────────▲────────────────────────────┘
                            │ Room DAO Queries / Preferences
┌───────────────────────────┴────────────────────────────┐
│                    Local Data Layer                    │
│  Room Database (v3) • SQLite • DataStore Preferences   │
└────────────────────────────────────────────────────────┘
```

### Technology Matrix

| Layer | Technologies & Libraries |
|---|---|
| **Language & Tooling** | Kotlin 2.2.10, Gradle Version Catalogs (`libs.versions.toml`), KSP |
| **UI & Theming** | Jetpack Compose (BOM 2026.02.01), Material Design 3, Compose Navigation |
| **State Management** | `ViewModel`, `StateFlow`, `SharedFlow`/`Channel`, `collectAsStateWithLifecycle` |
| **Local Persistence** | AndroidX Room 2.7.2 (Entities, TypeConverters, MIGRATION_2_3) |
| **Preferences** | AndroidX DataStore Preferences 1.1.7 |
| **Hardware & Security** | AndroidX Biometric 1.1.0 (`BiometricPrompt` with fallback PIN) |
| **Asynchronous Engine** | Kotlin Coroutines, Kotlin Flow |

---

## 📂 Project Directory Structure

```text
mobile app/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/mymoneymanager/
│   │   │   │   ├── MainActivity.kt                # Jetpack Compose single-activity container
│   │   │   │   ├── SplashActivity.kt              # Dedicated lightweight Splash screen activity
│   │   │   │   ├── MyMoneyManagerApp.kt           # Application class & DI container bootstrap
│   │   │   │   ├── data/
│   │   │   │   │   ├── local/
│   │   │   │   │   │   ├── AppDatabase.kt         # Room database definition with v3 migrations
│   │   │   │   │   │   ├── converter/             # Room TypeConverters (Enum, LocalDate)
│   │   │   │   │   │   ├── dao/                   # Room DAOs (TxnDao, AccountDao, BudgetDao)
│   │   │   │   │   │   ├── entity/                # Room entities (TxnEntity, AccountEntity, etc.)
│   │   │   │   │   │   └── preferences/           # UserPreferences via DataStore
│   │   │   │   │   ├── mapper/                    # Entity <-> UI Model transformation mappers
│   │   │   │   │   ├── repository/                # MoneyRepository (single source of truth)
│   │   │   │   │   └── util/                      # Date & currency formatting utilities
│   │   │   │   └── ui/
│   │   │   │       ├── components/                # Modular Compose dialogs & pickers
│   │   │   │       ├── model/                     # Immutable UI State models & UiTransactionType
│   │   │   │       ├── navigation/                # Compose NavHost & route declarations
│   │   │   │       ├── screens/                   # Screens (Splash, Transactions, AddTxn, etc.)
│   │   │   │       ├── security/                  # Biometric authentication helper
│   │   │   │       ├── theme/                     # Material 3 Color tokens, Type, Theme
│   │   │   │       ├── util/                      # MoneyFormat, CurrencyCatalog, UI helpers
│   │   │   │       └── viewmodel/                 # MVVM ViewModels & ViewModelFactory
│   │   │   └── res/
│   │   │       ├── drawable/
│   │   │       │   ├── ic_mymoneymanager_logo.xml # Vector logo: wallet with dual-tone flow
│   │   │       │   ├── ic_decorative_card_accents.xml # Faint geometric coins & sparkles overlay
│   │   │       │   └── ic_decorative_empty_state.xml  # Decorative placeholder vector
│   │   │       ├── layout/
│   │   │       │   ├── activity_splash.xml        # Splash screen layout
│   │   │       │   ├── card_dashboard_balance.xml # Dashboard balance card layout
│   │   │       │   └── layout_transaction_form.xml# Form layout (Income vs Expense chips)
│   │   │       └── values/                        # colors.xml, strings.xml, themes.xml
│   │   └── test/                                  # Unit tests (TransactionValidationTest.kt)
│   └── build.gradle.kts                           # App-level build configurations
├── gradle/
│   └── libs.versions.toml                         # Centralized Gradle version catalog
├── build.gradle.kts                               # Project-level build configuration
├── settings.gradle.kts                            # Sub-project settings
└── README.md
```

---

## 🚀 Quick Start & Prerequisites

### Prerequisites
- **Android Studio**: Ladybug (2024.2.1+) or Jellyfish (2024.1.1+)
- **JDK**: OpenJDK 17 or 21
- **Android SDK**: Compile SDK `37`, Target SDK `37`, Min SDK `26` (Android 8.0 Oreo)

### Build Instructions

1. **Clone the Repository**
   ```bash
   git clone https://github.com/FahryFauzi-001/MyMoneyManager.git
   cd MyMoneyManager/"mobile app"
   ```

2. **Sync Project with Gradle Files**
   Open the `mobile app` folder inside Android Studio and allow Gradle to sync dependencies defined in `gradle/libs.versions.toml`.

3. **Run Local Unit Tests**
   ```bash
   ./gradlew testDebugUnitTest
   ```

4. **Assemble Debug APK**
   ```bash
   ./gradlew assembleDebug
   ```
   The generated APK will be available in:
   `app/build/outputs/apk/debug/app-debug.apk`

5. **Deploy to Device / Emulator**
   Press **Run** (`Shift + F10`) in Android Studio or install directly via ADB:
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

---

## ⚖️ License

This project is licensed under the **MIT License**. See the [LICENSE](LICENSE) file for more information.

---

## 👨‍💻 Author & Maintainer

Crafted with care by **[Fahry Fauzi](https://github.com/FahryFauzi-001)**.  
Feedback, feature suggestions, and contributions are warmly welcome!
