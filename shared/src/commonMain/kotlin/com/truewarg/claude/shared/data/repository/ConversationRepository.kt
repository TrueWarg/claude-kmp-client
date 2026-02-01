package com.truewarg.claude.shared.data.repository

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import com.truewarg.claude.shared.data.models.ChatMessage
import com.truewarg.claude.shared.data.models.Conversation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ConversationRepository(
    private val settings: Settings,
    private val json: Json
) {
    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: Flow<List<Conversation>> = _conversations.asStateFlow()

    init {
        loadConversations()
    }

    fun getConversations(): List<Conversation> {
        return _conversations.value
    }

    fun getConversation(conversationId: String): Conversation? {
        return _conversations.value.find { it.id == conversationId }
    }

    fun createConversation(conversation: Conversation) {
        val updated = _conversations.value + conversation
        _conversations.value = updated
        saveConversations()
    }

    fun updateConversation(conversation: Conversation) {
        val updated = _conversations.value.map {
            if (it.id == conversation.id) conversation else it
        }
        _conversations.value = updated
        saveConversations()
    }

    fun deleteConversation(conversationId: String) {
        val updated = _conversations.value.filter { it.id != conversationId }
        _conversations.value = updated
        saveConversations()
        deleteMessages(conversationId)
    }

    fun getMessages(conversationId: String): List<ChatMessage> {
        val messagesJson = settings.getStringOrNull("messages_$conversationId")
            ?: return emptyList()

        return try {
            json.decodeFromString(messagesJson)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun addMessage(message: ChatMessage) {
        val messages = getMessages(message.conversationId).toMutableList()
        messages.add(message)
        saveMessages(message.conversationId, messages)
    }

    fun updateMessage(message: ChatMessage) {
        val messages = getMessages(message.conversationId).map {
            if (it.id == message.id) message else it
        }
        saveMessages(message.conversationId, messages)
    }

    fun deleteMessages(conversationId: String) {
        settings.remove("messages_$conversationId")
    }

    fun clearAll() {
        _conversations.value.forEach { conversation ->
            deleteMessages(conversation.id)
        }
        _conversations.value = emptyList()
        saveConversations()
    }

    private fun loadConversations() {
        val conversationsJson = settings.getStringOrNull("conversations")
            ?: return

        try {
            _conversations.value = json.decodeFromString(conversationsJson)
        } catch (e: Exception) {
            _conversations.value = emptyList()
        }
    }

    private fun saveConversations() {
        val conversationsJson = json.encodeToString(_conversations.value)
        settings["conversations"] = conversationsJson
    }

    private fun saveMessages(conversationId: String, messages: List<ChatMessage>) {
        val messagesJson = json.encodeToString(messages)
        settings["messages_$conversationId"] = messagesJson
    }
}
