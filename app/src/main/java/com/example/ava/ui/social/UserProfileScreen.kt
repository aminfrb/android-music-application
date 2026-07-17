package com.example.ava.ui.social

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.ava.core.designsystem.component.ShimmerSongList
import com.example.ava.core.designsystem.component.pressScale
import com.example.ava.core.designsystem.theme.AvaTheme
import com.example.ava.core.designsystem.theme.PremiumGold
import com.example.ava.ui.navigation.Destination
import com.example.ava.R

/** Someone else's profile: follow them, open a DM, browse their public playlists. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    navController: NavController,
    viewModel: UserProfileViewModel = hiltViewModel(),
) {
    val user by viewModel.user.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.openChat.collect { conversationId ->
            user?.let { navController.navigate(Destination.Chat.of(conversationId, it.id)) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(user?.displayName.orEmpty()) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.cd_back))
                    }
                },
            )
        }
    ) { padding ->
        val current = user
        if (current == null) {
            ShimmerSongList(modifier = Modifier.padding(padding))
            return@Scaffold
        }

        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(AvaTheme.spacing.screen),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AsyncImage(
                model = current.avatarUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(AvaTheme.sizes.avatarLarge)
                    .clip(CircleShape),
            )
            Spacer(Modifier.height(AvaTheme.spacing.sm))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(current.displayName, style = MaterialTheme.typography.headlineSmall)
                if (current.isPremium) {
                    Spacer(Modifier.width(AvaTheme.spacing.xs))
                    Icon(
                        Icons.Filled.WorkspacePremium,
                        stringResource(R.string.profile_premium),
                        tint = PremiumGold,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Text(
                stringResource(R.string.followers_count, current.followers),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(AvaTheme.spacing.md))

            Row(horizontalArrangement = Arrangement.spacedBy(AvaTheme.spacing.sm)) {
                if (current.isFollowing) {
                    OutlinedButton(onClick = viewModel::toggleFollow) {
                        Text(stringResource(R.string.unfollow))
                    }
                } else {
                    Button(onClick = viewModel::toggleFollow) {
                        Text(stringResource(R.string.follow))
                    }
                }
                OutlinedButton(onClick = viewModel::message) {
                    Icon(Icons.AutoMirrored.Filled.Chat, null)
                    Spacer(Modifier.width(AvaTheme.spacing.xs))
                    Text(stringResource(R.string.chat_title))
                }
            }

            Spacer(Modifier.height(AvaTheme.spacing.lg))

            if (current.publicPlaylists.isNotEmpty()) {
                Text(
                    stringResource(R.string.public_playlists),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.align(Alignment.Start),
                )
                Spacer(Modifier.height(AvaTheme.spacing.sm))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(AvaTheme.spacing.gutter)) {
                    items(current.publicPlaylists, key = { it.id }) { playlist ->
                        Column(
                            Modifier
                                .width(AvaTheme.sizes.artCard)
                                .pressScale {
                                    navController.navigate(Destination.PlaylistDetail.of(playlist.id))
                                }
                        ) {
                            AsyncImage(
                                model = playlist.coverUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(AvaTheme.sizes.artCard)
                                    .clip(MaterialTheme.shapes.medium),
                            )
                            Spacer(Modifier.height(AvaTheme.spacing.xs))
                            Text(
                                playlist.title,
                                style = MaterialTheme.typography.titleSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}
