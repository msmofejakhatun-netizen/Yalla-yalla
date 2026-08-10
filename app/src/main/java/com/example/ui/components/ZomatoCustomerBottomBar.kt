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
import com.example.ui.theme.ZomatoRed
import com.example.ui.theme.ZomatoTextPrimary
import com.example.ui.viewmodel.NavigationTab

@Composable
fun ZomatoCustomerBottomBar(
    selectedTab: NavigationTab,
    onTabSelect: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.height(68.dp),
        containerColor = Color.White,
        tonalElevation = 10.dp
    ) {
        NavigationBarItem(
            selected = selectedTab == NavigationTab.ZOMATO_DELIVERY,
            onClick = { onTabSelect(NavigationTab.ZOMATO_DELIVERY) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Moped,
                    contentDescription = "Delivery"
                )
            },
            label = {
                Text(
                    text = "Delivery",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (selectedTab == NavigationTab.ZOMATO_DELIVERY) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 11.sp
                    )
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ZomatoRed,
                selectedTextColor = ZomatoRed,
                indicatorColor = ZomatoRed.copy(alpha = 0.1f),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )

        NavigationBarItem(
            selected = selectedTab == NavigationTab.ZOMATO_ORDERS,
            onClick = { onTabSelect(NavigationTab.ZOMATO_ORDERS) },
            icon = {
                Icon(
                    imageVector = Icons.Default.ReceiptLong,
                    contentDescription = "History / Orders"
                )
            },
            label = {
                Text(
                    text = "History / Orders",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (selectedTab == NavigationTab.ZOMATO_ORDERS) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 11.sp
                    )
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ZomatoRed,
                selectedTextColor = ZomatoRed,
                indicatorColor = ZomatoRed.copy(alpha = 0.1f),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )

        NavigationBarItem(
            selected = selectedTab == NavigationTab.ZOMATO_MONEY,
            onClick = { onTabSelect(NavigationTab.ZOMATO_MONEY) },
            icon = {
                Icon(
                    imageVector = Icons.Default.LocalOffer,
                    contentDescription = "Money / Offers"
                )
            },
            label = {
                Text(
                    text = "Money / Offers",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (selectedTab == NavigationTab.ZOMATO_MONEY) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 11.sp
                    )
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ZomatoRed,
                selectedTextColor = ZomatoRed,
                indicatorColor = ZomatoRed.copy(alpha = 0.1f),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )

        NavigationBarItem(
            selected = selectedTab == NavigationTab.ZOMATO_PROFILE,
            onClick = { onTabSelect(NavigationTab.ZOMATO_PROFILE) },
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
                        fontWeight = if (selectedTab == NavigationTab.ZOMATO_PROFILE) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 11.sp
                    )
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ZomatoRed,
                selectedTextColor = ZomatoRed,
                indicatorColor = ZomatoRed.copy(alpha = 0.1f),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )
    }
}
