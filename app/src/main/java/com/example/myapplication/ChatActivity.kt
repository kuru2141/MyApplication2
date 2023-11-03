package com.example.myapplication

import android.os.AsyncTask
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class ChatActivity : AppCompatActivity() {

    private lateinit var chatbotResponseTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val sendButton = findViewById<Button>(R.id.sendButton)
        val inputEditText = findViewById<EditText>(R.id.inputEditText)
        chatbotResponseTextView = findViewById<TextView>(R.id.chatbotResponseTextView)

        sendButton.setOnClickListener {
            val userInput = inputEditText.text.toString()
            if (userInput.isNotBlank()) {
                SendMessageTask().execute(userInput)
                inputEditText.text.clear()
            }
        }
    }

    inner class SendMessageTask : AsyncTask<String, Void, String>() {

        override fun doInBackground(vararg params: String?): String {
            val userInput = params[0]
            return sendMessageToChatbot(userInput)
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            chatbotResponseTextView.text = result
        }
    }

    private fun sendMessageToChatbot(userInput: String?): String {
        val url = "https://222zgn8i67.apigw.ntruss.com/chat/chatStage/"
        val secretKey = "TVh0aElJUndocVRqUXR3T3FuRWtMQ0tzRlF6YlhhYmY="
        val timestamp = (System.currentTimeMillis() / 1000).toString()
        val requestBody = JSONObject().apply {
            put("version", "v2")
            put("userId", "U47b00b58c90f8e47428af8b7bddcda3d1111111")
            put("timestamp", timestamp)
            put("bubbles", listOf(JSONObject().apply {
                put("type", "text")
                put("data", mapOf("description" to userInput))
            }))
            put("event", "send")
        }.toString()

        try {
            val signature = makeSignature(secretKey, timestamp, requestBody)
            val contentType = "application/json; charset=utf-8".toMediaTypeOrNull()

            val request = Request.Builder()
                .url(url)
                .header("Content-Type", contentType.toString())
                .header("X-NCP-CHATBOT_SIGNATURE", signature)
                .post(requestBody.toRequestBody(contentType))  // requestBody를 직접 전달
                .build()

            val client = OkHttpClient()
            val response = client.newCall(request).execute()
            return response.body?.string() ?: "Empty response"
        } catch (e: Exception) {
            e.printStackTrace()
            return "Error occurred: ${e.message}"
        }
    }

    private fun makeSignature(secretKey: String, timestamp: String, requestBody: String): String {
        try {
            val message = "$timestamp.$requestBody"
            val secretKeySpec = SecretKeySpec(Base64.getDecoder().decode(secretKey), "HmacSHA256")
            val mac = Mac.getInstance("HmacSHA256")
            mac.init(secretKeySpec)
            val signature = Base64.getEncoder().encodeToString(mac.doFinal(message.toByteArray()))

            return signature
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}