package com.androidassistant.core.model

data class Session(
    val id: String,
    val createdAt: Long,
    val updatedAt: Long,
    val title: String,
    val messageCount: Int = 0,
    val isActive: Boolean = true
)
