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
import com.hdlee73.fssinsights.data.SettingsStore
import com.hdlee73.fssinsights.model.AppSettings
import com.hdlee73.fssinsights.model.UsefulLink
import java.awt.Desktop
import java.net.URI

@Composable
fun LinksScreen(
    settingsStore: SettingsStore
) {
    var settings by remember { mutableStateOf(settingsStore.loadSettings()) }
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.BackgroundLight)
            .padding(24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Column {
                Text("주요 금융 사이트", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppTheme.TextPrimary)
                Text("업무 및 연구에 자주 참조하는 사이트 목록입니다.", fontSize = 13.sp, color = AppTheme.TextSecondary)
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = AppTheme.MintPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("사이트 추가")
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(settings.customLinks) { link ->
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = AppTheme.SurfaceWhite),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, AppTheme.CardBorder, RoundedCornerShape(10.dp))
                        .clickable {
                            runCatching { Desktop.getDesktop().browse(URI(link.url)) }
                        }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = link.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppTheme.TextPrimary
                            )
                            if (link.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = link.description,
                                    fontSize = 13.sp,
                                    color = AppTheme.TextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = link.url,
                                fontSize = 12.sp,
                                color = AppTheme.MintPrimary
                            )
                        }

                        IconButton(
                            onClick = {
                                val updated = settings.customLinks.filter { it.id != link.id }
                                val newSettings = settings.copy(customLinks = updated)
                                settingsStore.saveSettings(newSettings)
                                settings = newSettings
                            }
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "삭제", tint = AppTheme.TextMuted)
                        }

                        IconButton(
                            onClick = {
                                runCatching { Desktop.getDesktop().browse(URI(link.url)) }
                            }
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = "열기", tint = AppTheme.MintPrimary)
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            var name by remember { mutableStateOf("") }
            var url by remember { mutableStateOf("") }
            var desc by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("새 바로가기 사이트 추가") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("사이트 이름 (예: 한국은행)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = url,
                            onValueChange = { url = it },
                            label = { Text("웹 주소 (URL)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = desc,
                            onValueChange = { desc = it },
                            label = { Text("설명 (선택)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (name.isNotBlank() && url.isNotBlank()) {
                                val newLink = UsefulLink(
                                    name = name.trim(),
                                    url = if (url.startsWith("http")) url.trim() else "https://${url.trim()}",
                                    description = desc.trim()
                                )
                                val newSettings = settings.copy(customLinks = settings.customLinks + newLink)
                                settingsStore.saveSettings(newSettings)
                                settings = newSettings
                                showAddDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AppTheme.MintPrimary)
                    ) {
                        Text("추가")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("취소")
                    }
                }
            )
        }
    }
}
