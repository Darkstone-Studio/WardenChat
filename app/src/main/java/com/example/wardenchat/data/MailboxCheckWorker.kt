package com.example.wardenchat.data

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.wardenchat.MainActivity
import com.example.wardenchat.R
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first

class MailboxCheckWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val userPrefsRepo = UserPreferencesRepository(appContext)
        val myId = userPrefsRepo.myIdFlow.first() ?: ""

        val prefs = appContext.getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE)
        val isForeground = prefs.getBoolean(AppConstants.KEY_IS_FOREGROUND, false)

        if (myId.isBlank()) {
            return Result.success()
        }

        try {
            val firestore = FirebaseFirestore.getInstance()
            val queryTask = firestore.collection("mailbox")
                .document(myId)
                .collection("messages")
                .get()

            val snapshot = Tasks.await(queryTask)
            if (snapshot != null && !snapshot.isEmpty) {
                val messages = snapshot.documents
                val count = messages.size
                val senders = messages.mapNotNull { it.getString("senderId") }.distinct()
                val mainSender = senders.firstOrNull() ?: "Bilinmeyen"

                if (!isForeground) {
                    showNotification(count = count, senderId = mainSender)
                }

                // Delete queried messages from Firestore (ephemeral delivery)
                for (doc in messages) {
                    doc.reference.delete()
                }
            }
            return Result.success()
        } catch (e: Exception) {
            return Result.retry()
        }
    }

    private fun showNotification(count: Int, senderId: String) {
        val intent = Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("senderId", senderId)
        }

        val pendingIntent = PendingIntent.getActivity(
            appContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentTitle = "Warden Chat"
        val contentText = if (count == 1) {
            "Yeni mesaj: $senderId"
        } else {
            "$count yeni mesajınız var"
        }

        val builder = NotificationCompat.Builder(appContext, AppConstants.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            val notificationManager = NotificationManagerCompat.from(appContext)
            notificationManager.notify(1001, builder.build())
        } catch (e: SecurityException) {
            // Permission missing or denied
        }
    }
}
