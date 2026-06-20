package com.safarsakha.presentation.screens.profile.profiledashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileDashboardScreen(
    selectedItem: ProfileDrawerItem,
    onItemSelected: (ProfileDrawerItem) -> Unit,
    content: @Composable (openDrawer: () -> Unit) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = Color.White) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(top = 32.dp, bottom = 16.dp)
                ) {
                    Text(
                        text = "SafarSakha",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E3A8A),
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Profile Menu",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    ProfileDrawerItem.entries.forEach { item ->
                        NavigationDrawerItem(
                            label = {
                                Text(
                                    text = item.title,
                                    fontSize = 15.sp,
                                    fontWeight = if (item == selectedItem) FontWeight.SemiBold else FontWeight.Normal
                                )
                            },
                            selected = item == selectedItem,
                            onClick = {
                                scope.launch { drawerState.close() }
                                if (item != selectedItem) onItemSelected(item)
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = Color(0xFFE0ECFF),
                                selectedTextColor = Color(0xFF1E3A8A),
                                unselectedTextColor = Color(0xFF0F172A)
                            ),
                            modifier = Modifier
                                .padding(horizontal = 12.dp)
                                .fillMaxWidth()
                        )
                    }
                }
            }
        }
    ) {
        content { scope.launch { drawerState.open() } }
    }
}