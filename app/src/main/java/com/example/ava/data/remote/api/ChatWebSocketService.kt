package com.example.ava.data.remote.api

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class ChatWebSocketService(private val client: OkHttpClient) {

    private var webSocket: WebSocket? = null
    private val listeners = mutableListOf<WebSocketListener>()

    fun connect() {
        val request = Request.Builder()
            .url("wss://your-server.com/chat")
            .build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                // connection opened
            }
            override fun onMessage(webSocket: WebSocket, text: String) {
                // parse incoming message and notify repository
            }
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                // reconnect logic
            }
        })
    }

    fun sendMessage(message: String) {
        webSocket?.send(message)
    }

    fun disconnect() {
        webSocket?.close(1000, "Closing")
        webSocket = null
    }
}
