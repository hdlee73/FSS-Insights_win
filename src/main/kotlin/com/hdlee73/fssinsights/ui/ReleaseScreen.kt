package com.hdlee73.fssinsights.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hdlee73.fssinsights.data.AgencyRepository
import com.hdlee73.fssinsights.model.AgencyGroup
import com.hdlee73.fssinsights.model.AgencyId
import com.hdlee73.fssinsights.model.ReleaseItem
import kotlinx.coroutines.launch
import java.awt.Desktop
import java.net.URI

@Composable
fun ReleaseScreen(
    agencyGroup: AgencyGroup,
    agencyRepo: AgencyRepository
) {
    val coroutineScope = rememberCoroutineScope()
    val availableAgencies = remember(agencyGroup) {
        AgencyId.values().filter { it.group == agencyGroup && it != AgencyId.CUSTOM }
    }
    var selectedAgency by remember(agencyGroup) { mutableStateOf(availableAgencies.first()) }
    var searchQuery by remember { mutableStateOf("") }
    var items by remember { mutableStateOf<List<ReleaseItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    val loadData = {
        isLoading = true
        coroutineScope.launch {
            items = agencyRepo.fetchReleases(selectedAgency, searchQuery)
            isLoading = false
        }
    }

    LaunchedEffect(selectedAgency) {
        loadData()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.BackgroundLight)
            .padding(24.dp)
    ) {
        // Agency Select Tabs & Search
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = AppTheme.SurfaceWhite),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Tab Selection
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    availableAgencies.forEach { agency ->
                        val isSelected = agency == selectedAgency
                        Button(
                            onClick = { selectedAgency = agency },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) AppTheme.MintPrimary else AppTheme.MintLight,
                                contentColor = if (isSelected) androidx.compose.ui.graphics.Color.White else AppTheme.MintPrimary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(agency.label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Box
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("${selectedAgency.label} 내 제목 검색...") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppTheme.MintPrimary,
                            unfocusedBorderColor = AppTheme.CardBorder
                        )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = { loadData() },
                        colors = ButtonDefaults.buttonColors(containerColor = AppTheme.MintPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(56.dp)
                    ) {
                        Text("검색")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = {
                            runCatching { Desktop.getDesktop().browse(URI(selectedAgency.homeUrl)) }
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(56.dp)
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("사이트 바로가기")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppTheme.MintPrimary)
            }
        } else if (items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("가져온 자료가 없습니다.", color = AppTheme.TextSecondary)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(items) { item ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = AppTheme.SurfaceWhite),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, AppTheme.CardBorder, RoundedCornerShape(10.dp))
                            .clickable {
                                runCatching { Desktop.getDesktop().browse(URI(item.link)) }
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = AppTheme.MintLight
                                    ) {
                                        Text(
                                            text = item.label,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AppTheme.MintPrimary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    item.date?.let { d ->
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = d.toString(), fontSize = 12.sp, color = AppTheme.TextMuted)
                                    }
                                    item.author?.let { a ->
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = "저자: $a", fontSize = 12.sp, color = AppTheme.TextMuted)
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = item.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AppTheme.TextPrimary
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = "원문 열기",
                                tint = AppTheme.TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
