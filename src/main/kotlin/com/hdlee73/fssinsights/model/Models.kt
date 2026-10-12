package com.hdlee73.fssinsights.model

import java.time.Duration
import java.time.Instant
import java.time.LocalDate

enum class NewsProviderType(val label: String) {
    COMBINED("통합 검색"),
    NAVER("네이버 심층 검색"),
    GOOGLE_RSS("바로 검색 (Google RSS)")
}

enum class OutletScope(val label: String) {
    MAJOR_30("30대 언론"),
    ALL("전체 언론")
}

enum class TimeRange(val label: String, val googleToken: String, val duration: Duration) {
    DAY("24시간", "when:1d", Duration.ofDays(1)),
    WEEK("7일", "when:7d", Duration.ofDays(7)),
    MONTH("30일", "when:30d", Duration.ofDays(30))
}

data class NewsArticle(
    val id: String,
    val title: String,
    val summary: String,
    val url: String,
    val publisher: String,
    val publishedAt: Instant,
    val isMajorOutlet: Boolean = false,
    val isBookmarked: Boolean = false
)

data class SearchResult(
    val articles: List<NewsArticle>,
    val totalFetched: Int,
    val totalFiltered: Int,
    val duplicateRemoved: Int,
    val outletFiltered: Int
)

data class AppSettings(
    val keywords: List<String> = DEFAULT_KEYWORDS,
    val provider: NewsProviderType = NewsProviderType.COMBINED,
    val outletScope: OutletScope = OutletScope.ALL,
    val timeRange: TimeRange = TimeRange.WEEK,
    val naverClientId: String = "",
    val naverClientSecret: String = "",
    val customLinks: List<UsefulLink> = emptyList(),
    val customInstitutes: List<CustomInstitute> = emptyList()
) {
    companion object {
        val DEFAULT_KEYWORDS = listOf(
            "금융감독원", "증권사", "자산운용사", "금융투자", "금융사고",
            "금감원 제재", "불완전판매", "내부통제", "사모펀드", "공매도"
        )
    }
}

enum class AgencyGroup(val label: String) {
    PRESS("금융당국 보도자료"),
    RESEARCH("금융관련 연구원 자료")
}

enum class AgencyId(val label: String, val shortLabel: String, val group: AgencyGroup, val homeUrl: String, val itemNoun: String) {
    FSS("금융감독원", "금융감독원", AgencyGroup.PRESS, "https://www.fss.or.kr/fss/bbs/B0000188/list.do?menuNo=200218", "보도자료"),
    FSC("금융위원회", "금융위원회", AgencyGroup.PRESS, "https://www.fsc.go.kr/no010101", "보도자료"),
    SEC("미국 SEC", "SEC", AgencyGroup.PRESS, "https://www.sec.gov/newsroom", "보도자료"),
    KCMI("자본시장연구원", "자본시장연구원", AgencyGroup.RESEARCH, "https://www.kcmi.re.kr/report/report_list", "보고서"),
    KIF("한국금융연구원", "금융연구원", AgencyGroup.RESEARCH, "https://www.kif.re.kr/kif4/publication/pub_list?mid=10", "보고서"),
    IOSCO("IOSCO", "IOSCO", AgencyGroup.RESEARCH, "https://www.iosco.org/publications/?subsection=public_reports", "보고서"),
    CUSTOM("추가한 연구소", "추가", AgencyGroup.RESEARCH, "", "자료");

    val latestCount: Int get() = 20
}

data class ReleaseItem(
    val agency: AgencyId,
    val title: String,
    val link: String,
    val date: LocalDate? = null,
    val sourceLabel: String? = null,
    val author: String? = null
) {
    val label: String get() = sourceLabel ?: agency.shortLabel
}

data class CustomInstitute(val name: String, val url: String)

data class UsefulLink(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val url: String,
    val description: String = ""
)
