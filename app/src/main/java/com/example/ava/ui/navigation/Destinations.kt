package com.example.ava.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ava.R

sealed class Destination(val route: String) {
    data object Home : Destination("home")
    data object Search : Destination("search")
    data object Downloads : Destination("downloads")
    data object Playlists : Destination("playlists")
    data object Profile : Destination("profile")

    data object Settings : Destination("settings")
    data object Notifications : Destination("notifications")
    data object LikedSongs : Destination("liked_songs")
    data object RecentlyPlayed : Destination("recently_played")
    data object TopArtists : Destination("top_artists")
    data object Following : Destination("following")
    data object Conversations : Destination("conversations")
    data object FindFriends : Destination("find_friends")

    data object PlaylistDetail : Destination("playlist/{playlistId}") {
        fun of(id: Long) = "playlist/$id"
    }
    data object UserProfile : Destination("user/{userId}") {
        fun of(id: Long) = "user/$id"
    }
    data object Chat : Destination("chat/{conversationId}/{peerId}") {
        fun of(conversationId: Long, peerId: Long) = "chat/$conversationId/$peerId"
    }
}

data class BottomTab(
    val destination: Destination,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

val bottomTabs = listOf(
    BottomTab(Destination.Home, R.string.tab_home, Icons.Filled.Home, Icons.Outlined.Home),
    BottomTab(Destination.Search, R.string.tab_search, Icons.Filled.Search, Icons.Outlined.Search),
    BottomTab(Destination.Downloads, R.string.tab_downloads, Icons.Filled.Download, Icons.Outlined.Download),
    BottomTab(Destination.Playlists, R.string.tab_playlists, Icons.Filled.LibraryMusic, Icons.Outlined.LibraryMusic),
    BottomTab(Destination.Profile, R.string.tab_profile, Icons.Filled.Person, Icons.Outlined.Person),
)
