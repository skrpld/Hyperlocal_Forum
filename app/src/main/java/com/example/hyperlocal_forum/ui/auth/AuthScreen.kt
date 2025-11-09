package com.example.hyperlocal_forum.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.hyperlocal_forum.ui.theme.Hyperlocal_ForumTheme
import com.example.hyperlocal_forum.utils.AuthManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    authManager: AuthManager,
    viewModel: AuthViewModel,
    modifier: Modifier = Modifier,
    onLoginSuccess: () -> Unit
) {
    Hyperlocal_ForumTheme {

        val username by viewModel.username.collectAsState()
        val password by viewModel.password.collectAsState()
        val confirmPassword by viewModel.confirmPassword.collectAsState()
        val isLoginMode by viewModel.isLoginMode.collectAsState()
        val message by viewModel.authMessage.collectAsState()
        val isLoading by viewModel.isLoading.collectAsState()

        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(message) {
            message?.let { msg ->
                snackbarHostState.showSnackbar(msg)
                viewModel.clearMessage()
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text(if (isLoginMode) "Login" else "Register", modifier = Modifier.testTag("AuthScreen_Title")) }
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
                            onValueChange = { viewModel.onUsernameChange(it) },
                            label = { Text("Username") },
                            modifier = Modifier.fillMaxWidth().testTag("AuthScreen_Username"),
                            enabled = !isLoading
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = { viewModel.onPasswordChange(it) },
                            label = { Text("Password") },
                            modifier = Modifier.fillMaxWidth().testTag("AuthScreen_Password"),
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            enabled = !isLoading
                        )
                        if (!isLoginMode) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { viewModel.onConfirmPasswordChange(it) },
                                label = { Text("Confirm Password") },
                                modifier = Modifier.fillMaxWidth().testTag("AuthScreen_ConfirmPassword"),
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                enabled = !isLoading
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.authenticate(onLoginSuccess) },
                            modifier = Modifier.fillMaxWidth().testTag(if (isLoginMode) "AuthScreen_LoginButton" else "AuthScreen_RegisterButton"),
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
                    onClick = { viewModel.toggleLoginMode() },
                    modifier = Modifier.testTag("AuthScreen_ToggleModeButton"),
                    enabled = !isLoading
                ) {
                    Text(if (isLoginMode) "Don't have an account? Register" else "Already have an account? Login")
                }
            }
        }
    }
}
