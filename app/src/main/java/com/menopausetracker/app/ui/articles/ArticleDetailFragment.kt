package com.menopausetracker.app.ui.articles

import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.menopausetracker.app.R
import com.menopausetracker.app.data.model.Article
import com.menopausetracker.app.databinding.FragmentArticleDetailBinding
import androidx.core.text.HtmlCompat
import java.text.SimpleDateFormat
import java.util.Locale

class ArticleDetailFragment : Fragment() {
    private var _binding: FragmentArticleDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ArticlesViewModel
    private var currentArticle: Article? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArticleDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this).get(ArticlesViewModel::class.java)

        // Setup toolbar navigation
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Get article ID from args using Safe Args
        val args = ArticleDetailFragmentArgs.fromBundle(requireArguments())
        loadArticle(args.articleId)

        // Enable clickable links inside the article body
        binding.articleDetailContent.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun loadArticle(articleId: String?) {
        if (articleId == null) {
            Snackbar.make(binding.root, "Invalid article ID", Snackbar.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return
        }

        viewModel.getArticleById(articleId).observe(viewLifecycleOwner) { article ->
            if (article != null) {
                displayArticle(article)
                setupActionButtons(article)
                currentArticle = article
            } else {
                Snackbar.make(binding.root, "Article not found", Snackbar.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }
    }

    private fun displayArticle(article: Article) {
        // Set article title
        binding.articleDetailTitle.text = article.title

        // Set article metadata (date, reading time, category)
        val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        val dateString = dateFormat.format(article.publishDate)
        binding.articleMetadata.text = "$dateString • ${article.readTimeMinutes} min read • ${article.category}"

        // Display the complete article content without any limitations
        binding.articleDetailContent.text = formatArticleContent(article.content)

        // Set article source
        binding.articleSource.text = article.sourceName

        // Set toolbar title
        binding.toolbar.title = article.sourceName

        // Load article image
        article.imageUrl?.let { imageUrl ->
            Glide.with(requireContext())
                .load(imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(binding.articleDetailImage)
        } ?: run {
            binding.articleDetailImage.setImageResource(R.drawable.placeholder_image)
        }
    }

    /**
     * Format article content for better readability
     */
    private fun formatArticleContent(content: String): CharSequence {
        val sanitized = content
            .replace("&hellip;", "…")
            .replace("&rsquo;", "'")
            .replace("&lsquo;", "'")
            .replace("&ldquo;", "\"")
            .replace("&rdquo;", "\"")
            .replace("&mdash;", "—")
            .replace("&ndash;", "–")
            .replace(Regex("\n{3,}"), "\n\n")
            .trim()

        // Convert preserved formatting markers and newlines into HTML so HtmlCompat can render them
        val htmlReady = sanitized
            .replace("\n\n", "<br/><br/>")
            .replace("\n", "<br/>")

        return HtmlCompat.fromHtml(htmlReady, HtmlCompat.FROM_HTML_MODE_LEGACY)
    }

    private fun setupActionButtons(article: Article) {
        // Save button
        val saveButtonText = if (article.isSaved) "Unsave" else "Save"
        val saveButtonIcon = if (article.isSaved) R.drawable.ic_saved else R.drawable.ic_save

        binding.btnSaveArticle.text = saveButtonText
        // Replace setIcon with setCompoundDrawablesWithIntrinsicBounds
        binding.btnSaveArticle.setCompoundDrawablesWithIntrinsicBounds(saveButtonIcon, 0, 0, 0)

        binding.btnSaveArticle.setOnClickListener {
            viewModel.toggleArticleSaved(article)
            val message = if (article.isSaved) "Article removed from saved" else "Article saved"
            Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()

            // Update button appearance
            setupActionButtons(article.copy(isSaved = !article.isSaved))
        }

        // Share button
        binding.btnShareArticle.setOnClickListener {
            shareArticle(article)
        }
    }

    private fun shareArticle(article: Article) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, article.title)
            putExtra(
                Intent.EXTRA_TEXT,
                "Check out this article about menopause: \"${article.title}\" from ${article.sourceName}\n\n${article.sourceUrl}"
            )
        }
        startActivity(Intent.createChooser(shareIntent, "Share Article"))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
