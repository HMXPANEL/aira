package com.androidassistant.tool.registry

import android.content.Context
import com.androidassistant.agent.llm.FunctionDeclaration

class ToolRegistry(private val context: Context) {

    private val tools = mutableMapOf<String, ToolDefinition>()

    fun register(tool: ToolDefinition) {
        tools[tool.name] = tool
    }

    fun registerAll(toolList: List<ToolDefinition>) {
        toolList.forEach { register(it) }
    }

    fun resolve(name: String): ToolDefinition? = tools[name]

    fun getFunctionDeclarations(): List<FunctionDeclaration> {
        return tools.values.map { it.toFunctionDeclaration() }
    }

    fun listByCategory(category: ToolCategory): List<ToolDefinition> {
        return tools.values.filter { it.category == category }
    }

    fun listAll(): List<ToolDefinition> = tools.values.toList()

    fun unregister(name: String) {
        tools.remove(name)
    }

    fun clear() {
        tools.clear()
    }
}
