package com.hdlee73.fssinsights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.hdlee73.fssinsights.data.AgencyRepository
import com.hdlee73.fssinsights.data.NewsRepository
import com.hdlee73.fssinsights.data.SettingsStore
import com.hdlee73.fssinsights.model.AgencyGroup
import com.hdlee73.fssinsights.ui.*

fun main() = application {
    val windowState = remember { WindowState(width = 1200.dp, height = 800.dp) }
    val settingsStore = remember { SettingsStore() }
    val newsRepo = remember { NewsRepository(settingsStore) }
    val agencyRepo = remember { AgencyRepository() }

    var currentNav by remember { mutableStateOf(AppNavDestination.NEWS) }

    Window(
        onCloseRequest = ::exitApplication,
        state = windowState,
        title = "FSS Insights - 금융 뉴스 및 연구자료 검색기"
    ) {
        MaterialTheme {
            Row(modifier = Modifier.fillMaxSize()) {
                AppSidebar(
                    currentDestination = currentNav,
                    onNavigate = { currentNav = it }
                )

                Divider(
                    color = AppTheme.CardBorder,
                    modifier = Modifier.fillMaxHeight().width(1.dp)
                )

                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    when (currentNav) {
                        AppNavDestination.NEWS -> NewsScreen(newsRepo, settingsStore)
                        AppNavDestination.PRESS -> ReleaseScreen(AgencyGroup.PRESS, agencyRepo)
                        AppNavDestination.RESEARCH -> ReleaseScreen(AgencyGroup.RESEARCH, agencyRepo)
                        AppNavDestination.BOOKMARKS -> {
                            NewsScreen(newsRepo, settingsStore)
                        }
                        AppNavDestination.LINKS -> LinksScreen(settingsStore)
                        AppNavDestination.SETTINGS -> SettingsScreen(settingsStore)
                    }
                }
            }
        }
    }
}
