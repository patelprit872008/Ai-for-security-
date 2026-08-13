package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val email: String,
    val displayName: String,
    val passwordHash: String,
    val isVerified: Boolean = true,
    val isTwoFactorEnabled: Boolean = false,
    val totpSecret: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "scan_results")
data class ScanResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val scanType: String, // FILE, APK, URL, PHONE, SMS, EMAIL, NETWORK, PERMISSION
    val target: String,
    val threatLevel: String, // SAFE, LOW, MEDIUM, HIGH, CRITICAL
    val riskScore: Int, // 0 - 100
    val summaryReason: String,
    val technicalDetailsJson: String,
    val confidenceScore: Int = 90,
    val actionTaken: String = "NONE", // NONE, QUARANTINED, DELETED, BLOCKED, IGNORED
    val timestamp: Long = System.currentTimeMillis(),
    val isQuarantined: Boolean = false
)

@Entity(tableName = "quarantine_items")
data class QuarantineItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val originalPath: String,
    val quarantinePath: String,
    val fileName: String,
    val fileSize: Long,
    val mimeType: String,
    val hashSha256: String,
    val threatLevel: String,
    val reason: String,
    val quarantinedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "installed_apps")
data class InstalledAppEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val versionName: String,
    val isSystemApp: Boolean,
    val riskLevel: String, // SAFE, LOW, MEDIUM, HIGH, CRITICAL
    val riskScore: Int,
    val dangerousPermissionsJson: String,
    val riskReasonsJson: String,
    val hasAccessibilityPermission: Boolean = false,
    val hasOverlayPermission: Boolean = false,
    val lastScannedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "phone_reports")
data class PhoneReportEntity(
    @PrimaryKey val phoneNumber: String,
    val country: String,
    val spamCategory: String, // SCAM, SPAM, TELEMARKETER, FINANCIAL_FRAUD, SAFE, UNKNOWN
    val spamRiskPercent: Int,
    val communityReportsCount: Int,
    val reasoning: String,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "url_reports")
data class UrlReportEntity(
    @PrimaryKey val url: String,
    val domain: String,
    val category: String, // PHISHING, MALWARE, BRAND_IMPERSONATION, SAFE, SUSPICIOUS
    val riskScore: Int,
    val reasonsJson: String,
    val isHttps: Boolean,
    val domainAgeDays: Int,
    val scannedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "email_integrations")
data class EmailIntegrationEntity(
    @PrimaryKey val email: String,
    val provider: String, // GMAIL, OUTLOOK
    val isConnected: Boolean,
    val connectedAt: Long = System.currentTimeMillis(),
    val scannedMessagesCount: Int = 0,
    val detectedThreatsCount: Int = 0
)

@Entity(tableName = "security_notifications")
data class SecurityNotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val message: String,
    val severity: String, // INFO, WARNING, HIGH, CRITICAL
    val category: String, // SECURITY_ALERT, SCAN_RESULT, SPAM, SYSTEM, ACCOUNT
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val deepLinkRoute: String = ""
)

@Entity(tableName = "ai_messages")
data class AiMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String, // USER, AI
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val relatedThreatId: Long? = null
)

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val dailyScanEnabled: Boolean = true,
    val dailyScanTimeHour: Int = 0,
    val dailyScanTimeMinute: Int = 0,
    val realTimeAlertsEnabled: Boolean = true,
    val spamProtectionEnabled: Boolean = true,
    val linkProtectionEnabled: Boolean = true,
    val smsScanningEnabled: Boolean = true,
    val callScreeningEnabled: Boolean = true,
    val clipboardProtectionEnabled: Boolean = true,
    val emergencyLockdownMode: Boolean = false,
    val autoQuarantineHighRisk: Boolean = false
)

@Entity(tableName = "security_score_history")
data class SecurityScoreHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val score: Int,
    val criticalCount: Int,
    val warningCount: Int,
    val timestamp: Long = System.currentTimeMillis()
)
