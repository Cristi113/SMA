package com.mymediashelf.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.mymediashelf.app.ui.theme.GradientBackgroundEnd
import com.mymediashelf.app.ui.theme.GradientBackgroundMiddle
import com.mymediashelf.app.ui.theme.GradientBackgroundStart
import androidx.navigation.compose.rememberNavController
import com.mymediashelf.app.ui.navigation.BottomNavigationBar
import com.mymediashelf.app.ui.navigation.NavGraph
import com.mymediashelf.app.ui.theme.GradientBackgroundSecond
import com.mymediashelf.app.ui.theme.MyMediaShelfTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyMediaShelfTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.ui.graphics.Color.Transparent
                ) {
                    val navController = rememberNavController()

                    androidx.compose.material3.Scaffold(
                        bottomBar = {
                            BottomNavigationBar(navController = navController)
                        },
                        containerColor = androidx.compose.ui.graphics.Color.Transparent
                    ) { padding ->
                        NavGraph(
                            navController = navController,
                            modifier = androidx.compose.ui.Modifier.padding(padding)
                        )
                    }
                }
            }
        }
    }
}