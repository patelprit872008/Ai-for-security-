package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        ScanResultEntity::class,
        QuarantineItemEntity::class,
        InstalledAppEntity::class,
        PhoneReportEntity::class,
        UrlReportEntity::class,
        EmailIntegrationEntity::class,
        SecurityNotificationEntity::class,
        AiMessageEntity::class,
        UserSettingsEntity::class,
        SecurityScoreHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun scanResultDao(): ScanResultDao
    abstract fun quarantineDao(): QuarantineDao
    abstract fun installedAppDao(): InstalledAppDao
    abstract fun phoneReportDao(): PhoneReportDao
    abstract fun urlReportDao(): UrlReportDao
    abstract fun emailIntegrationDao(): EmailIntegrationDao
    abstract fun notificationDao(): NotificationDao
    abstract fun aiChatDao(): AiChatDao
    abstract fun userSettingsDao(): UserSettingsDao
    abstract fun securityScoreHistoryDao(): SecurityScoreHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "shield_ai_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
