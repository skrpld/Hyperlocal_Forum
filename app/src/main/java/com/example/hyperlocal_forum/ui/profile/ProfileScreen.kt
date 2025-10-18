package com.example.hyperlocal_forum.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hyperlocal_forum.data.ForumDao
import com.example.hyperlocal_forum.ui.auth.AuthManager
import com.example.hyperlocal_forum.data.ForumDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    authManager: AuthManager
) {
    val context = LocalContext.current
    val forumDao = ForumDatabase.getDatabase(context).forumDao()
    val viewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(forumDao, authManager))

    val user by viewModel.user.collectAsState()

    var username by remember(user) { mutableStateOf(user?.username ?: "") }
    var password by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text("Profile") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Profile ID: ${user?.id ?: ""}")
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("New Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                viewModel.updateUsername(username)
                if (password.isNotEmpty()) {
                    viewModel.updatePassword(password)
                }
            }) {
                Text("Save Changes")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { viewModel.logout() }) {
                Text("Log Out")
            }
        }
    }
}

@Preview
@Composable
fun ProfileScreenPreview() {
    val authManager = AuthManager(LocalContext.current, ForumDatabase.getDatabase(LocalContext.current).forumDao())
    ProfileScreen(authManager = authManager)
}
