package com.example.hyperlocal_forum.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hyperlocal_forum.ui.theme.Hyperlocal_ForumTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    authManager: AuthManager,
    modifier: Modifier = Modifier,
    onLoginSuccess: () -> Unit
) {
    Hyperlocal_ForumTheme {
        val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(authManager))

        val username by authViewModel.username.collectAsState()
        val password by authViewModel.password.collectAsState()
        val isLoginMode by authViewModel.isLoginMode.collectAsState()
        val message by authViewModel.message.collectAsState()
        val isLoading by authViewModel.isLoading.collectAsState()

        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(message) {
            message?.let { msg ->
                snackbarHostState.showSnackbar(msg)
                authViewModel.clearMessage()
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text(if (isLoginMode) "Login" else "Register") }
                )
            },
            modifier = modifier.fillMaxSize()
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        OutlinedTextField(
                            value = username,
                            onValueChange = { authViewModel.onUsernameChange(it) },
                            label = { Text("Username") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = { authViewModel.onPasswordChange(it) },
                            label = { Text("Password") },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            enabled = !isLoading
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { authViewModel.authenticate(onLoginSuccess) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text(if (isLoginMode) "Login" else "Register")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = { authViewModel.toggleLoginMode() },
                    enabled = !isLoading
                ) {
                    Text(if (isLoginMode) "Don't have an account? Register" else "Already have an account? Login")
                }
            }
        }
    }
}
