package com.example.hyperlocal_forum

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hyperlocal_forum.ui.theme.Hyperlocal_ForumTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Hyperlocal_ForumTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    Scaffold(
        // 1. Верхняя панель
        topBar = {
            TopAppBar(
                title = { Text("Мой Экран") },
                navigationIcon = {
                    IconButton(onClick = { /* Открыть меню */ }) {
                        Icon(Icons.Filled.Menu, contentDescription = "Меню")
                    }
                }
            )
        },
        // 2. Плавающая кнопка действия
        floatingActionButton = {
            FloatingActionButton(onClick = { /* Действие по клику */ }) {
                Icon(Icons.Filled.Add, contentDescription = "Добавить")
            }
        },
        // 3. Нижняя панель навигации
        bottomBar = {
            BottomAppBar {
                // Здесь обычно размещают иконки навигации
                Text(text = "Нижняя панель")
            }
        }
    ) { innerPadding ->
        // 4. Основное содержимое экрана
        // innerPadding содержит отступы от topBar и bottomBar,
        // их нужно применить к корневому элементу контента.
        Column(
            modifier = Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                modifier = Modifier.padding(8.dp),
                text = "Это основное содержимое экрана."
            )
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
    Column(modifier = Modifier
        .fillMaxSize()) {  }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Hyperlocal_ForumTheme {
        Greeting("Android")
    }
}
