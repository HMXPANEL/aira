package com.androidassistant.agent.engine.safety

import com.androidassistant.core.model.ApprovalRequest
import com.androidassistant.core.model.ApprovalResult

interface ApprovalCallback {
    suspend fun requestApproval(request: ApprovalRequest): ApprovalResult
}