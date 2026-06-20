package com.androidassistant.ui.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.androidassistant.core.model.ApprovalRequest
import com.androidassistant.core.model.ApprovalResult
import com.androidassistant.tool.registry.ToolPermissionLevel

@Composable
fun ApprovalDialog(
    request: ApprovalRequest?,
    onApprove: (Boolean, Map<String, Any>?) -> Unit,
    onDismiss: () -> Unit
) {
    val request = request ?: return

    var showDetails by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { onApprove(false, null); onDismiss() },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Approval required",
                    tint = MaterialTheme.colorScheme.primary
                )
                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.padding(horizontal = 8.dp))
                Text(
                    text = "Permission Required",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = request.explanation,
                    style = MaterialTheme.typography.bodyLarge
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = androidx.compose.material3.CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Tool: ${request.toolName}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            PermissionBadge(level = request.permissionLevel)
                        }

                        if (showDetails) {
                            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.padding(vertical = 8.dp))
                            Text(
                                text = "Arguments: ${formatArgs(request.args)}",
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 5,
                                overflow = androidx.compose.ui.text.TextOverflow.Ellipsis
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.end
                        ) {
                            TextButton(onClick = { showDetails = !showDetails }) {
                                Text(if (showDetails) "Hide Details" else "Show Details")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.end
            ) {
                TextButton(onClick = { onApprove(false, null); onDismiss() }) {
                    Text("Deny")
                }
                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.padding(horizontal = 8.dp))
                Button(onClick = { onApprove(true, null); onDismiss() }) {
                    Text("Allow")
                }
            }
        },
        dismissButton = null
    )
}

@Composable
private fun PermissionBadge(level: ToolPermissionLevel) {
    val (text, color) = when (level) {
        ToolPermissionLevel.NONE -> "None" to MaterialTheme.colorScheme.primary
        ToolPermissionLevel.NORMAL -> "Normal" to MaterialTheme.colorScheme.secondary
        ToolPermissionLevel.SENSITIVE -> "Sensitive" to Color(0xFFFF9800)
        ToolPermissionLevel.CRITICAL -> "Critical" to MaterialTheme.colorScheme.error
        ToolPermissionLevel.BLOCKED -> "Blocked" to MaterialTheme.colorScheme.onError
    }

    androidx.compose.material3.Chip(
        modifier = Modifier.padding(horizontal = 4.dp),
        onClick = {},
        colors = androidx.material3.ChipDefaults.chipColors(
            containerColor = color.copy(alpha = 0.15f)
        )
    ) {
        Text(text = text, color = color, fontSize = 12.sp)
    }
}

private fun formatArgs(args: Map<String, Any>): String {
    return args.map { (k, v) -> "$k: $v" }.joinToString(", ")
}