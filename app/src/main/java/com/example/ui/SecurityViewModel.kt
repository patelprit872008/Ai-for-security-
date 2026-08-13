package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.security.DetailedScanResult
import com.example.worker.DailyScanWorker
import androidx.work.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

sealed class ScanState {
    object Idle : ScanState()
    data class Scanning(val progress: Float, val currentTarget: String) : ScanState()
    data class Completed(val results: List<ScanResultEntity>) : ScanState()
}

class SecurityViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SecurityRepository = SecurityRepository(AppDatabase.getInstance(application))

    val currentUser: StateFlow<UserEntity?> = repository.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allScanResults: StateFlow<List<ScanResultEntity>> = repository.allScanResults
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val criticalThreats: StateFlow<List<ScanResultEntity>> = repository.criticalThreats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val quarantineItems: StateFlow<List<QuarantineItemEntity>> = repository.quarantineItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val installedApps: StateFlow<List<InstalledAppEntity>> = repository.installedApps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val connectedEmails: StateFlow<List<EmailIntegrationEntity>> = repository.connectedEmails
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<SecurityNotificationEntity>> = repository.notifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotifCount: StateFlow<Int> = repository.unreadNotifCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val aiMessages: StateFlow<List<AiMessageEntity>> = repository.aiMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userSettings: StateFlow<UserSettingsEntity> = repository.userSettings
        .map { it ?: UserSettingsEntity() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettingsEntity())

    val scoreHistory: StateFlow<List<SecurityScoreHistoryEntity>> = repository.scoreHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)
    val scanState: StateFlow<ScanState> = _scanState.asStateFlow()

    private val _lastUrlResult = MutableStateFlow<DetailedScanResult?>(null)
    val lastUrlResult: StateFlow<DetailedScanResult?> = _lastUrlResult.asStateFlow()

    private val _lastPhoneReport = MutableStateFlow<PhoneReportEntity?>(null)
    val lastPhoneReport: StateFlow<PhoneReportEntity?> = _lastPhoneReport.asStateFlow()

    private val _lastSmsResult = MutableStateFlow<DetailedScanResult?>(null)
    val lastSmsResult: StateFlow<DetailedScanResult?> = _lastSmsResult.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    val securityScore: StateFlow<Int> = allScanResults.map { list ->
        val criticals = list.count { it.threatLevel == "CRITICAL" || it.threatLevel == "HIGH" }
        val warnings = list.count { it.threatLevel == "MEDIUM" || it.threatLevel == "LOW" }
        minOf(100, maxOf(10, 100 - (criticals * 15) - (warnings * 5)))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 92)

    init {
        viewModelScope.launch {
            repository.initializeDefaultStateIfNeeded(getApplication())
            setupPeriodicWorkManager()
        }
    }

    private fun setupPeriodicWorkManager() {
        val workManager = WorkManager.getInstance(getApplication())
        val scanWorkRequest = PeriodicWorkRequestBuilder<DailyScanWorker>(24, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            "ShieldAIDailyScan",
            ExistingPeriodicWorkPolicy.KEEP,
            scanWorkRequest
        )
    }

    fun login(email: String, pass: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val user = repository.loginUser(email, pass)
            onResult(user != null)
        }
    }

    fun register(email: String, pass: String, name: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            repository.registerUser(email, pass, name)
            onResult(true)
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    fun runQuickScan() {
        viewModelScope.launch {
            _scanState.value = ScanState.Scanning(0.2f, "Scanning Installed Packages...")
            kotlinx.coroutines.delay(400)
            _scanState.value = ScanState.Scanning(0.6f, "Checking Active Network Capabilities...")
            kotlinx.coroutines.delay(400)
            _scanState.value = ScanState.Scanning(0.9f, "Auditing Application Permissions...")
            kotlinx.coroutines.delay(300)

            val results = repository.runQuickScan(getApplication())
            _scanState.value = ScanState.Completed(results)
        }
    }

    fun runFullScan() {
        viewModelScope.launch {
            _scanState.value = ScanState.Scanning(0.1f, "Initializing Deep System Audit...")
            kotlinx.coroutines.delay(300)
            _scanState.value = ScanState.Scanning(0.35f, "Hashing & Checking Storage Downloads...")
            kotlinx.coroutines.delay(500)
            _scanState.value = ScanState.Scanning(0.70f, "Auditing Accessibility & Overlay Services...")
            kotlinx.coroutines.delay(400)
            _scanState.value = ScanState.Scanning(0.92f, "Evaluating Network Encryption & DNS...")
            kotlinx.coroutines.delay(300)

            val results = repository.runFullScan(getApplication())
            _scanState.value = ScanState.Completed(results)
        }
    }

    fun scanUrl(url: String) {
        viewModelScope.launch {
            val result = repository.scanUrlAndSave(url)
            _lastUrlResult.value = result
        }
    }

    fun scanPhone(number: String) {
        viewModelScope.launch {
            val report = repository.scanPhoneAndSave(number)
            _lastPhoneReport.value = report
        }
    }

    fun scanSms(sender: String, body: String) {
        viewModelScope.launch {
            val result = repository.scanSmsAndSave(sender, body)
            _lastSmsResult.value = result
        }
    }

    fun quarantineItem(scanResult: ScanResultEntity) {
        viewModelScope.launch {
            repository.quarantineThreat(scanResult)
        }
    }

    fun deleteScanResult(id: Long) {
        viewModelScope.launch {
            repository.deleteScanResult(id)
        }
    }

    fun connectEmail(email: String, provider: String) {
        viewModelScope.launch {
            repository.connectEmailAccount(email, provider)
        }
    }

    fun disconnectEmail(email: String) {
        viewModelScope.launch {
            repository.disconnectEmailAccount(email)
        }
    }

    fun sendAiPrompt(prompt: String, threatContext: ScanResultEntity? = null) {
        if (prompt.isBlank()) return
        viewModelScope.launch {
            _isAiLoading.value = true
            repository.sendAiMessage(prompt, threatContext)
            _isAiLoading.value = false
        }
    }

    fun updateSettings(settings: UserSettingsEntity) {
        viewModelScope.launch {
            repository.updateUserSettings(settings)
        }
    }

    fun markNotificationRead(id: Long) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearScanHistory()
        }
    }
}
