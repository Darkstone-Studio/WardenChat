package com.example.wardenchat.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey
    val peerId: String,
    val addedAt: Long = System.currentTimeMillis(),
    val lastMessage: String? = null,
    val lastMessageTime: Long? = null
)
