package com.example.wardenchat.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class MailboxRepository(context: Context) {

    companion object {
        private const val MAX_MAILBOX_SIZE = 50
    }

    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val connectivityManager =
        context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    /**
     * Ensures Firebase Anonymous Authentication is complete.
     */
    suspend fun ensureAnonymousAuth() {
        if (auth.currentUser == null) {
            try {
                Tasks.await(auth.signInAnonymously())
                Log.d("WardenChat", "Firebase Anonymous Auth başarılı: ${auth.currentUser?.uid}")
            } catch (e: Exception) {
                Log.e("WardenChat", "Firebase Anonymous Auth hatası: ${e.message}", e)
            }
        }
    }

    /**
     * Saves identity mapping: identity_map/{peerId} -> { ownerUid: String }
     */
    suspend fun saveIdentityMapping(peerId: String) {
        ensureAnonymousAuth()
        val uid = auth.currentUser?.uid ?: return
        val mapData = hashMapOf("ownerUid" to uid)
        try {
            Tasks.await(
                firestore.collection("identity_map")
                    .document(peerId)
                    .set(mapData)
            )
            Log.d("WardenChat", "identity_map yazıldı: $peerId")
        } catch (e: Exception) {
            Log.e("WardenChat", "identity_map yazılamadı", e)
        }
    }

    /**
     * Clears identity mapping document for oldId.
     */
    suspend fun clearIdentityMapping(peerId: String) {
        if (peerId.isBlank()) return
        try {
            Tasks.await(
                firestore.collection("identity_map")
                    .document(peerId)
                    .delete()
            )
            Log.d("WardenChat", "Identity mapping silindi: $peerId")
        } catch (e: Exception) {
            Log.e("WardenChat", "Identity mapping silinemedi: ${e.message}", e)
        }
    }

    /**
     * Sends a message to the recipient's mailbox in Firestore.
     * Path: mailbox/{recipientId}/messages
     * Enforces MAX_MAILBOX_SIZE (50) limit via FIFO deletion before adding new message.
     */
    fun sendMessage(
        recipientId: String,
        senderId: String,
        text: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit,
    ) {
        Log.d("WardenChat", "Gönderiliyor: alıcı=$recipientId, metin=$text")

        val messagesRef = firestore.collection("mailbox")
            .document(recipientId)
            .collection("messages")

        val messageMap = hashMapOf(
            "text" to text,
            "senderId" to senderId,
            "timestamp" to System.currentTimeMillis(),
            "delivered" to false
        )

        messagesRef.orderBy("timestamp", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val docs = snapshot.documents
                if (docs.size >= MAX_MAILBOX_SIZE) {
                    val excessCount = (docs.size - MAX_MAILBOX_SIZE) + 1
                    for (i in 0 until excessCount) {
                        docs[i].reference.delete()
                    }
                }

                messagesRef.add(messageMap)
                    .addOnSuccessListener { docRef ->
                        Log.d("WardenChat", "Mesaj yazıldı: ${docRef.id}")
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        Log.e("WardenChat", "Mesaj yazılamadı", e)
                        onError(e)
                    }
            }
            .addOnFailureListener { queryEx ->
                Log.w("WardenChat", "FIFO limit kontrolü başarısız oldu, doğrudan yazılıyor: ${queryEx.message}")
                messagesRef.add(messageMap)
                    .addOnSuccessListener { docRef ->
                        Log.d("WardenChat", "Mesaj yazıldı: ${docRef.id}")
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        Log.e("WardenChat", "Mesaj yazılamadı", e)
                        onError(e)
                    }
            }
    }

    /**
     * Listens for incoming messages in my mailbox.
     * Path: mailbox/{myId}/messages
     * Deletes each document immediately upon receipt so no trace is left on the server.
     */
    fun listenToMailbox(
        myId: String,
        onMessageReceived: (senderId: String, text: String, timestamp: Long) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        Log.d("WardenChat", "Dinleniyor: mailbox/$myId/messages")

        return firestore.collection("mailbox")
            .document(myId)
            .collection("messages")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("WardenChat", "Listener hatası", error)
                    onError(error)
                    return@addSnapshotListener
                }

                Log.d("WardenChat", "Snapshot geldi, değişiklik sayısı: ${snapshot?.documentChanges?.size}")

                if (snapshot != null) {
                    for (dc in snapshot.documentChanges) {
                        if (dc.type == DocumentChange.Type.ADDED) {
                            val text = dc.document.getString("text") ?: ""
                            val senderId = dc.document.getString("senderId") ?: ""
                            val timestamp = dc.document.getLong("timestamp") ?: System.currentTimeMillis()

                            Log.d("WardenChat", "Yeni mesaj alındı (ADDED)! Gönderen: $senderId, İçerik: $text")

                            // Immediately delete from Firestore after reading
                            dc.document.reference.delete()
                                .addOnSuccessListener { Log.d("WardenChat", "Okunan mesaj Firestore'dan silindi: ${dc.document.id}") }
                                .addOnFailureListener { delErr -> Log.e("WardenChat", "Okunan mesaj silinemedi", delErr) }

                            if (text.isNotEmpty() && senderId.isNotEmpty()) {
                                onMessageReceived(senderId, text, timestamp)
                            }
                        }
                    }
                }
            }
    }

    /**
     * Clears any leftover messages in the mailbox for oldId when identity changes.
     */
    fun clearMailbox(myId: String, onComplete: (() -> Unit)? = null) {
        if (myId.isBlank()) {
            onComplete?.invoke()
            return
        }

        Log.d("WardenChat", "Mailbox temizleniyor... myId: $myId")
        firestore.collection("mailbox")
            .document(myId)
            .collection("messages")
            .get()
            .addOnSuccessListener { snapshot ->
                for (doc in snapshot.documents) {
                    doc.reference.delete()
                }
                Log.d("WardenChat", "Mailbox başarıyla temizlendi: $myId")
                onComplete?.invoke()
            }
            .addOnFailureListener { e ->
                Log.e("WardenChat", "Mailbox temizleme hatası", e)
                onComplete?.invoke()
            }
    }

    /**
     * Observes real-time network connectivity status.
     */
    fun observeNetworkConnectivity(): Flow<Boolean> = callbackFlow {
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d("WardenChat", "Ağ bağlantısı mevcut")
                trySend(true)
            }

            override fun onLost(network: Network) {
                Log.d("WardenChat", "Ağ bağlantısı koptu")
                trySend(false)
            }
        }

        val activeNetwork = connectivityManager.activeNetwork
        val hasInternet = activeNetwork != null &&
                connectivityManager.getNetworkCapabilities(activeNetwork)
                    ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        Log.d("WardenChat", "İlk ağ durumu: $hasInternet")
        trySend(hasInternet)

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, networkCallback)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }
}
