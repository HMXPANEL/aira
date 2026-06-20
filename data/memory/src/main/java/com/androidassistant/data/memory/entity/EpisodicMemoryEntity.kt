package com.androidassistant.data.memory.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "episodic_memories",
    indices = [
        Index("timestamp"),
        Index("importance"),
        Index("session_id")
    ]
)
data class EpisodicMemoryEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "session_id")
    val sessionId: String,

    @ColumnInfo(name = "summary")
    val summary: String,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "importance")
    val importance: Int,

    @ColumnInfo(name = "token_count")
    val tokenCount: Int = 0,

    @ColumnInfo(name = "entities_json")
    val entitiesJson: String? = null,

    @ColumnInfo(name = "tool_calls_json")
    val toolCallsJson: String? = null
)
