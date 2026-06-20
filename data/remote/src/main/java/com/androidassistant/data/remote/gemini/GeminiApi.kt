package com.androidassistant.data.remote.gemini

import com.androidassistant.core.common.Constants
import com.androidassistant.core.network.HttpClientProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class GeminiApi(apiKey: String) {

    @Volatile
    private var _apiKey: String = apiKey

    fun updateApiKey(newKey: String) {
        _apiKey = newKey
    }


    private val client: OkHttpClient = HttpClientProvider.createClient()
    private val baseUrl = Constants.GEMINI_BASE_URL

    suspend fun generateContent(
        request: GeminiRequest,
        model: String = Constants.GEMINI_MODEL_FLASH
    ): com.androidassistant.core.common.Result<GeminiResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val json = buildRequestJson(request)
                val body = json.toRequestBody(HttpClientProvider.JSON_MEDIA_TYPE)
                val url = "${baseUrl}models/$model:generateContent?key=$_apiKey"

                val httpRequest = Request.Builder()
                    .url(url)
                    .post(body)
                    .build()

                val response = client.newCall(httpRequest).execute()
                val responseBody = response.body?.string()

                if (!response.isSuccessful) {
                    return@withContext com.androidassistant.core.common.Result.error(
                        IOException("HTTP ${response.code}: $responseBody")
                    )
                }

                val geminiResponse = parseResponse(responseBody ?: "")
                com.androidassistant.core.common.Result.success(geminiResponse)
            } catch (e: Exception) {
                com.androidassistant.core.common.Result.error(e)
            }
        }
    }

    fun streamGenerateContent(
        request: GeminiRequest,
        model: String = Constants.GEMINI_MODEL_FLASH,
        onChunk: (GeminiStreamChunk) -> Unit,
        onComplete: () -> Unit,
        onError: (String) -> Unit
    ): EventSource {
        val json = buildRequestJson(request)
        val body = json.toRequestBody(HttpClientProvider.JSON_MEDIA_TYPE)
        val url = "${baseUrl}models/$model:streamGenerateContent?key=$_apiKey"

        val httpRequest = Request.Builder()
            .url(url)
            .post(body)
            .build()

        val factory = EventSources.createFactory(client)

        return factory.newEventSource(httpRequest, object : EventSourceListener() {
            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                try {
                    val jsonObject = JSONObject(data)
                    val candidates = jsonObject.optJSONArray("candidates")

                    if (candidates != null && candidates.length() > 0) {
                        val candidate = candidates.getJSONObject(0)
                        val content = candidate.optJSONObject("content")

                        if (content != null) {
                            val parts = content.optJSONArray("parts")
                            if (parts != null && parts.length() > 0) {
                                val part = parts.getJSONObject(0)

                                if (part.has("text")) {
                                    onChunk(GeminiStreamChunk(text = part.getString("text")))
                                }

                                if (part.has("functionCall")) {
                                    val fc = part.getJSONObject("functionCall")
                                    val args = fc.optJSONObject("args")?.let { jsonToMap(it) }
                                    onChunk(GeminiStreamChunk(
                                        functionCall = ResponseFunctionCall(
                                            name = fc.getString("name"),
                                            args = args
                                        )
                                    ))
                                }
                            }
                        }

                        val finishReason = candidate.optString("finishReason")
                        if (finishReason == "STOP") {
                            val usage = jsonObject.optJSONObject("usageMetadata")
                            val metadata = usage?.let {
                                UsageMetadata(
                                    promptTokenCount = it.optInt("promptTokenCount"),
                                    candidatesTokenCount = it.optInt("candidatesTokenCount"),
                                    totalTokenCount = it.optInt("totalTokenCount")
                                )
                            }
                            onChunk(GeminiStreamChunk(isComplete = true, usageMetadata = metadata))
                        }
                    }
                } catch (e: Exception) {
                    onError("Parse error: ${e.message}")
                }
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: okhttp3.Response?) {
                onError(t?.message ?: "Unknown error")
            }

            override fun onClosed(eventSource: EventSource) {
                onComplete()
            }
        })
    }

    private fun buildRequestJson(request: GeminiRequest): String {
        val json = JSONObject()

        if (request.systemInstruction != null) {
            json.put("systemInstruction", buildContentJson(request.systemInstruction))
        }

        val contentsArray = JSONArray()
        request.contents.forEach { content ->
            contentsArray.put(buildContentJson(content))
        }
        json.put("contents", contentsArray)

        if (!request.tools.isNullOrEmpty()) {
            val toolsArray = JSONArray()
            request.tools.forEach { tool ->
                val toolJson = JSONObject()
                val functionsArray = JSONArray()
                tool.functionDeclarations.forEach { fn ->
                    functionsArray.put(buildFunctionDeclarationJson(fn))
                }
                toolJson.put("functionDeclarations", functionsArray)
                toolsArray.put(toolJson)
            }
            json.put("tools", toolsArray)
        }

        request.generationConfig?.let { config ->
            val configJson = JSONObject()
            configJson.put("temperature", config.temperature)
            configJson.put("maxOutputTokens", config.maxOutputTokens)
            configJson.put("topP", config.topP)
            configJson.put("topK", config.topK)
            json.put("generationConfig", configJson)
        }

        return json.toString()
    }

    private fun buildContentJson(content: Any): JSONObject {
        return when (content) {
            is Content -> {
                val json = JSONObject()
                json.put("role", content.role)
                val partsArray = JSONArray()
                content.parts.forEach { part ->
                    val partJson = JSONObject()
                    part.text?.let { partJson.put("text", it) }
                    part.functionCall?.let { fc ->
                        val fcJson = JSONObject()
                        fcJson.put("name", fc.name)
                        fc.args?.let { fcJson.put("args", JSONObject(it as Map<*, *>)) }
                        partJson.put("functionCall", fcJson)
                    }
                    part.functionResponse?.let { fr ->
                        val frJson = JSONObject()
                        frJson.put("name", fr.name)
                        frJson.put("response", JSONObject(fr.response))
                        partJson.put("functionResponse", frJson)
                    }
                    partsArray.put(partJson)
                }
                json.put("parts", partsArray)
                json
            }
            is SystemInstruction -> {
                val json = JSONObject()
                val partsArray = JSONArray()
                content.parts.forEach { part ->
                    val partJson = JSONObject()
                    part.text?.let { partJson.put("text", it) }
                    partsArray.put(partJson)
                }
                json.put("parts", partsArray)
                json
            }
            else -> JSONObject()
        }
    }

    private fun buildFunctionDeclarationJson(fn: FunctionDeclaration): JSONObject {
        val json = JSONObject()
        json.put("name", fn.name)
        json.put("description", fn.description)
        val paramsJson = JSONObject()
        paramsJson.put("type", fn.parameters.type)
        fn.parameters.properties?.let { props ->
            val propsJson = JSONObject()
            props.forEach { (name, prop) ->
                val propJson = JSONObject()
                propJson.put("type", prop.type)
                prop.description?.let { propJson.put("description", it) }
                prop.enum?.let { propJson.put("enum", JSONArray(it)) }
                propsJson.put(name, propJson)
            }
            paramsJson.put("properties", propsJson)
        }
        fn.parameters.required?.let {
            paramsJson.put("required", JSONArray(it))
        }
        json.put("parameters", paramsJson)
        return json
    }

    private fun parseResponse(json: String): GeminiResponse {
        val root = JSONObject(json)
        val candidatesArray = root.optJSONArray("candidates")
        val candidates = mutableListOf<Candidate>()

        if (candidatesArray != null) {
            for (i in 0 until candidatesArray.length()) {
                val candidateJson = candidatesArray.getJSONObject(i)
                val contentJson = candidateJson.optJSONObject("content")

                val parts = mutableListOf<ResponsePart>()
                val partsArray = contentJson?.optJSONArray("parts")

                if (partsArray != null) {
                    for (j in 0 until partsArray.length()) {
                        val partJson = partsArray.getJSONObject(j)
                        val text = partJson.optString("text", null)
                        val fcJson = partJson.optJSONObject("functionCall")

                        val functionCall = fcJson?.let {
                            val args = it.optJSONObject("args")?.let { args -> jsonToMap(args) }
                            ResponseFunctionCall(
                                name = it.getString("name"),
                                args = args
                            )
                        }

                        parts.add(ResponsePart(text = text, functionCall = functionCall))
                    }
                }

                candidates.add(Candidate(
                    content = ResponseContent(
                        role = contentJson?.optString("role"),
                        parts = parts
                    ),
                    finishReason = candidateJson.optString("finishReason"),
                    safetyRatings = null
                ))
            }
        }

        val usage = root.optJSONObject("usageMetadata")?.let {
            UsageMetadata(
                promptTokenCount = it.optInt("promptTokenCount"),
                candidatesTokenCount = it.optInt("candidatesTokenCount"),
                totalTokenCount = it.optInt("totalTokenCount")
            )
        }

        return GeminiResponse(candidates = candidates, usageMetadata = usage)
    }

    private fun jsonToMap(json: JSONObject): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        json.keys().forEach { key ->
            val value = json.get(key)
            map[key] = when (value) {
                is JSONObject -> jsonToMap(value)
                is JSONArray -> {
                    val list = mutableListOf<Any>()
                    for (i in 0 until value.length()) {
                        val item = value.get(i)
                        list.add(when (item) {
                            is JSONObject -> jsonToMap(item)
                            else -> item
                        })
                    }
                    list
                }
                else -> value
            }
        }
        return map
    }
}
