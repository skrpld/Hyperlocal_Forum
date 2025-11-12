package com.example.hyperlocal_forum.ui.screens.auth

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    modifier: Modifier = Modifier,
    onLoginSuccess: () -> Unit
) {
    Hyperlocal_ForumTheme {

        val username by viewModel.username.collectAsState()
        val email by viewModel.email.collectAsState()
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
                    title = { Text(if (isLoginMode) "Вход" else "Регистрация", modifier = Modifier.testTag("AuthScreen_Title")) }
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

                        if (!isLoginMode) {
                            OutlinedTextField(
                                value = username,
                                onValueChange = { viewModel.onUsernameChange(it) },
                                label = { Text("Имя пользователя") },
                                modifier = Modifier.fillMaxWidth().testTag("AuthScreen_Username"),
                                enabled = !isLoading
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        OutlinedTextField(
                            value = email,
                            onValueChange = { viewModel.onEmailChange(it) },
                            label = { Text("Электронная почта") },
                            modifier = Modifier.fillMaxWidth().testTag("AuthScreen_Email"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            enabled = !isLoading
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { viewModel.onPasswordChange(it) },
                            label = { Text("Пароль") },
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
                                label = { Text("Подтвердите пароль") },
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
                                Text(if (isLoginMode) "Войти" else "Зарегистрироваться")
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
                    Text(if (isLoginMode) "Нет аккаунта? Зарегистрируйтесь" else "Уже есть аккаунт? Войдите")
                }
            }
        }
    }
}