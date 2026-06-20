package com.androidassistant.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.androidassistant.data.local.entity.ConversationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: ConversationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<ConversationEntity>)

    @Query("SELECT * FROM conversations WHERE session_id = :sessionId ORDER BY timestamp ASC")
    fun getSessionMessages(sessionId: String): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE session_id = :sessionId ORDER BY timestamp ASC LIMIT :limit")
    suspend fun getRecentMessages(sessionId: String, limit: Int): List<ConversationEntity>

    @Query("DELETE FROM conversations WHERE session_id = :sessionId")
    suspend fun deleteSessionMessages(sessionId: String)

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteMessage(id: String)
}
