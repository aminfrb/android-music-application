package com.example.ava.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.ava.core.designsystem.component.pressScale
import com.example.ava.core.designsystem.theme.AvaTheme
import com.example.ava.core.designsystem.theme.PremiumGold
import com.example.ava.ui.navigation.Destination
import com.example.ava.R

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val upgraded = stringResource(R.string.profile_upgraded)
    val failed = stringResource(R.string.error_generic)

    LaunchedEffect(Unit) {
        viewModel.effects.collect {
            snackbarHostState.showSnackbar(if (it is ProfileEffect.Upgraded) upgraded else failed)
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(AvaTheme.spacing.screen),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box {
                AsyncImage(
                    model = state.user?.avatarUrl,
                    contentDescription = stringResource(R.string.cd_profile_picture),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(AvaTheme.sizes.avatarLarge)
                        .clip(CircleShape),
                )
                Box(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .pressScale {
                            // A picker would go here; the demo cycles a deterministic avatar.
                            viewModel.changeAvatar("https://i.pravatar.cc/300?u=${System.currentTimeMillis()}")
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.PhotoCamera,
                        stringResource(R.string.profile_change_avatar),
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            Spacer(Modifier.height(AvaTheme.spacing.md))
            Text(state.user?.displayName.orEmpty(), style = MaterialTheme.typography.headlineSmall)
            Text(
                "@${state.user?.username.orEmpty()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(AvaTheme.spacing.md))
            PremiumBadge(isPremium = state.user?.isPremium == true)

            Spacer(Modifier.height(AvaTheme.spacing.md))
            Button(
                onClick = viewModel::buyPremium,
                enabled = !state.isPurchasing,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.isPurchasing) {
                    CircularProgressIndicator(
                        Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Spacer(Modifier.width(AvaTheme.spacing.sm))
                    Text(stringResource(R.string.profile_processing))
                } else {
                    Text(
                        stringResource(
                            if (state.user?.isPremium == true) R.string.profile_renew
                            else R.string.profile_get_premium
                        )
                    )
                }
            }

            Spacer(Modifier.height(AvaTheme.spacing.lg))

            listOf(
                Triple(Icons.Filled.Favorite, R.string.home_liked_songs, Destination.LikedSongs.route),
                Triple(Icons.Filled.History, R.string.home_recently_played, Destination.RecentlyPlayed.route),
                Triple(Icons.Filled.People, R.string.profile_following, Destination.Following.route),
                Triple(Icons.Filled.PersonSearch, R.string.find_friends, Destination.FindFriends.route),
                Triple(Icons.Filled.Chat, R.string.chat_title, Destination.Conversations.route),
                Triple(Icons.Filled.Settings, R.string.settings_title, Destination.Settings.route),
            ).forEach { (icon, label, route) ->
                ListItem(
                    headlineContent = { Text(stringResource(label)) },
                    leadingContent = { Icon(icon, null) },
                    trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null) },
                    modifier = Modifier.pressScale(pressedScale = 0.98f) { navController.navigate(route) },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background),
                )
            }
        }
    }
}

/** Gold, and gold only here. It has to mean something. */
@Composable
private fun PremiumBadge(isPremium: Boolean) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = if (isPremium) PremiumGold.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            Modifier.padding(horizontal = AvaTheme.spacing.md, vertical = AvaTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AvaTheme.spacing.xs),
        ) {
            Icon(
                if (isPremium) Icons.Filled.WorkspacePremium else Icons.Filled.Person,
                null,
                tint = if (isPremium) PremiumGold else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
            Text(
                stringResource(if (isPremium) R.string.profile_premium else R.string.profile_free),
                style = MaterialTheme.typography.labelLarge,
                color = if (isPremium) PremiumGold else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
