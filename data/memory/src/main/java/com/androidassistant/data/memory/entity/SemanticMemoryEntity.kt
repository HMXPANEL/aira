package com.androidassistant.data.memory.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "semantic_memories",
    indices = [
        Index("importance"),
        Index("category"),
        Index("created_at")
    ]
)
data class SemanticMemoryEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "content")
    val content: String,

    @ColumnInfo(name = "embedding")
    val embedding: ByteArray,

    @ColumnInfo(name = "category")
    val category: String,

    @ColumnInfo(name = "source")
    val source: String,

    @ColumnInfo(name = "importance")
    val importance: Int,

    @ColumnInfo(name = "access_count")
    val accessCount: Int = 0,

    @ColumnInfo(name = "last_accessed")
    val lastAccessed: Long = 0,

    @ColumnInfo(name = "metadata_json")
    val metadataJson: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long
)
