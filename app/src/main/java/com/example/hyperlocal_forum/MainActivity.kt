package com.example.hyperlocal_forum

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.hyperlocal_forum.topic.TopicDetailScreen
import com.example.hyperlocal_forum.topic.TopicsScreen
import com.example.hyperlocal_forum.ui.theme.Hyperlocal_ForumTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Hyperlocal_ForumTheme {
                ForumNavGraph()
            }
        }
    }
}