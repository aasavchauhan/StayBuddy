package com.example.staybuddy.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val label: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

object BottomNavItems {
    val studentItems = listOf(
        BottomNavItem("Home", Screen.Home.route, Icons.Filled.Home, Icons.Outlined.Home),
        BottomNavItem("Search", Screen.Search.route, Icons.Filled.Search, Icons.Outlined.Search),
        BottomNavItem("Roommate", Screen.RoommateList.route, Icons.Filled.Add, Icons.Outlined.Add),
        BottomNavItem("Chats", Screen.ChatList.route, Icons.Filled.Chat, Icons.Outlined.Chat),
        BottomNavItem("Profile", Screen.Profile.route, Icons.Filled.Person, Icons.Outlined.Person)
    )

    val ownerItems = listOf(
        BottomNavItem("Home", Screen.Home.route, Icons.Filled.Home, Icons.Outlined.Home),
        BottomNavItem("Search", Screen.Search.route, Icons.Filled.Search, Icons.Outlined.Search),
        BottomNavItem("Add PG", Screen.AddListing.ROUTE, Icons.Filled.Add, Icons.Outlined.Add),
        BottomNavItem("Chats", Screen.ChatList.route, Icons.Filled.Chat, Icons.Outlined.Chat),
        BottomNavItem("Profile", Screen.Profile.route, Icons.Filled.Person, Icons.Outlined.Person)
    )
}
