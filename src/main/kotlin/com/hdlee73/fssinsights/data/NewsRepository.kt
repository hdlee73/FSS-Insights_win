package com.hdlee73.fssinsights.data

import com.hdlee73.fssinsights.model.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.time.Instant

class NewsRepository(
    private val settingsStore: SettingsStore
) {
    private val googleProvider = GoogleNewsRssProvider()

    suspend fun search(
        rawQuery: String,
        outletScope: OutletScope = OutletScope.ALL,
        timeRange: TimeRange = TimeRange.WEEK,
        providerType: NewsProviderType = NewsProviderType.COMBINED
    ): SearchResult = coroutineScope {
        val settings = settingsStore.loadSettings()
        val plan = SearchQueryParser.parse(rawQuery)

        val naverProvider = NaverNewsProvider(settings.naverClientId, settings.naverClientSecret)
        val queries = if (plan.providerQueries.isNotEmpty()) plan.providerQueries else listOf(rawQuery)

        val allFetched = mutableListOf<NewsArticle>()

        for (q in queries) {
            when (providerType) {
                NewsProviderType.GOOGLE_RSS -> {
                    allFetched.addAll(googleProvider.search(q, timeRange))
                }
                NewsProviderType.NAVER -> {
                    if (naverProvider.isConfigured) {
                        allFetched.addAll(naverProvider.search(q))
                    } else {
                        // Fallback to Google if Naver keys are missing
                        allFetched.addAll(googleProvider.search(q, timeRange))
                    }
                }
                NewsProviderType.COMBINED -> {
                    val gJob = async { googleProvider.search(q, timeRange) }
                    val nJob = async {
                        if (naverProvider.isConfigured) naverProvider.search(q) else emptyList()
                    }
                    allFetched.addAll(gJob.await())
                    allFetched.addAll(nJob.await())
                }
            }
        }

        val totalFetched = allFetched.size

        // 1. 제외어 필터링 (-단어)
        val withoutExcluded = if (plan.excludedTerms.isNotEmpty()) {
            allFetched.filter { art ->
                val text = (art.title + " " + art.summary).lowercase()
                plan.excludedTerms.none { ex -> text.contains(ex.lowercase()) }
            }
        } else {
            allFetched
        }

        // 2. 시간 범위 필터링
        val cutoff = Instant.now().minus(timeRange.duration)
        val withinTime = withoutExcluded.filter { it.publishedAt.isAfter(cutoff) }

        // 3. 중복 제거 (정규화된 제목 기준)
        val seenTitles = mutableSetOf<String>()
        val distinctArticles = mutableListOf<NewsArticle>()
        var duplicateCount = 0

        for (art in withinTime) {
            val norm = NewsText.normalizeTitle(art.title)
            if (norm.length > 5 && seenTitles.contains(norm)) {
                duplicateCount++
            } else {
                seenTitles.add(norm)
                distinctArticles.add(art)
            }
        }

        // 4. 30대 언론 필터링
        var outletFiltered = 0
        val finalArticles = if (outletScope == OutletScope.MAJOR_30) {
            distinctArticles.filter {
                val isMaj = it.isMajorOutlet || PublisherCatalog.isMajor(it.publisher, it.url)
                if (!isMaj) outletFiltered++
                isMaj
            }
        } else {
            distinctArticles
        }

        // 시간순 정렬 (최신순)
        val bookmarks = settingsStore.getBookmarks()
        val sorted = finalArticles.sortedByDescending { it.publishedAt }.map { art ->
            art.copy(isBookmarked = bookmarks.contains(art.url))
        }

        SearchResult(
            articles = sorted,
            totalFetched = totalFetched,
            totalFiltered = sorted.size,
            duplicateRemoved = duplicateCount,
            outletFiltered = outletFiltered
        )
    }
}
