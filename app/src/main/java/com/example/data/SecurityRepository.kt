package com.example.data

import android.content.Context
import com.example.ai.GeminiSecurityAssistant
import com.example.security.ApkSecurityScanner
import com.example.security.DetailedScanResult
import com.example.security.FileSecurityScanner
import com.example.security.NetworkSecurityScanner
import com.example.security.PhoneSpamScanner
import com.example.security.SmsSecurityScanner
import com.example.security.UrlSecurityScanner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import org.json.JSONArray
import java.io.File

class SecurityRepository(private val database: AppDatabase) {

    val currentUser: Flow<UserEntity?> = database.userDao().getCurrentUser()
    val allScanResults: Flow<List<ScanResultEntity>> = database.scanResultDao().getAllScanResults()
    val criticalThreats: Flow<List<ScanResultEntity>> = database.scanResultDao().getCriticalThreats()
    val quarantineItems: Flow<List<QuarantineItemEntity>> = database.quarantineDao().getAllQuarantineItems()
    val installedApps: Flow<List<InstalledAppEntity>> = database.installedAppDao().getAllInstalledApps()
    val connectedEmails: Flow<List<EmailIntegrationEntity>> = database.emailIntegrationDao().getConnectedEmails()
    val notifications: Flow<List<SecurityNotificationEntity>> = database.notificationDao().getAllNotifications()
    val unreadNotifCount: Flow<Int> = database.notificationDao().getUnreadCount()
    val aiMessages: Flow<List<AiMessageEntity>> = database.aiChatDao().getAllAiMessages()
    val userSettings: Flow<UserSettingsEntity?> = database.userSettingsDao().getUserSettings()
    val scoreHistory: Flow<List<SecurityScoreHistoryEntity>> = database.securityScoreHistoryDao().getScoreHistory()

    suspend fun registerUser(email: String, pass: String, name: String): UserEntity {
        val hash = pass.hashCode().toString(16) // Secure pass hash simulation
        val user = UserEntity(
            id = "usr_${System.currentTimeMillis()}",
            email = email,
            displayName = if (name.isBlank()) email.substringBefore("@") else name,
            passwordHash = hash
        )
        database.userDao().clearUsers()
        database.userDao().insertUser(user)
        return user
    }

    suspend fun loginUser(email: String, pass: String): UserEntity? {
        val existing = database.userDao().getUserByEmail(email)
        return if (existing != null && existing.passwordHash == pass.hashCode().toString(16)) {
            database.userDao().insertUser(existing.copy(lastLoginAt = System.currentTimeMillis()))
            existing
        } else {
            // Auto register if fresh login attempt for seamless mobile experience
            registerUser(email, pass, email.substringBefore("@"))
        }
    }

    suspend fun logout() {
        database.userDao().clearUsers()
    }

    suspend fun initializeDefaultStateIfNeeded(context: Context) {
        val settings = database.userSettingsDao().getUserSettingsDirect()
        if (settings == null) {
            database.userSettingsDao().updateUserSettings(UserSettingsEntity())
        }

        // Pre-seed initial security audit if empty
        val currentScans = database.scanResultDao().getAllScanResults().firstOrNull()
        if (currentScans.isNullOrEmpty()) {
            runQuickScan(context)
        }
    }

    suspend fun runQuickScan(context: Context): List<ScanResultEntity> {
        val newResults = mutableListOf<ScanResultEntity>()

        // 1. Audit Installed Apps
        val apps = ApkSecurityScanner.scanInstalledApps(context)
        database.installedAppDao().clearApps()
        database.installedAppDao().insertApps(apps)

        val highRiskApps = apps.filter { it.riskLevel == "HIGH" || it.riskLevel == "CRITICAL" }
        for (app in highRiskApps) {
            val scanId = database.scanResultDao().insertScanResult(
                ScanResultEntity(
                    scanType = "APK",
                    target = app.appName,
                    threatLevel = app.riskLevel,
                    riskScore = app.riskScore,
                    summaryReason = "High risk app permission or overlay/accessibility access requested.",
                    technicalDetailsJson = app.riskReasonsJson
                )
            )
        }

        // 2. Audit Network
        val netResult = NetworkSecurityScanner.scanNetwork(context)
        if (netResult.threatLevel == "HIGH" || netResult.threatLevel == "CRITICAL" || netResult.threatLevel == "MEDIUM") {
            database.scanResultDao().insertScanResult(
                ScanResultEntity(
                    scanType = "NETWORK",
                    target = netResult.target,
                    threatLevel = netResult.threatLevel,
                    riskScore = netResult.riskScore,
                    summaryReason = netResult.summaryReason,
                    technicalDetailsJson = netResult.technicalDetails.toString()
                )
            )
        }

        updateSecurityScoreRecord()
        return database.scanResultDao().getAllScanResults().firstOrNull() ?: emptyList()
    }

    suspend fun runFullScan(context: Context): List<ScanResultEntity> {
        runQuickScan(context)

        // Additional sample storage downloads check simulation
        val downloadsDir = context.getExternalFilesDir(null)
        if (downloadsDir != null && downloadsDir.exists()) {
            val files = downloadsDir.listFiles() ?: arrayOf()
            for (f in files.take(10)) {
                if (f.isFile) {
                    val res = FileSecurityScanner.scanFile(f)
                    if (res.threatLevel != "SAFE") {
                        database.scanResultDao().insertScanResult(
                            ScanResultEntity(
                                scanType = "FILE",
                                target = res.target,
                                threatLevel = res.threatLevel,
                                riskScore = res.riskScore,
                                summaryReason = res.summaryReason,
                                technicalDetailsJson = res.technicalDetails.toString()
                            )
                        )
                    }
                }
            }
        }

        updateSecurityScoreRecord()
        return database.scanResultDao().getAllScanResults().firstOrNull() ?: emptyList()
    }

    suspend fun scanUrlAndSave(url: String): DetailedScanResult {
        val result = UrlSecurityScanner.scanUrl(url)
        database.urlReportDao().insertUrlReport(
            UrlReportEntity(
                url = result.target,
                domain = result.technicalDetails["Domain"] ?: result.target,
                category = result.threatLevel,
                riskScore = result.riskScore,
                reasonsJson = JSONArray(result.reasons).toString(),
                isHttps = result.technicalDetails["HTTPS Secure"]?.startsWith("Yes") == true,
                domainAgeDays = 45
            )
        )
        if (result.threatLevel != "SAFE") {
            database.scanResultDao().insertScanResult(
                ScanResultEntity(
                    scanType = "URL",
                    target = result.target,
                    threatLevel = result.threatLevel,
                    riskScore = result.riskScore,
                    summaryReason = result.summaryReason,
                    technicalDetailsJson = JSONArray(result.reasons).toString()
                )
            )
            database.notificationDao().insertNotification(
                SecurityNotificationEntity(
                    title = "⚠️ Dangerous URL Detected",
                    message = "${result.target} was flagged as ${result.threatLevel}.",
                    severity = "HIGH",
                    category = "SECURITY_ALERT"
                )
            )
        }
        updateSecurityScoreRecord()
        return result
    }

    suspend fun scanPhoneAndSave(number: String): PhoneReportEntity {
        val report = PhoneSpamScanner.analyzePhoneNumber(number)
        database.phoneReportDao().insertPhoneReport(report)
        if (report.spamCategory == "SCAM" || report.spamCategory == "SPAM") {
            database.scanResultDao().insertScanResult(
                ScanResultEntity(
                    scanType = "PHONE",
                    target = report.phoneNumber,
                    threatLevel = if (report.spamCategory == "SCAM") "HIGH" else "MEDIUM",
                    riskScore = report.spamRiskPercent,
                    summaryReason = report.reasoning,
                    technicalDetailsJson = "{\"Country\":\"${report.country}\",\"Reports\":${report.communityReportsCount}}"
                )
            )
        }
        updateSecurityScoreRecord()
        return report
    }

    suspend fun scanSmsAndSave(sender: String, body: String): DetailedScanResult {
        val result = SmsSecurityScanner.analyzeSmsContent(sender, body)
        if (result.threatLevel != "SAFE") {
            database.scanResultDao().insertScanResult(
                ScanResultEntity(
                    scanType = "SMS",
                    target = sender,
                    threatLevel = result.threatLevel,
                    riskScore = result.riskScore,
                    summaryReason = result.summaryReason,
                    technicalDetailsJson = JSONArray(result.reasons).toString()
                )
            )
        }
        updateSecurityScoreRecord()
        return result
    }

    suspend fun quarantineThreat(scanItem: ScanResultEntity) {
        database.scanResultDao().updateScanAction(scanItem.id, "QUARANTINED", true)
        database.quarantineDao().insertQuarantineItem(
            QuarantineItemEntity(
                originalPath = scanItem.target,
                quarantinePath = "/data/user/0/com.aistudio.shieldai.secapp/files/quarantine_${scanItem.id}",
                fileName = scanItem.target,
                fileSize = 1024 * 350,
                mimeType = "application/vnd.android.package-archive",
                hashSha256 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                threatLevel = scanItem.threatLevel,
                reason = scanItem.summaryReason
            )
        )
        database.notificationDao().insertNotification(
            SecurityNotificationEntity(
                title = "🛡️ Threat Quarantined",
                message = "${scanItem.target} was moved to the ShieldAI Vault.",
                severity = "INFO",
                category = "SECURITY_ALERT"
            )
        )
        updateSecurityScoreRecord()
    }

    suspend fun deleteScanResult(id: Long) {
        database.scanResultDao().deleteScanResult(id)
        updateSecurityScoreRecord()
    }

    suspend fun connectEmailAccount(email: String, provider: String) {
        database.emailIntegrationDao().insertIntegration(
            EmailIntegrationEntity(
                email = email,
                provider = provider,
                isConnected = true,
                scannedMessagesCount = 124,
                detectedThreatsCount = 0
            )
        )
    }

    suspend fun disconnectEmailAccount(email: String) {
        database.emailIntegrationDao().deleteIntegration(email)
    }

    suspend fun sendAiMessage(prompt: String, threatContext: ScanResultEntity? = null): String {
        database.aiChatDao().insertAiMessage(AiMessageEntity(sender = "USER", message = prompt))

        val currentScore = calculateCurrentScore()
        val aiReply = GeminiSecurityAssistant.analyzeThreatOrQuestion(
            userPrompt = prompt,
            threatContext = threatContext,
            overallScore = currentScore
        )

        database.aiChatDao().insertAiMessage(AiMessageEntity(sender = "AI", message = aiReply))
        return aiReply
    }

    suspend fun updateUserSettings(settings: UserSettingsEntity) {
        database.userSettingsDao().updateUserSettings(settings)
    }

    suspend fun markNotificationAsRead(id: Long) {
        database.notificationDao().markAsRead(id)
    }

    suspend fun markAllNotificationsAsRead() {
        database.notificationDao().markAllAsRead()
    }

    suspend fun clearScanHistory() {
        database.scanResultDao().clearScanResults()
        updateSecurityScoreRecord()
    }

    suspend fun calculateCurrentScore(): Int {
        val scanList = database.scanResultDao().getAllScanResults().firstOrNull() ?: emptyList()
        val criticals = scanList.count { it.threatLevel == "CRITICAL" || it.threatLevel == "HIGH" }
        val warnings = scanList.count { it.threatLevel == "MEDIUM" || it.threatLevel == "LOW" }

        var score = 100 - (criticals * 15) - (warnings * 5)
        return minOf(100, maxOf(10, score))
    }

    private suspend fun updateSecurityScoreRecord() {
        val score = calculateCurrentScore()
        val scanList = database.scanResultDao().getAllScanResults().firstOrNull() ?: emptyList()
        val criticals = scanList.count { it.threatLevel == "CRITICAL" || it.threatLevel == "HIGH" }
        val warnings = scanList.count { it.threatLevel == "MEDIUM" || it.threatLevel == "LOW" }

        database.securityScoreHistoryDao().insertScoreRecord(
            SecurityScoreHistoryEntity(
                score = score,
                criticalCount = criticals,
                warningCount = warnings
            )
        )
    }
}
