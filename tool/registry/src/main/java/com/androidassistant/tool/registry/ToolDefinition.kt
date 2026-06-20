package com.androidassistant.tool.registry

import com.androidassistant.core.model.ToolResult
import com.androidassistant.agent.llm.FunctionDeclaration
import com.androidassistant.agent.llm.FunctionParameter
import com.androidassistant.agent.llm.ParameterProperty

enum class ToolPermissionLevel {
    NONE,
    NORMAL,
    SENSITIVE,
    CRITICAL,
    BLOCKED
}

enum class ToolCategory {
    INFORMATION,
    NOTIFICATION,
    APP_CONTROL,
    SYSTEM,
    CLIPBOARD,
    MESSAGING,
    AUTOMATION
}

class ToolDefinition(
    val name: String,
    val description: String,
    val category: ToolCategory,
    val permissionLevel: ToolPermissionLevel,
    val parameters: List<ToolParameter> = emptyList(),
    val executor: suspend (Map<String, Any>) -> ToolResult
)

data class ToolParameter(
    val name: String,
    val type: ToolParameterType,
    val description: String,
    val required: Boolean = true,
    val enumValues: List<String>? = null
)

enum class ToolParameterType {
    STRING,
    INTEGER,
    BOOLEAN,
    NUMBER
}

fun ToolDefinition.toFunctionDeclaration(): FunctionDeclaration {
    return FunctionDeclaration(
        name = name,
        description = description,
        parameters = FunctionParameter(
            type = "object",
            properties = parameters.associate { param ->
                param.name to ParameterProperty(
                    type = when (param.type) {
                        ToolParameterType.STRING -> "string"
                        ToolParameterType.INTEGER -> "integer"
                        ToolParameterType.BOOLEAN -> "boolean"
                        ToolParameterType.NUMBER -> "number"
                    },
                    description = param.description,
                    enum = param.enumValues
                )
            },
            required = parameters.filter { it.required }.map { it.name }
        )
    )
}
