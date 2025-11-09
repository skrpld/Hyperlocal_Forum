package com.example.hyperlocal_forum

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.hyperlocal_forum.ui.theme.Hyperlocal_ForumTheme
import com.example.hyperlocal_forum.data.AuthManager
import com.example.hyperlocal_forum.ui.navigation.ForumNavGraph
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val authManager = remember { AuthManager() }
            val isLoggedIn by authManager.isLoggedIn.collectAsState()

            Hyperlocal_ForumTheme {
                ForumNavGraph(
                    isLoggedIn = isLoggedIn,
                    authManager = authManager
                )
            }
        }
    }
}