package com.mymediashelf.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.mymediashelf.app.R
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavigationBar(
    navController: NavController,
    modifier: Modifier = Modifier
){
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val context = LocalContext.current
    NavigationBar(
        modifier = modifier,
        containerColor = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
        contentColor = androidx.compose.ui.graphics.Color(0xFF1F2937)
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = context.getString(R.string.nav_home)) },
            label = { Text(context.getString(R.string.nav_home)) },
            selected = currentRoute == Screen.Home.route,
            onClick = { navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Home.route) { inclusive = true }
            } }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Movie, contentDescription = context.getString(R.string.nav_items)) },
            label = { Text(context.getString(R.string.nav_items)) },
            selected = currentRoute == Screen.Items.route,
            onClick = { navController.navigate(Screen.Items.route) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Label, contentDescription = context.getString(R.string.nav_tags)) },
            label = { Text(context.getString(R.string.nav_tags)) },
            selected = currentRoute == Screen.Tags.route,
            onClick = { navController.navigate(Screen.Tags.route) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.List, contentDescription = context.getString(R.string.nav_lists)) },
            label = { Text(context.getString(R.string.nav_lists)) },
            selected = currentRoute == Screen.Lists.route,
            onClick = { navController.navigate(Screen.Lists.route) }
        )
    }
}