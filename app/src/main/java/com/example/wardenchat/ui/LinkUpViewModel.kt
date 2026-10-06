package com.example.wardenchat.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wardenchat.data.ChatMessage
import com.example.wardenchat.data.ConnectionStatus
import com.example.wardenchat.data.MailboxRepository
import com.example.wardenchat.data.UserPreferencesRepository
import com.example.wardenchat.data.room.ContactEntity
import com.example.wardenchat.data.room.WardenDatabase
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

class LinkUpViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        // Pool: Uppercase letters + numbers, excluding O, I, 0, 1
        private const val CHAR_POOL = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"
        private val VALID_CODE_REGEX = Regex("^[2-9A-HJ-NP-Z]{3}-[2-9A-HJ-NP-Z]{3}-[2-9A-HJ-NP-Z]{3}$")
    }

    private val repository = MailboxRepository(application)
    private val userPrefsRepo = UserPreferencesRepository(application)
    private val contactDao = WardenDatabase.getDatabase(application).contactDao()

    private var mailboxListenerRegistration: ListenerRegistration? = null
    private var isOnline: Boolean = true

    private val _myId = MutableStateFlow("")
    val myId: StateFlow<String> = _myId.asStateFlow()

    // Persistent contacts list from Room database
    val contacts: StateFlow<List<ContactEntity>> = contactDao.getAllContactsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Map of peerId -> List of ChatMessages (in-memory for active session)
    private val _conversations = MutableStateFlow<Map<String, List<ChatMessage>>>(emptyMap())
    val conversations: StateFlow<Map<String, List<ChatMessage>>> = _conversations.asStateFlow()

    private val _connectionStatus = MutableStateFlow(ConnectionStatus.READY)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val _selfDestructEnabled = MutableStateFlow(false)
    val selfDestructEnabled: StateFlow<Boolean> = _selfDestructEnabled.asStateFlow()

    private val _uiEvent = MutableSharedFlow<String>()
    val uiEvent: SharedFlow<String> = _uiEvent.asSharedFlow()

    init {
        observeNetworkConnectivity()
        loadOrGenerateId()
    }

    private fun observeNetworkConnectivity() {
        viewModelScope.launch {
            repository.observeNetworkConnectivity().collect { online ->
                isOnline = online
                _connectionStatus.value = if (online) ConnectionStatus.READY else ConnectionStatus.OFFLINE
                if (!online) {
                    _uiEvent.emit("İnternet bağlantısı yok")
                }
            }
        }
    }

    /**
     * Loads saved ID from DataStore. If none exists, generates a new random 9-character ID,
     * saves it to DataStore, and starts listening to the mailbox.
     */
    private fun loadOrGenerateId() {
        viewModelScope.launch {
            val savedId = userPrefsRepo.myIdFlow.first()
            if (!savedId.isNullOrBlank() && VALID_CODE_REGEX.matches(savedId)) {
                _myId.value = savedId
                startMailboxListener(savedId)
            } else {
                createNewRandomIdAndSave()
            }
        }
    }

    private suspend fun createNewRandomIdAndSave() {
        val sb = StringBuilder()
        for (i in 0 until 9) {
            val randomChar = CHAR_POOL[Random.nextInt(CHAR_POOL.length)]
            sb.append(randomChar)
            if (i == 2 || i == 5) {
                sb.append("-")
            }
        }
        val newId = sb.toString()
        _myId.value = newId
        _conversations.value = emptyMap()
        _connectionStatus.value = if (isOnline) ConnectionStatus.READY else ConnectionStatus.OFFLINE

        userPrefsRepo.saveMyId(newId)
        startMailboxListener(newId)
    }

    /**
     * Generates a new random 9-character ID, clears old mailbox messages in Firestore,
     * updates DataStore and starts listening to the new mailbox.
     */
    fun generateNewId() {
        viewModelScope.launch {
            val oldId = _myId.value

            // Stop previous listener and clear old mailbox
            mailboxListenerRegistration?.remove()
            if (oldId.isNotEmpty()) {
                repository.clearMailbox(oldId)
            }

            createNewRandomIdAndSave()
        }
    }

    fun toggleSelfDestruct(enabled: Boolean) {
        _selfDestructEnabled.value = enabled
    }

    fun removeMessageById(peerId: String, messageId: String) {
        val currentMap = _conversations.value.toMutableMap()
        val list = currentMap[peerId].orEmpty().filterNot { it.id == messageId }
        if (list.isEmpty()) {
            currentMap.remove(peerId)
        } else {
            currentMap[peerId] = list
        }
        _conversations.value = currentMap
    }

    private fun scheduleSelfDestructIfNeeded(peerId: String, messageId: String) {
        if (_selfDestructEnabled.value) {
            viewModelScope.launch {
                kotlinx.coroutines.delay(5000)
                removeMessageById(peerId, messageId)
            }
        }
    }

    private fun startMailboxListener(id: String) {
        mailboxListenerRegistration?.remove()
        mailboxListenerRegistration = repository.listenToMailbox(
            myId = id,
            onMessageReceived = { senderId, text, timestamp ->
                val incomingMessage = ChatMessage(
                    text = text,
                    timestamp = timestamp,
                    isMine = false
                )
                val currentMap = _conversations.value.toMutableMap()
                val peerMessages = currentMap[senderId].orEmpty().toMutableList()
                peerMessages.add(incomingMessage)
                currentMap[senderId] = peerMessages
                _conversations.value = currentMap

                viewModelScope.launch {
                    contactDao.insertContact(ContactEntity(peerId = senderId))
                    contactDao.updateLastMessage(senderId, text, timestamp)
                }

                scheduleSelfDestructIfNeeded(senderId, incomingMessage.id)
            },
            onError = {
                viewModelScope.launch {
                    _uiEvent.emit("Mesaj kutusu dinlenirken hata oluştu")
                }
            }
        )
    }

    /**
     * Validates and registers a peer ID to start a conversation (persisted in Room).
     * Returns true if valid format, false otherwise.
     */
    fun startChatWithPeer(code: String): Boolean {
        val formattedCode = code.trim().uppercase()
        if (!VALID_CODE_REGEX.matches(formattedCode)) {
            return false
        }
        if (formattedCode == _myId.value) {
            viewModelScope.launch {
                _uiEvent.emit("Kendi kimliğinize mesaj gönderemezsiniz")
            }
            return false
        }

        viewModelScope.launch {
            contactDao.insertContact(ContactEntity(peerId = formattedCode))
        }
        return true
    }

    /**
     * Appends message locally (optimistic UI) and pushes to recipient mailbox in Firestore.
     */
    fun sendMessage(recipientId: String, text: String) {
        val trimmedText = text.trim()
        if (trimmedText.isEmpty()) return

        val newMessage = ChatMessage(
            text = trimmedText,
            timestamp = System.currentTimeMillis(),
            isMine = true
        )
        val currentMap = _conversations.value.toMutableMap()
        val peerMessages = currentMap[recipientId].orEmpty().toMutableList()
        peerMessages.add(newMessage)
        currentMap[recipientId] = peerMessages
        _conversations.value = currentMap

        viewModelScope.launch {
            contactDao.insertContact(ContactEntity(peerId = recipientId))
            contactDao.updateLastMessage(recipientId, trimmedText, newMessage.timestamp)
        }

        scheduleSelfDestructIfNeeded(recipientId, newMessage.id)

        if (!isOnline) {
            viewModelScope.launch {
                _uiEvent.emit("İnternet bağlantısı yok, mesaj gönderilemedi")
            }
            return
        }

        repository.sendMessage(
            recipientId = recipientId,
            senderId = _myId.value,
            text = trimmedText,
            onSuccess = {
                // Sent successfully
            },
            onError = { e ->
                viewModelScope.launch {
                    _uiEvent.emit(e.message ?: "Mesaj yazılamadı")
                }
            }
        )
    }

    fun formatConnectInput(input: String): String {
        val cleaned = input.uppercase().filter { it in CHAR_POOL }
        val limited = if (cleaned.length > 9) cleaned.substring(0, 9) else cleaned

        val result = StringBuilder()
        for (i in limited.indices) {
            if (i == 3 || i == 6) {
                result.append('-')
            }
            result.append(limited[i])
        }
        return result.toString()
    }

    fun deleteContact(peerId: String) {
        viewModelScope.launch {
            contactDao.deleteContactByPeerId(peerId)
        }
        val currentMap = _conversations.value.toMutableMap()
        currentMap.remove(peerId)
        _conversations.value = currentMap
    }

    override fun onCleared() {
        super.onCleared()
        mailboxListenerRegistration?.remove()
    }
}
