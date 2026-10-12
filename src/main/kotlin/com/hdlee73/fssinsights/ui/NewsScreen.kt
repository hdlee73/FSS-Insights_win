package com.hdlee73.fssinsights.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hdlee73.fssinsights.data.NewsRepository
import com.hdlee73.fssinsights.data.SettingsStore
import com.hdlee73.fssinsights.model.*
import kotlinx.coroutines.launch
import java.awt.Desktop
import java.net.URI
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun NewsScreen(
    newsRepo: NewsRepository,
    settingsStore: SettingsStore
) {
    val coroutineScope = rememberCoroutineScope()
    var appSettings by remember { mutableStateOf(settingsStore.loadSettings()) }
    var searchQuery by remember { mutableStateOf("금융감독원") }
    var searchResult by remember { mutableStateOf<SearchResult?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Search Execution
    val doSearch = {
        if (searchQuery.isNotBlank()) {
            isLoading = true
            errorMessage = null
            coroutineScope.launch {
                try {
                    val result = newsRepo.search(
                        rawQuery = searchQuery,
                        outletScope = appSettings.outletScope,
                        timeRange = appSettings.timeRange,
                        providerType = appSettings.provider
                    )
                    searchResult = result
                } catch (e: Exception) {
                    errorMessage = e.message ?: "검색 중 오류가 발생했습니다."
                } finally {
                    isLoading = false
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        doSearch()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.BackgroundLight)
            .padding(24.dp)
    ) {
        // Search & Filter Box
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = AppTheme.SurfaceWhite),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Search Input Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("키워드 또는 수식 입력 (예: 금감원 AND (증권사 OR 운용사) -연예)") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AppTheme.MintPrimary) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppTheme.MintPrimary,
                            unfocusedBorderColor = AppTheme.CardBorder
                        )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = { doSearch() },
                        colors = ButtonDefaults.buttonColors(containerColor = AppTheme.MintPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(56.dp)
                    ) {
                        Text("검색", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Keywords Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(appSettings.keywords) { kw ->
                        val isSelected = searchQuery == kw
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) AppTheme.MintPrimary else AppTheme.MintLight,
                            modifier = Modifier.clickable {
                                searchQuery = kw
                                doSearch()
                            }
                        ) {
                            Text(
                                text = kw,
                                color = if (isSelected) androidx.compose.ui.graphics.Color.White else AppTheme.MintPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Options Row (TimeRange, Scope, Provider)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // TimeRange Select
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("기간: ", fontSize = 13.sp, color = AppTheme.TextSecondary)
                        TimeRange.values().forEach { tr ->
                            val selected = appSettings.timeRange == tr
                            Text(
                                text = tr.label,
                                fontSize = 13.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) AppTheme.MintPrimary else AppTheme.TextSecondary,
                                modifier = Modifier
                                    .clickable {
                                        appSettings = appSettings.copy(timeRange = tr)
                                        settingsStore.saveSettings(appSettings)
                                        doSearch()
                                    }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Scope Select
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("언론 범위: ", fontSize = 13.sp, color = AppTheme.TextSecondary)
                        OutletScope.values().forEach { scope ->
                            val selected = appSettings.outletScope == scope
                            Text(
                                text = scope.label,
                                fontSize = 13.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) AppTheme.MintPrimary else AppTheme.TextSecondary,
                                modifier = Modifier
                                    .clickable {
                                        appSettings = appSettings.copy(outletScope = scope)
                                        settingsStore.saveSettings(appSettings)
                                        doSearch()
                                    }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Provider Select
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("엔진: ", fontSize = 13.sp, color = AppTheme.TextSecondary)
                        NewsProviderType.values().forEach { p ->
                            val selected = appSettings.provider == p
                            Text(
                                text = p.label,
                                fontSize = 13.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) AppTheme.MintPrimary else AppTheme.TextSecondary,
                                modifier = Modifier
                                    .clickable {
                                        appSettings = appSettings.copy(provider = p)
                                        settingsStore.saveSettings(appSettings)
                                        doSearch()
                                    }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Result Statistics Bar
        searchResult?.let { res ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "검색 결과 ${res.totalFiltered}건 (수집 ${res.totalFetched}건 · 중복 제외 ${res.duplicateRemoved}건 · 비주요언론 제외 ${res.outletFiltered}건)",
                    fontSize = 13.sp,
                    color = AppTheme.TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Content Area (List / Loading / Error)
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppTheme.MintPrimary)
            }
        } else if (errorMessage != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
            }
        } else {
            val articles = searchResult?.articles ?: emptyList()
            if (articles.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("검색 조건에 맞는 기사가 없습니다.", color = AppTheme.TextSecondary)
                }
            } else {
                val dateFormatter = remember {
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault())
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(articles, key = { it.id }) { article ->
                        NewsCard(
                            article = article,
                            dateStr = dateFormatter.format(article.publishedAt),
                            onBookmarkToggle = {
                                val isBookmarked = settingsStore.toggleBookmark(article.url)
                                searchResult = searchResult?.copy(
                                    articles = searchResult!!.articles.map {
                                        if (it.id == article.id) it.copy(isBookmarked = isBookmarked) else it
                                    }
                                )
                            },
                            onOpenLink = {
                                runCatching { Desktop.getDesktop().browse(URI(article.url)) }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NewsCard(
    article: NewsArticle,
    dateStr: String,
    onBookmarkToggle: () -> Unit,
    onOpenLink: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.SurfaceWhite),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, AppTheme.CardBorder, RoundedCornerShape(10.dp))
            .clickable { onOpenLink() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Publisher & Date Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (article.isMajorOutlet) AppTheme.MajorBadgeBg else AppTheme.MintLight
                ) {
                    Text(
                        text = article.publisher,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (article.isMajorOutlet) AppTheme.MajorBadgeText else AppTheme.MintPrimary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = dateStr,
                    fontSize = 12.sp,
                    color = AppTheme.TextMuted
                )
                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = onBookmarkToggle,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (article.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "북마크",
                        tint = if (article.isBookmarked) AppTheme.ApricotAccent else AppTheme.TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onOpenLink,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = "원문 보기",
                        tint = AppTheme.TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Article Title
            Text(
                text = article.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (article.summary.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = article.summary,
                    fontSize = 13.sp,
                    color = AppTheme.TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
