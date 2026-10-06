package com.example.wardenchat.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts ORDER BY COALESCE(lastMessageTime, addedAt) DESC")
    fun getAllContactsFlow(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts")
    suspend fun getAllContacts(): List<ContactEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertContact(contact: ContactEntity)

    @Query("UPDATE contacts SET lastMessage = :message, lastMessageTime = :time WHERE peerId = :peerId")
    suspend fun updateLastMessage(peerId: String, message: String, time: Long)

    @Query("DELETE FROM contacts WHERE peerId = :peerId")
    suspend fun deleteContactByPeerId(peerId: String)
}
