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
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val label: String,
    val route: String,
    val navigationRoute: String = route,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

object BottomNavItems {
    val studentItems = listOf(
        BottomNavItem("Home", Screen.Home.route, selectedIcon = Icons.Filled.Home, unselectedIcon = Icons.Outlined.Home),
        BottomNavItem("Search", Screen.Search.route, selectedIcon = Icons.Filled.Search, unselectedIcon = Icons.Outlined.Search),
        BottomNavItem("Roommate", Screen.RoommateList.route, selectedIcon = Icons.Filled.Add, unselectedIcon = Icons.Outlined.Add),
        BottomNavItem("Chats", Screen.ChatList.route, selectedIcon = Icons.Filled.Chat, unselectedIcon = Icons.Outlined.Chat),
        BottomNavItem("Profile", Screen.Profile.route, selectedIcon = Icons.Filled.Person, unselectedIcon = Icons.Outlined.Person)
    )

    val ownerItems = listOf(
        BottomNavItem("Dashboard", Screen.OwnerDashboard.route, selectedIcon = Icons.Filled.Dashboard, unselectedIcon = Icons.Outlined.Dashboard),
        BottomNavItem("Inquiries", Screen.OwnerInquiries.route, selectedIcon = Icons.AutoMirrored.Filled.List, unselectedIcon = Icons.AutoMirrored.Outlined.List),
        BottomNavItem("Add PG", Screen.AddListing.createRoute(null), selectedIcon = Icons.Filled.Add, unselectedIcon = Icons.Outlined.Add),
        BottomNavItem("Chats", Screen.ChatList.route, selectedIcon = Icons.Filled.Chat, unselectedIcon = Icons.Outlined.Chat),
        BottomNavItem("Profile", Screen.Profile.route, selectedIcon = Icons.Filled.Person, unselectedIcon = Icons.Outlined.Person)
    )
}
