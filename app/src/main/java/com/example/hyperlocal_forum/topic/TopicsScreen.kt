package com.example.hyperlocal_forum.topic

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun TopicsScreen(
    modifier: Modifier = Modifier,
    viewModel: TopicsViewModel = viewModel()
) {
    Scaffold(
        modifier = modifier.fillMaxSize()
    ) {  }
}