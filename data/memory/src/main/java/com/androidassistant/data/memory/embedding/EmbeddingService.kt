package com.androidassistant.data.memory.embedding

import com.androidassistant.core.common.Result
import com.androidassistant.core.network.HttpClientProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

class EmbeddingService(private val apiKey: String) {

    companion object {
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/"
        private const val EMBEDDING_MODEL = "text-embedding-004"
        private const val DIMENSIONS = 768
    }

    private val client = HttpClientProvider.createClient(
        connectTimeout = 10,
        readTimeout = 30
    )

    suspend fun embed(text: String): Result<FloatArray> {
        return withContext(Dispatchers.IO) {
            try {
                val requestBody = JSONObject().apply {
                    put("model", "models/$EMBEDDING_MODEL")
                    put("content", JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", text)
                            })
                        })
                    })
                    put("outputDimensionality", DIMENSIONS)
                }

                val body = requestBody.toString().toRequestBody(
                    HttpClientProvider.JSON_MEDIA_TYPE
                )

                val request = Request.Builder()
                    .url("${BASE_URL}models/$EMBEDDING_MODEL:embedContent?key=$apiKey")
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (!response.isSuccessful) {
                    return@withContext Result.error(
                        IOException("HTTP ${response.code}: $responseBody")
                    )
                }

                val json = JSONObject(responseBody ?: "")
                val embedding = json
                    .getJSONObject("embedding")
                    .getJSONArray("values")

                val values = FloatArray(embedding.length()) { i ->
                    embedding.getDouble(i).toFloat()
                }

                Result.success(values)
            } catch (e: Exception) {
                Result.error(e)
            }
        }
    }

    suspend fun embedBatch(texts: List<String>): Result<List<FloatArray>> {
        val results = mutableListOf<FloatArray>()
        for (text in texts) {
            when (val result = embed(text)) {
                is Result.Success -> results.add(result.data)
                is Result.Error -> return Result.error(result.exception)
            }
        }
        return Result.success(results)
    }

    fun floatArrayToByteArray(values: FloatArray): ByteArray {
        val buffer = ByteBuffer.allocate(values.size * 4)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        values.forEach { buffer.putFloat(it) }
        return buffer.array()
    }

    fun byteArrayToFloatArray(bytes: ByteArray): FloatArray {
        val buffer = ByteBuffer.wrap(bytes)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        val values = FloatArray(bytes.size / 4)
        for (i in values.indices) {
            values[i] = buffer.getFloat()
        }
        return values
    }

    fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
        var dotProduct = 0f
        var normA = 0f
        var normB = 0f
        for (i in a.indices) {
            dotProduct += a[i] * b[i]
            normA += a[i] * a[i]
            normB += b[i] * b[i]
        }
        val denominator = kotlin.math.sqrt(normA) * kotlin.math.sqrt(normB)
        return if (denominator == 0f) 0f else dotProduct / denominator
    }
}
