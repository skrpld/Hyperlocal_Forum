package com.example.hyperlocal_forum

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.hyperlocal_forum.auth.AuthScreen
import com.example.hyperlocal_forum.auth.AuthManager
import com.example.hyperlocal_forum.data.ForumDatabase
import com.example.hyperlocal_forum.ui.theme.Hyperlocal_ForumTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val forumDatabase = remember { ForumDatabase.getDatabase(context) }
            val authManager = remember { AuthManager(context, forumDatabase.userDao()) }
            val isLoggedIn by authManager.isLoggedIn.collectAsState()

            Hyperlocal_ForumTheme {
                if (isLoggedIn) {
                    ForumNavGraph(authManager = authManager)
                }
                else {
                    AuthScreen(
                        authManager = authManager,
                        onLoginSuccess = { }
                    )
                }
            }
        }
    }
}