package com.androidassistant.agent.engine.safety

import com.androidassistant.core.model.ApprovalRequest
import com.androidassistant.core.model.ApprovalResult
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel

interface ApprovalCallback {
    suspend fun requestApproval(request: ApprovalRequest): ApprovalResult
    fun getApprovalChannel(): ReceiveChannel<ApprovalRequest>
}

class DefaultApprovalCallback : ApprovalCallback {
    private val approvalChannel = Channel<ApprovalRequest>()
    private val pendingResponses = mutableMapOf<String, kotlin.coroutines.CompletableDeferred<ApprovalResult>>()

    override suspend fun requestApproval(request: ApprovalRequest): ApprovalResult {
        approvalChannel.send(request)

        val deferred = kotlinx.coroutines.CompletableDeferred<ApprovalResult>()
        pendingResponses[request.id] = deferred

        return deferred.await()
    }

    fun respond(requestId: String, result: ApprovalResult) {
        pendingResponses.remove(requestId)?.complete(result)
    }

    override fun getApprovalChannel(): ReceiveChannel<ApprovalRequest> {
        return approvalChannel
    }
}