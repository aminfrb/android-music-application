package com.example.ava.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.ava.core.util.Outcome
import com.example.ava.core.util.runCatchingOutcome
import com.example.ava.data.local.dao.ChatDao
import com.example.ava.data.local.dao.SongDao
import com.example.ava.data.local.entity.MessageEntity
import com.example.ava.data.local.entity.toDomain
import com.example.ava.data.local.entity.toEntity
import com.example.ava.data.local.prefs.TokenStore
import com.example.ava.data.remote.api.AvaApi
import com.example.ava.data.remote.dto.toDomain
import com.example.ava.data.remote.socket.ChatSocket
import com.example.ava.di.ApplicationScope
import com.example.ava.di.IoDispatcher
import com.example.ava.domain.model.Conversation
import com.example.ava.domain.model.Message
import com.example.ava.domain.model.MessageStatus
import com.example.ava.domain.repository.ChatRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Room is the single source of truth for chat. The socket writes into it, and Paging 3
 * reads out of it — so history survives a dead connection and the UI has one place to watch.
 */
@OptIn(ExperimentalPagingApi::class)
@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val api: AvaApi,
    private val chatDao: ChatDao,
    private val songDao: SongDao,
    private val socket: ChatSocket,
    private val tokenStore: TokenStore,
    @IoDispatcher private val io: CoroutineDispatcher,
    @ApplicationScope private val appScope: CoroutineScope,
) : ChatRepository {

    private val _incoming = MutableSharedFlow<Message>(extraBufferCapacity = 32)
    override val incomingMessages: Flow<Message> = _incoming.asSharedFlow()

    override val typingEvents: Flow<Pair<Long, Boolean>> =
        socket.typing.map { it.userId to it.isTyping }

    init {
        // Persist anything the socket hands us, then re-emit for whoever is on screen.
        appScope.launch(io) {
            socket.messages.collect { dto ->
                dto.song?.let { songDao.upsert(it.toDomain().toEntity()) }
                chatDao.upsert(dto.toDomain().toEntity())
                _incoming.emit(dto.toDomain())
            }
        }
        appScope.launch(io) {
            socket.statuses.collect { event ->
                val me = tokenStore.currentUserId()
                chatDao.updateStatusForSender(event.conversationId, me, event.status)
            }
        }
    }

    override suspend fun connect() = socket.connect()
    override suspend fun disconnect() = socket.disconnect()

    override suspend fun conversations(): Outcome<List<Conversation>> = withContext(io) {
        runCatchingOutcome { api.conversations().map { it.toDomain() } }
    }

    override suspend fun openConversation(peerId: Long): Outcome<Long> = withContext(io) {
        runCatchingOutcome { api.openConversation(peerId).id }
    }

    /**
     * Pages out of Room. `RemoteMediator` fetches older pages from the API when the user
     * scrolls past what we have cached.
     */
    override fun pagedMessages(conversationId: Long): Flow<PagingData<Message>> =
        Pager(
            config = PagingConfig(pageSize = 30, prefetchDistance = 10, enablePlaceholders = false),
            remoteMediator = MessageRemoteMediator(conversationId, api, chatDao, songDao),
            pagingSourceFactory = { chatDao.pagedMessages(conversationId) },
        ).flow.map { paging ->
            paging.map { entity ->
                val song = entity.songId?.let { songDao.byId(it)?.toDomain() }
                entity.toDomain(song)
            }
        }

    /** Optimistic send: the row exists locally as SENDING before the socket answers. */
    override suspend fun sendText(peerId: Long, conversationId: Long, body: String) = withContext(io) {
        val clientId = UUID.randomUUID().toString()
        val me = tokenStore.currentUserId()
        chatDao.upsert(
            MessageEntity(
                serverId = null, clientId = clientId, conversationId = conversationId,
                senderId = me, body = body, songId = null,
                status = MessageStatus.SENDING.name, createdAt = System.currentTimeMillis(),
            )
        )
        socket.sendMessage(peerId, clientId, body = body) { message ->
            if (message != null) {
                appScope.launch(io) { chatDao.confirm(clientId, message.id, message.status) }
            }
        }
    }

    override suspend fun sendSong(peerId: Long, conversationId: Long, songId: Long) = withContext(io) {
        val clientId = UUID.randomUUID().toString()
        val me = tokenStore.currentUserId()
        chatDao.upsert(
            MessageEntity(
                serverId = null, clientId = clientId, conversationId = conversationId,
                senderId = me, body = null, songId = songId,
                status = MessageStatus.SENDING.name, createdAt = System.currentTimeMillis(),
            )
        )
        socket.sendMessage(peerId, clientId, songId = songId) { message ->
            if (message != null) {
                appScope.launch(io) { chatDao.confirm(clientId, message.id, message.status) }
            }
        }
    }

    override suspend fun markRead(conversationId: Long) = socket.markRead(conversationId)
    override fun setTyping(peerId: Long, typing: Boolean) = socket.setTyping(peerId, typing)
}
