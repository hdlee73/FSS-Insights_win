package com.hdlee73.fssinsights.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class AppNavDestination(val label: String, val icon: ImageVector) {
    NEWS("뉴스 검색", Icons.Default.Newspaper),
    PRESS("금융당국 보도자료", Icons.Default.Campaign),
    RESEARCH("연구원 보고서", Icons.Default.Description),
    BOOKMARKS("보관한 기사", Icons.Default.Bookmark),
    LINKS("주요 사이트", Icons.Default.Link),
    SETTINGS("설정 및 키 관리", Icons.Default.Settings)
}

@Composable
fun AppSidebar(
    currentDestination: AppNavDestination,
    onNavigate: (AppNavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(240.dp)
            .fillMaxHeight()
            .background(AppTheme.SurfaceWhite)
            .padding(16.dp)
    ) {
        // App Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AccountBalance,
                contentDescription = null,
                tint = AppTheme.MintPrimary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "FSS Insights",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.MintPrimary
                )
                Text(
                    text = "금융 뉴스 & 리포트",
                    fontSize = 12.sp,
                    color = AppTheme.TextSecondary
                )
            }
        }

        Divider(color = AppTheme.CardBorder, thickness = 1.dp)
        Spacer(modifier = Modifier.height(16.dp))

        // Navigation Items
        AppNavDestination.values().forEach { dest ->
            val isSelected = dest == currentDestination
            val bgColor = if (isSelected) AppTheme.MintLight else androidx.compose.ui.graphics.Color.Transparent
            val textColor = if (isSelected) AppTheme.MintPrimary else AppTheme.TextPrimary
            val iconTint = if (isSelected) AppTheme.MintPrimary else AppTheme.TextSecondary

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .background(bgColor, RoundedCornerShape(8.dp))
                    .clickable { onNavigate(dest) }
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Icon(
                    imageVector = dest.icon,
                    contentDescription = dest.label,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = dest.label,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = textColor
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "v1.0.0 (Windows 64-bit)",
            fontSize = 11.sp,
            color = AppTheme.TextMuted,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}
