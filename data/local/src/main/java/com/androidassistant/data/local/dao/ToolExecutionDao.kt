package com.androidassistant.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.androidassistant.data.local.entity.ToolExecutionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ToolExecutionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(execution: ToolExecutionEntity)

    @Query("SELECT * FROM tool_executions ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentExecutions(limit: Int = 50): Flow<List<ToolExecutionEntity>>

    @Query("SELECT * FROM tool_executions WHERE session_id = :sessionId ORDER BY timestamp ASC")
    fun getSessionExecutions(sessionId: String): Flow<List<ToolExecutionEntity>>

    @Query("SELECT * FROM tool_executions WHERE tool_name = :toolName ORDER BY timestamp DESC LIMIT 10")
    suspend fun getRecentByTool(toolName: String): List<ToolExecutionEntity>

    @Query("DELETE FROM tool_executions WHERE timestamp < :before")
    suspend fun deleteOlderThan(before: Long)
}
