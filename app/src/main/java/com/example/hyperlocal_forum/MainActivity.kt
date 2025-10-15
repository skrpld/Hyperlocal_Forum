package com.example.hyperlocal_forum

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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