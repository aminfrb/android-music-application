package com.example.ava.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.ava.core.util.Outcome
import com.example.ava.core.util.runCatchingOutcome
import com.example.ava.data.remote.api.AvaApi
import com.example.ava.data.remote.dto.toDomain
import com.example.ava.data.remote.paging.UserPagingSource
import com.example.ava.di.IoDispatcher
import com.example.ava.domain.model.User
import com.example.ava.domain.repository.SocialRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocialRepositoryImpl @Inject constructor(
    private val api: AvaApi,
    @IoDispatcher private val io: CoroutineDispatcher,
) : SocialRepository {

    override fun searchUsers(query: String): Flow<PagingData<User>> =
        Pager(PagingConfig(pageSize = 20, enablePlaceholders = false)) {
            UserPagingSource(api, query)
        }.flow

    override suspend fun user(id: Long): Outcome<User> = withContext(io) {
        runCatchingOutcome { api.user(id).toDomain() }
    }

    override suspend fun setFollowing(userId: Long, follow: Boolean): Outcome<Unit> = withContext(io) {
        runCatchingOutcome { if (follow) api.follow(userId) else api.unfollow(userId) }
    }

    override suspend fun following(): Outcome<List<User>> = withContext(io) {
        runCatchingOutcome { api.following().map { it.toDomain() } }
    }
}
