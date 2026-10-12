package com.hdlee73.fssinsights.data

import com.hdlee73.fssinsights.model.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class SettingsStore {
    private val appDir: File by lazy {
        val appData = System.getenv("APPDATA") ?: System.getProperty("user.home")
        val dir = File(appData, "FSSInsights")
        if (!dir.exists()) dir.mkdirs()
        dir
    }

    private val configFile: File by lazy { File(appDir, "settings.json") }
    private val bookmarksFile: File by lazy { File(appDir, "bookmarks.json") }

    fun loadSettings(): AppSettings {
        if (!configFile.exists()) return AppSettings()
        return runCatching {
            val json = JSONObject(configFile.readText(Charsets.UTF_8))
            val kwArray = json.optJSONArray("keywords")
            val keywords = if (kwArray != null) {
                (0 until kwArray.length()).map { kwArray.getString(it) }
            } else AppSettings.DEFAULT_KEYWORDS

            val providerName = json.optString("provider", NewsProviderType.COMBINED.name)
            val outletName = json.optString("outletScope", OutletScope.ALL.name)
            val timeName = json.optString("timeRange", TimeRange.WEEK.name)
            val clientId = json.optString("naverClientId", "")
            val clientSecret = json.optString("naverClientSecret", "")

            // Custom links
            val linksArr = json.optJSONArray("customLinks")
            val links = if (linksArr != null) {
                (0 until linksArr.length()).map { i ->
                    val obj = linksArr.getJSONObject(i)
                    UsefulLink(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        name = obj.optString("name"),
                        url = obj.optString("url"),
                        description = obj.optString("description")
                    )
                }
            } else defaultUsefulLinks()

            AppSettings(
                keywords = keywords,
                provider = runCatching { NewsProviderType.valueOf(providerName) }.getOrDefault(NewsProviderType.COMBINED),
                outletScope = runCatching { OutletScope.valueOf(outletName) }.getOrDefault(OutletScope.ALL),
                timeRange = runCatching { TimeRange.valueOf(timeName) }.getOrDefault(TimeRange.WEEK),
                naverClientId = clientId,
                naverClientSecret = clientSecret,
                customLinks = links
            )
        }.getOrDefault(AppSettings(customLinks = defaultUsefulLinks()))
    }

    fun saveSettings(settings: AppSettings) {
        val json = JSONObject()
        json.put("keywords", JSONArray(settings.keywords))
        json.put("provider", settings.provider.name)
        json.put("outletScope", settings.outletScope.name)
        json.put("timeRange", settings.timeRange.name)
        json.put("naverClientId", settings.naverClientId)
        json.put("naverClientSecret", settings.naverClientSecret)

        val linksArr = JSONArray()
        settings.customLinks.forEach { link ->
            val o = JSONObject()
            o.put("id", link.id)
            o.put("name", link.name)
            o.put("url", link.url)
            o.put("description", link.description)
            linksArr.put(o)
        }
        json.put("customLinks", linksArr)

        configFile.writeText(json.toString(2), Charsets.UTF_8)
    }

    fun getBookmarks(): Set<String> {
        if (!bookmarksFile.exists()) return emptySet()
        return runCatching {
            val arr = JSONArray(bookmarksFile.readText(Charsets.UTF_8))
            (0 until arr.length()).map { arr.getString(it) }.toSet()
        }.getOrDefault(emptySet())
    }

    fun toggleBookmark(url: String): Boolean {
        val current = getBookmarks().toMutableSet()
        val isNowBookmarked = if (current.contains(url)) {
            current.remove(url)
            false
        } else {
            current.add(url)
            true
        }
        bookmarksFile.writeText(JSONArray(current).toString(2), Charsets.UTF_8)
        return isNowBookmarked
    }

    companion object {
        fun defaultUsefulLinks(): List<UsefulLink> = listOf(
            UsefulLink(name = "금융감독원", url = "https://www.fss.or.kr", description = "금융감독원 공식 홈페이지"),
            UsefulLink(name = "금융위원회", url = "https://www.fsc.go.kr", description = "금융위원회 공식 홈페이지"),
            UsefulLink(name = "자본시장연구원", url = "https://www.kcmi.re.kr", description = "자본시장 분석 및 보고서"),
            UsefulLink(name = "한국금융연구원", url = "https://www.kif.re.kr", description = "금융 동향 및 간행물"),
            UsefulLink(name = "DART 전자공시", url = "https://dart.fss.or.kr", description = "기업 공시 정보 시스템"),
            UsefulLink(name = "e-금융민원센터", url = "https://www.fcsc.kr", description = "금융 민원 신청 및 조회")
        )
    }
}
