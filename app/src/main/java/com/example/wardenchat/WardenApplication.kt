package com.example.wardenchat

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.wardenchat.data.AppConstants
import com.example.wardenchat.data.MailboxCheckWorker
import java.util.concurrent.TimeUnit

class WardenApplication : Application() {

    companion object {
        private const val TAG = "WardenChat"
    }

    override fun onCreate() {
        super.onCreate()

        // Reset foreground flag on app startup
        val prefs = getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(AppConstants.KEY_IS_FOREGROUND, false).apply()
        Log.d(TAG, "WardenApplication: isAppInForeground false olarak sıfırlandı.")

        createNotificationChannel()
        scheduleMailboxCheckWorker()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                AppConstants.NOTIFICATION_CHANNEL_ID,
                AppConstants.NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Gelen yeni mesajlar için arka plan bildirimleri"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
            Log.d(TAG, "WardenApplication: NotificationChannel oluşturuldu.")
        }
    }

    private fun scheduleMailboxCheckWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicWorkRequest = PeriodicWorkRequestBuilder<MailboxCheckWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            AppConstants.WORKER_UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWorkRequest
        )
        Log.d(TAG, "WardenApplication: Periyodik MailboxCheckWorker (15dk) başarıyla kuyruğa eklendi.")
    }
}
