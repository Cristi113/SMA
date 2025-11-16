package com.mymediashelf.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mymediashelf.app.ui.screens.*

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Items : Screen("items")
    object Tags : Screen("tags")
    object Lists : Screen("lists")
    object Search : Screen("search")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
){
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route){
            HomeScreen(
                onNavigateToItems = { navController.navigate(Screen.Items.route) },
                onNavigateToTags = { navController.navigate(Screen.Tags.route) },
                onNavigateToLists = { navController.navigate(Screen.Lists.route) }
            )
        }

        composable(Screen.Items.route){
            ItemsScreen(
                onNavigateToSearch = { navController.navigate(Screen.Search.route) }
            )
        }

        /*composable(Screen.Tags.route){
            TagsScreen()
        }*/

        composable(Screen.Lists.route){
            ListsScreen()
        }

        composable(Screen.Search.route){
            SearchScreen()
        }
    }
}