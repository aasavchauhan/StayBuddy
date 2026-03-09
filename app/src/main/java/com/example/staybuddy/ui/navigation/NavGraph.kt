package com.example.staybuddy.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.staybuddy.ui.screens.auth.LoginScreen
import com.example.staybuddy.ui.screens.auth.RegisterScreen
import com.example.staybuddy.ui.screens.chat.ChatListScreen
import com.example.staybuddy.ui.screens.chat.ChatScreen
import com.example.staybuddy.ui.screens.favorites.FavoritesScreen
import com.example.staybuddy.ui.screens.home.HomeScreen
import com.example.staybuddy.ui.screens.listing.ListingDetailScreen
import com.example.staybuddy.ui.screens.map.MapViewScreen
import com.example.staybuddy.ui.screens.onboarding.OnboardingScreen
import com.example.staybuddy.ui.screens.owner.AddListingScreen
import com.example.staybuddy.ui.screens.owner.OwnerDashboardScreen
import com.example.staybuddy.ui.screens.profile.ProfileScreen
import com.example.staybuddy.ui.screens.roommate.AddRoommatePostScreen
import com.example.staybuddy.ui.screens.roommate.RoommateListScreen
import com.example.staybuddy.ui.screens.search.SearchScreen
import com.example.staybuddy.ui.screens.splash.SplashScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToOnboarding = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                onNavigateToListingDetail = { listingId ->
                    navController.navigate(Screen.ListingDetail(listingId).route)
                },
                onNavigateToRoommates = { navController.navigate(Screen.RoommateList.route) }
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                onNavigateToListingDetail = { listingId ->
                    navController.navigate(Screen.ListingDetail(listingId).route)
                },
                onNavigateToMapView = { navController.navigate(Screen.MapView.route) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.MapView.route) {
            MapViewScreen(
                onNavigateToListingDetail = { listingId ->
                    navController.navigate(Screen.ListingDetail(listingId).route)
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.ListingDetail.ROUTE,
            arguments = listOf(navArgument("listingId") { type = NavType.StringType })
        ) { backStackEntry ->
            val listingId = backStackEntry.arguments?.getString("listingId") ?: ""
            ListingDetailScreen(
                listingId = listingId,
                onNavigateToChat = { chatId ->
                    navController.navigate(Screen.Chat(chatId).route)
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Favorites.route) {
            FavoritesScreen(
                onNavigateToListingDetail = { listingId ->
                    navController.navigate(Screen.ListingDetail(listingId).route)
                }
            )
        }

        composable(Screen.RoommateList.route) {
            RoommateListScreen(
                onNavigateToAddPost = { navController.navigate(Screen.AddRoommatePost.route) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AddRoommatePost.route) {
            AddRoommatePostScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AddListing.route) {
            AddListingScreen(
                onNavigateBack = { navController.popBackStack() },
                onListingAdded = {
                    navController.navigate(Screen.OwnerDashboard.route) {
                        popUpTo(Screen.AddListing.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.OwnerDashboard.route) {
            OwnerDashboardScreen(
                onNavigateToAddListing = { navController.navigate(Screen.AddListing.route) },
                onNavigateToListingDetail = { listingId ->
                    navController.navigate(Screen.ListingDetail(listingId).route)
                }
            )
        }

        composable(Screen.ChatList.route) {
            ChatListScreen(
                onNavigateToChat = { chatId ->
                    navController.navigate(Screen.Chat(chatId).route)
                }
            )
        }

        composable(
            route = Screen.Chat.ROUTE,
            arguments = listOf(navArgument("chatId") { type = NavType.StringType })
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            ChatScreen(
                chatId = chatId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateToFavorites = { navController.navigate(Screen.Favorites.route) },
                onNavigateToOwnerDashboard = { navController.navigate(Screen.OwnerDashboard.route) },
                onNavigateToChatList = { navController.navigate(Screen.ChatList.route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
