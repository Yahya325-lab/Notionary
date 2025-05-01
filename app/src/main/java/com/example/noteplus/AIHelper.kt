import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException
import org.json.JSONObject

object AIHelper {
    private const val API_URL = "https://api.together.xyz/v1/chat/completions"
    private const val API_KEY = "ccf16e0e5d18e176d3539f46281ddb238d29bcb0a7ec1876acc5d1540cef496a"

    fun summarize(note: String, callback: (String) -> Unit) {
        val client = OkHttpClient()
        val json = JSONObject().apply {
            put("model", "deepseek-ai/deepseek-r1-distill-llama-70b")
            put("prompt", listOf(
                JSONObject().apply {
                    put("role", "system")
                    put("temperature", 0.1)
                    put("content", "You're an AI for summarize something that i write, summarize it under 200 words")
                },
                JSONObject().apply {
                    put("role", "user")
                    put("content", "Summarize : \n$note")
                }
            ))
            put("max_tokens", 300)
        }

        val requestBody = json.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(API_URL)
            .header("Authorization", "Bearer $API_KEY")
            .header("Content-Type", "application/json")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("Error: ${e.message}") // Debugging
                callback("Gagal mendapatkan ringkasan: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                println("Response: $responseBody") // Debugging

                if (response.isSuccessful && responseBody != null) {
                    try {
                        val jsonResponse = JSONObject(responseBody)
                        val summary = jsonResponse.getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content")
                        callback(summary.trim())
                    } catch (e: Exception) {
                        callback("Gagal memproses respons AI: ${e.message}")
                    }
                } else {
                    callback("Gagal mendapatkan ringkasan: ${response.message}")
                }
            }
        })
    }
}
