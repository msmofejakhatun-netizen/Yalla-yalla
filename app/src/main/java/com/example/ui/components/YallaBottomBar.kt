package com.example.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.YallaGold
import com.example.ui.theme.YallaOrange
import com.example.ui.viewmodel.NavigationTab

@Composable
fun YallaBottomBar(
    selectedTab: NavigationTab,
    onTabSelect: (NavigationTab) -> Unit,
    yallaCoinsCount: Int = 1450,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = Color.White,
        tonalElevation = 10.dp,
        windowInsets = NavigationBarDefaults.windowInsets
    ) {
        // Home
        NavigationBarItem(
            selected = selectedTab == NavigationTab.YALLA_HOME || selectedTab == NavigationTab.ZOMATO_DELIVERY,
            onClick = { onTabSelect(NavigationTab.YALLA_HOME) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home"
                )
            },
            label = {
                Text(
                    text = "Home",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (selectedTab == NavigationTab.YALLA_HOME || selectedTab == NavigationTab.ZOMATO_DELIVERY) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 11.sp
                    )
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = YallaOrange,
                selectedTextColor = YallaOrange,
                indicatorColor = YallaOrange.copy(alpha = 0.12f),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )

        // Search
        NavigationBarItem(
            selected = selectedTab == NavigationTab.YALLA_SEARCH,
            onClick = { onTabSelect(NavigationTab.YALLA_SEARCH) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            },
            label = {
                Text(
                    text = "Search",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (selectedTab == NavigationTab.YALLA_SEARCH) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 11.sp
                    )
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = YallaOrange,
                selectedTextColor = YallaOrange,
                indicatorColor = YallaOrange.copy(alpha = 0.12f),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )

        // Orders
        NavigationBarItem(
            selected = selectedTab == NavigationTab.YALLA_ORDERS || selectedTab == NavigationTab.ZOMATO_ORDERS,
            onClick = { onTabSelect(NavigationTab.YALLA_ORDERS) },
            icon = {
                Icon(
                    imageVector = Icons.Default.ReceiptLong,
                    contentDescription = "Orders"
                )
            },
            label = {
                Text(
                    text = "Orders",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (selectedTab == NavigationTab.YALLA_ORDERS || selectedTab == NavigationTab.ZOMATO_ORDERS) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 11.sp
                    )
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = YallaOrange,
                selectedTextColor = YallaOrange,
                indicatorColor = YallaOrange.copy(alpha = 0.12f),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )

        // Yalla Coins
        NavigationBarItem(
            selected = selectedTab == NavigationTab.YALLA_COINS || selectedTab == NavigationTab.ZOMATO_MONEY,
            onClick = { onTabSelect(NavigationTab.YALLA_COINS) },
            icon = {
                BadgedBox(
                    badge = {
                        Badge(containerColor = YallaGold) {
                            Text("NEW", fontSize = 8.sp, color = Color.Black, fontWeight = FontWeight.Black)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = "Yalla Coins"
                    )
                }
            },
            label = {
                Text(
                    text = "Yalla Coins",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (selectedTab == NavigationTab.YALLA_COINS || selectedTab == NavigationTab.ZOMATO_MONEY) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 11.sp
                    )
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = YallaOrange,
                selectedTextColor = YallaOrange,
                indicatorColor = YallaOrange.copy(alpha = 0.12f),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )

        // Profile
        NavigationBarItem(
            selected = selectedTab == NavigationTab.YALLA_PROFILE || selectedTab == NavigationTab.ZOMATO_PROFILE,
            onClick = { onTabSelect(NavigationTab.YALLA_PROFILE) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile"
                )
            },
            label = {
                Text(
                    text = "Profile",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (selectedTab == NavigationTab.YALLA_PROFILE || selectedTab == NavigationTab.ZOMATO_PROFILE) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 11.sp
                    )
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = YallaOrange,
                selectedTextColor = YallaOrange,
                indicatorColor = YallaOrange.copy(alpha = 0.12f),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )
    }
}
