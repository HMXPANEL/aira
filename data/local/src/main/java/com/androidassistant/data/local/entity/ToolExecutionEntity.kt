package com.androidassistant.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tool_executions",
    indices = [Index("timestamp")]
)
data class ToolExecutionEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "session_id")
    val sessionId: String,

    @ColumnInfo(name = "tool_name")
    val toolName: String,

    @ColumnInfo(name = "args_json")
    val argsJson: String,

    @ColumnInfo(name = "result_json")
    val resultJson: String?,

    @ColumnInfo(name = "status")
    val status: String,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "duration_ms")
    val durationMs: Long = 0
)
