package com.hdlee73.fssinsights.data

import com.hdlee73.fssinsights.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class AgencyRepository {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    suspend fun fetchReleases(agency: AgencyId, query: String = "", page: Int = 1): List<ReleaseItem> = withContext(Dispatchers.IO) {
        when (agency) {
            AgencyId.FSS -> fetchFss(query, page)
            AgencyId.FSC -> fetchFsc(query, page)
            AgencyId.KCMI -> fetchKcmi(query, page)
            AgencyId.IOSCO -> fetchIosco(query, page)
            else -> emptyList()
        }
    }

    private fun fetchFss(query: String, page: Int): List<ReleaseItem> {
        val q = URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
        val url = if (query.isNotBlank()) {
            "https://www.fss.or.kr/fss/bbs/B0000188/list.do?menuNo=200218&pageIndex=$page&searchCnd=1&searchWrd=$q"
        } else {
            "https://www.fss.or.kr/fss/bbs/B0000188/list.do?menuNo=200218&pageIndex=$page"
        }

        return runCatching {
            val html = NewsHttpClient.get(url)
            val doc = Jsoup.parse(html, "https://www.fss.or.kr")
            val rows = doc.select("table.board-list tbody tr, .bd-list tbody tr")
            val list = mutableListOf<ReleaseItem>()

            for (row in rows) {
                val linkEl = row.selectFirst("a[href*='view.do']") ?: continue
                val title = linkEl.text().trim()
                val href = linkEl.attr("abs:href")
                val dateText = row.select("td").lastOrNull()?.text()?.trim().orEmpty()
                val date = runCatching { LocalDate.parse(dateText, dateFormatter) }.getOrNull()

                if (title.isNotBlank()) {
                    list.add(
                        ReleaseItem(
                            agency = AgencyId.FSS,
                            title = title,
                            link = href,
                            date = date
                        )
                    )
                }
            }
            list
        }.getOrDefault(emptyList())
    }

    private fun fetchFsc(query: String, page: Int): List<ReleaseItem> {
        val q = URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
        val url = if (query.isNotBlank()) {
            "https://www.fsc.go.kr/no010101?curPage=$page&srchKey=sj&srchText=$q"
        } else {
            "https://www.fsc.go.kr/no010101?curPage=$page"
        }

        return runCatching {
            val html = NewsHttpClient.get(url)
            val doc = Jsoup.parse(html, "https://www.fsc.go.kr")
            val rows = doc.select(".board-list tbody tr, table tbody tr")
            val list = mutableListOf<ReleaseItem>()

            for (row in rows) {
                val linkEl = row.selectFirst("a[href*='no010101']") ?: continue
                val title = linkEl.text().trim()
                val href = linkEl.attr("abs:href")
                val dateText = row.select("td.date, td:matches(\\d{4}-\\d{2}-\\d{2})").text().trim()
                val date = runCatching { LocalDate.parse(dateText, dateFormatter) }.getOrNull()

                if (title.isNotBlank()) {
                    list.add(
                        ReleaseItem(
                            agency = AgencyId.FSC,
                            title = title,
                            link = href,
                            date = date
                        )
                    )
                }
            }
            list
        }.getOrDefault(emptyList())
    }

    private fun fetchKcmi(query: String, page: Int): List<ReleaseItem> {
        val url = "https://www.kcmi.re.kr/report/report_list?pg=$page&pp=20"
        return runCatching {
            val html = NewsHttpClient.get(url)
            val doc = Jsoup.parse(html, "https://www.kcmi.re.kr")
            val items = doc.select(".report_list li, .board_list li, .list_area tr")
            val list = mutableListOf<ReleaseItem>()

            for (item in items) {
                val linkEl = item.selectFirst("a[href*='report_view']") ?: continue
                val title = linkEl.select(".tit, .subject").text().ifBlank { linkEl.text() }.trim()
                val href = linkEl.attr("abs:href")
                val author = item.select(".name, .author").text().trim()

                if (title.isNotBlank()) {
                    if (query.isBlank() || title.contains(query, ignoreCase = true)) {
                        list.add(
                            ReleaseItem(
                                agency = AgencyId.KCMI,
                                title = title,
                                link = href,
                                author = author.ifBlank { null }
                            )
                        )
                    }
                }
            }
            list
        }.getOrDefault(emptyList())
    }

    private fun fetchIosco(query: String, page: Int): List<ReleaseItem> {
        val q = URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
        val url = if (query.isNotBlank()) {
            "https://www.iosco.org/publications/?subsection=public_reports&keywords=$q&keywordsTitle=on" + if (page > 1) "&page=$page" else ""
        } else {
            "https://www.iosco.org/publications/?subsection=public_reports" + if (page > 1) "&page=$page" else ""
        }

        return runCatching {
            val html = NewsHttpClient.get(url)
            val doc = Jsoup.parse(html, "https://www.iosco.org")
            val rows = doc.select("a[href*='pubdocs/pdf']")
            val list = mutableListOf<ReleaseItem>()

            for (a in rows) {
                val title = a.text().trim()
                val href = a.attr("abs:href")
                if (title.isNotBlank()) {
                    list.add(
                        ReleaseItem(
                            agency = AgencyId.IOSCO,
                            title = title,
                            link = href
                        )
                    )
                }
            }
            list.distinctBy { it.link }
        }.getOrDefault(emptyList())
    }
}
