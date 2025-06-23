package com.menopausetracker.app.data.api

import com.menopausetracker.app.data.model.Article
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.net.URL
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.net.ssl.HttpsURLConnection

/**
 * Service for fetching menopause and women's health related articles from online sources
 */
class ArticleService {
    companion object {
        private val RSS_SOURCES = listOf(
            // WebMD Women's Health News
            RssSource(
                url = "https://rss.webmd.com/women_health_rss/rss.xml",
                sourceName = "WebMD Women's Health",
                category = "Women's Health"
            ),
            // Science Daily Women's Health News
            RssSource(
                url = "https://www.sciencedaily.com/rss/health_medicine/women's_health.xml",
                sourceName = "Science Daily",
                category = "Women's Health"
            ),
            // NIH News
            RssSource(
                url = "https://www.nih.gov/news-events/news-releases/feed",
                sourceName = "National Institutes of Health",
                category = "Health & Aging"
            ),
            // Healthline
            RssSource(
                url = "https://www.healthline.com/health-news/rss",
                sourceName = "Healthline",
                category = "Health & Aging"
            )
        )

        private val dateFormats = listOf(
            SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US),
            SimpleDateFormat("yyyy-MM-dd", Locale.US)
        )

        // Estimated words per minute for reading
        private const val WORDS_PER_MINUTE = 200
    }

    /**
     * Fetch articles from all configured RSS sources
     */
    suspend fun fetchArticles(): List<Article> = withContext(Dispatchers.IO) {
        val deferredResults = RSS_SOURCES.map { source ->
            async {
                try {
                    fetchArticlesFromRss(source)
                } catch (e: Exception) {
                    println("Error fetching from ${source.sourceName}: ${e.message}")
                    emptyList()
                }
            }
        }

        try {
            // Wait for all fetches to complete and combine results
            val allArticles = deferredResults.awaitAll().flatten()

            // If we have network articles, use them
            if (allArticles.isNotEmpty()) {
                // Filter for menopause-related content and sort by date
                val filteredArticles = allArticles
                    .filter { isRelevantToMenopause(it) }
                    .sortedByDescending { it.publishDate }

                // If we have relevant articles after filtering, return them
                if (filteredArticles.isNotEmpty()) {
                    return@withContext filteredArticles
                }

                // If no articles are relevant, fall back to all articles if any exist
                if (allArticles.isNotEmpty()) {
                    return@withContext allArticles.sortedByDescending { it.publishDate }
                }
            }

            // If no network articles were found or none were relevant, use fallback articles
            println("No relevant articles found from network sources. Using fallback articles.")
            return@withContext ArticleFallbackProvider.getFallbackArticles()
        } catch (e: Exception) {
            // On any exception, return the fallback articles
            println("Error in fetchArticles: ${e.message}")
            return@withContext ArticleFallbackProvider.getFallbackArticles()
        }
    }

    /**
     * Check if an article is relevant to menopause or women's health topics
     */
    private fun isRelevantToMenopause(article: Article): Boolean {
        val relevantTerms = listOf(
            "menopause", "perimenopause", "postmenopause",
            "hot flash", "hot flush", "night sweat",
            "hormone", "estrogen", "progesterone",
            "women's health", "aging", "midlife",
            "mood swing", "vaginal dryness", "sleep issue",
            "osteoporosis", "weight gain", "libido", "sexual health",
            "heart health", "bone health", "cognitive", "memory"
        )

        val content = (article.title + " " + article.summary).lowercase()

        // Check if the article contains any relevant terms
        return relevantTerms.any { content.contains(it.lowercase()) } ||
               article.category.contains("Menopause", ignoreCase = true) ||
               article.category.contains("Women", ignoreCase = true)
    }

    /**
     * Fetch articles from a specific RSS source
     */
    private suspend fun fetchArticlesFromRss(source: RssSource): List<Article> = withContext(Dispatchers.IO) {
        val url = URL(source.url)
        val connection = url.openConnection() as HttpsURLConnection

        try {
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.requestMethod = "GET"
            connection.doInput = true

            val responseCode = connection.responseCode
            if (responseCode != HttpsURLConnection.HTTP_OK) {
                throw Exception("HTTP error code: $responseCode")
            }

            // Read the XML content
            val xmlContent = connection.inputStream.bufferedReader().use { it.readText() }
            parseRssXml(xmlContent, source)

        } finally {
            connection.disconnect()
        }
    }

    /**
     * Parse RSS XML content with robust error handling
     */
    private fun parseRssXml(xmlContent: String, source: RssSource): List<Article> {
        val articles = mutableListOf<Article>()

        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true

            val parser = factory.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true)
            parser.setInput(StringReader(xmlContent))

            var eventType = parser.eventType
            var inItem = false

            // Temporary variables to hold item data
            var currentTitle = ""
            var currentDescription = ""
            var currentLink = ""
            var currentPubDate = ""
            var currentContent = ""
            var currentImage = ""

            while (eventType != XmlPullParser.END_DOCUMENT) {
                try {
                    when (eventType) {
                        XmlPullParser.START_TAG -> {
                            when (parser.name.lowercase()) {
                                "item", "entry" -> inItem = true
                                "title" -> if (inItem) currentTitle = safeGetText(parser)
                                "description", "summary", "content:encoded" -> if (inItem) currentDescription = safeGetText(parser)
                                "link" -> if (inItem) {
                                    // Try to get link href attribute first (for Atom feeds)
                                    val href = parser.getAttributeValue(null, "href")
                                    currentLink = href ?: safeGetText(parser).trim()
                                }
                                "pubdate", "published", "date", "dc:date" -> if (inItem) currentPubDate = safeGetText(parser)
                                "content" -> if (inItem) currentContent = safeGetText(parser)
                                "enclosure" -> {
                                    if (inItem && currentImage.isEmpty()) {
                                        val type = parser.getAttributeValue(null, "type")
                                        val url = parser.getAttributeValue(null, "url")
                                        if (type?.startsWith("image/") == true && url != null) {
                                            currentImage = url
                                        }
                                    }
                                }
                                "media:content", "media:thumbnail" -> {
                                    if (inItem && currentImage.isEmpty()) {
                                        val url = parser.getAttributeValue(null, "url")
                                        if (url != null) {
                                            currentImage = url
                                        }
                                    }
                                }
                            }
                        }

                        XmlPullParser.END_TAG -> {
                            if (parser.name.lowercase() in listOf("item", "entry")) {
                                // Only add complete items with title and link
                                if (currentTitle.isNotEmpty() && (currentLink.isNotEmpty() || currentContent.isNotEmpty())) {
                                    val date = parseDate(currentPubDate) ?: Date()
                                    val finalContent = if (currentContent.isNotEmpty()) currentContent else currentDescription
                                    val readingTime = calculateReadingTime(finalContent)

                                    // Generate link if missing
                                    val finalLink = if (currentLink.isEmpty()) {
                                        "https://example.com/article/${UUID.randomUUID()}"
                                    } else {
                                        currentLink
                                    }

                                    // Create article
                                    articles.add(
                                        Article(
                                            id = UUID.nameUUIDFromBytes(finalLink.toByteArray()).toString(),
                                            title = currentTitle.cleanText(),
                                            summary = currentDescription.take(300).cleanText(),
                                            content = finalContent.cleanText(),
                                            imageUrl = currentImage,
                                            sourceUrl = finalLink,
                                            sourceName = source.sourceName,
                                            publishDate = date,
                                            category = source.category,
                                            readTimeMinutes = readingTime.coerceAtLeast(1)
                                        )
                                    )
                                }

                                // Reset item data for next item
                                inItem = false
                                currentTitle = ""
                                currentDescription = ""
                                currentLink = ""
                                currentPubDate = ""
                                currentContent = ""
                                currentImage = ""
                            }
                        }
                    }
                    eventType = parser.next()
                } catch (e: XmlPullParserException) {
                    // Log error but try to continue parsing
                    println("XML parsing error in ${source.sourceName} feed: ${e.message}")

                    try {
                        // Try to skip to next event
                        eventType = parser.next()
                    } catch (e2: Exception) {
                        // If we can't recover, break the loop
                        break
                    }
                } catch (e: Exception) {
                    println("Error parsing ${source.sourceName} feed: ${e.message}")
                    break
                }
            }
        } catch (e: Exception) {
            println("Error setting up parser for ${source.sourceName}: ${e.message}")
        }

        return articles
    }

    /**
     * Safely get text from an XML tag, handling errors
     */
    private fun safeGetText(parser: XmlPullParser): String {
        return try {
            // Try the standard approach first
            getTextFromTag(parser)
        } catch (e: Exception) {
            // If it fails, try an alternative approach
            try {
                parser.next()
                val result = parser.text ?: ""
                parser.next() // Try to move past this element
                result
            } catch (e2: Exception) {
                // If all fails, return empty string
                ""
            }
        }
    }

    /**
     * Extract text content from an XML tag
     */
    private fun getTextFromTag(parser: XmlPullParser): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text ?: ""
            parser.nextTag()
        }
        return result
    }

    /**
     * Parse date string using multiple formats
     */
    private fun parseDate(dateStr: String): Date? {
        if (dateStr.isEmpty()) return null

        for (format in dateFormats) {
            try {
                return format.parse(dateStr)
            } catch (e: ParseException) {
                // Try next format
            }
        }

        return null
    }

    /**
     * Calculate estimated reading time in minutes
     */
    private fun calculateReadingTime(content: String): Int {
        val wordCount = content.split(Regex("\\s+")).size
        return (wordCount / WORDS_PER_MINUTE) + 1
    }

    /**
     * Clean up text by removing HTML tags and normalizing whitespace
     */
    private fun String.cleanText(): String {
        return this.removeTags()
            .replace(Regex("\\s{2,}"), " ") // Replace multiple spaces with single space
            .trim()
    }

    /**
     * Remove HTML tags from text
     */
    private fun String.removeTags(): String {
        return this.replace(Regex("<[^>]*>"), "")
            .replace("&nbsp;", " ")
            .replace("&quot;", "\"")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&apos;", "'")
            .replace("&#39;", "'")
            .replace("&#8230;", "...")
    }

    /**
     * Data class to represent an RSS source
     */
    data class RssSource(val url: String, val sourceName: String, val category: String)
}
