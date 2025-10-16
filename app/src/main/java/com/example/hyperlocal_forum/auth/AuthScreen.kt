package com.example.hyperlocal_forum.auth

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hyperlocal_forum.data.ForumDatabase

@Composable
fun AuthScreen(
    modifier: Modifier = Modifier,
    onLoginSuccess: (String) -> Unit
) {
    val context = LocalContext.current
    val forumDatabase = ForumDatabase.getDatabase(context)
    val authManager = remember(context) { AuthManager(context, forumDatabase.userDao()) }
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(authManager))

    val username by authViewModel.username.collectAsState()
    val password by authViewModel.password.collectAsState()
    val isLoginMode by authViewModel.isLoginMode.collectAsState()
    val message by authViewModel.message.collectAsState()

    LaunchedEffect(message) {
        message?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            authViewModel.clearMessage()
        }
    }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isLoginMode) "Login" else "Register",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = username,
            onValueChange = { authViewModel.onUsernameChange(it) },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = password,
            onValueChange = { authViewModel.onPasswordChange(it) },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )
        Button(
            onClick = { authViewModel.authenticate(onLoginSuccess) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isLoginMode) "Login" else "Register")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { authViewModel.toggleLoginMode() }) {
            Text(if (isLoginMode) "Don't have an account? Register" else "Already have an account? Login")
        }
    }
}