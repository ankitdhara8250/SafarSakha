package com.safarsakha.presentation.screens.profile.profiledashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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

// ── Premium Design Tokens ────────────────────────────────────────────────────
private val NavyColor = Color(0xFF0F172A)
private val SkyColor = Color(0xFF0EA5E9)
private val SlateColor = Color(0xFF64748B)
private val BorderColor = Color(0xFFE2E8F0)
private val BgColor = Color(0xFFFFFFFF)

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
            ModalDrawerSheet(
                drawerContainerColor = BgColor,
                drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(top = 40.dp, bottom = 16.dp)
                ) {
                    Text(
                        text = "SafarSakha",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyColor,
                        letterSpacing = (-0.5f).sp,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Profile Menu",
                        fontSize = 13.sp,
                        color = SlateColor,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    HorizontalDivider(color = BorderColor.copy(alpha = 0.6f))

                    Spacer(modifier = Modifier.height(16.dp))

                    ProfileDrawerItem.entries.forEach { item ->
                        val isSelected = item == selectedItem

                        NavigationDrawerItem(
                            label = {
                                Text(
                                    text = item.title,
                                    fontSize = 15.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    letterSpacing = (-0.1f).sp
                                )
                            },
                            selected = isSelected,
                            onClick = {
                                scope.launch { drawerState.close() }
                                if (!isSelected) onItemSelected(item)
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = SkyColor.copy(alpha = 0.08f),
                                selectedTextColor = SkyColor,
                                unselectedTextColor = NavyColor.copy(alpha = 0.85f)
                            ),
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 2.dp)
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