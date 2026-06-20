package com.androidassistant.core.model

import com.androidassistant.tool.registry.ToolPermissionLevel

data class ApprovalRequest(
    val id: String,
    val toolName: String,
    val toolDescription: String,
    val args: Map<String, Any>,
    val permissionLevel: ToolPermissionLevel,
    val explanation: String,
    val timestamp: Long = System.currentTimeMillis()
)

sealed class ApprovalResult {
    data class Approved(val persist: Boolean = false) : ApprovalResult()
    data class ApprovedWithModification(val modifiedArgs: Map<String, Any>) : ApprovalResult()
    data class Denied(val reason: String?) : ApprovalResult()
    object Expired : ApprovalResult()
}