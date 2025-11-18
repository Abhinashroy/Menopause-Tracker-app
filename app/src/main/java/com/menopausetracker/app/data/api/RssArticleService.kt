package com.menopausetracker.app.data.api

import android.content.Context
import com.menopausetracker.app.data.model.Article
import com.prof18.rssparser.RssParser
import com.prof18.rssparser.RssParserBuilder
import com.prof18.rssparser.model.RssItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.util.Date
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.math.abs

/**
 * Service for fetching menopause and women's health related articles using RSS Parser library
 */
class RssArticleService(private val context: Context) {

    // Create an OkHttpClient with increased timeouts
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    // Initialize the RSS parser with our custom OkHttpClient
    private val rssParser = RssParserBuilder(callFactory = okHttpClient).build()

    companion object {
        // RSS feed URLs specifically for menopause and women's health
        private val RSS_FEEDS = listOf(
            // High-quality, full-content WordPress feeds
            "https://www.feistymenopause.com/blog.rss", // Sticking with Feisty Menopause (already good)
            "https://www.menopausenaturalsolutions.com/blog.rss", // Retain: consistently provides full articles
            "https://www.mymenopausecentre.com/feed/", // My Menopause Centre insights + clinical posts
            "https://www.themenopausecharity.org/feed/", // Educational articles with complete bodies
            "https://www.balance-menopause.com/menopause-library/feed/", // Balance app library feed (rich HTML)
            "https://www.joinmidi.com/blog?format=rss" // Midi Health Squarespace feed exposing content:encoded
        )

        private val FALLBACK_IMAGE_URLS = listOf(
            "https://images.unsplash.com/photo-1518611012118-696072aa579a?auto=format&fit=crop&w=1200&q=80",
            "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?auto=format&fit=crop&w=1200&q=80",
            "https://images.unsplash.com/photo-1441974231531-c6227db76b6e?auto=format&fit=crop&w=1200&q=80",
            "https://images.unsplash.com/photo-1506126613408-eca07ce68773?auto=format&fit=crop&w=1200&q=80",
            "https://images.unsplash.com/photo-1487412720507-e7ab37603c6f?auto=format&fit=crop&w=1200&q=80"
        )

        // Legacy feeds we paused because they only surfaced headlines/teasers. Kept here for reference.
        // https://jessicasepel.com/feed/
        // https://www.intimina.com/blog/feed/
        // https://blog.metagenics.com/post/category/womens-health/feed/
        // https://6pillarhealth.com/f.atom

        // Terms related to menopause to filter relevant content
        private val MENOPAUSE_TERMS = listOf(
            "menopause", "perimenopause", "postmenopause",
            "hot flash", "hot flush", "night sweat",
            "hormone", "estrogen", "progesterone",
            "women's health", "aging", "midlife",
            "mood swing", "vaginal dryness", "sleep issue",
            "osteoporosis", "weight gain", "libido", "sexual health",
            "heart health", "bone health", "cognitive", "memory"
        )
    }

    /**
     * Fetch articles from RSS feeds
     */
    suspend fun fetchArticles(): List<Article> = withContext(Dispatchers.IO) {
        val allArticles = mutableListOf<Article>()

        for (feedUrl in RSS_FEEDS) {
            try {
                println("Attempting to fetch articles from: $feedUrl")

                // Use the RSS Parser to get the feed
                val channel = rssParser.getRssChannel(feedUrl)
                println("Successfully fetched channel: ${channel.title} with ${channel.items.size} articles")

                // Map RSS items to our Article model
                val articles = channel.items.mapNotNull { item ->
                    val fullContent = buildFullArticleContent(item.title, item.content, item.description)

                    if (shouldSkipArticle(channel.title, item.title)) {
                        return@mapNotNull null
                    }

                    Article(
                        id = UUID.nameUUIDFromBytes((item.guid ?: item.link ?: UUID.randomUUID().toString()).toByteArray()).toString(),
                        title = item.title ?: "No Title",
                        summary = item.description?.take(200)?.cleanHtml() ?: "",
                        content = fullContent,
                        imageUrl = resolveImageUrl(item),
                        sourceUrl = item.link ?: "",
                        sourceName = channel.title ?: feedUrl.substringAfter("://").substringBefore("/"),
                        publishDate = Date(), // Using current date instead of item.pubDate to resolve type mismatch
                        category = "Women's Health",
                        readTimeMinutes = calculateReadingTime(fullContent)
                    )
                }

                allArticles.addAll(articles)
                println("Added ${articles.size} articles from ${channel.title}")

            } catch (e: Exception) {
                // Log error but continue with other feeds
                println("Error fetching from $feedUrl: ${e.message}")
                e.printStackTrace()
            }
        }

        // Return empty list if no articles were fetched (removed fallback)
        if (allArticles.isEmpty()) {
            println("No articles fetched from any feed. Returning empty list.")
            return@withContext emptyList()
        }

        // Filter for menopause-related content and sort by date
        val filteredArticles = allArticles
            .filter { isRelevantToMenopause(it) }
            .sortedByDescending { it.publishDate }

        println("Filtered down to ${filteredArticles.size} menopause-relevant articles")

        // If no relevant articles after filtering, return all articles
        if (filteredArticles.isEmpty()) {
            println("No menopause-relevant articles found. Returning all ${allArticles.size} articles.")
            return@withContext allArticles.sortedByDescending { it.publishDate }
        }

        return@withContext filteredArticles
    }

    /**
     * Check if an article is relevant to menopause or women's health
     */
    private fun isRelevantToMenopause(article: Article): Boolean {
        val content = (article.title + " " + article.summary).lowercase()
        return MENOPAUSE_TERMS.any { content.contains(it.lowercase()) } ||
                article.category.contains("Menopause", ignoreCase = true)
    }

    /**
     * Calculate estimated reading time in minutes
     * Modified to provide more realistic reading times for articles
     */
    private fun calculateReadingTime(content: String): Int {
        val cleanedContent = content.cleanHtml()
        val wordCount = cleanedContent.split(Regex("\\s+")).size

        // If content is very short, add some artificial reading time
        // to account for images, diagrams, and supplementary content
        // that might not be included in the RSS feed
        val effectiveWordCount = when {
            // For very short content, assume there's supplementary content
            wordCount < 100 -> wordCount + 300
            // For medium content, add proportional extra words
            wordCount < 300 -> wordCount * 2
            // For longer content, add a smaller proportion
            else -> (wordCount * 1.5).toInt()
        }

        // Slightly slower reading speed (175 wpm) for health/medical content
        // since it typically requires more careful reading
        val wordsPerMinute = 175

        // Calculate reading time with minimum of 2 minutes
        return (effectiveWordCount / wordsPerMinute).coerceAtLeast(2)
    }

    /**
     * Clean HTML tags from text while preserving important formatting
     */
    private fun String.cleanHtml(): String {
        return this
            // Replace bold tags with Android-compatible format markers
            .replace("<b>", "<b>")
            .replace("</b>", "</b>")
            .replace("<strong>", "<b>")
            .replace("</strong>", "</b>")
            // Replace italic tags
            .replace("<i>", "<i>")
            .replace("</i>", "</i>")
            .replace("<em>", "<i>")
            .replace("</em>", "</i>")
            // Replace header tags with bold
            .replace("<h1>", "\n\n<b>")
            .replace("</h1>", "</b>\n\n")
            .replace("<h2>", "\n\n<b>")
            .replace("</h2>", "</b>\n\n")
            .replace("<h3>", "\n\n<b>")
            .replace("</h3>", "</b>\n\n")
            // Replace paragraph tags with newlines
            .replace("<p>", "\n\n")
            .replace("</p>", "")
            // Replace list items
            .replace("<li>", "\n• ")
            .replace("</li>", "")
            // Replace line breaks
            .replace("<br>", "\n")
            .replace("<br/>", "\n")
            .replace("<br />", "\n")
            // Special HTML entities for punctuation and symbols
            .replace("&mdash;", "—")
            .replace("&ndash;", "–")
            .replace("&rsquo;", "'")
            .replace("&lsquo;", "'")
            .replace("&ldquo;", """)
            .replace("&rdquo;", """)
            .replace("&hellip;", "…")
            .replace("&amp;", "&")
            .replace("&nbsp;", " ")
            .replace("&quot;", "\"")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&apos;", "'")
            .replace("&#39;", "'")
            .replace("&#8230;", "...")
            // Remove all other HTML tags
            .replace(Regex("<(?!/?[bi]>)[^>]*>"), "")
            .trim()
    }

    /**
     * Build full article content from RSS item with standardized formatting
     * Works consistently across all feed types
     */
    private fun buildFullArticleContent(title: String?, content: String?, description: String?): String {
        // Step 1: Extract and clean the raw content
        var rawContent = ""

        if (!content.isNullOrBlank()) {
            rawContent = content.trim()
        } else if (!description.isNullOrBlank()) {
            rawContent = description.trim()
        }

        // Step 2: Pre-process content to fix common formatting issues
        rawContent = rawContent
            // Fix common issues with attribution lines
            .replace(Regex("<i>Submitted by[^<]*</i>"), "")
            // Keep inline HTML formatting while fixing broken tags
            .replace("</i>of", "</i> of")
            .replace("add</i>", "add</i> ")
            // Fix broken line breaks and improve flow of text
            .replace(Regex("([a-z])\\s*\n\\s*([a-z])"), "$1 $2")

        // Step 3: Clean and standardize the HTML
        var formattedContent = rawContent.cleanHtml()

        // Step 4: Apply smart paragraph detection if needed
        if (!formattedContent.contains("\n\n")) {
            formattedContent = formattedContent
                // Add paragraph breaks after sentences that end a thought
                .replace(Regex("\\.\\s+([A-Z])"), ".\n\n$1")
        }

        // Step 5: Build the final article with consistent structure
        val sb = StringBuilder()

        // Add the title as a bold heading
        title?.let {
            sb.append("<b>").append(it.trim()).append("</b>\n\n")
        }

        // Add the formatted content
        sb.append(formattedContent)

        // Step 6: Final cleanup of spacing and structure
        return sb.toString()
            // Fix excessive newlines
            .replace(Regex("\n{3,}"), "\n\n")
            // Ensure proper spacing for bullet points
            .replace(Regex("(?m)^•\\s*"), "• ")
            .trim()
    }

    /**
     * Check if an article is from Metagenics based on content patterns
     */
    private fun isMetagenicsArticle(content: String, title: String?): Boolean {
        return content.contains("Metagenics") ||
                content.contains("HerWellness") ||
                title?.contains("Metagenics") == true ||
                content.contains("<b>References:</b>") ||
                content.contains("Submitted by the Metagenics Marketing Team")
    }

    /**
     * Special formatter for Metagenics articles
     * Handles their unique structure including references section
     */
    private fun formatMetagenicsArticle(title: String?, content: String): String {
        val sb = StringBuilder()

        // Add title in bold
        title?.let {
            sb.append("<b>").append(it.trim()).append("</b>\n\n")
        }

        // Remove attribution text if present
        var cleanedContent = content.replace(Regex("<i>Submitted by[^<]*</i>"), "")

        // Split article into main content and references if references exist
        val parts = cleanedContent.split("<b>References:</b>", ignoreCase = true)
        var mainContent = parts[0]
        val hasReferences = parts.size > 1
        val references = if (hasReferences) parts[1] else ""

        // Clean up main content
        mainContent = mainContent
            // Fix line breaks that should be paragraph breaks
            .replace(Regex("(\\.)\\s*([A-Z])"), "$1\n\n$2")
            // Handle interrupted sentences (common in Metagenics feeds)
            .replace(Regex("(\\w)\\n(\\w)"), "$1 $2")
            // Clean up multiple spaces
            .replace(Regex(" +"), " ")
            // Preserve important formatting
            .replace("<b>", "<b>")
            .replace("</b>", "</b>")
            .replace("<i>", "<i>")
            .replace("</i>", "</i>")
            // Remove other HTML tags
            .replace(Regex("<(?!/?[bi]>)[^>]*>"), "")

        // Add clean main content
        sb.append(mainContent.trim())

        // Format references section if present
        if (hasReferences) {
            sb.append("\n\n<b>References:</b>\n\n")

            // Process reference entries
            var refSection = references
                // Keep formatting for author names and publication titles
                .replace("<b>", "<b>")
                .replace("</b>", "</b>")
                .replace("<i>", "<i>")
                .replace("</i>", "</i>")
                // Fix newlines in references
                .replace(Regex("(\\.)\\s*<b>"), ".\n\n<b>")
                // Remove other HTML
                .replace(Regex("<(?!/?[bi]>)[^>]*>"), "")
                // Fix incorrect line breaks in reference entries
                .replace(Regex("(\\w)\\n(\\w)"), "$1 $2")

            sb.append(refSection.trim())
        }

        // Fix any remaining spacing issues
        return sb.toString()
            .replace(Regex("\n{3,}"), "\n\n")
            .trim()
    }

    private fun resolveImageUrl(item: RssItem): String? {
        item.image?.takeIf { it.startsWith("http") }?.let { return it }

        extractImageFromHtml(item.content)?.let { return it }
        extractImageFromHtml(item.description)?.let { return it }

        val seed = item.guid ?: item.link ?: item.title ?: UUID.randomUUID().toString()
        val index = abs(seed.hashCode()) % FALLBACK_IMAGE_URLS.size
        return FALLBACK_IMAGE_URLS[index]
    }

    private fun extractImageFromHtml(html: String?): String? {
        if (html.isNullOrBlank()) return null

        val regex = Regex("<img[^>]+src=[\"']([^\"'>]+)[\"']", RegexOption.IGNORE_CASE)
        val match = regex.find(html)
        val src = match?.groups?.get(1)?.value ?: return null

        return when {
            src.startsWith("//") -> "https:$src"
            src.startsWith("http") -> src
            else -> null
        }
    }

    private fun shouldSkipArticle(sourceTitle: String?, articleTitle: String?): Boolean {
        val normalizedSource = sourceTitle?.lowercase() ?: ""
        val normalizedTitle = articleTitle?.lowercase() ?: ""

        if (normalizedSource.contains("intimina")) {
            return true
        }

        if (normalizedTitle.contains("intimina")) {
            return true
        }

        return false
    }
}
