package com.hdlee73.fssinsights.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hdlee73.fssinsights.data.SettingsStore
import com.hdlee73.fssinsights.model.AppSettings

@Composable
fun SettingsScreen(
    settingsStore: SettingsStore
) {
    var settings by remember { mutableStateOf(settingsStore.loadSettings()) }
    var naverClientId by remember { mutableStateOf(settings.naverClientId) }
    var naverClientSecret by remember { mutableStateOf(settings.naverClientSecret) }
    var keywordsText by remember { mutableStateOf(settings.keywords.joinToString(", ")) }
    var showSavedMessage by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.BackgroundLight)
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("설정 및 키 관리", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppTheme.TextPrimary)
        Text("네이버 뉴스 API 키 설정 및 맞춤 키워드 관리", fontSize = 13.sp, color = AppTheme.TextSecondary)

        Spacer(modifier = Modifier.height(20.dp))

        // Naver API Key Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = AppTheme.SurfaceWhite),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("네이버 검색 API (심층 검색용)", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = AppTheme.TextPrimary)
                Text(
                    "네이버 오픈API (검색) Client ID와 Client Secret을 입력하시면 네이버 뉴스 심층 검색 및 통합 검색이 활성화됩니다. 미입력 시 Google 뉴스 검색만 동작합니다.",
                    fontSize = 12.sp,
                    color = AppTheme.TextSecondary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = naverClientId,
                    onValueChange = { naverClientId = it },
                    label = { Text("Client ID") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = naverClientSecret,
                    onValueChange = { naverClientSecret = it },
                    label = { Text("Client Secret") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Keywords Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = AppTheme.SurfaceWhite),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("기본 빠른 검색 키워드 (쉼표 구분)", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = AppTheme.TextPrimary)
                Text(
                    "첫 화면 상단에 노출되는 추천 키워드 태그 목록입니다.",
                    fontSize = 12.sp,
                    color = AppTheme.TextSecondary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = keywordsText,
                    onValueChange = { keywordsText = it },
                    label = { Text("키워드 목록") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = {
                    val kws = keywordsText.split(",")
                        .map { it.trim() }
                        .filter { it.isNotBlank() }

                    val updated = settings.copy(
                        naverClientId = naverClientId.trim(),
                        naverClientSecret = naverClientSecret.trim(),
                        keywords = if (kws.isNotEmpty()) kws else AppSettings.DEFAULT_KEYWORDS
                    )
                    settingsStore.saveSettings(updated)
                    settings = updated
                    showSavedMessage = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = AppTheme.MintPrimary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Text("설정 저장", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            if (showSavedMessage) {
                Spacer(modifier = Modifier.width(16.dp))
                Text("설정이 성공적으로 저장되었습니다.", color = AppTheme.MintPrimary, fontSize = 14.sp)
            }
        }
    }
}
