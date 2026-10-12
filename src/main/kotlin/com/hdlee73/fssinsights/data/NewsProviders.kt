package com.hdlee73.fssinsights.data

import com.hdlee73.fssinsights.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.jsoup.Jsoup
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

object NewsHttpClient {
    val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    fun get(url: String, headers: Map<String, String> = emptyMap()): String {
        val reqBuilder = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
        
        headers.forEach { (k, v) -> reqBuilder.header(k, v) }
        
        val resp = client.newCall(reqBuilder.build()).execute()
        if (!resp.isSuccessful) {
            throw Exception("HTTP request failed with code: ${resp.code}")
        }
        return resp.body?.string().orEmpty()
    }
}

class GoogleNewsRssProvider {
    suspend fun search(query: String, timeRange: TimeRange, maxCount: Int = 100): List<NewsArticle> = withContext(Dispatchers.IO) {
        val fullQuery = "$query ${timeRange.googleToken}"
        val encoded = URLEncoder.encode(fullQuery, StandardCharsets.UTF_8.toString())
        val url = "https://news.google.com/rss/search?q=$encoded&hl=ko&gl=KR&ceid=KR:ko"
        
        val xml = NewsHttpClient.get(url)
        val doc = Jsoup.parse(xml, "", org.jsoup.parser.Parser.xmlParser())
        val items = doc.select("item")
        
        val rfc822 = DateTimeFormatter.RFC_1123_DATE_TIME
        val list = mutableListOf<NewsArticle>()

        for (item in items.take(maxCount)) {
            val titleFull = item.selectFirst("title")?.text().orEmpty()
            val link = item.selectFirst("link")?.text().orEmpty()
            val pubDateStr = item.selectFirst("pubDate")?.text().orEmpty()
            val desc = item.selectFirst("description")?.text().orEmpty()

            // Separate title and publisher from "Title - Publisher"
            val lastDash = titleFull.lastIndexOf(" - ")
            val title = if (lastDash > 0) titleFull.substring(0, lastDash).trim() else titleFull
            val publisherRaw = if (lastDash > 0) titleFull.substring(lastDash + 3).trim() else ""

            val pubDate = runCatching {
                ZonedDateTime.parse(pubDateStr, rfc822).toInstant()
            }.getOrDefault(Instant.now())

            val canonicalPub = PublisherCatalog.canonicalName(publisherRaw, link)

            list.add(
                NewsArticle(
                    id = UUID.nameUUIDFromBytes(link.toByteArray()).toString(),
                    title = NewsText.stripHtml(title),
                    summary = NewsText.stripHtml(desc),
                    url = link,
                    publisher = canonicalPub,
                    publishedAt = pubDate,
                    isMajorOutlet = PublisherCatalog.isMajor(canonicalPub, link)
                )
            )
        }
        list
    }
}

class NaverNewsProvider(
    private val clientId: String,
    private val clientSecret: String
) {
    val isConfigured: Boolean get() = clientId.isNotBlank() && clientSecret.isNotBlank()

    suspend fun search(query: String, maxCount: Int = 100, start: Int = 1): List<NewsArticle> = withContext(Dispatchers.IO) {
        if (!isConfigured) return@withContext emptyList()
        val size = maxCount.coerceIn(10, 100)
        val encoded = URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
        val url = "https://openapi.naver.com/v1/search/news.json?query=$encoded&display=$size&start=$start&sort=date"

        val headers = mapOf(
            "X-Naver-Client-Id" to clientId,
            "X-Naver-Client-Secret" to clientSecret
        )

        val jsonStr = NewsHttpClient.get(url, headers)
        val root = JSONObject(jsonStr)
        val items = root.optJSONArray("items") ?: return@withContext emptyList()

        val rfc822 = DateTimeFormatter.RFC_1123_DATE_TIME
        val list = mutableListOf<NewsArticle>()

        for (i in 0 until items.length()) {
            val item = items.getJSONObject(i)
            val title = NewsText.stripHtml(item.optString("title"))
            val desc = NewsText.stripHtml(item.optString("description"))
            val originLink = item.optString("originallink")
            val naverLink = item.optString("link")
            val link = originLink.ifBlank { naverLink }
            val pubDateStr = item.optString("pubDate")

            val pubDate = runCatching {
                ZonedDateTime.parse(pubDateStr, rfc822).toInstant()
            }.getOrDefault(Instant.now())

            val canonicalPub = PublisherCatalog.canonicalName("", link)

            list.add(
                NewsArticle(
                    id = UUID.nameUUIDFromBytes(link.toByteArray()).toString(),
                    title = title,
                    summary = desc,
                    url = link,
                    publisher = canonicalPub,
                    publishedAt = pubDate,
                    isMajorOutlet = PublisherCatalog.isMajor(canonicalPub, link)
                )
            )
        }
        list
    }
}
