package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users LIMIT 1")
    fun getCurrentUser(): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("DELETE FROM users")
    suspend fun clearUsers()
}

@Dao
interface ScanResultDao {
    @Query("SELECT * FROM scan_results ORDER BY timestamp DESC")
    fun getAllScanResults(): Flow<List<ScanResultEntity>>

    @Query("SELECT * FROM scan_results WHERE threatLevel IN ('HIGH', 'CRITICAL') ORDER BY timestamp DESC")
    fun getCriticalThreats(): Flow<List<ScanResultEntity>>

    @Query("SELECT * FROM scan_results WHERE id = :id LIMIT 1")
    suspend fun getScanResultById(id: Long): ScanResultEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScanResult(result: ScanResultEntity): Long

    @Query("UPDATE scan_results SET actionTaken = :action, isQuarantined = :isQuarantined WHERE id = :id")
    suspend fun updateScanAction(id: Long, action: String, isQuarantined: Boolean)

    @Query("DELETE FROM scan_results WHERE id = :id")
    suspend fun deleteScanResult(id: Long)

    @Query("DELETE FROM scan_results")
    suspend fun clearScanResults()
}

@Dao
interface QuarantineDao {
    @Query("SELECT * FROM quarantine_items ORDER BY quarantinedAt DESC")
    fun getAllQuarantineItems(): Flow<List<QuarantineItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuarantineItem(item: QuarantineItemEntity): Long

    @Query("DELETE FROM quarantine_items WHERE id = :id")
    suspend fun deleteQuarantineItem(id: Long)
}

@Dao
interface InstalledAppDao {
    @Query("SELECT * FROM installed_apps ORDER BY riskScore DESC")
    fun getAllInstalledApps(): Flow<List<InstalledAppEntity>>

    @Query("SELECT * FROM installed_apps WHERE riskLevel IN ('HIGH', 'CRITICAL') ORDER BY riskScore DESC")
    fun getHighRiskApps(): Flow<List<InstalledAppEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApps(apps: List<InstalledAppEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApp(app: InstalledAppEntity)

    @Query("DELETE FROM installed_apps")
    suspend fun clearApps()
}

@Dao
interface PhoneReportDao {
    @Query("SELECT * FROM phone_reports WHERE phoneNumber = :number LIMIT 1")
    suspend fun getPhoneReport(number: String): PhoneReportEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoneReport(report: PhoneReportEntity)
}

@Dao
interface UrlReportDao {
    @Query("SELECT * FROM url_reports WHERE url = :url LIMIT 1")
    suspend fun getUrlReport(url: String): UrlReportEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUrlReport(report: UrlReportEntity)
}

@Dao
interface EmailIntegrationDao {
    @Query("SELECT * FROM email_integrations")
    fun getConnectedEmails(): Flow<List<EmailIntegrationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIntegration(integration: EmailIntegrationEntity)

    @Query("DELETE FROM email_integrations WHERE email = :email")
    suspend fun deleteIntegration(email: String)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM security_notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<SecurityNotificationEntity>>

    @Query("SELECT COUNT(*) FROM security_notifications WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: SecurityNotificationEntity)

    @Query("UPDATE security_notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("UPDATE security_notifications SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("DELETE FROM security_notifications")
    suspend fun clearNotifications()
}

@Dao
interface AiChatDao {
    @Query("SELECT * FROM ai_messages ORDER BY timestamp ASC")
    fun getAllAiMessages(): Flow<List<AiMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAiMessage(message: AiMessageEntity)

    @Query("DELETE FROM ai_messages")
    suspend fun clearMessages()
}

@Dao
interface UserSettingsDao {
    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    fun getUserSettings(): Flow<UserSettingsEntity?>

    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    suspend fun getUserSettingsDirect(): UserSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateUserSettings(settings: UserSettingsEntity)
}

@Dao
interface SecurityScoreHistoryDao {
    @Query("SELECT * FROM security_score_history ORDER BY timestamp DESC LIMIT 30")
    fun getScoreHistory(): Flow<List<SecurityScoreHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScoreRecord(record: SecurityScoreHistoryEntity)
}
