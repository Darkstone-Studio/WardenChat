package com.example.wardenchat.data

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.wardenchat.MainActivity
import com.example.wardenchat.R
import com.example.wardenchat.ui.LinkUpViewModel
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first

class MailboxCheckWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "WardenChat"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "MailboxCheckWorker başladı.")

        val userPrefsRepo = UserPreferencesRepository(appContext)
        val myId = userPrefsRepo.myIdFlow.first() ?: ""
        Log.d(TAG, "Worker okunan kimlik kodu (myId): '$myId'")

        val prefs = appContext.getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE)
        val isForeground = prefs.getBoolean(AppConstants.KEY_IS_FOREGROUND, false)
        val force = inputData.getBoolean("force", false)
        Log.d(TAG, "Worker isAppInForeground: $isForeground, force: $force")

        if (myId.isBlank()) {
            Log.d(TAG, "Worker: myId boş, görev sonlandırılıyor.")
            if (force) {
                showTestNotification()
            }
            return Result.success()
        }

        try {
            val auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser
            Log.d(TAG, "Worker FirebaseAuth.currentUser durumu: ${currentUser?.uid}")

            if (currentUser == null) {
                Log.d(TAG, "Worker: Kullanıcı oturum açmamış, signInAnonymously() çağrılıyor...")
                Tasks.await(auth.signInAnonymously())
                Log.d(TAG, "Worker: signInAnonymously() tamamlandı, UID: ${auth.currentUser?.uid}")
            }

            val firestore = FirebaseFirestore.getInstance()
            Log.d(TAG, "Worker: Firestore sorgulanıyor -> mailbox/$myId/messages")
            val queryTask = firestore.collection("mailbox")
                .document(myId)
                .collection("messages")
                .get()

            val snapshot = try {
                Tasks.await(queryTask)
            } catch (e: Exception) {
                Log.e(TAG, "Worker Hata (Firestore query): ${e.message}", e)
                null
            }

            val messages = snapshot?.documents.orEmpty()
            val msgCount = messages.size
            Log.d(TAG, "Worker: Sorgudan dönen mesaj sayısı: $msgCount")

            val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    appContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
            Log.d(TAG, "Worker: POST_NOTIFICATIONS izni var mı? $hasPermission")

            if (!isForeground || force) {
                if (hasPermission) {
                    if (msgCount > 0) {
                        val rawSender = messages.firstOrNull()?.getString("senderName")
                        val sanitizedSender = LinkUpViewModel.sanitizeSenderName(rawSender)
                        val fallbackSender = messages.firstOrNull()?.getString("senderId") ?: "Bilinmeyen"
                        val mainSender = sanitizedSender ?: fallbackSender

                        showNotification(count = msgCount, senderId = mainSender)
                        Log.d(TAG, "Worker: Bildirim başarıyla gönderildi (gönderen: $mainSender, adet: $msgCount)")

                        // Delete queried messages from Firestore (ephemeral delivery)
                        for (doc in messages) {
                            doc.reference.delete()
                                .addOnSuccessListener { Log.d(TAG, "Worker: Mesaj dokümanı silindi: ${doc.id}") }
                                .addOnFailureListener { err -> Log.e(TAG, "Worker: Mesaj dokümanı silinemedi: ${err.message}", err) }
                        }
                    } else if (force) {
                        // If forced and mailbox is empty or query failed, show test notification anyway
                        showTestNotification()
                        Log.d(TAG, "Worker: Force testi aktif olduğu için örnek test bildirimi gösterildi.")
                    }
                } else {
                    Log.e(TAG, "Worker Hata: POST_NOTIFICATIONS izni verilmemiş, bildirim gösterilemedi!")
                }
            } else {
                Log.d(TAG, "Worker: Uygulama ön planda (isForeground=true), bildirim bastırıldı.")
            }

            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Worker Hata (doWork): ${e.message}", e)
            if (force) {
                showTestNotification()
            }
            return Result.retry()
        }
    }

    private fun showNotification(count: Int, senderId: String) {
        val notificationManager = NotificationManagerCompat.from(appContext)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                AppConstants.NOTIFICATION_CHANNEL_ID,
                AppConstants.NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Gelen yeni mesajlar için arka plan bildirimleri"
            }
            notificationManager.createNotificationChannel(channel)
        }

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
            notificationManager.notify(1001, builder.build())
            Log.d(TAG, "NotificationManagerCompat.notify() çağrıldı.")
        } catch (e: SecurityException) {
            Log.e(TAG, "Notification SecurityException: ${e.message}", e)
        }
    }

    private fun showTestNotification() {
        val notificationManager = NotificationManagerCompat.from(appContext)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                AppConstants.NOTIFICATION_CHANNEL_ID,
                AppConstants.NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Gelen yeni mesajlar için arka plan bildirimleri"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            appContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(appContext, AppConstants.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Test")
            .setContentText("Warden Chat bildirimleri çalışıyor")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            notificationManager.notify(1002, builder.build())
            Log.d(TAG, "Test NotificationManagerCompat.notify() çağrıldı.")
        } catch (e: SecurityException) {
            Log.e(TAG, "Test Notification SecurityException: ${e.message}", e)
        }
    }
}
