package com.hdlee73.fssinsights.data

data class SearchPlan(
    val providerQueries: List<String>,
    val terms: List<String>,
    val usesBooleanOperators: Boolean,
    val excludedTerms: List<String> = emptyList()
)

object SearchQueryParser {
    private const val MAX_BRANCHES = 12

    fun parse(raw: String): SearchPlan {
        val trimmed = raw.trim()
        if (trimmed.isBlank()) {
            return SearchPlan(emptyList(), emptyList(), false)
        }

        val tokens = mutableListOf<String>()
        val excluded = mutableListOf<String>()

        val regex = Regex("""("[^"]+"|\S+)""")
        val matches = regex.findAll(trimmed)

        for (match in matches) {
            val valStr = match.value
            if (valStr.startsWith("-") && valStr.length > 1) {
                excluded.add(valStr.substring(1).replace("\"", "").trim())
            } else {
                tokens.add(valStr)
            }
        }

        val queryText = tokens.joinToString(" ")
        // Split OR
        val orBranches = queryText.split(Regex("""\s+OR\s+""", RegexOption.IGNORE_CASE))
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val branches = if (orBranches.isNotEmpty()) orBranches else listOf(queryText)

        return SearchPlan(
            providerQueries = branches.take(MAX_BRANCHES),
            terms = tokens.map { it.replace("\"", "").replace(Regex("""^(AND|OR)$""", RegexOption.IGNORE_CASE), "") }
                .filter { it.isNotBlank() }
                .distinct(),
            usesBooleanOperators = queryText.contains(" OR ", ignoreCase = true) || queryText.contains(" AND ", ignoreCase = true),
            excludedTerms = excluded.distinct()
        )
    }
}
