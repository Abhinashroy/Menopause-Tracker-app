package com.menopausetracker.app.ui.articles

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.menopausetracker.app.R
import com.menopausetracker.app.databinding.FragmentArticleDetailBinding
import com.menopausetracker.app.util.FontSizeManager

class ArticleDetailFragment : Fragment() {
    private var _binding: FragmentArticleDetailBinding? = null
    private val binding get() = _binding!!

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

        try {
            // Set up back button
            binding.backButton.setOnClickListener {
                findNavController().navigateUp()
            }

            // Get article data from arguments
            arguments?.let { args ->
                // Try to get articleId as Long first, then fall back to String if needed
                val articleId = if (args.containsKey("articleId")) {
                    try {
                        args.getLong("articleId")
                    } catch (e: Exception) {
                        args.getString("articleId") ?: ""
                    }
                } else {
                    ""
                }

                val title = args.getString("articleTitle") ?: ""
                val content = args.getString("articleContent") ?: ""
                val imageUrl = args.getString("articleImageUrl") ?: ""

                // Display article details
                displayArticle(articleId, title, content, imageUrl)
            }

            // Apply font size from settings
            applyFontSize()
        } catch (e: Exception) {
            // Log any errors to help with debugging
            e.printStackTrace()
        }
    }

    private fun displayArticle(id: Any, title: String, content: String, imageUrl: String) {
        try {
            // Set title and content
            binding.articleDetailTitle.text = title
            binding.articleDetailContent.text = content

            // Load image
            if (imageUrl.isNotEmpty()) {
                Glide.with(requireContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(binding.articleDetailImage)
            } else {
                binding.articleDetailImage.setImageResource(R.drawable.placeholder_image)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun applyFontSize() {
        try {
            // Apply font sizes using the FontSizeManager while preserving theme colors
            FontSizeManager.applyFontSize(requireContext(), binding.articleDetailTitle, true)
            FontSizeManager.applyFontSize(requireContext(), binding.articleDetailContent, false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
