<div align="center">

# 💰 MyMoneyManager

**Aplikasi pencatat keuangan pribadi yang minimalis dan *local-first*, dirancang khusus untuk Android.**  
*Tanpa bloat. Tanpa pelacakan. 100% kejernihan finansial secara offline.*

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Android-SDK%2026%20--%2037-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Room DB](https://img.shields.io/badge/Room%20DB-2.7.2%20(v3)-FFA800?style=for-the-badge&logo=sqlite&logoColor=white)](https://developer.android.com/training/data-storage/room)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](https://opensource.org/licenses/MIT)

---

</div>

## 📌 Ringkasan

**MyMoneyManager** adalah aplikasi Android native modern yang dibangun dengan **Kotlin**, **Jetpack Compose (Material 3)**, dan **Room Database**, mengikuti prinsip **MVVM Clean Architecture** secara ketat.

Sebagian besar aplikasi pengelola anggaran terlalu berat dengan fitur berlebihan, sinkronisasi cloud yang tidak transparan, serta mekanisme transfer yang membingungkan. **MyMoneyManager** memangkas kerumitan yang tidak perlu agar fokus pada hal terpenting: mencatat **Pemasukan (Income)** dan **Pengeluaran (Expense)** dengan cepat, perhitungan saldo instan, dan privasi penuh secara lokal.

---

## ✨ Fitur Utama

- **🎯 Mesin Transaksi Sederhana (Hanya Pemasukan & Pengeluaran)**
  Menghilangkan skema transfer antar-akun yang rumit dan loop internal. Transaksi dibatasi secara tegas pada dua status deterministik: `INCOME` dan `EXPENSE`, sehingga pembukuan tetap bersih dan riwayat transaksi sangat mudah dipahami.

- **⚡ Perhitungan Saldo & Anggaran Instan**
  Pipeline agregasi reaktif yang ditenagai **Kotlin Coroutines** dan **Room SQL Reactive Flows** memastikan saldo akun, rincian kategori, dan anggaran pengeluaran bulanan dihitung ulang secara instan tanpa jeda.

- **✨ Splash Screen Animasi & Preload Database**
  Saat aplikasi dibuka, pengguna disambut layar Splash yang ringan dan mulus dengan logo dompet kulit lipat khas **MyMoneyManager**, aliran mata uang dua warna, serta tagline (*"Kelola Keuangan Lebih Bijak"*). Database Room dimuat secara asinkron di latar belakang dan langsung beralih mulus ke dashboard utama tanpa pengalihan yang mengganggu.

- **🎨 Elegan dengan Material 3 & Aksen Dekoratif Non-Interaktif**
  Dirancang agar tampilan tidak monoton dengan lapisan ilustrasi vektor finansial halus (koin geometris tipis, kilau mata uang, dan garis pita mengalir) beropasitas rendah (`alpha = 0.08 - 0.15`). Seluruh lapisan dekoratif menerapkan isolasi sentuhan ketat (`clickable="false"`, `focusable="false"`, `importantForAccessibility="no"` / `clearAndSetSemantics`) agar tidak mengganggu klik atau scroll.

- **🔒 100% Local-First & Privat**
  Data keuangan Anda tidak pernah meninggalkan perangkat. Seluruh data tersimpan eksklusif di penyimpanan SQLite lokal yang dikelola Room dengan migrasi skema otomatis (`MIGRATION_1_2`, `MIGRATION_2_3`), didukung opsi kunci keamanan biometrik / PIN.

---

## 🏗️ Arsitektur & Tech Stack

MyMoneyManager menerapkan rekomendasi resmi Arsitektur Android menggunakan **Unidirectional Data Flow (UDF)** dengan pemisahan tanggung jawab yang bersih:

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

### Matriks Teknologi

| Lapisan | Teknologi & Library |
|---|---|
| **Bahasa & Tooling** | Kotlin 2.2.10, Gradle Version Catalogs (`libs.versions.toml`), KSP |
| **UI & Tema** | Jetpack Compose (BOM 2026.02.01), Material Design 3, Compose Navigation |
| **Manajemen State** | `ViewModel`, `StateFlow`, `SharedFlow`/`Channel`, `collectAsStateWithLifecycle` |
| **Penyimpanan Lokal** | AndroidX Room 2.7.2 (Entities, TypeConverters, MIGRATION_2_3) |
| **Preferensi** | AndroidX DataStore Preferences 1.1.7 |
| **Perangkat Keras & Keamanan** | AndroidX Biometric 1.1.0 (`BiometricPrompt` dengan fallback PIN) |
| **Mesin Asinkron** | Kotlin Coroutines, Kotlin Flow |

---

## 📂 Struktur Direktori Proyek

```text
mobile app/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/mymoneymanager/
│   │   │   │   ├── MainActivity.kt                # Container single-activity Jetpack Compose
│   │   │   │   ├── SplashActivity.kt              # Activity khusus Splash screen ringan
│   │   │   │   ├── MyMoneyManagerApp.kt           # Class Application & inisialisasi DI container
│   │   │   │   ├── data/
│   │   │   │   │   ├── local/
│   │   │   │   │   │   ├── AppDatabase.kt         # Definisi Room database dengan migrasi v3
│   │   │   │   │   │   ├── converter/             # Room TypeConverters (Enum, LocalDate)
│   │   │   │   │   │   ├── dao/                   # Room DAOs (TxnDao, AccountDao, BudgetDao)
│   │   │   │   │   │   ├── entity/                # Room entities (TxnEntity, AccountEntity, dll.)
│   │   │   │   │   │   └── preferences/           # UserPreferences via DataStore
│   │   │   │   │   ├── mapper/                    # Mapper transformasi Entity <-> UI Model
│   │   │   │   │   ├── repository/                # MoneyRepository (single source of truth)
│   │   │   │   │   └── util/                      # Utilitas format tanggal & mata uang
│   │   │   │   └── ui/
│   │   │   │       ├── components/                # Dialog & picker Compose modular
│   │   │   │       ├── model/                     # Model UI State immutable & UiTransactionType
│   │   │   │       ├── navigation/                # Compose NavHost & deklarasi rute
│   │   │   │       ├── screens/                   # Layar UI (Splash, Transactions, AddTxn, dll.)
│   │   │   │       ├── security/                  # Helper autentikasi biometrik
│   │   │   │       ├── theme/                     # Token warna Material 3, Tipografi, Tema
│   │   │   │       ├── util/                      # MoneyFormat, CurrencyCatalog, helper UI
│   │   │   │       └── viewmodel/                 # MVVM ViewModels & ViewModelFactory
│   │   │   └── res/
│   │   │       ├── drawable/
│   │   │       │   ├── ic_mymoneymanager_logo.xml # Logo vektor: dompet dengan aliran dua warna
│   │   │       │   ├── ic_decorative_card_accents.xml # Overlay koin geometris & kilau tipis
│   │   │       │   └── ic_decorative_empty_state.xml  # Vektor placeholder dekoratif
│   │   │       ├── layout/
│   │   │       │   ├── activity_splash.xml        # Layout layar Splash
│   │   │       │   ├── card_dashboard_balance.xml # Layout kartu saldo dashboard
│   │   │       │   └── layout_transaction_form.xml# Layout formulir (chip Pemasukan vs Pengeluaran)
│   │   │       └── values/                        # colors.xml, strings.xml, themes.xml
│   │   └── test/                                  # Unit test (TransactionValidationTest.kt)
│   └── build.gradle.kts                           # Konfigurasi build tingkat aplikasi
├── gradle/
│   └── libs.versions.toml                         # Katalog versi Gradle terpusat
├── build.gradle.kts                               # Konfigurasi build tingkat proyek
├── settings.gradle.kts                            # Pengaturan sub-proyek
└── README.md
```

---

## 🚀 Panduan Cepat & Prasyarat

### Prasyarat
- **Android Studio**: Ladybug (2024.2.1+) atau Jellyfish (2024.1.1+)
- **JDK**: OpenJDK 17 atau 21
- **Android SDK**: Compile SDK `37`, Target SDK `37`, Min SDK `26` (Android 8.0 Oreo)

### Instruksi Build

1. **Clone Repositori**
   ```bash
   git clone https://github.com/FahryFauzi-001/MyMoneyManager.git
   cd MyMoneyManager/"mobile app"
   ```

2. **Sinkronisasi Proyek dengan Gradle**
   Buka folder `mobile app` di Android Studio dan biarkan Gradle menyinkronkan dependensi yang didefinisikan pada `gradle/libs.versions.toml`.

3. **Jalankan Unit Test Lokal**
   ```bash
   ./gradlew testDebugUnitTest
   ```

4. **Build Debug APK**
   ```bash
   ./gradlew assembleDebug
   ```
   File APK hasil build akan tersedia di:
   `app/build/outputs/apk/debug/app-debug.apk`

5. **Jalankan di Perangkat / Emulator**
   Tekan **Run** (`Shift + F10`) di Android Studio atau install langsung melalui ADB:
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

---

## ⚖️ Lisensi

Proyek ini dilisensikan di bawah **MIT License**. Lihat file [LICENSE](LICENSE) untuk informasi lebih lanjut.

---

## 👨‍💻 Penulis & Pengelola

Dibuat dengan sepenuh hati oleh **[Fahry Fauzi](https://github.com/FahryFauzi-001)**.  
Masukan, saran fitur, dan kontribusi sangat terbuka dan diterima dengan senang hati!
