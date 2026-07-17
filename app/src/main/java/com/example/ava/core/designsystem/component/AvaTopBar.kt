package com.example.ava.core.designsystem.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.shape.CircleShape
import coil.compose.AsyncImage
import com.example.ava.core.designsystem.theme.AvaTheme
import com.example.ava.R

/**
 * Shared top bar. Logo + name sit at the start of the line and the actions at the end, so
 * the whole bar mirrors itself when the layout direction flips to RTL — no manual swapping.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvaTopBar(
    avatarUrl: String?,
    onProfileClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AvaTheme.spacing.sm),
            ) {
                Icon(
                    painterResource(R.drawable.ic_ava_logo),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(AvaTheme.sizes.iconLarge),
                )
                Text(
                    stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        },
        actions = {
            IconButton(onClick = onNotificationsClick) {
                Icon(Icons.Filled.Notifications, stringResource(R.string.cd_notifications))
            }
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Filled.Settings, stringResource(R.string.cd_settings))
            }
            IconButton(onClick = onProfileClick) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = stringResource(R.string.cd_profile_picture),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(AvaTheme.sizes.icon + AvaTheme.spacing.sm)
                        .clip(CircleShape),
                )
            }
            Spacer(Modifier.width(AvaTheme.spacing.xs))
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
        ),
    )
}
