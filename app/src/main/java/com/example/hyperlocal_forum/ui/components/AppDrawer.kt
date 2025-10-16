package com.example.hyperlocal_forum.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AppDrawer(
    modifier: Modifier = Modifier,
    drawerState: DrawerState,
    onLogout: () -> Unit,
    content: @Composable () -> Unit
) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Hyperlocal Forum")
                    Button(
                        onClick = onLogout,
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                        Text("Logout")
                    }
                }
            }
        },
        modifier = modifier,
        content = content
    )
}