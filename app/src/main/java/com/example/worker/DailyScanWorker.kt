package com.example.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.AppDatabase
import com.example.data.ScanResultEntity
import com.example.data.SecurityNotificationEntity
import com.example.security.ApkSecurityScanner
import com.example.security.NetworkSecurityScanner

class DailyScanWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getInstance(appContext)
        val scanDao = database.scanResultDao()
        val appDao = database.installedAppDao()
        val notifDao = database.notificationDao()

        try {
            // 1. Scan Installed Apps & Permissions
            val apps = ApkSecurityScanner.scanInstalledApps(appContext)
            appDao.clearApps()
            appDao.insertApps(apps)

            val highRiskApps = apps.filter { it.riskLevel == "HIGH" || it.riskLevel == "CRITICAL" }

            for (app in highRiskApps) {
                scanDao.insertScanResult(
                    ScanResultEntity(
                        scanType = "APK",
                        target = app.appName,
                        threatLevel = app.riskLevel,
                        riskScore = app.riskScore,
                        summaryReason = "Suspicious application permissions or accessibility usage detected.",
                        technicalDetailsJson = app.riskReasonsJson
                    )
                )
            }

            // 2. Scan Network
            val netResult = NetworkSecurityScanner.scanNetwork(appContext)
            if (netResult.threatLevel == "HIGH" || netResult.threatLevel == "CRITICAL") {
                scanDao.insertScanResult(
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

            // 3. Post Notification
            val threatCount = highRiskApps.size
            val notificationTitle = if (threatCount > 0) "⚠️ ShieldAI Alert: $threatCount Threat(s) Found" else "✓ Daily Security Scan Complete"
            val notificationMessage = if (threatCount > 0) "Critical permissions or suspicious APKs detected. Open ShieldAI to review." else "All installed apps and network conditions are safe."

            notifDao.insertNotification(
                SecurityNotificationEntity(
                    title = notificationTitle,
                    message = notificationMessage,
                    severity = if (threatCount > 0) "HIGH" else "INFO",
                    category = "SCAN_RESULT"
                )
            )

            sendSystemNotification(appContext, notificationTitle, notificationMessage)

            return Result.success()
        } catch (e: Exception) {
            return Result.retry()
        }
    }

    private fun sendSystemNotification(context: Context, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "shield_ai_alerts"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "ShieldAI Security Alerts", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        notificationManager.notify(1001, builder.build())
    }
}
