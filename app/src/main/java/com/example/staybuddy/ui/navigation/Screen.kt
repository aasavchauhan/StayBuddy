package com.example.staybuddy.ui.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object Home : Screen("home")
    data object Search : Screen("search")
    data object MapView : Screen("map_view")
    data object Favorites : Screen("favorites")
    data object RoommateList : Screen("roommate_list")
    data class AddRoommatePost(val postId: String = "{postId}") : Screen("add_roommate_post/$postId") {
        companion object {
            const val ROUTE = "add_roommate_post/{postId}"
            fun createRoute(postId: String?) = "add_roommate_post/${postId ?: "new"}"
        }
    }
    data class AddListing(val listingId: String = "{listingId}") : Screen("add_listing/$listingId") {
        companion object {
            const val ROUTE = "add_listing/{listingId}"
            fun createRoute(listingId: String?) = "add_listing/${listingId ?: "new"}"
        }
    }
    data object OwnerDashboard : Screen("owner_dashboard")
    data object OwnerInquiries : Screen("owner_inquiries")
    data object ChatList : Screen("chat_list")
    data object Profile : Screen("profile")
    data object EditProfile : Screen("edit_profile")
    data object FinishRegistration : Screen("finish_registration")

    data class ListingDetail(val listingId: String = "{listingId}") : Screen("listing_detail/$listingId") {
        companion object {
            const val ROUTE = "listing_detail/{listingId}"
        }
    }

    data class Chat(val chatId: String = "{chatId}") : Screen("chat/$chatId") {
        companion object {
            const val ROUTE = "chat/{chatId}"
        }
    }
}
